# MovieMind Agent Guide

## 项目总览
MovieMind 是一个前后端分离的电影平台项目。

- 后端：Spring Boot 4、MyBatis Plus、PostgreSQL、Springdoc OpenAPI
- 前端：Vue 3、Vite
- 数据预处理：`pre/` 下的 Python 脚本负责数据清洗、导入、海报抓取和向量写入
- 推荐能力：当前是单轮电影推荐接口，不是完整多轮 Agent 系统

当前仓库里，前后端都已经进入可联调状态：

- 前端已完成中文化主站、登录注册、首页、发现、详情、收藏、个人中心
- 前端已接入 AI 推荐页，形态为单轮推荐输入 + 结果展示
- 前端已接入管理员登录、电影管理、评论管理
- 首页精选已采用独立排序逻辑；发现页支持热门 / 高分 / 最新三种排序
- 后端仍然是主要业务承载面，很多联调问题优先从接口、DTO、Mapper 和数据库结构核对

## 仓库结构

### `backend/`
核心业务代码目录。

- `controller/`：HTTP 接口
- `service/`、`service/impl/`：业务逻辑
- `mapper/`、`resources/mapper/`：MyBatis 接口与 SQL
- `entity/`、`dto/`：实体与接口类型
- `config/`：鉴权、异常、静态资源映射等

### `frontend/`
Vue 3 + Vite 前端工程，已完成核心功能对接。

- 已接入：认证、首页精选、发现页排序浏览、详情、收藏、评论、个人信息、头像上传、AI 推荐
- 已接入：管理员登录、电影管理、评论管理
- 当前仍在持续打磨：异常提示、移动端体验、局部交互细节、展示文案

### `pre/`
数据准备脚本目录。

- `import_movies_to_pg.py`：导入 `movies`
- `embed_movies_to_pg.py`：生成并写入 `movie_embeddings`

### `uploads/`
本地上传文件目录，对外映射为 `/uploads/**`。

### 设计稿与论文材料
`数据库表设计.md`、`功能模块设想.md`、论文目录都只能作为背景材料，不能替代源码判断现状。

## 本地运行

### 后端
- Java 17
- Maven Wrapper：`backend/mvnw.cmd`
- 默认端口：`19999`
- 数据库：`moviemind`
- JDBC：以 `backend/src/main/resources/application.properties` 为准
- 用户名：`moviemind`
- 密码：`moviemind_pwd`

启动：

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

### 前端
- Node.js + npm
- Vite

启动：

```powershell
cd frontend
npm install
npm run dev
```

### 推荐接口依赖
需要环境变量 `DASHSCOPE_API_KEY`。

- 已配置：可用通义模型做意图识别、生成回答、embedding 检索
- 未配置：退化为关键词规则 + 简单文本拼接

## 当前后端能力

### 认证
- `/api/auth/register`
- `/api/auth/login`
- `/api/auth/me`
- `/api/auth/logout`

### 用户
- `/api/users/me`
- `/api/users/me/avatar`

### 电影
- `/api/movies`
- `/api/movies/page`
- `/api/movies/detail`

说明：
- 首页精选走 `/api/movies/page?scene=featured`
- 发现页支持 `/api/movies/page?sort=popular|rating|latest`
- `scene=featured` 当前使用“最低票数过滤 + 加权评分”的精选排序
- `sort=rating` 不是裸评分排序，而是加权评分排序，用于降低小样本高分失真

### 评论
- `/api/reviews`
- `/api/reviews/page`
- `/api/reviews/detail`
- `/api/reviews/update`
- `/api/reviews/delete`
- `/api/reviews/movie`
- `/api/reviews/movie/page`

说明：
- `/api/reviews` 与 `/api/reviews/page` 是“我的评论”
- `/api/reviews/movie` 与 `/api/reviews/movie/page` 是电影公共评论
- `ReviewResponse` 现在除了评论本身，也可能携带 `movieTitle`、`moviePosterFile`、`username`、`avatarUrl`、时间字段，前端“我的评论”和电影详情页都会消费这些信息

### 收藏
- `/api/favorites`
- `/api/favorites/page`
- `/api/favorites/detail`
- `/api/favorites/delete`
- `/api/favorites/status`

说明：
- 当前前端主用 `/api/favorites/status`、`/api/favorites/page`、`/api/favorites/delete`
- 数据库里的 `favorites.type` 是 `integer not null`，没有默认值；排查收藏问题时必须先核对真实表结构，不能只看实体类推断

