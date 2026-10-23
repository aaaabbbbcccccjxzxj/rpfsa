# RPFSA 宠物救助平台 — 代码审查与优化建议报告

> 审查对象：`rpfsa`（Spring Boot 后端） / `rpfsa_front`（用户端） / `rpfsa_manage`（管理端） / `rpfsa.sql`
> 审查方式：全量源码静态走查（含全部 Java 类、全部 Vue 组件、完整建表与初始化脚本）
> 前置约束：**以下所有建议均在"不改变现有功能与业务目标"的前提下提出**，不涉及产品形态变更。

---

## 0. 审查范围与统计

| 模块 | 技术栈 | 文件数 | 说明 |
|---|---|---|---|
| 后端 `rpfsa` | Spring Boot 2.7.9 / MyBatis-Plus 3.5.2 / Druid / Java 11 | 40 个`.java` + `pom.xml` + `application.yml` | 10 个 Controller、10 个 Service(Impl)、合 17 个文件；10 个 Mapper；11 个 POJO |
| 用户端 `rpfsa_front` | Vue 3.2 + Vite 4 + Element Plus 2.3 + vue-quill | 13 个`.vue` + 3 个`.js` | 单页 2 个 + 子页 11 个 |
| 管理端 `rpfsa_manage` | Vue 3.2 + Vite 4 + Element Plus 2.3 + ECharts 5 | 13 个`.vue` + 3 个`.js` | 登录 1 + 布局 3 + 业务 8 + 仪表盘 1 |
| 数据库 | MySQL 8.0.32 | 10 张表 | 无外键、无二级索引 |

**总体评价**：功能完整、业务闭环清晰，作为本科毕设而言"跑通"这一关没有问题；分层骨架（Controller / Service / Mapper）也搭起来了。但代码停留在"能跑就行"的阶段，存在 **4 类结构性问题**：业务流程原子性缺失、鉴权体系完全空白、N+1 查询泛滥、前后端重复代码失控。这些问题在毕设答辩中很容易被追问，也是本次优化的主战场。

---

## 1. 问题分级与统计

| 等级 | 含义 | 数量 | 占比 |
|---|---|---|---|
| **P0 阻断级** | 功能正确性 / 数据安全有实质缺陷，必须修 | 9 | 15% |
| **P1 严重** | 架构与安全硬伤，强烈建议修 | 17 | 29% |
| **P2 重要** | 可维护性、性能、代码质量 | 21 | 36% |
| **P3 建议** | 规范与整洁度优化 | 12 | 20% |

**按模块分布**：后端 31 项、前端 24 项、数据库 4 项。

---

## 2. 后端（Spring Boot）问题详解

### 2.1 安全类（P0 / P1）

#### 🔴 P0-1 鉴权体系完全缺失，所有接口裸奔

全部 10 个 Controller 仅用 `@CrossOrigin` 开放跨域，**没有任何身份校验**。任何人在浏览器地址栏就能执行：

```
DELETE http://<host>/user/1        # 删掉任意用户
DELETE http://<host>/pet/44        # 删掉任意宠物
PATCH  http://<host>/user         {"id":1,"petCoin":999999}   # 给自己刷无限宠宠币
```

`sessionStorage` 里的登录态只影响前端渲染，后端完全信任调用方。管理端 `/admin/login` 同理 —— 只要拿到接口路径就能绕过管理页面直接操作数据。

**优化方向**：
- 引入 `spring-boot-starter-security` + JWT（或毕设体量下用轻量的 **拦截器 + Token**）：登录成功签发 token，`WebMvcConfigurer` 注册 `HandlerInterceptor` 校验除 `/user/login`、`/admin/login`、静态资源外的全部请求。
- 角色区分两条链路：`/user/**` 与 `/admin/**`（管理端接口单独加前缀或加 `@PreAuthorize`）。
- 关键写操作（删除、审核、改积分）服务端二次校验归属：例如 `PATCH /user` 只允许修改自己，`DELETE /pet/{id}` 只允许上传者或管理员。

#### 🔴 P0-2 明文密码存储，且密码随对象下发到前端

- `rpfsa.sql` 中 `admin` 表直接存 `('admin','admin')`；`user.password` 同为明文（`UserServiceImpl.userlogin` 用 `eq(password, ...)` 直接比对）。
- `UserController.userlogin` 返回**完整 User 实体**，`password` 字段被原样序列化给前端，并被写入 `sessionStorage`（`UserLogin.vue:144`）。

**优化方向**：
- 入库用 BCrypt（`spring-security-crypto` 的 `BCryptPasswordEncoder`，可单独引依赖，不强制引入整套 Security），登录时 `matches()` 比对。
- 返回体剥离敏感字段：新增 `UserVO`（去掉 `password`），或至少在 `User.password` 上加 `@JsonIgnore` 并对写接口单独用 DTO 接收。
- 存量数据写一条一次性迁移脚本把明文转成 BCrypt 密文。

#### 🔴 P0-3 URL 路径参数直接拼进 SQL 条件（列名注入）

三处把前端传来的列名原样交给 MyBatis-Plus 的 `QueryWrapper`：

```java
// UserServiceImpl.java:91-95
public Pagination likequery(int current, String column, String condition) {
    QueryWrapper qw = new QueryWrapper<>();
    qw.like(column, condition);          // column 来自 @PathVariable，未做白名单
```

```java
// PetServiceImpl.java:119-130
if (column.equals("petname") || column.equals("variety")) qw.like(column, condition);
else qw.eq(column, condition);           // else 分支等价于"列名随便传"
```

```java
// PostServiceImpl.java:128-135
if (column.equals("title")) qw.like(column, condition);
else if (column.equals("uid")) qw.ne(column, condition);
else qw.eq(column, condition);           // 同上
```

MyBatis-Plus 对 `column` 做的是**字符串拼接**，值 `condition` 才是参数化绑定。因此 `GET /pet/1/is_adopt,pet_coin=1--/x` 之类构造可以操纵 SQL 结构（列名注入 / 联合条件注入）。`AdoptServiceImpl.java:53-55`、`DonateServiceImpl.java:89-91` 用 `qw.like(column.equals("username"), column, condition)` 的写法同样绕过了白名单校验。

**优化方向（不改接口形态）**：把 `column` 收敛为**枚举白名单**，非白名单直接返回 400。

```java
private static final Set<String> USER_SORTABLE = Set.of("username", "nickname", "phone", "email");

public Pagination likequery(int current, String column, String condition) {
    if (!USER_SORTABLE.contains(column)) throw new IllegalArgumentException("非法查询字段");
    return queryusersbypage(current, new LambdaQueryWrapper<User>().like(...));
}
```
更彻底的做法：Controller 不再暴露 `{col}/{con}` 路径段，改为 `?field=&keyword=` 查询参数，语义更清楚。

#### 🟠 P1-1 文件上传：路径与类型双重风险

```java
// UploadController.java:28-64
String filename = file.getOriginalFilename();
String newname = System.currentTimeMillis() + filename.substring(filename.lastIndexOf("."));
String path = ResourceUtils.getURL("classpath:").getPath() + "static/img/";
String copypath = "src/main/resources/static/img/";
```

四个问题：
1. **扩展名未校验**：只截取原文件后缀直接落盘，`.jsp / .html / .svg` 等可被上传，配合静态资源映射可造成存储型 XSS 或更严重后果。仅靠前端 `beforeAvatarUpload` 拦截（`Pet.vue:381`）不构成服务端防护。
2. **`ResourceUtils.getURL("classpath:")` 在打包成 jar 后必然失败**（classpath 指向 jar 内部，不是文件系统路径），只剩 IDE 直跑能用 —— 部署即挂。
3. **`"src/main/resources/static/img/"` 是相对工作目录的源码路径**，运行期往源码目录写文件属于反模式：产物被污染、打包会把用户上传图带进 jar。
4. **`System.currentTimeMillis()` 作文件名**在高并发下会撞名覆盖；且 `getOriginalFilename()` 为 `null` 时会 NPE。

