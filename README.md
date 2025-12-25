# 厨房助手 - 智能美食分享平台

一个基于 Vue 3 + Spring Boot 的智能美食分享平台，支持用户发布做菜笔记、收藏菜品、关注其他用户、私信交流、营养信息查询、智能购物清单等功能。

## 项目简介

这是一个人机交互（HCI）课程项目，旨在提供一个功能丰富、交互友好的智能美食分享平台。用户可以浏览菜品、发布做菜笔记、关注其他用户、查看关注动态、私信交流，并通过智能推荐功能获得做菜灵感。平台还提供营养信息查询、根据菜品生成购物清单、根据食材推荐菜品等实用功能。

## 技术栈

### 前端
- **Vue 3** - 渐进式 JavaScript 框架
- **TypeScript** - 类型安全的 JavaScript
- **Vite** - 快速的前端构建工具
- **Vue Router** - 官方路由管理器
- **Pinia** - 状态管理库
- **Element Plus** - Vue 3 组件库
- **Axios** - HTTP 客户端
- **html2canvas** - 将 HTML 元素转换为图片（用于分享卡片）

### 后端
- **Spring Boot 3.2.0** - Java 应用框架
- **Spring Security** - 安全框架
- **Spring Data JPA** - 数据持久化
- **JWT** - JSON Web Token 认证
- **H2 Database** - 嵌入式数据库（开发环境）
- **MySQL** - 生产环境数据库支持
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
- ✅ 菜品分类浏览
- ✅ 菜品详情页（食材、制作步骤、图片、营养信息）
- ✅ 关键词搜索（支持按菜品名称和食材搜索）
- ✅ 响应式布局，适配移动端和桌面端

### 3. 菜品收藏功能
- ✅ 收藏/取消收藏菜品
- ✅ 收藏/取消收藏笔记
- ✅ 个人中心查看收藏列表（菜谱和笔记）
- ✅ 收藏状态实时更新
- ✅ 收藏列表搜索功能

### 4. 用户做菜笔记
- ✅ 发布做菜笔记（菜名、描述、图片、用料、做法、标签）
- ✅ 图片上传功能
- ✅ 快速选择常用食材和标签
- ✅ 公开/私密设置
- ✅ 公开笔记在首页和笔记页展示
- ✅ 私密笔记仅自己可见
- ✅ 笔记可见性切换（公开↔私密）
- ✅ 笔记删除功能
- ✅ 笔记详情页
- ✅ **草稿箱功能**：保存未完成的笔记，支持继续编辑和发布
- ✅ **分享卡片功能**：生成精美的分享卡片图片，包含完整信息

### 5. 关注/粉丝系统
- ✅ 关注其他用户
- ✅ 取消关注
- ✅ 查看关注列表和粉丝列表
- ✅ 查看关注用户的动态（公开笔记）
- ✅ 在笔记列表、详情页等位置直接关注作者
- ✅ 用户个人页面展示（查看其他用户的公开笔记）
- ✅ 关注状态实时更新

### 6. 浏览历史记录
- ✅ 自动记录浏览的菜品和笔记
- ✅ 个人中心查看历史记录
- ✅ 区分菜品和笔记类型
- ✅ 清空历史记录功能
- ✅ 按时间倒序显示

### 7. 发现功能（智能推荐）

#### 7.1 根据菜品买菜
- ✅ 输入一个或多个菜品名称（支持逗号、换行、空格分隔）
- ✅ 智能识别菜品并提取所需食材
- ✅ 自动分类为主要食材和基础调料
- ✅ 按超市区域分类展示（蔬菜区、肉类区、调料区等）
- ✅ 支持点击标记已有食材（带动画效果）
- ✅ 一键复制待购买清单
- ✅ 生成精美购物卡片图片
- ✅ 从"根据食材找菜品"页面直接跳转并填充菜品名称

#### 7.2 根据食材找菜品
- ✅ 输入已有食材，智能推荐可做的菜品
- ✅ 显示匹配的食材数量和匹配率
- ✅ 显示还需准备的食材（分类显示：主要食材、基础调料）
- ✅ 按匹配度排序推荐
- ✅ 易腐食材提醒（如蔬菜、肉类等）
- ✅ 推荐菜品卡片显示完整信息（图片、名称、匹配率、缺少食材）
- ✅ 一键跳转到"根据菜品买菜"页面

#### 7.3 营养信息查询
- ✅ 输入一个或多个菜品名称
- ✅ 显示每个菜品的详细营养信息（卡路里、蛋白质、脂肪、碳水化合物、纤维）
- ✅ 自动计算多个菜品的营养总和
- ✅ 营养信息可视化展示
- ✅ 支持营养数据导出