### 推荐
- `/api/agent/recommend`

### 管理员
- `/api/admin/movies`
- `/api/admin/movies/page`
- `/api/admin/movies/{id}`
- `/api/admin/movies/upload-poster`
- `/api/admin/reviews/page`
- `/api/admin/reviews/{id}`

## 鉴权边界
默认拦截 `/api/**`，以下路径除外：

- `/api/auth/**`
- `/uploads/**`
- `/swagger-ui/**`
- `/api-docs/**`
- `/error`

也就是说，除登录注册、文档和静态资源外，大部分业务接口都要求先建立 session。

额外说明：
- `/api/admin/**` 除要求已登录外，还要求管理员角色
- 前端虽然存在单独管理员登录页，但底层仍然是同一套 session 认证体系

## 关键公开类型

```text
AgentRecommendRequest {
  question: String
  limit: Integer
}

AgentRecommendResponse {
  answer: String
  movies: List<MovieListResponse>
}

AuthSessionResponse {
  loggedIn: Boolean
  userId: Long
  username: String
  role: Integer
}

CurrentUserResponse {
  userId: Long
  username: String
  role: Integer
  avatarUrl: String
  bio: String
}

FavoriteStatusResponse {
  movieId: Long
  favorited: Boolean
  favoriteId: Long | null
}

ReviewResponse {
  id: Long
  movieId: Long
  movieTitle: String
  moviePosterFile: String
  userId: Long
  username: String
  avatarUrl: String
  score: BigDecimal
  content: String
  createdAt: OffsetDateTime
  updatedAt: OffsetDateTime
}
```

## 推荐链路
`POST /api/agent/recommend`

处理流程：
1. 先解析用户问题意图，产出 `strategy`、`sort`、`limit`
2. 结构化问题直接按数据库排序，语义问题走 embedding + `movie_embeddings`
3. 最后用通义模型生成中文推荐语，失败时回退为简单文本拼接

当前模型：
- `qwen-plus`
- `text-embedding-v4`

当前边界：
- 没有多轮会话记忆
- 没有用户画像
- 前端已有单轮推荐页，但不是多轮聊天产品
- 本质上仍是“检索增强推荐接口”

## 首页与发现页排序

### 首页精选
- 前端通过 `scene=featured` 请求精选列表
- 后端当前使用“最低票数过滤 + 加权评分”排序
- 目标是避免少量评分样本直接排到首页最前面

### 发现页
- 默认排序：`popular`
- 可切换排序：`popular`、`rating`、`latest`
- `popular` 以评价人数为主，结合评分与上映时间
- `rating` 使用加权评分，不是直接按平均分倒序
- `latest` 按上映时间倒序，辅以热度和评分作为次级排序

## 数据来源
- 电影主数据来自 `pre/` 下 CSV，通过 `import_movies_to_pg.py` 导入 `movies`
- 向量数据通过 `embed_movies_to_pg.py` 写入 `movie_embeddings`
- 当前推荐接口实际使用 `content_type = 3` 的 `combined` 向量

如果推荐结果异常，优先检查：
- `movies` 是否有数据
- `movie_embeddings` 是否有 `content_type = 3`
- PostgreSQL 是否已安装 `pgvector`

## 协作注意事项
- 前端已经不是模板状态，判断“功能是否已完成”时要先分别确认前端页面、请求层、后端接口和数据库约束
- 设计稿比代码超前，判断现状时以源码为准
- 当前身份机制是 session，不是 JWT
- 中文异常很多时候是终端显示问题，不一定是文件本身坏了
- 当前仓库已经有用户端和管理端两套主要页面流，判断功能现状时不要只看用户端
- 只要问题涉及数据库字段、约束、默认值，先查 PostgreSQL 真实表结构再改代码，尤其是 `favorites`、`reviews` 这类已经发生过结构漂移的表
- 如果问题涉及推荐排序、首页展示或发现页结果，优先确认是首页精选逻辑还是发现页排序逻辑，不要混为一谈

## 建议阅读顺序
1. 先看 `backend/src/main/resources/application.properties`
2. 再看 `backend/src/main/java/com/lzz/backend/controller/`
3. 然后看 `backend/src/main/resources/mapper/MovieMapper.xml`
4. 再看 `backend/src/main/java/com/lzz/backend/service/impl/AgentServiceImpl.java`
5. 再看 `frontend/src/stores/movies.js` 和 `frontend/src/views/`
6. 最后看 `pre/import_movies_to_pg.py` 和 `pre/embed_movies_to_pg.py`
