# RPFSA 宠物救助平台

## 📖 项目简介

RPFSA（Rescue Pet For Society Association）是一个宠物救助平台，旨在为流浪动物提供救助、领养和关爱服务。平台包含用户端和管理端两个部分，为宠物救助工作提供完整的数字化解决方案。

## 🎯 项目特色

- 🐕 **宠物展示** - 展示待领养的宠物信息
- 💝 **爱心捐赠** - 支持宠物救助的捐赠功能
- 📝 **社区交流** - 用户分享宠物故事和经验
- 📋 **领养申请** - 在线申请领养心仪宠物
- 🏠 **用户中心** - 个人资料和申请记录管理
- ⚙️ **管理后台** - 管理员统一管理平台内容

## 🖼️ 项目截图

### 用户端界面

#### 首页展示
![首页](rpfsa_front/src/assets/lbt/welcome.png)
*平台首页，展示最新宠物信息和重要公告*

#### 宠物详情
![宠物详情](rpfsa_front/src/assets/lbt/cat.png)
*宠物详细信息页面，包含照片、描述和领养条件*

#### 社区交流
![社区](rpfsa_front/src/assets/lbt/dog1.png)
*用户社区，分享宠物故事和救助经验*

### 管理端界面

#### 管理后台首页
![管理首页](rpfsa_manage/public/logo.png)
*管理员工作台，查看平台运营数据*

#### 宠物管理
*宠物信息录入、编辑和状态管理*

#### 用户管理
*用户信息查看和权限管理*

## 🛠️ 技术栈

### 前端技术
- **Vue.js 3** - 渐进式JavaScript框架
- **Vue Router** - 官方路由管理器
- **Vite** - 现代化构建工具
- **Element Plus** - Vue 3组件库

### 后端技术
- **Spring Boot** - Java企业级应用框架
- **MySQL** - 关系型数据库
- **MyBatis** - 持久层框架

### 开发工具
- **VS Code** - 前端开发环境
- **IntelliJ IDEA** - 后端开发环境
- **Git** - 版本控制

## 📁 项目结构

```
rpfsa/
├── rpfsa_front/          # 用户端前端项目
│   ├── src/
│   │   ├── components/   # Vue组件
│   │   │   ├── home/     # 首页相关组件
│   │   │   └── homechild/ # 子页面组件
│   │   ├── assets/       # 静态资源
│   │   └── router/       # 路由配置
│   └── package.json
├── rpfsa_manage/         # 管理端前端项目
│   ├── src/
│   │   ├── components/   # Vue组件
│   │   │   ├── manage/   # 管理布局组件
│   │   │   └── work/     # 工作页面组件
│   │   └── utils/        # 工具函数
│   └── package.json
├── rpfsa.sql            # 数据库脚本
└── README.md            # 项目说明
```

## 🚀 快速开始

### 环境要求
- Node.js 16+
- MySQL 8.0+
- Java 11+（后端 `pom.xml` 中 `java.version=11`）

### 安装步骤

1. **克隆项目**
   ```bash
   git clone [项目地址]
   cd rpfsa
   ```

2. **安装前端依赖**
   ```bash
   # 用户端
   cd rpfsa_front
   npm install
   npm run dev
   
   # 管理端
   cd rpfsa_manage
   npm install
   npm run dev
   ```

3. **配置数据库**
   - 导入 `rpfsa.sql` 到MySQL数据库
   - 存量库升级：依次执行 `rpfsa_upgrade.sql`（安全改造）与 `rpfsa_upgrade_v2.sql`（功能扩展：收藏/相册/签到/充值/点赞/找回密码等新表）
   - 配置数据库连接信息

4. **启动后端服务**
   - 解压 `rpfsa.zip`
   - 在IDEA中导入项目
   - 配置数据库连接
   - 启动Spring Boot应用

### 默认账号

**管理员账号：**
- 用户名：`admin`
- 密码：`admin`

## 📋 功能模块

### 用户端功能
- [x] 宠物浏览和搜索（支持品种/来源/性别/年龄/疫苗/领养状态多条件组合筛选）
- [x] 宠物详情查看（多图相册轮播）
- [x] 宠物收藏 / 收藏列表
- [x] 领养申请提交（含领养协议确认、审核中可取消申请）
- [x] 社区发帖交流（帖子点赞，作者获宠宠币奖励）
- [x] 个人中心管理
- [x] 捐赠功能
- [x] 公告查看
- [x] 消息中心（系统消息 / 领养通知 / 捐助记录聚合，未读角标）
- [x] 宠宠币钱包（每日签到 +10、模拟充值、充值记录）
- [x] 找回密码（邮箱验证码，演示模式验证码直接回显）
- [x] 领养回访日记奖励（领养人发日记 +5 宠宠币）

