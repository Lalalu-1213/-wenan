# 智能文案生成助手

基于大模型 API 的商品文案智能生成系统。输入商品信息，一键生成小红书、淘宝、朋友圈、标题优化等多种风格的营销文案，支持单条生成、批量生成和历史记录查询。

> 项目地址：https://github.com/Lalalu-1213/-wenan

---

## 功能特性

- 📝 **单条文案生成**：输入商品名称、描述、卖点、目标人群，选择风格即可生成文案。
- 📦 **批量文案生成**：一次性提交多个商品，后台异步处理，实时查看进度。
- 🎨 **多风格支持**：小红书 / 淘宝 / 朋友圈 / 标题优化。
- 💾 **生成历史**：自动保存生成结果到 MySQL，支持分页查询。
- ⚡ **结果缓存**：基于内存缓存，相同请求直接返回，降低 API 调用成本。
- 🔌 **模型可切换**：OpenAI 兼容接口，默认 DeepSeek，可替换为其他服务商。

---

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端 | Java 17 + Spring Boot 3.2 + Spring Data JPA |
| 前端 | Vue 3 + Vite + Element Plus |
| 数据库 | MySQL 8.0 |
| 缓存 | 本地内存缓存（ConcurrentHashMap + TTL） |
| 大模型 API | OpenAI 兼容接口，默认 DeepSeek |

---

## 项目结构

```
智能文案助手/
├── back/                          # 后端 Spring Boot 项目
│   ├── src/main/java/com/copywriting/assistant/
│   │   ├── controller/            # REST API 入口
│   │   ├── service/               # 业务逻辑
│   │   ├── cache/                 # 内存缓存
│   │   ├── repository/            # JPA 数据访问
│   │   ├── model/                 # DTO / Entity / Enum
│   │   ├── config/                # 配置类
│   │   ├── util/                  # 工具类
│   │   └── exception/             # 全局异常处理
│   └── src/main/resources/
│       └── application.yml        # 主配置文件
├── front/                         # 前端 Vue 3 项目
│   ├── src/
│   │   ├── api/                   # 后端接口封装
│   │   ├── components/            # 业务组件
│   │   ├── views/                 # 页面
│   │   └── utils/                 # axios 封装
│   └── package.json
├── start.bat                      # Windows 一键启动脚本
├── start.sh                       # Linux/Mac 一键启动脚本
└── README.md
```

---

## 环境要求

- **Java 17+**
- **Maven 3.6+**
- **Node.js 18+**
- **MySQL 8.0+**（必须运行在 `localhost:3306`，数据库名 `copywriting_db`，用户名/密码 `root`/`111111`）
- **大模型 API Key**（当前默认 DeepSeek，可自行替换）

---

## 快速开始

### 1. 克隆项目

```bash
git clone https://github.com/Lalalu-1213/-wenan.git
cd -wenan
```

### 2. 启动 MySQL

确保 MySQL 运行在 `localhost:3306`，并已创建数据库 `copywriting_db`。

项目配置了 `createDatabaseIfNotExist=true`，启动时会自动建库；JPA `ddl-auto: update` 会自动建表，无需手动执行 SQL。

### 3. 配置大模型 API Key

编辑 `back/src/main/resources/application.yml`：

```yaml
openai:
  api-key: sk-your-api-key-here
  base-url: https://api.deepseek.com
  model: deepseek-v4-flash
```

> ⚠️ **安全提示**：不要把真实的 API Key 明文提交到 GitHub。建议通过环境变量或 `.env` 文件注入，并定期更换 Key。

### 4. 启动后端

```bash
cd back
mvn spring-boot:run
```

后端默认运行在 `http://localhost:8080`。

### 5. 启动前端

```bash
cd front
npm install
npm run dev
```

前端开发服务器默认运行在 `http://localhost:5173`，`/api` 请求会自动代理到后端 `http://localhost:8080`。

### 6. 一键启动（Windows）

如果已经安装好 Java、Maven、Node.js 和 MySQL，可以直接双击运行：

```bash
start.bat
```

---

## API 接口