**优化方向**：上传目录改为外部可配置（`file.upload-dir=/data/rpfsa/upload`），通过 `WebMvcConfigurer#addResourceHandlers` 映射到 `/img/**`；扩展名走白名单 `Set.of("jpg","jpeg","png","gif","webp")`；文件名用 `UUID.randomUUID()`；并删除"复制一份到 src 目录"的双写逻辑。

#### 🟠 P1-2 配置文件泄露与不安全参数

- `application.yml:9` 明文 `password: 135246`，且 `application.yml` 随源码入库。
- `:1` `server.port: 80` 硬编码（多环境无法切换）。
- `:7` `allowPublicKeyRetrieval=true` 生产环境存在中间人风险。
- `:31` `log-impl: org.apache.ibatis.logging.stdout.StdOutImpl` —— SQL 直打标准输出，生产环境既不落盘也无分级。

**优化方向**：凭据改 `${DB_PASSWORD}` 环境变量占位；拆 `application-dev.yml` / `application-prod.yml`；日志实现换 `Slf4jImpl` 并配置 `logging.level`；`useSSL=false` 仅保留在 dev profile。

#### 🟠 P1-3 CORS 粒度过粗

11 个 Controller 全部标 `@CrossOrigin`（等价于 `allowedOrigins="*"`）。一旦将来引入 Cookie/Token，通配来源会成为 CSRF 面。

**优化方向**：删除类级 `@CrossOrigin`，改为全局 `WebMvcConfigurer#addCorsMappings`，来源从配置读取。

#### 🟡 P2-1 无参数校验

全项目未引入 `spring-boot-starter-validation`，没有任何 `@Valid / @NotBlank / @Min`。前端校验（如手机号正则 `UserLogin.vue:117`）可被直接绕过；`Pagination` 的 `current` 未做上下界校验，`page=0` 或 `page=-1` 会打到数据库。

**优化方向**：引依赖 → 请求 DTO 加注解 → `@RestControllerAdvice` 统一处理 `MethodArgumentNotValidException`。

---

### 2.2 架构与分层（P0 / P1）

#### 🔴 P0-4 跨表业务流程完全不具备原子性（最严重的架构问题）

"捐助"这一笔业务在**数据库里要同时改动 4 张表**：扣用户宠宠币、加宠物宠宠币、扣 food 库存、写 donate 记录；"领养审核通过"要同时改 adopt、pet、user 三张表。

但这两条流程**后端没有对应的事务方法**，全部由前端串联多个 HTTP 请求完成：

```javascript
// PetDetail.vue:371-411 —— 一次捐助 = 4~8 个串行请求
axios.patch('/pet',  { id: pid, petCoin: currentpet.value.petCoin + total })
  .then(() => axios.patch('/user', { id: currentUser.id, petCoin: currentUser.petCoin - total })
    .then(() => {
      for (...) axios.patch('/food', { id, inventory: inventory - tempCount })   // 循环发 N 个请求
      axios.post('/donate', {...})
    }))
```

```javascript
// Adopt.vue:166-188 —— 一次审核 = 3 个串行请求
axios.patch('/adopt', {...}).then(() => axios.patch('/pet', {...})
  .then(() => { if (pass) axios.patch('/user', { howAdopt: +1 }) }))
```

后果：
- 任意一个请求失败（网络抖动、库存并发），前面的修改**已落库且无法回滚** → 用户扣了钱但没生成捐助记录、宠物被领养但用户领养数没加。
- 每个请求都是独立的 `@Transactional`（甚至多数是单表操作，事务毫无意义），跨表一致性无从保证。

**优化方向（关键整改项）**：把业务下沉到 Service，用一个事务方法承载整笔业务，前端只发**一个**请求。

```java
// DonateService
@Transactional(rollbackFor = Exception.class)
public void donate(DonateDTO dto) {
    // 1. 校验用户余额（服务端读库，不信前端）
    // 2. 校验库存（带版本或行锁，防超卖）
    // 3. 扣用户宠宠币 / 加宠物宠宠币
    // 4. 扣库存（UPDATE food SET inventory = inventory - ? WHERE id=? AND inventory >= ?）
    // 5. 写 donate 记录 + 序列化 donateList
}
// Controller: @PostMapping("/donate/commit") → 前端一次调用
```

> 这也顺带解决 P0-5。

#### 🔴 P0-5 金额与积分由前端计算并提交（业务数据可篡改）

```javascript
// PetDetail.vue:363-404
if (totalprice.value <= currentUser.petCoin) {          // 余额判断在前端
  axios.patch('/pet', { petCoin: currentpet.value.petCoin + total })
  axios.post('/donate', { totalPrice: total, donateList: JSON.stringify(...) })  // 总价由前端给
}
```

`totalPrice`、`donateList`、扣减后的 `petCoin` 全部由前端算好传给后端，后端只做 `insert` / `updateById`。刷新页面或改一下 `sessionStorage` 就能零成本捐助、任意改价。

**优化方向**：前端只提交 `{ petId, items: [{ foodId, count }] }`，**总价、余额校验、库存校验全部在服务端重算**（P0-4 的 Service 内一并实现）。

#### 🟠 P1-4 Controller 直连 Mapper，业务逻辑写在 Controller

```java
// AdminController.java:29 / 42-48
@Autowired private UserMapper userMapper;                 // Controller 层直接注入 Mapper
...
List<User> users = userMapper.selectList(null);           // 在 Controller 里写业务流程
for (User user : users) messageCount += messageService.queryallnoread(user.getId(), "admin");
```

`AdminServiceImpl` 已经存在且已有 `getallinfo()`，这段统计逻辑却写在 Controller 里，同时造成 N 次查询（见 P1-6）。

**优化方向**：Controller 只做「参数接收 → 调 Service → 包装响应」三件事，数据组装全部下沉。`AdminController` 改为一行 `adminService.getallinfo()`。

#### 🟠 P1-5 无全局异常处理，异常靠"假成功"掩盖

- 全项目没有 `@RestControllerAdvice`。`UploadController.java:59` 的 `throw new RuntimeException(e)` 会直接把 500 堆栈甩给前端。
- 失配用 `ResultBean` 的 `code=0` 表达，但 **HTTP 状态码恒为 200**，前端只能靠 `res.data.code == 200` 判断（`UserLogin.vue:141` 等 20 余处）。这让接口对第三方 / Postman 调试 / 监控完全不可用。

**优化方向**：新增 `GlobalExceptionHandler`（捕获 `BizException`、`MethodArgumentNotValidException`、`Exception`），返回带语义的 HTTP 状态码；`ResultBean` 泛型化为 `ResultBean<T>`，`code` 改用 `200/400/401/403/404/500` 语义化常量。

#### 🟠 P1-6 无 DTO / VO 层，实体直接进出

请求体直接绑 `User`、`Pet`、`Adopt` 等持久化实体（`@RequestBody User user`），导致三类问题：
- 前端可以提交任意字段（如 `isDel`、`petCoin`、`comPermiss`）—— 越权改字段；
- 响应体带出password 等敏感列（P0-2）；
- 实体字段一旦改动会同时破坏接口契约与前端。

**优化方向**：按场景补 `XxxQueryDTO` / `XxxSaveDTO` / `XxxVO`，用 `BeanUtils` 或 MapStruct 转换（毕设体量用前者即可）。

#### 🟠 P1-7 Service 层退化为"透传壳"

`FoodServiceImpl`、`MessageServiceImpl`、`JournalServiceImpl` 等方法几乎全是 `mapper.insert/updateById/deleteById` 的一行包装，没有业务含义；与此同时真正的业务（算价、校验、级联）却散在 Controller 和前端。分层存在形式主义问题。

