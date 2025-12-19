# 美食厨房 - 做菜相关的Web项目

这是一个基于Vue 3 + SpringBoot的美食分享平台，提供菜品展示、搜索、收藏等功能。

## 技术栈

### 前端
- Vue 3 (Composition API)
- TypeScript
- Vue Router 4
- Pinia (状态管理)
- Element Plus (UI组件库)
- Axios (HTTP客户端)
- Vite (构建工具)

### 后端
- Spring Boot 3.2.0
- Spring Security (JWT认证)
- Spring Data JPA
- H2 Database (开发环境)
- MySQL (生产环境)

## 功能特性

1. **用户认证**
   - 用户注册
   - 用户登录
   - JWT Token认证

2. **菜品展示**
   - 首页展示菜品列表
   - 菜品详情页（食材、制作步骤等）
   - 响应式布局

3. **搜索功能**
   - 支持按菜品名称搜索
   - 支持按食材搜索
   - 实时搜索

4. **收藏功能**
   - 收藏/取消收藏菜品
   - 个人中心查看收藏列表

5. **个人中心**
   - 查看用户信息
   - 管理收藏的菜品

## 项目结构

```
HCI/
├── src/                    # 前端源码
│   ├── api/               # API服务
│   ├── assets/            # 静态资源
│   ├── components/        # 组件
│   ├── router/             # 路由配置
│   ├── stores/             # Pinia状态管理
│   └── views/              # 页面组件
├── backend/                # 后端源码
│   └── src/main/java/com/cooking/
│       ├── config/         # 配置类
│       ├── controller/     # 控制器
│       ├── dto/            # 数据传输对象
│       ├── entity/         # 实体类
│       ├── repository/     # 数据访问层
│       ├── security/        # 安全配置
│       └── service/         # 业务逻辑层
└── README.md
```

## 快速开始

### 环境要求

- Node.js >= 20.19.0 或 >= 22.12.0
- Java 17+
- Maven 3.6+

### 前端启动

1. 安装依赖
```bash
npm install
```

2. 启动开发服务器
```bash
npm run dev
```

前端将在 `http://localhost:5173` 启动

### 后端启动

1. 进入后端目录
```bash
cd backend
```

2. 使用Maven构建项目
```bash
mvn clean install
```

3. 运行Spring Boot应用
```bash
mvn spring-boot:run
```

或者使用IDE直接运行 `CookingApplication.java`

后端将在 `http://localhost:8080` 启动，API基础路径为 `/api`

### 配置说明

前端API地址配置在 `src/api/index.ts` 中，默认指向 `http://localhost:8080/api`

如需修改，可以创建 `.env` 文件：
```
VITE_API_BASE_URL=http://localhost:8080/api
```

## API接口

### 认证接口
- `POST /api/auth/register` - 用户注册
- `POST /api/auth/login` - 用户登录

### 菜品接口
- `GET /api/dishes` - 获取菜品列表（支持keyword查询参数）
- `GET /api/dishes/{id}` - 获取菜品详情
- `POST /api/dishes/{id}/favorite` - 收藏/取消收藏菜品

### 用户接口
- `GET /api/user/info` - 获取当前用户信息
- `GET /api/user/favorites` - 获取收藏列表

## 默认数据

系统启动时会自动初始化6道菜品数据：
- 宫保鸡丁
- 番茄鸡蛋
- 红烧肉
- 麻婆豆腐
- 糖醋里脊
- 清蒸鲈鱼

## 开发说明

### 前端开发
- 使用Vue 3 Composition API
- 使用TypeScript进行类型检查
- 使用Element Plus组件库
- 使用Pinia进行状态管理

### 后端开发
- 使用Spring Boot 3.2.0
- 使用JWT进行身份认证
- 使用H2内存数据库（开发环境）
- 使用Spring Data JPA进行数据访问

## 注意事项

1. 后端默认使用H2内存数据库，重启后数据会丢失
2. 生产环境建议使用MySQL数据库
3. JWT密钥在生产环境需要修改
4. 前端和后端需要同时运行才能正常使用

## 许可证

MIT License