### 管理端功能
- [x] 宠物信息管理（含相册多图管理）
- [x] 用户信息管理
- [x] 领养申请审核（结果自动站内通知申请人）
- [x] 社区内容管理（帖子 + 评论管理页）
- [x] 公告发布
- [x] 数据统计查看（总览统计 + 近 6 个月领养/用户/捐助趋势图）
- [x] 捐赠记录管理（分配完成自动站内通知；支持 CSV 导出）
- [x] 领养记录 CSV 导出
- [x] 充值记录查看

### 性能与工程
- 榜单接口（领养/救助/热门帖子/猫狗）只返回榜单所需列，并带 60s 进程内 TTL 缓存
- 关键写操作（签到/充值/点赞/收藏）均由数据库唯一约束 + 条件更新保证幂等与并发安全

**开发团队：** [您的姓名/团队名称]
**联系方式：** [邮箱/电话]
**项目地址：** [GitHub/Gitee链接]

## 🔧 工程说明

### 配置项

前后端均不再硬编码主机地址与凭据，统一由环境变量（后端）与 `.env`（前端）控制：

| 变量 | 说明 | 默认值 |
| --- | --- | --- |
| `VITE_API_BASE_URL` | 前端请求前缀 | 开发 `/api`（vite proxy 转发）；生产 `/` |
| `VITE_PROXY_TARGET` | 开发期后端地址 | `http://localhost:80` |
| `RPFSA_PORT` | 后端端口 | `80` |
| `RPFSA_DB_HOST` / `RPFSA_DB_PORT` / `RPFSA_DB_NAME` | 数据库地址 | `localhost` / `3306` / `rpfsa` |
| `RPFSA_DB_USERNAME` / `RPFSA_DB_PASSWORD` | 数据库凭据 | `root` / `135246` |
| `RPFSA_UPLOAD_DIR` | 图片上传目录 | `./upload` |
| `RPFSA_TOKEN_SECRET` | 登录令牌签名密钥，**生产环境必须覆盖** | 开发用默认值 |
| `RPFSA_CORS_ORIGINS` | 允许的跨域来源，留空即关闭跨域 | 空 |

> 变量统一加了 `RPFSA_` 前缀。Spring Boot 存在"宽松绑定"：环境变量 `SERVER_PORT`
> 会被直接映射到配置项 `server.port`，与变量名怎么写无关。
> 若机器上恰好存在 `SERVER_PORT`（某些工具链会注入），应用会监听到意料之外的端口，
> 此时用命令行参数覆盖即可：`mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=80`。

### 启动

后端默认 80 端口：`cd rpfsa && mvn spring-boot:run`
用户端默认 5173：`cd rpfsa_front && npm install && npm run dev`
管理端默认 5174：`cd rpfsa_manage && npm install && npm run dev`

开发期前端通过 vite proxy 把 `/api` 与 `/img` 转发到后端，因此不存在跨域问题，后端也无需开放 `@CrossOrigin`。

### 接口鉴权

- 登录成功后返回 `token`，前端存入 `sessionStorage`，由 `src/utils/request.js` 自动附加请求头 `Authorization: Bearer <token>`。
- **GET 接口默认放行**（游客可浏览宠物、帖子、公告）；其余写操作一律要求登录。
- 仅管理员可用的接口用 `@RequireAdmin` 标注并在服务端强制校验角色：用户管理、领养审核、救助品维护、后台统计等。
- 用户端与管理端的接口路径已分离，管理端的用户管理统一收敛到 `/admin/users/**`。
- 响应体不再包含密码字段（`User.password` 已标记 `@JsonIgnore`）。

### 数据库

- 全新部署：直接导入 `rpfsa.sql`（已含索引与密码列扩容）。
- 已有数据库：执行 `rpfsa_upgrade.sql`，**无需重置任何账号密码** —— 历史明文密码会在该账号首次登录成功时自动升级为 BCrypt 密文。

### 图片上传

上传文件保存至 `UPLOAD_DIR`（默认 `./upload`，相对于后端启动目录，即 `rpfsa/upload`），经 `/img/**` 对外访问；不再写入 classpath 或源码目录，打包为 jar 后依然可用。仅接受 jpg / jpeg / png / gif / webp / bmp，单文件不超过 2MB。若在仓库根目录启动 jar，可通过 `RPFSA_UPLOAD_DIR=./rpfsa/upload` 指向同一目录。

## 📄 许可证

本项目采用 [MIT License](LICENSE) 许可证。