**优化方向**：保留分层骨架（答辩需要），但**把业务搬进来**：分数统计、评论数维护、状态机校验、事务编排都落在 Service。

#### 🟡 P2-2 `@Transactional` 使用不当

- `@Transactional` 大量挂在**单条** `insert/delete/updateById` 上（如 `UserServiceImpl.java:59-79`、`FoodServiceImpl.java:30-45`），无实际收益。
- 真正需要事务的跨表流程反而没有（P0-4）。
- `JournalServiceImpl` 全类无 `@Transactional`，与同类实现不一致。

**优化方向**：单表操作去掉注解；只为"多写操作"方法加 `@Transactional(rollbackFor = Exception.class)`。

---

### 2.3 性能（P0 / P1 / P2）

#### 🔴 P0-6 N+1 / N×M 查询（最影响性能的一类问题）

**① 宠物详情页三级嵌套**

```java
// PetServiceImpl.java:65-86
List<Comment> comments = commentMapper.selectList(lqw);          // 1 次
for (Comment comment : comments) {
    List<Comment> levelTwo = commentMapper.selectList(...);      // 每条一级评论 1 次
    for (Comment twocomment : levelTwo)
        twocomment.setUser(userMapper.selectById(...));          // 每条二级评论 1 次
    comment.setUser(userMapper.selectById(comment.getUid()));    // 每条一级评论 1 次
}
```

400 条评论 ≈ **400+ 次查询**。

**② 只有一个用户的宠物列表也逐行查用户**

```java
// PetServiceImpl.java:112-114
for (Pet pet : pets)
    if (pet.getType() == 1) pet.setUser(userMapper.selectOne(...));  // 每行 1 次
```

**③ 帖子列表双重循环 + 循环内重复查同一条 post**

```java
// PostServiceImpl.java:153-166
for (Post post : postList) {
    post.setUser(userMapper.selectById(post.getUid()));
    List<Comment> comments = commentMapper.selectList(...);
    for (Comment comment : comments) {
        comment.setUser(userMapper.selectById(comment.getUid()));
        comment.setPost(postMapper.selectById(post.getId()));     // ← 同一条 post 被查了 N 次
    }
    post.setComments(comments);
}
```

**④ 后台统计 O(n) 查询**

```java
// AdminController.java:42-47
List<User> users = userMapper.selectList(null);
for (User user : users) messageCount += messageService.queryallnoread(user.getId(), "admin");  // 每个用户 1 次
```

**⑤ 用户列表页的"前端 N+1"**

```javascript
// User.vue:222-227 / 180-184
for (let i = 0; i < tableData.value.length; i++) {
  axios.get(`/message/noread/${tableData.value[i].id}/admin`)   // 每行发一个请求
}
```
一页 10 行 = 11 个请求。

**优化方向（通用套路）**：先在内存中收集主键集合 → 一次 `selectBatchIds` / `selectList(in(...))` → 转 `Map<id, Entity>` → 循环里 `map.get()` 装配；统计类改成聚合 SQL：

```java
// 后台未读消息：一条 SQL 搞定
SELECT COUNT(*) FROM message WHERE content IS NULL AND is_read = 1
// 后台统计也可以合并为一次 selectMaps 的条件聚合
```
用户列表的未读数建议**由后端在分页接口里一次性返回**（VO 增加 `noReadCount` 字段），前端删除循环请求。

#### 🟠 P1-8 数据库无任何二级索引，查询全表扫描

`rpfsa.sql` 中 10 张表除主键外**没有任何索引**。而实际高频查询条件为：

| 表 | 高频条件 | 现状 |
|---|---|---|
| `comment` | `poid=? and level=0`、`pid=?`、`level=?` | 全表扫描 |
| `adopt` | `userid=? and is_read=1` | 全表扫描 |
| `donate` | `uid=?`、`uid` 分组聚合 | 全表扫描 |
| `message` | `uid=? and is_read=1` | 全表扫描 |
| `pet` | `type=?`、`classic=?`、`uid=?` | 全表扫描 |
| `post` | `type=? and classic=?` | 全表扫描 |

**优化方向**：补索引（不改逻辑，纯增益）：
```sql
ALTER TABLE comment ADD INDEX idx_poid_level (poid, level), ADD INDEX idx_pid (pid);
ALTER TABLE adopt   ADD INDEX idx_uid_isread (userid, is_read), ADD INDEX idx_petid (petid);
ALTER TABLE donate  ADD INDEX idx_uid (uid), ADD INDEX idx_create_time (create_time);
ALTER TABLE message ADD INDEX idx_uid_isread (uid, is_read);
ALTER TABLE pet     ADD INDEX idx_type_classic (type, classic), ADD INDEX idx_uid (uid);
ALTER TABLE post    ADD INDEX idx_type_classic (type, classic), ADD INDEX idx_uid (uid);
```

#### 🟠 P1-9 分页返回的 `size` 与实际页大小不一致（隐性 bug）

| 位置 | 实际分页 | `Pagination` 上报 size |
|---|---|---|
| `UserServiceImpl.java:83/87` | 10 | 10 ✅ |
| `PetServiceImpl.java:108/115` | **12** | **10** ❌ |
| `AdoptServiceImpl.java:38/47` | **10** | **12** ❌ |
| `DonateServiceImpl.java:73/82` | 10 | 10 ✅ |
| `PostServiceImpl.java:129/141` | 12 | 12 ✅ |

`Pagination.size` 是前后端约定的分页元数据，错值会让任何依赖它的前端逻辑出错。`AdoptServiceImpl.java:47` 还用了 `new Pagination(current, ...)` 而非 `pageinfo.getCurrent()`。

**优化方向**：页大小抽成常量（或配置项），`Pagination` 统一从 `IPage` 取值构造，杜绝手写魔数。

#### 🟠 P1-10 循环内逐条 update，应改批量

```java
// MessageServiceImpl.java:52-56 / AdoptServiceImpl.java:83-87 / DonateServiceImpl.java:104-108
for (Xxx x : list) { x.setIsRead(0); mapper.updateById(x); }   // N 次 UPDATE
```

**优化方向**：一条批量语句，且无需先把实体查出来：
```java
messageMapper.update(null, new LambdaUpdateWrapper<Message>()
        .set(Message::getIsRead, 0).eq(Message::getUid, uid).eq(Message::getIsRead, 1));
```
或直接用 `@Update` 自定义 SQL。这也顺带消除了"先查后改"的 TOCTOU 窗口。

#### 🟡 P2-3 其他性能点

- 首页 `Introduce.vue:82-86` 一次拉全部公告再前端 `filter` 分三类（`postsall` 无分页、无 limit），数据量一大会拖慢首屏；`NoticeList.vue:21` 注释也自认"全部查了，不分页"。建议后端按 `classic` 分组返回或前端改成 `Promise.all` 三个带 limit 的查询。
- 统计接口 `/admin` 无缓存，仪表盘每次进入都跑十几条 `count(*)`。可加 `@Cacheable` 或 30s 内存缓存。

---

### 2.4 代码质量与可读性（P2 / P3）

