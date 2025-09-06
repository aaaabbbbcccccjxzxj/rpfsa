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
- Java 8+

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
- [x] 宠物浏览和搜索
- [x] 宠物详情查看
- [x] 领养申请提交
- [x] 社区发帖交流
- [x] 个人中心管理
- [x] 捐赠功能
- [x] 公告查看

### 管理端功能
- [x] 宠物信息管理
- [x] 用户信息管理
- [x] 领养申请审核
- [x] 社区内容管理
- [x] 公告发布
- [x] 数据统计查看
- [x] 捐赠记录管理

**开发团队：** [您的姓名/团队名称]
**联系方式：** [邮箱/电话]
**项目地址：** [GitHub/Gitee链接]

## 📄 许可证

本项目采用 [MIT License](LICENSE) 许可证。