### 8. 评论系统
- ✅ 菜品评论功能（公开可见）
- ✅ 笔记评论功能
- ✅ 回复评论（二级评论）
- ✅ 评论删除（仅自己的评论）
- ✅ 评论时间友好显示
- ✅ 评论列表展示

### 9. 私信功能
- ✅ 用户间私信交流
- ✅ 对话列表展示（显示最后一条消息和未读数量）
- ✅ 未读消息提示（导航栏红色徽章）
- ✅ 消息详情页（左右布局区分发送和接收）
- ✅ 查看对话对象主页
- ✅ 实时消息状态更新
- ✅ 消息时间显示

### 10. 个人中心
- ✅ 用户信息展示（头像、用户名、邮箱）
- ✅ 统计数据展示（关注数、粉丝数、收藏数、笔记数）
- ✅ Tab 切换查看：
  - 收藏的菜谱（支持搜索）
  - 收藏的笔记（支持搜索）
  - 我的笔记
  - 草稿箱
  - 历史记录
  - 关注用户动态
- ✅ 笔记管理（切换可见性、删除、编辑草稿）
- ✅ 快速导航（用户下拉菜单）

### 11. 用户个人页面
- ✅ 查看其他用户的个人主页
- ✅ 显示用户基本信息（头像、用户名、邮箱）
- ✅ 显示用户统计数据（关注数、粉丝数、笔记数）
- ✅ 查看该用户发布的公开笔记
- ✅ 在用户页面直接关注/取消关注
- ✅ 收藏该用户发布的笔记

### 12. 分享功能
- ✅ **菜品分享卡片**：生成包含菜品图片、名称、描述、用料、步骤的分享卡片
- ✅ **笔记分享卡片**：生成包含笔记图片、标题、描述、作者、标签、用料、步骤的分享卡片
- ✅ **购物清单卡片**：生成包含待购买食材的购物清单卡片
- ✅ 一键保存分享卡片为图片
- ✅ 精美的卡片设计，适合社交媒体分享

## 项目结构