| # | 问题 | 位置 | 建议 |
|---|---|---|---|
| P2-4 | 命名拼写错误并已扩散到前后端与数据库 | `ResultBean`/`BaseController` 的 `FAILE`；`User.comPermiss`；`pet.describtion`；`Adopt.adress`；`PetDetail.vue:56` 的 `vatriety` | 统一为 `FAIL`、`commentPermission`、`description`、`address`、`variety`；用一次迁移脚本 + 全局替换 |
| P2-5 | 魔法值散落 | `isRead`（0/1 语义反直觉：**1=未读**）、`state`（0/1/2）、`isAdopt`（0/1/2）、`type`（0/1）、`classic`（0~7） | 抽 `enum` 或常量类，如 `AdoptState.PENDING(2)/PASSED(1)/REJECTED(0)`；避免 `adopt.state == 2 ? '审核中' : adopt.state ? '已通过' : '失败'` 这种三目嵌套 |
| P2-6 | 注释被当成代码用 | `AdoptServiceImpl.java:53-55`、`DonateServiceImpl.java:89-91` 用 `qw.like(column.equals("username"), ...)` 隐藏条件开关 | 改为显式 `if`，可读性优先 |
| P2-7 | 死代码与未使用 import | `UserMapper` 空注释块 + 未用 `@Select/List/Pet`；`User.java` 未用 `TableField/List`；`Message.java` 未用 `@Autowired` | IDE 一键清理（`Optimize Imports`） |
| P2-8 | `Pagination.records` 用裸类型 `List` | `Pagination.java:21` | 泛型化 `List<T>` 或直接复用 MP 的 `IPage` |
| P2-9 | `BaseController` 常量非 static 且大小写不当 | `BaseController.java:11-12` | `private static final int SUCCESS = 200;` |
| P2-10 | 大量调试输出残留 | `UploadController.java:40-41` `System.out.println`；前端 20+ 处 `console.log`（如 `PetDetail.vue:406`、`User.vue:301`） | 后端用 `@Slf4j` + 分级日志；前端删除或改 `vite` 生产构建剔除 |
| P2-11 | 注释中出现口语化 / 不当措辞 | `User.vue:223` `//可以增加属性,卧槽,牛逼`；`User.vue:147` `awfawfaw` 类残留；`PetDetail.vue:369` `//不知道为什么...原因不明` | 答辩前必须清理，改为描述性注释 |
| P2-12 | 逻辑删除配置与实体无显式声明 | `application.yml:24-29` 全局配 `logic-delete-field: isDel`，实体未加 `@TableLogic` | 依赖全局配置可行，但建议实体显式标注，避免后续误判 |
| P3-1 | `querypetbyuid` 条件语义混乱 | `PetServiceImpl.java:61` `.eq(uid).eq(type,1).or().eq(uid).eq(isAdopt,1)` | 用 `lqw.and(w -> ...).or(w -> ...)` 明确括号边界，否则 SQL 优先级变化即出错 |

---

### 2.5 过时实现（P2）

| # | 现状 | 问题 | 建议 |
|---|---|---|---|
| P2-13 | `springfox-swagger2 / swagger-ui 2.9.2` | 2020 年已停止维护，与 Spring Boot 2.7 不兼容（因此被迫在 `application.yml:11-13` 打 `ant_path_matcher` 补丁），且存在已知 CVE | 换 **springdoc-openapi-ui 1.6.x**（对 SB 2.7 原生适配），可移除 `pathmatch` 补丁 |
| P2-14 | Swagger 无环境开关 | 生产环境会暴露全部接口结构 | `@Profile("dev")` 或 `springdoc.api-docs.enabled=false` |
| P2-15 | `log-impl: StdOutImpl` | 生产不可用 | `Slf4jImpl` + `logging.level.com.erdongj.mapper: debug` |
| P2-16 | `spring.web.resources.static-locations: classpath:static/` | 覆盖默认值，可能影响 Swagger UI 静态资源加载 | 明确列出所需位置，或改用 `addResourceHandlers` 追加而非覆盖 |
| P3-2 | 依赖版本 | `commons-io 2.11.0`、`druid 1.2.9` 偏旧 | 升级到当前稳定版并统一由 `spring-boot-dependencies` 管理 |
| P3-3 | 交付物冗余 | 仓库同时保留源码与 `rpfsa.zip`（README 还写着"解压 zip 导入 IDEA"） | 删 zip，README 改为标准 `git clone` 流程 |

---

### 2.6 数据库设计（P1 / P2）

| # | 问题 | 说明 | 建议 |
|---|---|---|---|
| P1-11 | **反范式冗余且必然产生不一致** | `adopt` 表存 `username / nickname / petname`，`donate` 表同样。用户改昵称、宠物改名后，历史记录仍是旧值；且写入时由前端提供（`PetDetail.vue:471-476`），可伪造 | 通过 `userid/petid` 关联查询；若为性能保留冗余，则由服务端在写入时填充并接受"历史快照"语义并注明 |
| P1-12 | **无外键约束** | 全库 zero FK。删除用户/宠物后，`adopt`、`comment`、`donate`、`journal` 中残留孤儿数据（`UserServiceImpl.deleteonebyID` 只删主表） | 加 FK + `ON DELETE CASCADE`，或在 Service 里做级联软删 |
| P1-13 | `donate.donate_list` 用 `text` 存 JSON 字符串 | 前端每次手动 `JSON.parse` / `stringify`（`Donate.vue:87`、`UserCenter.vue:304`），无结构校验 | MySQL 8 用原生 `JSON` 类型；后端用 `TypeHandler` 或 Jackson 直接映射为 `List<FoodItem>`，前端拿到即对象 |
| P2-17 | `admin` 表以 `adminname` 作主键 | `varchar(20)` 主键，后续扩展困难 | 加自增 `id` 主键，`adminname` 建唯一索引 |
| P2-18 | `pet.uid` 用 `0` 表示"未领养"（注释自认"不知道怎么置空"） | 语义与 NULL 混淆，且 `post.uid=0` 表示"管理员发布"是另一种语义 | 统一为 `NULL`，查询用 `isNull()`；管理员发布的帖子加 `is_admin` 标志位更清晰 |
| P2-19 | 逻辑删除列 `is_del` 与业务状态列并存但无约束 | 如 `pet.is_del`、`pet.is_adopt` 语义交叉 | 文档化状态机，服务端强校验状态流转合法性（如已领养的宠物不能再被领养） |

---

## 3. 前端（Vue 3 + Element Plus）问题详解

### 3.1 结构与分层（P1 / P2）

#### 🟠 P1-14 没有 API 层，axios 调用散落在 26 个组件里

每个组件各自 `import axios` 并硬编码 URL 字符串，例如：

```javascript
// 同一接口在多个文件重复出现
axios.get(`/pet/page/${currentpage.value}`)   // DogandCat.vue:176
axios.get(`/pet/page/${currentpage.value}`)   // Pet.vue:289
axios.get(`/adopt/noread/${usersession.id}`)  // Rpfsa.vue:77
```

**优化方向**：建 `src/api/` 目录按域拆分（`pet.js`、`adopt.js`、`comment.js`…），组件只调 `petApi.page(current)`。收益：
- URL 变更只需改一处；
- 可在 API 层统一处理 `code != 200` 的提示；
- 便于后续 mock / 换真实网关。

#### 🟠 P1-15 没有全局状态管理，登录态靠 sessionStorage 手工同步

`Rpfsa.vue:73` / `DogandCat.vue:195` / `UserCenter.vue:421` / `Community.vue:184` / `PetDetail.vue:326` / `Diary.vue:175` / `PostDetail.vue:208` —— **7 个组件各自**在 setup 顶层执行：

```javascript
const currentUser = reactive(JSON.parse(sessionStorage.getItem("user")))
```

问题：
- 未登录时 `JSON.parse(null)` → `null`；`reactive(null)` 返回 `null`，后续 `currentUser.id` 全部依赖 `v-if` 兜底，极易漏判；
- 用户修改资料后要手工 `sessionStorage.setItem("user", ...)`（`UserCenter.vue:210`、`PetDetail.vue:393`）逐处同步，一旦漏写就出现"页面显示旧昵称"；
- 管理端 `Header.vue:21` 直接读 `sessionStorage`，退出时 `clear()` 会顺带清掉其他键。

