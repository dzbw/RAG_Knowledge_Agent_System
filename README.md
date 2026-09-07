# Java1234 RAG 企业知识库问答系统

基于 **Spring Boot 4 + Spring AI 2.0 + Vue3 + Element Plus + MySQL 8 + Redis（向量）+ Ollama** 的入门向 RAG 知识库演示项目。

## 目录说明

- `server`：后端 API（端口 `8080`）
- `client`：前端 SPA（Vite 开发端口 `5173`）

## 环境准备

1. **MySQL 8** 监听 **3308**，`root` 密码与本项目一致为 **123456**（可在 [`server/src/main/resources/application.properties`](server/src/main/resources/application.properties) 修改）。
2. 执行建库脚本：[`server/src/main/resources/sql/init.sql`](server/src/main/resources/sql/init.sql)
3. **Redis**：需支持向量检索（建议使用 **Redis Stack / 带 RediSearch 模块** 的发行版），默认 `localhost:6379`。
4. **Ollama**：安装并拉取模型：
   ```bash
   ollama pull qwen3:4b
   ollama pull qwen3-embedding:4b
   ```
5. **上传目录**：文件默认保存到 **`D:/uploads3`**（与配置一致；启动时会自动创建）。

## 默认测试账号（密码均为 `123456`，数据库存 MD5）

| 用户名 | 角色   |
|--------|--------|
| admin  | 管理员 |
| user1  | 普通用户 |
| user2  | 普通用户 |
| user3  | 普通用户 |

## 启动后端

```bash
cd server
./mvnw spring-boot:run
```

（Windows 可使用 `mvnw.cmd`。）

### 向量维度说明

Redis 向量索引维度需与嵌入模型一致。若启动时报向量维度错误，请在 Spring AI 的 Redis 向量配置中按实际 `qwen3-embedding:4b` 输出维度调整（常见为 **1024**，具体以本机 `ollama` 模型为准）。

## 启动前端

```bash
cd client
npm install
npm run dev
```

浏览器访问：`http://localhost:5173`

- 管理员登录后进入 **管理后台**（数据统计、用户/分类/文档、问答测试）。
- 普通用户进入 **知识问答 / 会话历史 / 个人中心**。

## 支持的文档格式

上传后解析并向量化：**txt、pdf、doc、docx、markdown（md）**。

## API 前缀

- 业务接口：`/api/**`
- 静态访问已上传文件：`/files/**`（映射到 `file.upload.path`）

## 技术要点摘要

- 登录：**JWT**；密码：**MD5**（与库表一致）。
- RAG：**Ollama 嵌入** + **Redis VectorStore**；**QuestionAnswerAdvisor** + **ChatClient** 完成检索增强问答。
- 文档解析：**Apache Tika**（通用）+ **MarkdownDocumentReader**（md）。

## 许可与学习用途

代码含中文类/方法注释，复杂度适中，适合作为 Spring AI 2.0 RAG 入门演练。
