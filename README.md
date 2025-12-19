# 美食厨房 - 做菜分享平台

一个基于 Vue 3 + Spring Boot 的美食分享平台，支持用户发布做菜笔记、收藏菜品、搜索食材、智能推荐等功能。

## 项目简介

这是一个人机交互（HCI）课程项目，旨在提供一个功能丰富、交互友好的美食分享平台。用户可以浏览菜品、发布做菜笔记、收藏喜欢的菜品、搜索食材，并通过智能推荐功能获得做菜灵感。

## 技术栈

### 前端
- **Vue 3** - 渐进式 JavaScript 框架
- **TypeScript** - 类型安全的 JavaScript
- **Vite** - 快速的前端构建工具
- **Vue Router** - 官方路由管理器
- **Pinia** - 状态管理库
- **Element Plus** - Vue 3 组件库
- **Axios** - HTTP 客户端

### 后端
- **Spring Boot 3.2.0** - Java 应用框架
- **Spring Security** - 安全框架
- **Spring Data JPA** - 数据持久化
- **JWT** - JSON Web Token 认证
- **H2 Database** - 嵌入式数据库（开发环境）
- **Lombok** - 简化 Java 代码

## 功能特性

### 1. 用户认证系统
- ✅ 用户注册
- ✅ 用户登录
- ✅ JWT Token 认证
- ✅ 密码加密存储
- ✅ 路由守卫保护

### 2. 菜品浏览与搜索
- ✅ 首页展示系统菜品和用户公开笔记
- ✅ 菜品详情页（食材、制作步骤、图片）
- ✅ 关键词搜索（支持按菜品名称和食材搜索）
- ✅ 响应式布局，适配不同屏幕

### 3. 菜品收藏功能
- ✅ 收藏/取消收藏菜品
- ✅ 个人中心查看收藏列表
- ✅ 收藏状态实时更新

### 4. 用户做菜笔记
- ✅ 发布做菜笔记（菜名、描述、图片、用料、做法、标签）
- ✅ 图片上传功能
- ✅ 快速选择常用食材和标签
- ✅ 公开/私密设置
- ✅ 公开笔记在首页展示
- ✅ 私密笔记仅自己可见
- ✅ 笔记可见性切换（公开↔私密）
- ✅ 笔记删除功能
- ✅ 笔记详情页

### 5. 发现功能
- ✅ **根据菜品买菜**：输入菜品名称，生成分类购物清单
  - 自动分类为主要食材和基础调料
  - 支持勾选已有食材
  - 一键复制待购买清单
- ✅ **根据食材找菜品**：输入已有食材，智能推荐可做的菜品
  - 显示匹配的食材数量
  - 显示还需准备的食材（分类显示）
  - 按匹配度排序推荐

### 6. 评论系统
- ✅ 菜品评论功能（公开可见）
- ✅ 回复评论（二级评论）
- ✅ 评论删除（仅自己的评论）
- ✅ 评论时间友好显示
- ✅ 评论列表展示

### 7. 个人中心
- ✅ Tab 切换查看收藏和笔记
- ✅ 用户信息展示
- ✅ 收藏管理
- ✅ 笔记管理（切换可见性、删除）
- ✅ 快速导航（下拉菜单）

## 项目结构

```
HCI/
├── backend/                    # Spring Boot 后端
│   ├── src/main/java/com/cooking/
│   │   ├── config/            # 配置类（安全、CORS、数据初始化）
│   │   ├── controller/        # REST 控制器
│   │   ├── dto/               # 数据传输对象
│   │   ├── entity/             # 实体类（User, Dish, Note, Comment）
│   │   ├── repository/        # 数据访问层
│   │   ├── security/          # 安全配置（JWT）
│   │   └── service/            # 业务逻辑层
│   ├── src/main/resources/
│   │   ├── static/images/     # 系统内置菜品图片
│   │   └── application.yml    # 应用配置
│   └── uploads/               # 用户上传图片目录
│
└── src/                        # Vue 前端
    ├── api/                    # API 请求配置
    ├── components/             # 组件
    │   ├── Layout.vue         # 布局组件
    │   └── CommentItem.vue    # 评论组件
    ├── stores/                 # Pinia 状态管理
    │   ├── user.ts            # 用户状态
    │   ├── dishes.ts          # 菜品状态
    │   ├── notes.ts            # 笔记状态
    │   ├── comments.ts         # 评论状态
    │   └── search.ts           # 搜索状态
    ├── views/                  # 页面组件
    │   ├── Home.vue           # 首页
    │   ├── Login.vue          # 登录页
    │   ├── Register.vue       # 注册页
    │   ├── DishDetail.vue     # 菜品详情页
    │   ├── Profile.vue        # 个人中心
    │   ├── CreateNote.vue     # 发布笔记页
    │   ├── NoteDetail.vue     # 笔记详情页
    │   └── Discover.vue       # 发现页
    ├── router/                 # 路由配置
    └── utils/                  # 工具函数
```