**优化方向**：引入 **Pinia**（或一个轻量 `composables/useUser.js`），把 `currentUser / token / 未读计数` 收拢为一处；持久化用 `pinia-plugin-persistedstate`。同时把 `adoptnews / donatenews / messagenews` 这三个 `provide/inject` 的裸 ref（`Rpfsa.vue:66-71`）一并纳入 store，避免"跨组件用 provide 传可变引用"的隐式耦合。

#### 🟠 P1-16 用户端与管理端重复实现，未抽公共库

`rpfsa_front` 与 `rpfsa_manage` 是两个独立工程，`axios.defaults.baseURL`、`Element Plus` 中文包、图片 URL 拼接、上传校验逻辑全部各写一份，且互有出入（见 P2-21）。毕设场景下可接受，但至少应将可复用代码抽成一个 `shared` 包或用 monorepo（`pnpm workspace`）统一。

#### 🟠 P1-17 巨型组件，必须拆分

| 组件 | 行数 | 问题 |
|---|---|---|
| `PetDetail.vue` | 723 | 宠物详情 + 评论树 + 捐助弹层 + 领养弹层 + 地址级联，5 种职责 |
| `UserCenter.vue` | 645 | 6 个 el-dialog 挤在一个组件 |
| `User.vue` | 441 | 表格 + 5 个弹层 + 消息会话 |
| `Community.vue` | 363 | 置顶 + 筛选 + 列表 + 编辑器 |

其中 **`PetDetail.vue:72-145` 与 `PostDetail.vue:14-87` 的评论区块几乎逐行相同**（含二级评论渲染、popover 用户卡、删除确认），是最高价值的抽取目标。

**优化方向**：
```
components/
  comment/CommentSection.vue      # 评论树（可复用于宠物/帖子）
  comment/CommentItem.vue         # 单条评论 + 回复
  comment/CommentEditor.vue       # 输入框
  common/ImageUploader.vue        # 统一上传 + 预览 + 校验
  common/SearchBar.vue            # 统一"字段选择 + 关键词 + 刷新"
  common/UserPopover.vue          # 统一的用户信息浮层
  detail/PetDetailDialog.vue      # 捐助 / 领养弹层
```

---

### 3.2 工程配置（P0 / P1）

#### 🔴 P0-7 接口地址硬编码，无法切换环境 / 无法部署

```javascript
// rpfsa_front/src/main.js:10
axios.defaults.baseURL = 'http://localhost'
// rpfsa_manage/src/main.js:9
axios.defaults.baseURL = 'http://localhost/'
```

图片地址同样是硬编码，且**每个组件各写一份**：

```javascript
// PetDetail.vue:325、DogandCat.vue:266、User.vue:201（管理端）、Thank.vue:138 ...
function getImageUrl(url) { return new URL(`img/${url}`, 'http://localhost/').href }
// 而另外一些地方是纯字符串拼接
:src="`http://localhost/img/${comment.user.photo}`"     // PetDetail.vue:85
:src="`http://localhost/img/${food.picture}`"           // PetDetail.vue:152
```

此外 `el-upload` 的 `action` 也硬编码：`action="http://localhost/upload"`（`DogandCat.vue:93`、`UserCenter.vue:178`、`User.vue:75`、`Pet.vue:107`、`Food.vue:19/66`）。

后果：换服务器 IP 要全局搜索替换；`vite build` 产物只能跑在本机。

**优化方向**：
1. 建 `.env.development` / `.env.production`：
   ```
   VITE_API_BASE_URL=/api
   VITE_STATIC_BASE_URL=/
   ```
2. `vite.config.js` 配 `server.proxy` 解决开发跨域（顺带可以去掉后端全部 `@CrossOrigin`）：
   ```javascript
   server: { proxy: { '/api': { target: 'http://localhost:80', changeOrigin: true, rewrite: p => p.replace(/^\/api/, '') } } }
   ```
3. 抽 `src/utils/url.js` 导出唯一的 `resolveImg(name)`，全项目统一调用（当前 `UserCenter.vue:422` 的 `getimgurl` **返回 URL 对象而非字符串**，依赖隐式 `toString` 才没出错，属于必须收敛的隐患）。

#### 🟠 P1-18 没有 axios 拦截器，错误处理重复 20 余次

`res.data.code == 200` 的判断在每个请求里手写；`.catch(() => ElMessage.error('服务器故障'))` 在 `User.vue`、`Pet.vue`、`Food.vue`、`UserLogin.vue` 等反复出现。

**优化方向**：统一封装 `src/utils/request.js`
```javascript
const request = axios.create({ baseURL: import.meta.env.VITE_API_BASE_URL, timeout: 10000 })
request.interceptors.request.use(cfg => { cfg.headers.Authorization = useUserStore().token; return cfg })
request.interceptors.response.use(
  res => { const { code, msg, data } = res.data
           if (code !== 200) { ElMessage.error(msg || '请求失败'); return Promise.reject(new Error(msg)) }
           return data },                                  // ← 组件不再解 .data.data
  err => { ElMessage.error('网络异常，请稍后重试'); return Promise.reject(err) }
)
```
这一项改完，26 个组件里的重复判断和 `.catch` 样板代码可以直接删掉一大半。

#### 🟠 P1-19 路由守卫写法有 bug，未登录访问会卡死导航

```javascript
// rpfsa_front/src/router/index.js:28-31
beforeEnter: (to, from, next) => {
    if (sessionStorage.getItem("user")) next()
    else router.replace('/login')          // ← 没有 next()，也没有 return
}
// rpfsa_manage/src/router/index.js:23-26 同款
```

Vue Router 4 中守卫既不调用 `next()` 也不返回值时，导航会一直处于 **pending** 状态（页面无响应）。正确写法（推荐新版写法）：

```javascript
beforeEnter: () => (useUserStore().isLogin ? true : { path: '/login', replace: true })
```
前端路由守卫只是体验优化，**真正的守门必须放在后端**（见 P0-1）。

---

### 3.3 代码缺陷（P0 / P2）

#### 🔴 P0-8 模板中存在会导致报错 / 渲染异常的写法

| # | 位置 | 问题 | 修正 |
|---|---|---|---|
| 1 | `PetDetail.vue:44`、`Community.vue:36` | `<template #>` —— **空插槽名**，Vue 编译告警且插槽内容不渲染（用户信息浮层会空白） | `<template #default>` |
| 2 | `Community.vue:58` | `<div class="post-author">{{ }}</div>` —— **空插值表达式**，编译产物为无参调用 | 删除该节点（作者信息本就未实现）或补上变量 |
| 3 | `PetDetail.vue:14`、`PetDetail.vue:60` | `<el-tag v-else="currentpet.isVaccine == 0">` —— `v-else` 不接受表达式 | 改成 `<el-tag v-else>` |
| 4 | `PetDetail.vue:56` | `currentpet.vatriety` 拼写错误（后端字段是 `variety`），品种永远不显示 | 改为 `variety` |
| 5 | `PetDetail.vue:117` | 二级评论头像用了 `comment.user.photo`（应为 `leveltwocon.user.photo`），复制粘贴错误 —— **有子评论时头像显示主人头像** | 改为 `leveltwocon.user.photo` |
| 6 | `Diary.vue:43`、`Pet.vue:207` | `v-html="diary.value.diary"`，而 `diary` 是 `reactive({})`（`Diary.vue:168`、`Pet.vue:227`）。靠"给 reactive 对象挂 `value` 属性"歪打正着，语义完全错误 | 统一用 `ref(null)`，模板写 `diary?.diary` |
| 7 | `User.vue:235`、`Pet.vue:301` | 同上模式：`const userinfo_x = reactive({})` + `userinfo_x.value = user` | 改用 `ref()` |

> 第 1、2 条是**编译期/运行期会真实报错**的，答辩演示时最容易当场翻车。

#### 🟠 P1-20 定时器在 setup 顶层注册且从不清理，存在内存泄漏

