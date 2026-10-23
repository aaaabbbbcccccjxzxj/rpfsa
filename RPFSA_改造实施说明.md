# RPFSA 改造实施说明

> 对应《RPFSA_代码审查与优化建议.md》的五阶段路线图，本文记录**实际落地的改动**、
> 接口契约变化、启动方式与遗留事项。
> 验证结果：后端 `mvn compile` 通过；`rpfsa_front`、`rpfsa_manage` 的 `npm run build` 均通过。

---

## 1. 总览

| 维度 | 改造前 | 改造后 |
|---|---|---|
| 身份校验 | 无，任何接口可被直接调用 | 拦截器 + 签名令牌；写操作默认要求登录，管理接口校验角色 |
| 密码 | 明文入库、随用户对象下发到前端 | BCrypt 存储、`@JsonIgnore` 不外泄、历史明文首次登录自动升级 |
| 跨表业务 | 由前端串行发 4~8 个请求，中途失败留脏数据 | 收敛为单个事务方法，前端只发一个请求 |
| 金额/库存 | 前端计算与校验，可篡改 | 服务端重新计价 + 带条件的原子 UPDATE 防超卖透支 |
| 查询字段 | 前端传入的列名直通 SQL | 白名单守卫校验后才允许作为条件 |
| 数据装配 | 逐行查库（N+1），列表页甚至有前端 N+1 | 批量查询 + 内存 Map 装配；未读数由分页接口一次带回 |
| 部署 | 前后端地址硬编码 `http://localhost` | `.env` + vite proxy；后端凭据与上传目录走环境变量 |
| 图片上传 | 写入 classpath 与源码目录，扩展名不校验 | 外部目录 + 扩展名白名单 + UUID 命名 |
| 数据库 | 10 张表除主键外零索引 | 补 7 张表的二级索引，密码列扩容至 `varchar(100)` |

---

## 2. 后端改动清单（`rpfsa/`）

### 2.1 新增文件

| 文件 | 作用 |
|---|---|
| `common/BizException.java` | 业务异常，由全局处理器统一转换 |
| `common/GlobalExceptionHandler.java` | 统一异常出口，返回语义化 HTTP 状态码，堆栈只进日志 |
| `common/RequireAdmin.java` | 标注"仅管理员可访问" |
| `common/RequireLogin.java` | 标注"必须登录" |
| `util/TokenUtil.java` | HMAC-SHA256 无状态令牌（不引 JWT 依赖） |
| `util/PasswordUtil.java` | BCrypt 编解码 + 历史明文渐进式兼容 |
| `util/QueryFieldGuard.java` | 查询列名白名单 + 匹配模式（LIKE / EQ / NE） |
| `util/HtmlSanitizer.java` | jsoup 富文本白名单过滤，防存储型 XSS |
| `config/AuthInterceptor.java` | 登录与角色校验拦截器 |
| `config/WebConfig.java` | 拦截器注册、上传目录映射、CORS 收敛 |
| `dto/LoginDTO.java` `dto/AdminLoginDTO.java` | 登录入参（使实体可以安全地对 password 加 `@JsonIgnore`） |
| `dto/UserRegisterDTO.java` `dto/UserProfileDTO.java` `dto/UserAdminDTO.java` | 用户写操作的字段白名单 |
| `dto/DonateCommitDTO.java` | 捐助提交（只含宠物 id 与救助品明细） |
| `dto/AdoptAuditDTO.java` | 领养审核入参 |

### 2.2 关键改造

**统一响应与分页**
- `ResultBean<T>` 泛型化，新增 `success/fail` 静态工厂与语义化状态码常量。
- `Pagination<T>` 泛型化，新增 `Pagination.of(IPage, records)`，`size` 一律取自 `IPage`。
  修掉了原来"宠物列表实际 12 条却上报 size=10"、"领养列表实际 10 条却上报 12"的错误。

**事务边界**
- `DonateService.commit(dto, uid)`：服务端重新计价 → 原子扣库存（`WHERE inventory >= ?`）→
  原子扣余额（`WHERE pet_coin >= ?`）→ 给宠物加币 → 写入捐助记录，全在一个事务内。
- `AdoptService.audit(dto)`：改申请记录 + 流转宠物状态 + 累加用户领养数（`how_adopt = how_adopt + 1`）。
- `AdoptService.addadopttable`：提交申请时一并把宠物置为"审核中"。
- `CommentServiceImpl`：评论增删时在同事务内维护 `comment_count`，
  用 `GREATEST(comment_count + (+1/-n), 0)` 保证不为负。

**N+1 消除**