## 快速开始

### 环境要求
- Node.js >= 20.19.0 或 >= 22.12.0
- Java 17+
- Maven 3.6+

### 前端启动

```bash
# 安装依赖
npm install

# 启动开发服务器
npm run dev

# 访问 http://localhost:5173
```

### 后端启动

```bash
# 进入后端目录
cd backend

# 使用 Maven 启动（或使用 IDE）
mvn spring-boot:run

# 后端服务运行在 http://localhost:8080/api
```

### 数据库配置

项目使用 H2 文件数据库，数据保存在 `backend/data/cookingdb.mv.db`。

首次启动会自动创建数据库并初始化示例数据。

## API 接口

### 认证相关
- `POST /api/auth/register` - 用户注册
- `POST /api/auth/login` - 用户登录

### 菜品相关
- `GET /api/dishes` - 获取菜品列表（支持关键词搜索）
- `GET /api/dishes/{id}` - 获取菜品详情
- `POST /api/dishes/{id}/favorite` - 收藏/取消收藏菜品
- `GET /api/dishes/favorites` - 获取收藏列表

### 笔记相关
- `POST /api/notes` - 创建笔记（需登录）
- `GET /api/notes/my` - 获取我的笔记（需登录）
- `GET /api/notes/public` - 获取公开笔记
- `GET /api/notes/{id}` - 获取笔记详情
- `PUT /api/notes/{id}/visibility` - 更新笔记可见性（需登录）
- `DELETE /api/notes/{id}` - 删除笔记（需登录）

### 评论相关
- `POST /api/dishes/{dishId}/comments` - 发表评论（需登录）
- `GET /api/dishes/{dishId}/comments` - 获取评论列表
- `DELETE /api/dishes/{dishId}/comments/{commentId}` - 删除评论（需登录）

### 搜索相关
- `GET /api/search?keyword=xxx` - 搜索菜品和公开笔记

### 文件上传
- `POST /api/upload/image` - 上传图片（需登录）

## 主要功能说明

### 1. 用户认证
- 使用 JWT Token 进行身份认证
- Token 存储在 localStorage
- 自动刷新和过期处理

### 2. 图片管理
- 系统内置图片：`backend/src/main/resources/static/images/`
- 用户上传图片：`backend/uploads/`
- 图片通过 `/api/images/**` 和 `/api/uploads/**` 访问

### 3. 数据持久化
- 使用 H2 文件数据库，数据持久化保存
- 数据库文件：`backend/data/cookingdb.mv.db`
- 支持数据迁移和备份

### 4. 搜索功能
- 支持按菜品名称搜索
- 支持按食材搜索
- 搜索结果包含系统菜品和用户公开笔记

### 5. 发现功能
- **根据菜品买菜**：智能分析菜品所需食材，分类展示
- **根据食材找菜品**：根据已有食材推荐可做的菜品，显示缺少的食材

## 开发说明

### 添加新菜品
在 `backend/src/main/java/com/cooking/config/DataInitializer.java` 中添加菜品数据，并将图片放入 `backend/src/main/resources/static/images/` 目录。

### 修改配置
- 前端 API 地址：`src/api/index.ts`
- 后端配置：`backend/src/main/resources/application.yml`
- CORS 配置：`backend/src/main/java/com/cooking/config/WebConfig.java`

## 注意事项

1. 首次启动后端会自动初始化示例数据
2. 用户上传的图片保存在 `backend/uploads/` 目录
3. 数据库文件会自动创建，无需手动配置
4. 生产环境建议使用 MySQL 等专业数据库

## 许可证

本项目为人机交互课程项目。