```javascript
// Rpfsa.vue:59-64
onMounted(() => { getlatestmessage() })
setInterval(getlatestmessage, 1000 * 60 * 60)     // ← 在 setup 作用域顶层执行
```

`setInterval` 在组件初始化时立即执行，`Rpfsa.vue` 每次重新挂载都会新增一个定时器，且组件卸载不清理（`App.vue` 的"快速刷新"机制会销毁重建 router-view，见 `App.vue:8-17`）。

**优化方向**：
```javascript
onMounted(() => { timer = setInterval(getlatestmessage, 60 * 60 * 1000); getlatestmessage() })
onUnmounted(() => clearInterval(timer))
```
更简单：登录后拉一次，页面可见性变化时拉一次（`document.visibilitychange`）。

#### 🟡 P2-20 未登录 / 无权限分支处理不一致

`DogandCat.vue:195` `const currentUser = reactive(JSON.parse(sessionStorage.getItem("user")))` 后，多处用 `if (currentUser != null)` 判断，但 `UserCenter.vue` 用 `currentUser.id`（`UserCenter.vue:266`）—— 未登录时该组件已被路由守卫拦住，但守卫本身有 bug（P1-19），实际会走到崩溃分支。

#### 🟡 P2-21 前后端职责错位：评论数在两处各改各的

```javascript
// PetDetail.vue:281-284 / PostDetail.vue:169-172
axios.post('/comment', {...}).then(() => {
    axios.patch('/pet', { id: pid, commentCount: currentpet.value.commentCount + 1 })  // 前端算计数
})
```
`comment_count` 由前端"读当前值 +1 再回写"，属于典型的 read-modify-write：并发或重试会导致计数漂移。同理删评论用 `- res.data.data` 依赖后端返回条数。

**优化方向**：评论的新增/删除由 `CommentService` 在同一事务内维护 `comment_count`（SQL 层 `comment_count = comment_count + 1`），前端删除这两个 `axios.patch`。

#### 🟡 P2-22 筛选 / 分页状态脱节

```javascript
// DogandCat.vue:186-190
function handleCurrentChange(currentpagec) {
    currentpage.value = currentpagec
    if (searchinfo.col == null && searchinfo.con == null) petsget()
    else search()
}
```
未考虑 `classic` / `showchange` 两个筛选条件 —— 翻页时如果用户是按"小猫"筛选的，翻到第 2 页会回到全量列表。
`Community.vue:238-243`、`Pet.vue:278-286`、`User.vue:210-214`、`Adopt.vue:208-212`、`Donate.vue:127-131` 全部是同一套手写逻辑的复制品。

**优化方向**：抽 `usePagedQuery(fetcher)` composable，内部维护 `{ page, filters }` 一个状态对象，翻页时带上全部筛选条件重新请求；同时把"筛选条件 + 分页"同步进 URL query（`router.replace`），实现刷新后状态不丢。

#### 🟡 P2-23 注释掉的大段死代码

`UserCenter.vue:71-83`、`PetDetail.vue:52-56`、`Pet.vue:171-184`、`Diary.vue:7-12`、`UserLogin.vue:39-41/119-122`、`Adopt.vue:171-184` —— 大量被注释的模板与样式。答辩前清理，需要保留的用 Git 历史即可。

---

### 3.4 安全（P0）

#### 🔴 P0-9 前端持有完整用户对象并回传，且包含密码

```javascript
// UserLogin.vue:144
sessionStorage.setItem('user', JSON.stringify(res.data.data))   // data 是后台完整 User（含 password）
// UserCenter.vue:207
axios.patch('/user', currentUser)                                // 把整个对象（含 password）发回后端
```

- `sessionStorage` 可被任意同源 XSS 读取；
- 修改资料的请求把 `password`、`comPermiss`、`isDel`、`petCoin` 一并提交（`UserCenter.vue:164` 的 `:model="currentUser"` 直接绑整个对象），后端 `updateById` 无条件接受 → **用户可自行提权**（改 `comPermiss`）和**改积分**（改 `petCoin`）。

**优化方向**：
1. 后端返回 `UserVO`（无 password），登录态改用 token（P0-1）；
2. 修改资料改提交白名单 DTO：`{ nickname, email, phone, photo }`，**服务端忽略其他字段**；
3. `sessionStorage` 至少换成 `sessionStorage` 存 token + 用户展示信息，敏感字段不落地。

> 这一条与 P0-1、P0-2 是同一个根因（后端信任前端），建议合并整改。

#### 🟠 P1-21 富文本 XSS 风险

帖子/日记内容由 Quill 生成，以 `contentType="html"` 直接入库（`Community.vue:76`、`Notice.vue:60`、`Diary.vue:27`），渲染时用 `v-html`（`PostDetail.vue:12`、`Diary.vue:43`、`Notice.vue:44`、`Post.vue:57`）。Quill 默认会过滤一部分标签，但 `content` 是**通过 HTTP 接口直接写入的**（`POST /post`），绕过编辑器构造 `<img onerror=...>` 即可存储型 XSS。

**优化方向**：后端入库前用 `jsoup.clean(content, Safelist.basic())` 白名单过滤；前端渲染前同样过滤一次（双层防护）。

---

### 3.5 性能与体验（P2）

| # | 问题 | 位置 | 建议 |
|---|---|---|---|
| P2-24 | 列表图片无懒加载 | `DogandCat.vue:47`、`Thank.vue:73`、`PetDetail.vue` 各处 `<img>` | 加 `loading="lazy"`，或 Element Plus `el-image` 的 `lazy` |
| P2-25 | 重复请求 | `Thank.vue:92-97` 首屏连发 4 个榜单请求；`Rpfsa.vue:77-85` 发 3 个未读请求 | 用 `Promise.all` 并行；或合并为一个 `/home/summary` 接口 |
| P2-26 | 管理端统计图靠 `setTimeout(300)` 硬等 | `AdminCenter.vue:58-61` | 改为 `await allinfoget()` 后再 `init`，或 `watch(allinfo, init)` |
| P2-27 | ECharts 实例未销毁 | `AdminCenter.vue:77/119` | `onUnmounted` 里 `chart.dispose()`；加 `window.resize` 监听 |
| P2-28 | 表格列宽用 `min-width="auto"` | `Pet.vue` 多处 | 改为具体像素值，避免渲染抖动 |
| P3-4 | `el-radio label` 用法已过时 | `DogandCat.vue:71-73`、`Pet.vue:85-87` 等 | Element Plus 2.6+ 推荐 `value`，升级依赖时一并改 |

---

### 3.6 可维护性（P2）

| # | 问题 | 说明 |
|---|---|---|
| P2-29 | **样式重复定义** | `.avatar-uploader` 相关全局样式在 `Pet.vue`、`User.vue`、`Food.vue`、`DogandCat.vue`、`UserCenter.vue` 各写一遍；`.content-list` 在 `Introduce.vue`、`NoticeList.vue`、`Community.vue` 重复。应收敛到 `src/styles/` |
| P2-30 | **工具函数重复** | `getImageUrl` ×3、`beforeAvatarUpload` ×5、`changeAvatar` ×5、`isred` ×2、`flush` ×6、`handleCurrentChange` ×6 —— 全部可以抽 util / composable |
| P2-31 | **无 lint 约束** | 两个前端工程都没有 ESLint / Prettier / `.editorconfig`，代码风格（引号、缩进、分号）混杂。建议加 `eslint-plugin-vue` + `prettier`，`vite build` 前跑一次 |
| P2-32 | **无 TypeScript / 无类型约束** | 四个工程均为 JS。若时间允许，建议至少给 `src/api/` 与 `src/utils/` 加 JSDoc 或迁移到 TS —— 答辩时是加分项 |
| P3-5 | `defineProps(['pid'])` 用了运行时数组写法 | 建议改 `defineProps<{ pid: number }>()` 或至少 `{ pid: { type: Number, required: true } }` |
| P3-6 | 部分 `v-for` 用 index 作 key | `Thank.vue:8/25/44` 用 `:key="user.id"` 是对的，但 `v-for="user, index"` 中 index 仅用于序号展示，无问题；建议统一审查一遍 |
| P3-7 | README 与实现不符 | README 写"Node 16+ / Java 8+"，实际 `pom.xml` 是 `java.version=11`；`rpfsa_front/README.md`、`rpfsa_manage/README.md` 仍是 Vite 模板原文 | 补齐真实环境要求、启动步骤、默认端口、账号 |