| 位置 | 改造前 | 改造后 |
|---|---|---|
| `PetServiceImpl.queryonebyID` | 三级嵌套，N 条评论 ≈ N+2 次查询 | 固定 4 次 |
| `PetServiceImpl.querypetsbypage` | 逐行查主人 | 批量一次 |
| `PostServiceImpl.queryonepostbyid` | 内层循环重复查同一条帖子 | 固定 4 次 |
| `PostServiceImpl.assemble` | 每帖查用户 + 每帖查评论 + 每条评论再查帖子 | 固定 2 次 |
| `AdoptServiceImpl` / `DonateServiceImpl` / `JournalServiceImpl` | 逐行查关联对象 | 批量一次 |
| `MessageService.countNoReadForAdmin` | 逐用户 `selectCount` | 一次 `GROUP BY` |
| 用户管理列表 | **前端**每行发一个请求取未读数 | 分页接口一次聚合带回 `noReadCount` |

**安全加固**
- 拦截器策略：OPTIONS 放行 → GET 默认放行（游客可浏览）→ `@RequireLogin` / `@RequireAdmin` 显式声明 →
  其余写操作默认要求登录。
- 密码：`PasswordUtil.matches` 同时支持 BCrypt 与历史明文；明文校验通过后自动升级为密文，存量账号无需重置。
- `User.password` 加 `@JsonIgnore`；登录改用 `LoginDTO` 接收，因此实体不再需要暴露密码字段。
- 归属校验下沉到 Service：帖子删除、评论删除、宠物查看、领养/捐助通知的读取与已读，
  非管理员一律只能操作自己的数据。
- 上传：目录由 `app.upload.dir` 配置，扩展名白名单，UUID 命名，拒绝写出到上传目录之外。

### 2.3 接口契约变更（**前端已同步适配**）

| 原接口 | 现接口 | 说明 |
|---|---|---|
| `POST /user`（注册） | `POST /user/register` | 注册与"管理员新增用户"解耦 |
| `POST /user`（管理员新增） | `POST /admin/users` | 收敛到管理端路径 |
| `GET /user/page/{c}` | `GET /admin/users/{c}` | 同上 |
| `GET /user/{c}/{col}/{con}` | `GET /admin/users/{c}/{col}/{con}` | 同上 |
| `PATCH /user`（用户改资料） | `PATCH /user`（入参改为 `UserProfileDTO`） | 路径不变，字段白名单化 |
| `PATCH /user`（管理员改用户） | `PATCH /admin/users` | 收敛到管理端路径 |
| `DELETE /user/{id}` | `DELETE /admin/users/{id}` | 同上 |
| `POST /user/login` | 同路径，返回 `{token, user}` | 响应结构变化 |
| `POST /admin/login` | 同路径，返回 `{token, adminname}` | 响应结构变化 |
| `GET /admin` | `GET /admin/stats` | 统计接口 |
| `POST /donate`（前端提交捐助） | `POST /donate/commit` | 入参改为 `{petId, items[]}` |
| `PATCH /adopt`（审核） | 同路径，入参改为 `{id, state, ponReason}` | 服务端补齐其余字段 |
| `PATCH /donate/{id}` | 同路径 | 语义不变（标记本人通知已读） |
| `PATCH /pet` | 仅管理员可用 | 用户端的评论数回写、领养状态流转已移除 |

> **前端所有调用点已同步更新**，两个工程均可正常构建。

### 2.4 请求头约定

```
Authorization: Bearer <token>
```

用户端令牌存 `sessionStorage.token`，管理端存 `sessionStorage.adminToken`，
由各自的 `src/utils/request.js` 自动附加。

---

## 3. 前端改动清单

### 3.1 新增基础设施（两个工程各一份）

| 文件 | 作用 |
|---|---|
| `.env.development` / `.env.production` | 接口前缀与静态资源前缀 |
| `vite.config.js` | 开发代理：`/api` → 后端（去掉前缀）、`/img` → 后端 |
| `src/utils/request.js` | axios 全局配置：baseURL、令牌、401 跳登录、403 提示 |
| `src/utils/url.js` | `resolveImg()` / `resolveUploadUrl()`，静态资源地址唯一出口 |

**设计要点**：`request.js` 直接配置 axios 实例本身（而不是 `new` 一个新实例），
因此 26 个组件里既有的 `import axios from 'axios'` + `axios.get(...)` 写法**无需改动**
就获得了统一的 baseURL 与鉴权头，改造面最小、回归风险最低。

### 3.2 缺陷修复

