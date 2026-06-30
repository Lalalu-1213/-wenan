# 智能文案生成助手 - 后端

## 环境要求

- Java 17
- Maven 3.6+
- Redis 6.0+

## 快速开始

### 1. 配置环境变量

复制环境变量示例文件：
```bash
copy .env.example .env
```

编辑 `.env` 文件，填入你的API配置：
```properties
OPENAI_API_KEY=sk-your-api-key-here
OPENAI_BASE_URL=https://api.openai.com
OPENAI_MODEL=gpt-3.5-turbo
```

### 2. 启动Redis

**方式一：本地安装**
- Windows下载：https://github.com/tporadowski/redis/releases
- 启动命令：`redis-server`

**方式二：Docker**
```bash
docker run -d -p 6379:6379 --name redis redis:latest
```

### 3. 启动项目

**Windows双击启动：**
```
start.bat
```

**命令行启动：**
```bash
mvn spring-boot:run
```

## API接口

### 生成单个商品文案
```bash
POST /api/copywriting/generate
Content-Type: application/json

{
  "product": {
    "productName": "蓝牙耳机",
    "productDesc": "高音质无线蓝牙耳机",
    "sellingPoints": "降噪、长续航、舒适佩戴",
    "targetAudience": "年轻上班族"
  },
  "style": "xiaohongshu",
  "additionalRequirements": ""
}
```

### 提交批量生成任务
```bash
POST /api/copywriting/batch/submit
Content-Type: application/json

{
  "products": [
    {"productName": "商品1", "productDesc": "描述1"},
    {"productName": "商品2", "productDesc": "描述2"}
  ],
  "style": "taobao"
}
```

### 查询批量任务状态
```bash
GET /api/copywriting/batch/{taskId}
```

## 支持的文案风格

| 风格代码 | 名称 | 说明 |
|---------|------|------|
| xiaohongshu | 小红书风格 | 种草风格，使用emoji，语气亲切活泼 |
| taobao | 淘宝风格 | 电商详情风格，突出产品卖点 |
| wechat | 朋友圈风格 | 生活化语气，简短精炼 |
| title | 标题优化 | 提取关键词，重组卖点 |