---

## 4. 各模块优化重点对照表

| 模块 | 优化重点（优先级从高到低） | 预期收益 |
|---|---|---|
| **后端 - 安全** | ① 加统一鉴权拦截器 + 角色隔离<br>② 密码 BCrypt 化 + 响应剥离敏感字段<br>③ 查询列名白名单化<br>④ 上传改外置目录 + 扩展名白名单 + UUID 命名<br>⑤ 配置外部化、关闭生产 Swagger | 消除 4 类 P0 安全缺陷；答辩"安全性"一问有可讲的完整方案 |
| **后端 - 架构** | ① **把捐助 / 领养审核等跨表流程下沉为 Service 事务方法，前端只发一个请求**<br>② Controller 去掉直连 Mapper<br>③ 引入 DTO/VO<br>④ 加 `@RestControllerAdvice` 全局异常<br>⑤ 状态流转合法性校验 | 数据一致性从"随时可能坏"变为"要么全成功要么全回滚"；分层名副其实 |
| **后端 - 性能** | ① N+1 → 批量查询 + Map 装配（宠物详情、帖子列表、后台统计）<br>② 补 6~10 个二级索引<br>③ 循环 update → 批量 update<br>④ 统一分页参数来源 | 列表/详情接口查询次数从 O(n) 降到 O(1)~O(2)；数据量上去后不再雪崩 |
| **后端 - 规范** | ① 拼写错误全局修正<br>② 枚举/常量替换魔法值<br>③ 清理死代码与 `System.out`<br>④ springfox → springdoc<br>⑤ 补 `service` 层真实业务 | 代码可读性显著提升；论文"系统实现"章节更有内容 |
| **前端 - 工程** | ① `.env` + `vite proxy` + 唯一 `resolveImg()` 替换所有硬编码 URL<br>② 封装 `request.js`（拦截器统一错误处理 + token）<br>③ 建 `src/api/` 层<br>④ 修路由守卫 pending bug<br>⑤ 引入 ESLint + Prettier | 换环境 0 改代码；组件里删掉大量样板代码 |
| **前端 - 结构** | ① 抽 5~8 个公共组件（**CommentSection 优先**，宠物/帖子两处共用）<br>② 抽 composable：`usePagedQuery`、`useUpload`、`useUnreadBadge`<br>③ Pinia 统一登录态与未读计数<br>④ 拆分 700 行巨型组件 | 组件平均行数下降 50%+；重复逻辑只维护一份 |
| **前端 - 正确性** | ① 修 7 处模板/语法缺陷（空插槽名、空插值、`v-else` 带值、`vatriety`、二级评论头像取错对象）<br>② 定时器改 `onUnmounted` 清理<br>③ 删死代码与调试输出<br>④ 清理不当注释 | 直接消除演示时的翻车点 |
| **前端 - 安全** | ① 不再把含 password 的用户对象存 `sessionStorage`<br>② 修改资料改白名单字段提交<br>③ `v-html` 前端二次过滤 | 用户无法自行提权 / 改积分 |
| **数据库** | ① 补索引、加外键或服务层级联<br>② 冗余列去留决策（推荐去冗余）与快照语义标注<br>③ `donate_list` 改 JSON 类型<br>④ 状态字段语义文档化 | 查询性能与数据完整性同时改善 |

---

## 5. 分阶段改造路线图

> 建议按此顺序推进，**每阶段结束都能独立编译运行、功能不回退**。

### 阶段一：止血（1~2 天）—— 让项目"站得住"
1. 修 7 处前端模板/语法缺陷（P0-8）。
2. 修路由守卫 pending bug（P1-19）。
3. 修定时器泄漏（P1-20）+ 清理 `console.log` 与不当注释（P2-10/11）。
4. 修 5 处 `Pagination.size` 不一致（P1-9）。
5. 补数据库索引（P1-8）。
6. 清理死代码与无用 import（P2-7/23）。

### 阶段二：工程化（2~3 天）—— 让项目"能交付"
7. `.env` + vite proxy + 统一 `resolveImg`，消除全部硬编码 `http://localhost`（P0-7）。
8. 封装 `request.js` + 建 `src/api/`（P1-14/18）。
9. 抽公共组件与 composable（P1-17、P2-29/30）。
10. 引入 Pinia 管理登录态与未读计数（P1-15）。
11. 后端配置外部化 + 日志改 Slf4j + springdoc 替换 springfox（P1-2、P2-13/15）。

### 阶段三：一致性攻坚（3~4 天）—— 让业务"可信任"
12. **捐助 / 领养审核流程下沉为 Service 事务方法**，前端改为单请求（P0-4/P0-5）。
13. 评论计数改由后端维护（P2-21）。
14. N+1 批量优化（P0-6）。
15. 循环 update 改批量（P1-10）。
16. 加 `@RestControllerAdvice` + `ResultBean<T>` 泛型化（P1-5）。

### 阶段四：安全加固（2~3 天）—— 让系统"抗得住"
17. 鉴权拦截器 + 角色隔离（P0-1）。
18. BCrypt 密码 + `UserVO` 剥离敏感字段 + 修改资料白名单（P0-2/P0-9/P1-6）。
19. 查询列名白名单（P0-3）。
20. 上传目录外置 + 扩展名白名单（P1-1）。
21. `jsoup` 富文本过滤（P1-21）。

### 阶段五：打磨（1~2 天）
22. 统一命名与拼写（P2-4）、魔法值枚举化（P2-5）。
23. 补 ESLint / Prettier / `.editorconfig`（P2-31）。
24. 更新三个 README（P3-7）。
25. 补最小可用测试：`UserServiceImplTest`、`DonateServiceTest`（事务回滚验证）—— 毕设答辩的加分项。

---

## 6. 关键改造示例（前后对比）

### 示例 1：捐助业务 —— 从"前端拼 8 个请求"到"一个事务"

**改造前**（`PetDetail.vue:363-411`，节选）：
```javascript
if (totalprice.value <= currentUser.petCoin) {            // 前端算钱、前端判余额
    axios.patch('/pet',  { id: pid, petCoin: currentpet.value.petCoin + total })
      .then(() => axios.patch('/user', { id: currentUser.id, petCoin: currentUser.petCoin - total })
        .then(() => {
          for (let i = 0; i < foodlist.value.length; i++)
            axios.patch('/food', { id: ..., inventory: ... - tempCount })   // N 个请求
          axios.post('/donate', { ..., totalPrice: total, donateList: JSON.stringify(...) })
        }))
}
```