```
HCI/
├── backend/                    # Spring Boot 后端
│   ├── src/main/java/com/cooking/
│   │   ├── config/            # 配置类（安全、CORS、数据初始化）
│   │   ├── controller/        # REST 控制器
│   │   │   ├── AuthController.java
│   │   │   ├── DishController.java
│   │   │   ├── NoteController.java
│   │   │   ├── CommentController.java
│   │   │   ├── NoteCommentController.java
│   │   │   ├── UserController.java
│   │   │   ├── FollowController.java
│   │   │   ├── HistoryController.java
│   │   │   ├── MessageController.java
│   │   │   ├── SearchController.java
│   │   │   └── FileUploadController.java
│   │   ├── dto/               # 数据传输对象
│   │   ├── entity/            # 实体类（User, Dish, Note, Comment, History, Message）
│   │   ├── repository/        # 数据访问层
│   │   ├── security/          # 安全配置（JWT）
│   │   ├── service/           # 业务逻辑层
│   │   │   ├── AuthService.java
│   │   │   ├── DishService.java
│   │   │   ├── NoteService.java
│   │   │   ├── CommentService.java
│   │   │   ├── FollowService.java
│   │   │   ├── HistoryService.java
│   │   │   ├── MessageService.java
│   │   │   └── SearchService.java
│   │   └── util/              # 工具类（JWT）
│   ├── src/main/resources/
│   │   ├── static/images/     # 系统内置菜品图片
│   │   └── application.yml    # 应用配置
│   ├── data/                  # H2 数据库文件
│   └── uploads/               # 用户上传图片目录
│
└── src/                        # Vue 前端
    ├── api/                    # API 请求配置
    ├── components/             # 组件
    │   ├── Layout.vue         # 布局组件（导航栏）
    │   └── CommentItem.vue    # 评论组件
    ├── stores/                 # Pinia 状态管理
    │   ├── user.ts            # 用户状态
    │   ├── dishes.ts          # 菜品状态
    │   ├── notes.ts           # 笔记状态
    │   ├── comments.ts        # 评论状态
    │   ├── search.ts          # 搜索状态
    │   └── messages.ts        # 私信状态
    ├── views/                  # 页面组件
    │   ├── Home.vue           # 首页
    │   ├── Login.vue          # 登录页
    │   ├── Register.vue       # 注册页
    │   ├── DishDetail.vue    # 菜品详情页
    │   ├── NoteDetail.vue     # 笔记详情页
    │   ├── Profile.vue        # 个人中心
    │   ├── UserProfile.vue    # 用户个人页面
    │   ├── CreateNote.vue    # 发布笔记页
    │   ├── Notes.vue          # 笔记列表页
    │   ├── Dishes.vue         # 菜品列表页
    │   ├── Categories.vue     # 分类页
    │   ├── CategoryDetail.vue # 分类详情页
    │   ├── Discover.vue       # 发现页（功能入口）
    │   ├── Messages.vue       # 私信页面
    │   ├── Settings.vue       # 设置页面
    │   └── discover/          # 发现功能详细页
    │       ├── ShopByDish.vue           # 根据菜品买菜
    │       ├── FindDishesByIngredients.vue  # 根据食材找菜品
    │       └── NutritionQuery.vue       # 营养信息查询
    ├── router/                 # 路由配置
    └── utils/                  # 工具函数
        └── image.ts           # 图片处理工具
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

首次启动会自动创建数据库并初始化示例数据（包括 20 道菜品及其营养信息）。

## API 接口

### 认证相关
- `POST /api/auth/register` - 用户注册
- `POST /api/auth/login` - 用户登录

### 菜品相关
- `GET /api/dishes` - 获取菜品列表（支持关键词搜索、分类筛选、分页）
- `GET /api/dishes/{id}` - 获取菜品详情
- `POST /api/dishes/{id}/favorite` - 收藏/取消收藏菜品
- `GET /api/user/favorites` - 获取收藏列表

### 笔记相关
- `POST /api/notes` - 创建笔记（需登录）
- `GET /api/notes/my` - 获取我的笔记（需登录）
- `GET /api/notes/public` - 获取公开笔记
- `GET /api/notes/{id}` - 获取笔记详情
- `PUT /api/notes/{id}/visibility` - 更新笔记可见性（需登录）
- `DELETE /api/notes/{id}` - 删除笔记（需登录）
- `POST /api/notes/{id}/favorite` - 收藏/取消收藏笔记（需登录）
- `GET /api/notes/following` - 获取关注用户的动态（需登录）

### 草稿相关
- `GET /api/notes/drafts` - 获取草稿列表（需登录）
- `POST /api/notes/draft` - 保存草稿（需登录）
- `PUT /api/notes/draft/{id}` - 更新草稿（需登录）

### 评论相关
- `POST /api/dishes/{dishId}/comments` - 发表菜品评论（需登录）
- `GET /api/dishes/{dishId}/comments` - 获取菜品评论列表
- `DELETE /api/dishes/{dishId}/comments/{commentId}` - 删除评论（需登录）
- `POST /api/notes/{noteId}/comments` - 发表笔记评论（需登录）
- `GET /api/notes/{noteId}/comments` - 获取笔记评论列表
- `DELETE /api/notes/{noteId}/comments/{commentId}` - 删除评论（需登录）

### 关注相关
- `POST /api/follow/{userId}` - 关注用户（需登录）
- `DELETE /api/follow/{userId}` - 取消关注（需登录）
- `GET /api/follow/following` - 获取我的关注列表（需登录）
- `GET /api/follow/followers` - 获取我的粉丝列表（需登录）
- `GET /api/follow/status/{userId}` - 检查关注状态（需登录）

### 用户相关
- `GET /api/user/info` - 获取当前用户信息（需登录）
- `GET /api/user/{userId}` - 获取指定用户信息
- `GET /api/user/{userId}/notes` - 获取指定用户的公开笔记
- `GET /api/user/favorite-notes` - 获取收藏的笔记（需登录）

### 历史记录相关
- `GET /api/history` - 获取浏览历史（需登录）
- `DELETE /api/history` - 清空浏览历史（需登录）

### 私信相关
- `GET /api/messages/conversations` - 获取对话列表（需登录）
- `GET /api/messages/{otherUserId}` - 获取与指定用户的对话消息（需登录）
- `POST /api/messages` - 发送私信（需登录）
- `PUT /api/messages/{messageId}/read` - 标记消息为已读（需登录）
- `GET /api/messages/unread-count` - 获取未读消息数量（需登录）

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
- 首次启动自动初始化 20 道菜品数据（包含营养信息）

### 4. 搜索功能
- 支持按菜品名称搜索
- 支持按食材搜索
- 搜索结果包含系统菜品和用户公开笔记

### 5. 发现功能详解

#### 根据菜品买菜
- 支持输入多个菜品（逗号、换行、空格分隔）
- 智能匹配菜品名称（支持模糊匹配）
- 自动提取并去重食材
- 按食材类型分类（主要食材、基础调料）
- 按超市区域分类（蔬菜区、肉类区、调料区等）
- 交互式标记已有食材
- 一键复制待购买清单
- 生成购物卡片图片

#### 根据食材找菜品
- 输入已有食材，智能匹配菜品
- 计算匹配率和缺少食材
- 按匹配度排序推荐
- 易腐食材提醒
- 一键跳转到购物清单功能

#### 营养信息查询
- 支持查询单个或多个菜品
- 显示详细营养信息（每 100g）
- 自动计算多菜品营养总和
- 营养数据可视化展示

### 6. 关注系统
- 用户可以关注其他用户
- 查看关注用户的公开笔记动态
- 在多个位置（笔记列表、详情页等）快速关注作者
- 查看关注列表和粉丝列表
- 访问其他用户的个人页面

### 7. 私信系统
- 用户间一对一私信交流
- 对话列表展示（最后消息预览、未读数量）
- 导航栏未读消息提示
- 消息详情页（左右布局区分发送和接收）
- 查看对话对象主页
- 消息已读状态管理

### 8. 草稿箱
- 保存未完成的笔记为草稿
- 草稿可以继续编辑
- 草稿可以发布为正式笔记
- 草稿仅自己可见

### 9. 浏览历史
- 自动记录浏览的菜品和笔记
- 5分钟内重复浏览同一内容会更新浏览时间
- 按时间倒序显示历史记录
- 支持清空历史记录

### 10. 分享功能
- 菜品和笔记支持生成分享卡片
- 分享卡片包含完整信息（图片、名称、描述、用料、步骤等）
- 使用 html2canvas 将 HTML 转换为图片
- 一键保存分享卡片为图片文件
- 精美的卡片设计，适合社交媒体分享

### 11. 响应式设计
- 适配桌面端、平板和移动端
- 移动端优化的导航和布局
- 响应式卡片和网格布局

## 开发说明

### 添加新菜品
在 `backend/src/main/java/com/cooking/config/DataInitializer.java` 中添加菜品数据，并将图片放入 `backend/src/main/resources/static/images/` 目录。

菜品数据包括：
- 基本信息：名称、描述、图片、分类、难度、烹饪时间、份数
- 食材列表：主要食材和基础调料
- 制作步骤
- 营养信息：卡路里、蛋白质、脂肪、碳水化合物、纤维（每 100g）

### 修改配置
- 前端 API 地址：`src/api/index.ts`
- 后端配置：`backend/src/main/resources/application.yml`
- CORS 配置：`backend/src/main/java/com/cooking/config/SecurityConfig.java`

### 数据库表结构
- `users` - 用户表
- `dishes` - 菜品表（包含营养信息字段）
- `notes` - 笔记表
- `comments` - 评论表
- `note_comments` - 笔记评论表
- `histories` - 历史记录表
- `messages` - 私信表
- `user_favorites` - 用户收藏菜品关联表
- `user_favorite_notes` - 用户收藏笔记关联表
- `user_follows` - 用户关注关联表

## 注意事项

1. 首次启动后端会自动初始化示例数据（20 道菜品及其营养信息）
2. 用户上传的图片保存在 `backend/uploads/` 目录
3. 数据库文件会自动创建，无需手动配置
4. 生产环境建议使用 MySQL 等专业数据库
5. 关注关系使用双向 `@ManyToMany` 关系，已优化避免循环引用问题
6. 营养信息数据在首次启动时自动初始化，如果数据库已有数据，会自动更新营养信息

## 功能演示

### 核心流程
1. **注册/登录** → 创建账户或登录
2. **浏览内容** → 查看菜品和笔记，自动记录浏览历史
3. **发布笔记** → 创建做菜笔记，支持保存草稿
4. **关注用户** → 关注感兴趣的用户，查看他们的动态
5. **私信交流** → 与美食达人私信互动
6. **收藏内容** → 收藏喜欢的菜品和笔记
7. **智能发现** → 使用发现功能获得做菜灵感
8. **查看个人中心** → 管理自己的内容、查看统计数据

### 特色功能
- 📝 **草稿箱**：随时保存，随时继续
- 👥 **关注系统**：发现更多美食达人
- 💬 **私信功能**：美食交流，互动更便捷
- 📊 **浏览历史**：快速找回看过的内容
- 🔍 **智能搜索**：按名称、食材搜索
- 🛒 **智能购物**：根据菜品生成购物清单
- 🍳 **智能推荐**：根据食材推荐菜品
- 📈 **营养查询**：查看菜品营养信息
- 🎨 **分享卡片**：精美设计，一键分享

## 更新日志

### v1.0.0
- ✅ 基础功能：用户认证、菜品浏览、笔记发布
- ✅ 收藏功能：收藏菜品和笔记
- ✅ 关注系统：关注用户、查看动态
- ✅ 评论系统：菜品和笔记评论
- ✅ 浏览历史：自动记录浏览记录
- ✅ 草稿箱：保存未完成的笔记
- ✅ 发现功能：根据菜品买菜、根据食材找菜品
- ✅ 营养信息查询：查看菜品营养数据
- ✅ 分享卡片：生成精美分享图片
- ✅ 私信功能：用户间私信交流

## 许可证

本项目为人机交互课程项目。