所有接口统一返回 `ApiResponse<T>` 结构：

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/copywriting/generate` | 单条文案生成 |
| POST | `/api/copywriting/batch/generate` | 批量生成（异步） |
| GET | `/api/copywriting/batch/{taskId}` | 查询批量任务进度 |
| GET | `/api/copywriting/history` | 查询生成历史（分页） |
| GET | `/api/copywriting/health` | 健康检查 |

### 单条生成示例

```bash
curl -X POST http://localhost:8080/api/copywriting/generate \
  -H "Content-Type: application/json" \
  -d '{
    "product": {
      "productName": "蓝牙耳机",
      "productDesc": "高音质无线蓝牙耳机",
      "sellingPoints": "降噪、长续航、舒适佩戴",
      "targetAudience": "年轻上班族"
    },
    "style": "xiaohongshu",
    "additionalRequirements": "突出性价比"
  }'
```

### 批量生成示例

```bash
curl -X POST http://localhost:8080/api/copywriting/batch/generate \
  -H "Content-Type: application/json" \
  -d '{
    "products": [
      {"productName": "商品1", "productDesc": "描述1"},
      {"productName": "商品2", "productDesc": "描述2"}
    ],
    "style": "taobao"
  }'
```

---

## 支持的文案风格

| 风格代码 | 名称 | 说明 |
|---------|------|------|
| `xiaohongshu` | 小红书风格 | 种草风格，使用 emoji，语气亲切活泼 |
| `taobao` | 淘宝风格 | 电商详情风格，突出产品卖点 |
| `wechat` | 朋友圈风格 | 生活化语气，简短精炼 |
| `title` | 标题优化 | 提取关键词，重组卖点 |

---

## 后端常用命令

```bash
# 进入后端目录
cd back

# 开发启动
mvn spring-boot:run

# 编译打包（生成 target/copywriting-assistant-1.0.0.jar）
mvn clean package

# 运行全部测试
mvn test

# 运行单个测试类
mvn test -Dtest=CopywritingServiceTest
```

---

## 前端常用命令

```bash
# 进入前端目录
cd front

# 安装依赖
npm install

# 启动开发服务器
npm run dev

# 生产构建（输出到 dist/）
npm run build
```

---

## 配置说明

主要配置位于 `back/src/main/resources/application.yml`：

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `server.port` | `8080` | 后端端口 |
| `spring.datasource.url` | `jdbc:mysql://localhost:3306/copywriting_db` | MySQL 连接 |
| `spring.jpa.hibernate.ddl-auto` | `update` | 自动更新表结构 |
| `async.core-pool-size` | `10` | 异步任务核心线程数 |
| `async.max-pool-size` | `50` | 异步任务最大线程数 |
| `async.queue-capacity` | `200` | 异步任务队列容量 |
| `openai.api-key` | - | 大模型 API Key |
| `openai.base-url` | `https://api.deepseek.com` | API 基础地址 |
| `openai.model` | `deepseek-v4-flash` | 模型名称 |
| `openai.max-tokens` | `2000` | 最大生成 token 数 |
| `openai.temperature` | `0.7` | 生成随机性 |
| `cache.copywriting.expire-hours` | `24` | 缓存过期时间 |
| `cache.copywriting.max-size` | `1000` | 最大缓存条数 |
| `batch.max-size` | `100` | 单次批量最大商品数 |

---

## 架构流程

1. **单条生成**：Controller → Service → 查缓存 → 构建 Prompt → 调用大模型 → 写缓存 → 存历史。
2. **批量生成**：提交后返回 `taskId`，`@Async` 异步执行，状态保存在内存中，通过轮询接口查询进度。
3. **缓存策略**：基于 `商品名 + 风格 + 额外要求` 的 MD5 作为 key，带 TTL 的内存缓存。

---

## 注意事项

- 当前项目使用 **内存缓存**，非 Redis，重启后缓存会丢失。
- 批量任务状态也保存在内存中，重启后任务状态会丢失。
- 生产环境建议将缓存和任务状态迁移到 Redis。
- **切勿将真实 API Key 提交到代码仓库**，建议通过环境变量或外部配置注入。

---

## License

[MIT](LICENSE)