**改造后**：
```java
// DonateController
@PostMapping("/commit")
public ResultBean<Void> commit(@RequestBody @Valid DonateCommitDTO dto, @CurrentUser User user) {
    donateService.commit(dto, user.getId());
    return ResultBean.success("捐助成功，感谢您的爱心");
}

// DonateServiceImpl
@Transactional(rollbackFor = Exception.class)
public void commit(DonateCommitDTO dto, Integer uid) {
    User user = userMapper.selectById(uid);
    Pet  pet  = petMapper.selectById(dto.getPetId());
    // 1) 服务端重新计价，完全忽略前端传来的金额
    int total = 0;
    List<FoodItem> items = new ArrayList<>();
    for (FoodItemDTO it : dto.getItems()) {
        if (it.getCount() <= 0) continue;
        Food food = foodMapper.selectById(it.getFoodId());
        if (food.getInventory() < it.getCount()) throw new BizException("库存不足：" + food.getFoodname());
        total += food.getPrice() * it.getCount();
        items.add(FoodItem.of(food, it.getCount()));
    }
    if (total == 0) throw new BizException("请至少选择一件救助品");
    if (user.getPetCoin() < total) throw new BizException("宠宠币不足，当前 " + user.getPetCoin());
    // 2) 同事务内完成全部写操作
    userMapper.deductCoin(uid, total);                    // UPDATE ... SET pet_coin = pet_coin - ? WHERE id=? AND pet_coin >= ?
    petMapper.addCoin(pet.getId(), total);
    for (FoodItem it : items) foodMapper.deductStock(it.getId(), it.getCount());  // 带库存校验的原子扣减
    donateMapper.insert(Donate.of(user, pet, items, total));
}
```

### 示例 2：N+1 → 两次查询（宠物详情评论树）

```java
// 改造前：PetServiceImpl.java:65-86，随评论数线性增长的查询次数
// 改造后：
public Pet queryonebyID(Integer id) {
    Pet pet = petMapper.selectById(id);
    if (pet == null) return null;

    List<Comment> topLevel = commentMapper.selectList(
            new LambdaQueryWrapper<Comment>().eq(Comment::getPid, id).eq(Comment::getLevel, 0));
    if (topLevel.isEmpty()) { pet.setComments(List.of()); return pet; }

    List<Integer> parentIds = topLevel.stream().map(Comment::getId).toList();
    List<Comment> children = commentMapper.selectList(
            new LambdaQueryWrapper<Comment>().in(Comment::getLevel, parentIds));   // 1 次拿到全部子评论

    // 一次性拿到所有相关用户，转 Map
    Set<Integer> uids = new HashSet<>();
    topLevel.forEach(c -> uids.add(c.getUid()));
    children.forEach(c -> uids.add(c.getUid()));
    if (pet.getType() == 1 && pet.getUid() != null) uids.add(pet.getUid());
    Map<Integer, User> userMap = userMapper.selectBatchIds(uids).stream()
            .collect(Collectors.toMap(User::getId, Function.identity()));

    Map<Integer, List<Comment>> childMap = children.stream()
            .collect(Collectors.groupingBy(Comment::getLevel));
    topLevel.forEach(c -> {
        c.setUser(userMap.get(c.getUid()));
        c.setCommentList(childMap.getOrDefault(c.getId(), List.of()));
        c.getCommentList().forEach(sub -> sub.setUser(userMap.get(sub.getUid())));
    });
    if (pet.getType() == 1) pet.setUser(userMap.get(pet.getUid()));
    pet.setComments(topLevel);
    return pet;
}
```
查询次数：`3 + N + N×M` → 固定 **4 次**。

### 示例 3：查询列名白名单（消除注入面）

```java
// 改造前：UserServiceImpl.java:91-95 / PetServiceImpl.java:119-130 —— column 直通 SQL
// 改造后：统一收敛到一个工具，非法字段直接拒绝
public final class QueryFieldGuard {
    private static final Map<String, Set<String>> ALLOW = Map.of(
        "user",  Set.of("username", "nickname", "phone", "email"),
        "pet",   Set.of("petname", "variety", "classic", "type"),
        "post",  Set.of("title", "classic"),
        "adopt", Set.of("username", "nickname", "petname"),
        "donate",Set.of("username", "nickname", "petname"));

    public static void check(String domain, String column) {
        if (column == null || !ALLOW.getOrDefault(domain, Set.of()).contains(column))
            throw new BizException(400, "不支持的查询字段：" + column);
    }
}
```

### 示例 4：统一响应 + 全局异常

```java
@Data
public class ResultBean<T> {
    private int code;
    private String msg;
    private T data;
    public static <T> ResultBean<T> success(T data) { ... }
    public static <T> ResultBean<T> fail(int code, String msg) { ... }
}

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(BizException.class)
    public ResponseEntity<ResultBean<Void>> biz(BizException e) {
        return ResponseEntity.status(e.getCode()).body(ResultBean.fail(e.getCode(), e.getMessage()));
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResultBean<Void>> valid(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage()).collect(Collectors.joining("; "));
        return ResponseEntity.badRequest().body(ResultBean.fail(400, msg));
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResultBean<Void>> other(Exception e) {
        log.error("系统异常", e);                       // 堆栈只进日志，不进响应体
        return ResponseEntity.status(500).body(ResultBean.fail(500, "系统繁忙，请稍后重试"));
    }
}
```

### 示例 5：前端公共 API 层 + 统一上传组件

```javascript
// src/utils/request.js —— 见 P1-18
// src/api/pet.js
import request from '@/utils/request'
export const petApi = {
  page:   (current)        => request.get(`/pet/page/${current}`),
  detail: (id)             => request.get(`/pet/${id}`),
  search: (current, col, con) => request.get(`/pet/${current}/${col}/${encodeURIComponent(con)}`),
  save:   (data)           => request.post('/pet', data),
  update: (data)           => request.patch('/pet', data),
  remove: (id)             => request.delete(`/pet/${id}`),
}
```

```vue
<!-- components/common/ImageUploader.vue —— 替换 5 处重复的上传逻辑 -->
<template>
  <el-upload :action="uploadUrl" :show-file-list="false" :auto-upload="false"
             :before-upload="beforeUpload" :on-change="onChange" :on-success="onSuccess" ref="ref">
    <img v-if="preview || modelValue" :src="preview || resolveImg(modelValue)" class="avatar" />
    <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
  </el-upload>
</template>
<script setup>
const props = defineProps({ modelValue: String })
const emit  = defineEmits(['update:modelValue'])
const uploadUrl = `${import.meta.env.VITE_STATIC_BASE_URL}upload`   // 不再硬编码
// beforeUpload / onChange / onSuccess 统一在此实现，出错统一 ElMessage
</script>
```

---

## 7. 验收清单（改造完成后自检）

- [ ] `mvn clean package` / 两个前端 `npm run build` 均零报错
- [ ] 浏览器控制台无 error、无 Vue warning（当前有 `<template #>`、`v-else` 带值等告警）
- [ ] 未登录状态下直接访问任意后端写接口，返回 401
- [ ] 普通用户无法修改 `comPermiss` / `petCoin` / 他人资料
- [ ] 捐助流程中途打断（手动断开连接）后，数据库无"半成品"数据
- [ ] `GET /pet/{id}` 的 SQL 条数与评论数**无关**
- [ ] 请求 `/user/1/id%3D1/xxx` 之类非法列名被拒绝且不影响数据库
- [ ] 上传 `.jsp` / `.html` / 伪造后缀文件被拒绝
- [ ] 换一个后端地址部署，前端只需改 `.env`，无需搜索替换代码
- [ ] 全项目搜索 `console.log` / `System.out.println` / `http://localhost` / 不当注释，结果为空

---

## 8. 结语

这个项目的**业务设计是完整且有想法的**：双端分离、双角色权限、积分体系、领养审核状态机、捐助分配、帖子/评论两级树、动态日记 —— 这些足够支撑一篇本科毕设，甚至比多数同题作品更完整。

真正拖后腿的不是功能，而是三件事：

1. **业务原子性被放在了前端**（P0-4/P0-5）—— 这是最需要优先解决、也最容易被答辩老师问到的问题；
2. **后端完全信任调用方**（P0-1/P0-2/P0-3/P0-9）—— 安全章节几乎是空白；
3. **数据装配靠循环查库**（P0-6）—— 性能优化没有抓手。

按第 5 节的五个阶段推进，前两阶段（约 3~5 天）就能让项目从"能演示"变成"经得起追问"，后三阶段则是把代码从"作业"提升到"工程"的差距。
