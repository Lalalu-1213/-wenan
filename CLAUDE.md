# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

智能文案生成助手 —— 基于大模型API的商品文案智能生成系统。后端 Java 17 + Spring Boot 3.2，前端 Vue 3 + Vite + Element Plus。

## 前置条件

- **Java 17+** + Maven 3.6+
- **Node.js 18+**
- **MySQL 8.0+** — 必须运行在 `localhost:3306`，数据库名 `copywriting_db`，用户名/密码 `root`/`111111`
  - 启动时 JPA `ddl-auto: update` 会自动建表，无需手动执行 SQL（URL 中 `createDatabaseIfNotExist=true` 会自动创建数据库）
- **大模型 API Key** — 当前配置调用 DeepSeek API（`api.deepseek.com`），可通过 `application.yml` 切换为其他 OpenAI 兼容接口

## 常用命令

### 后端 (back/)

```bash
# 一键启动前后端（推荐，需要先启动 MySQL）
start.bat

# 仅启动后端
cd back && mvn spring-boot:run

# 编译打包（生成 target/copywriting-assistant-1.0.0.jar）
cd back && mvn clean package

# 运行全部测试
cd back && mvn test

# 运行单个测试类
cd back && mvn test -Dtest=CopywritingServiceTest

```

### 环境配置

```bash
# 首次使用，配置环境向导
cd back && setup.bat
```

### 前端 (front/)

```bash
```

### 前端 (front/)

```bash
cd front && npm install    # 安装依赖
cd front &&     # 开发服务器（默认 localhost:5173）
cd front && npm run build  # 生产构建（输出到 dist/）
```

Vite 已配置代理：开发模式下 `/api` 前缀的请求自动转发到 `http://localhost:8080`，后端服务必须先启动。

## 架构

### 后端分层结构 (back/src/main/java/com/copywriting/assistant/)

- **controller/** — REST API 入口，`CopywritingController` 提供单条生成、批量生成、历史查询、健康检查
- **service/** — 业务逻辑。`CopywritingService` 接口 + `CopywritingServiceImpl` 实现；`OpenAiService` 负责调用大模型 API
- **cache/** — `CopywritingCache`，基于 ConcurrentHashMap 的内存缓存（带 TTL 过期），非 Redis
- **repository/** — Spring Data JPA 的 `CopywritingHistoryRepository`
- **model/dto/** — 请求/响应 DTO（`ProductRequest`、`CopywritingGenerateRequest`、`BatchGenerateRequest`、`CopywritingResponse`、`ApiResponse`）
- **model/dto/openai/** — OpenAI 兼容 API 的请求/响应模型（`ChatCompletionRequest`、`ChatCompletionResponse`）
- **model/entity/** — JPA 实体 `CopywritingHistory`（持久化到 MySQL）+ 非持久化实体 `BatchTask`（仅存在于内存 ConcurrentHashMap）
- **model/enums/** — `CopywritingStyle`（xiaohongshu/taobao/wechat/title 四种风格）、`TaskStatus`（PENDING/PROCESSING/COMPLETED/FAILED）
- **config/** — `OpenAiConfig`（@ConfigurationProperties(prefix="openai")）、`AsyncConfig`（线程池配置，核心线程10、最大50、队列200）
- **util/** — `PromptTemplate`（系统/用户提示词构建）、`CacheKeyGenerator`
- **exception/** — `BusinessException`（自定义业务异常，code+message）+ `GlobalExceptionHandler`（@RestControllerAdvice 全局异常处理）

### API 端点一览

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/copywriting/generate` | 单条文案生成 |
| POST | `/api/copywriting/batch/generate` | 批量生成（提交后异步执行） |
| GET | `/api/copywriting/batch/{taskId}` | 查询批量任务进度 |
| GET | `/api/copywriting/history` | 查询生成历史（分页） |
| GET | `/api/copywriting/health` | 健康检查 |

所有响应统一封装为 `ApiResponse<T>`，格式：`{ code: 200, message: "success", data: ... }`。前端 axios 拦截器在 `code !== 200` 时自动弹窗报错。

### 关键流程

1. **单条生成**：Controller → CopywritingServiceImpl.generateCopywriting() → 查缓存（`CacheKeyGenerator` 基于商品名+风格+额外要求 MD5 生成 key） → 构建 prompt（`PromptTemplate` 根据风格拼接不同的系统提示词） → OpenAiService.chatCompletion() → 写缓存 → 存历史到 MySQL
2. **批量生成**：提交后返回 taskId，`@Async("asyncTaskExecutor")` 异步执行，任务状态存内存 ConcurrentHashMap（`BatchTask` 实体不入库），通过轮询 GET /batch/{taskId} 查询进度。`BatchTask.isFinished()` 基于 completedCount + failedCount >= totalCount 判定完成
3. **大模型调用**：OpenAiService 使用 Apache HttpClient 5 调用 OpenAI 兼容 API（/v1/chat/completions），请求体为 JSON（model、messages、max_tokens、temperature），当前配置为 DeepSeek

### 配置

- `back/src/main/resources/application.yml` — 主配置（端口 8080、MySQL 连接、JPA ddl-auto: update、异步线程池核心10最大50队列200、大模型 API endpoint/model/max-tokens/temperature、缓存 TTL 24小时最大1000条、批量最大100条）
- 所有大模型 API 配置在 `openai.*` 命名空间下，通过 `OpenAiConfig` 绑定
- 缓存 TTL、批量限制等运行参数可通过 `copywriting.cache.ttl` 和 `copywriting.batch.max-count` 调整

### 前端 (front/src/)

- `api/copywriting.js` — 后端 API 调用封装（generateCopywriting、generateBatch、getBatchTask、getHistory、healthCheck）
- `components/` — SingleGenerate（单条生成表单）、BatchGenerate（批量生成，支持 JSON 数组输入）、HistoryList（历史记录分页表格）、GeneratingOverlay（批量生成进度遮罩层）
- `views/Home.vue` — 主页面，Tab 切换三个面板
- `utils/request.js` — axios 实例，baseURL `/api`，超时60s，响应拦截器统一处理 `code !== 200`