| 位置 | 问题 | 处理 |
|---|---|---|
| `PetDetail.vue`、`Community.vue` | `<template #>` 空插槽名，编译告警且插槽内容不渲染 | 改为 `<template #default>` |
| `Community.vue` | `{{ }}` 空插值表达式 | 补为作者昵称 |
| `PetDetail.vue` ×2 | `v-else="currentpet.isVaccine == 0"` 非法写法 | 改为 `v-else` |
| `PetDetail.vue` | 品种字段拼写 `vatriety`，永远显示空 | 改为 `variety` |
| `PetDetail.vue` | 二级评论头像误取 `comment.user.photo` | 改为 `leveltwocon.user.photo` |
| `Diary.vue`、`Pet.vue`、`User.vue` | `reactive({})` 当 ref 用（挂 `value` 属性） | 改用 `ref()` |
| `router/index.js` ×2 | 守卫只调 `router.replace` 不调 `next()`，导航永久 pending | 改为返回式守卫 |
| `Rpfsa.vue` | `setInterval` 在 setup 顶层且不清理 | 移入 `onMounted` + `onUnmounted` 清理 |
| `Rpfsa.vue`、`About.vue` | `src="logo.png"` 相对路径，**导致 `npm run build` 直接失败** | 改为 `/logo.png` |
| `AdminCenter.vue` | `setTimeout(300)` 硬等图表容器 | 改为 `await` 后再初始化；补 `dispose()` |
| `User.vue` | `Reflect.set` 动态挂属性、口语化注释 | 改用服务端返回的 `noReadCount` |

### 3.3 业务调用调整

- **捐助**：由 4~8 个串行请求改为 `POST /donate/commit` 单请求。
- **领养申请**：去掉单独的 `PATCH /pet`，改由后端在同一事务内流转。
- **领养审核**：由 3 个串行请求改为 `PATCH /adopt` 单请求。
- **评论数**：前端不再回写 `commentCount`（原先的"读出当前值 ± N"在并发下必然漂移）。
- **评论/发帖**：不再提交 `uid`，一律以令牌身份为准。
- **图片与上传地址**：全部走 `resolveImg()` / `uploadUrl`，消除硬编码。
- 清理 43 处 `console` 调试输出。

---

## 4. 数据库

| 脚本 | 用途 |
|---|---|
| `rpfsa.sql` | 全新部署。已包含密码列扩容与 7 张表的二级索引 |
| `rpfsa_upgrade.sql` | **已有数据库升级**：`ALTER` 扩容密码列 + 补索引，可重复执行 |

**无需重置任何密码**：扩容后历史明文会在该账号首次登录成功时自动改写为 BCrypt 密文。

新增索引：

```sql
comment (poid, level) / (pid)
adopt   (userid, is_read) / (petid)
donate  (uid) / (create_time)
message (uid, is_read)
pet     (type, classic) / (uid)
post    (type, classic) / (uid)
journal (pid)
```

---

## 5. 启动方式

```bash
# 后端（默认 80 端口）
cd rpfsa && mvn spring-boot:run

# 用户端（默认 5173）
cd rpfsa_front && npm install && npm run dev

# 管理端（默认 5174）
cd rpfsa_manage && npm install && npm run dev
```

常用环境变量：

| 变量 | 默认值 | 说明 |
|---|---|---|
| `SERVER_PORT` | `80` | 后端端口 |
| `DB_HOST` / `DB_PORT` / `DB_NAME` | `localhost` / `3306` / `rpfsa` | 数据库地址 |
| `DB_USERNAME` / `DB_PASSWORD` | `root` / `135246` | 数据库凭据 |
| `UPLOAD_DIR` | `./upload` | 图片上传目录 |
| `TOKEN_SECRET` | 开发默认值 | **生产必须覆盖** |
| `CORS_ORIGINS` | 空（关闭跨域） | 多个来源逗号分隔 |

> 开发期前端经 vite proxy 转发，不存在跨域；只有当有人直接以其他域名访问后端时才需要配置 `CORS_ORIGINS`。

---

## 6. 遗留事项

以下事项在审查报告中列出但**本次未实施**，可按需继续：

1. **公共组件抽取**：`PetDetail.vue` 与 `PostDetail.vue` 的评论区块仍逐行重复，
   建议抽为 `components/comment/CommentSection.vue`（含 `CommentItem`、`CommentEditor`）；
   上传逻辑也仍在这几个组件里重复，可抽 `ImageUploader`。
2. **代码规范工具**：两个前端工程尚未引入 ESLint / Prettier；也未迁移 TypeScript。
3. **接口文档**：仍使用已停止维护的 springfox 2.9.2（需 `ant_path_matcher` 打补丁），
   升级到 springdoc-openapi 属依赖级变更，本次为控制风险未动。
4. **筛选状态与分页联动**：`DogandCat.vue` 的 `handleCurrentChange` 未带全筛选条件，
   翻页时会丢失筛选状态；建议把筛选条件与页码统一收进一个 composable 并同步到 URL query。
5. **单元测试**：仍只有默认的空测试类，建议至少补 `DonateService.commit` 的事务回滚用例。
