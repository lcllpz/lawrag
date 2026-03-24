---
name: architecture-baseline
description: 为 LawRAG 的所有功能开发与缺陷修复提供统一架构基线约束。用户提到功能实现、代码修改、重构、修复 bug，或提到架构、基线、规范、一致性、鉴权、RAG、知识库、管理端模块时使用。
---

# LawRAG 架构基线

## 目的与范围

将本文件作为本仓库架构决策与实现约束的唯一事实源。

适用范围：

- 后端：`src/main/java/com/simon/lawrag` 下的 Spring Boot 应用
- 前端：`lawrag-frontend` 下的 Vue 应用

## 触发条件

出现以下任一场景时启用本 skill：

- 用户要求功能开发、代码修改、缺陷修复或重构
- 用户提到架构/基线/规范/约定/一致性
- 变更涉及鉴权、聊天、RAG、知识库、管理模块或数据模型

## 架构快照

核心能力：

- 用户认证与角色控制（user/admin）
- 知识库上传、解析、切片、向量化、向量入库
- SSE 流式聊天问答
- RAG 流程：改写、召回、融合、重排、提示词组装、生成、安全兜底
- AI 运行参数热更新（无需重启）
- 管理端：统计、模板、通知、审计日志、用户管理

后端技术栈：

- Java 17, Spring Boot 3.2.x
- Spring Security + JWT
- MyBatis-Plus + MySQL
- Redis
- Milvus
- LangChain4j（OpenAI 兼容接口）
- 本地文件存储（`upload.path`）

前端技术栈：

- Vue 3 + TypeScript + Vite
- Vue Router + Pinia
- Element Plus
- Axios + ECharts

## 分层与模块边界

后端分层：

- `controller`：接口层
- `service`/`service/impl`：业务层
- `service/rag`：检索与生成流水线
- `service/knowledge`：文档处理与向量入库
- `mapper`：数据访问层
- `entity`：实体/DTO/VO
- `config`/`security`/`common`：基础设施与横切能力

前端分层：

- `views`, `layout`, `router`, `stores`, `api`, `utils/request.ts`

业务域映射：

- user/auth: `/api/user/**`
- chat/rag: `/api/chat/**`
- knowledge: `/api/knowledge/**`
- admin ai-config: `/api/admin/ai-config/**`
- stats: `/api/stats/**`
- template: `/api/template/**`
- notification: `/api/notification/**`
- favorite: `/api/favorite/**`
- audit-log: `/api/audit-log/**`

## 核心运行流程

鉴权流程：

1. 前端登录后将 JWT 存入 `localStorage`
2. Axios 请求拦截器注入 `Authorization`
3. 后端 `JwtAuthenticationFilter` 校验并注入认证上下文
4. `SecurityConfig` 与角色校验共同执行授权
5. SSE 场景下因 EventSource 限制，使用 query token 兜底

聊天 + RAG 流程（`/api/chat/stream`）：

1. 获取或创建会话
2. 构建近期历史上下文
3. 保存用户消息
4. 问题归一化并统计频次
5. 高频命中缓存则直接回放
6. 未命中执行：改写 -> 向量召回 -> BM25 -> RRF -> 重排 -> 组装 Prompt -> 流式生成
7. 保存助手消息（含 sources/retrieval log）
8. 频次达阈值时写入缓存
9. 发送 done 事件（含可观测字段）

知识库处理流程（`/api/knowledge/upload`）：

1. 校验文件类型
2. 文件保存到本地
3. 写入 `med_knowledge_base`，状态为 uploading
4. 事务提交后触发异步 `DocumentProcessTask`
5. 异步执行：抽取 -> 切片 -> 分批向量化 -> 写入 Milvus -> 更新 ready/failed

## 数据与配置边界

MySQL 核心表：

- `sys_user`, `med_conversation`, `med_message`, `med_knowledge_base`
- `sys_ai_config`, `sys_question_template`, `sys_notification`, `sys_favorite`, `sys_audit_log`

Redis：

- 高频问答缓存
- 问题频次计数
- 找回密码验证码

Milvus：

- Collection：`legal_knowledge`
- 向量维度：1024（必须与 embedding 输出一致）

配置模型：

- 基础设施配置：`application.yml`
- 运行时 AI 参数：`sys_ai_config`
- 运行期持有与热切换：`AiConfigHolder`

结论：AI 调参必须配置化，不应仅靠发版硬编码修改。

## 每次变更的必执行流程

1. 将需求映射到一个业务域：
   - user/auth
   - chat/rag
   - knowledge
   - admin (ai-config/stats/template/audit-log/notification)
2. 确认边界影响：
   - permissions and role boundaries
   - API contract (`Result` envelope and endpoint style)
   - storage boundaries (MySQL/Redis/Milvus/local files)
   - async/sync boundaries (especially knowledge processing)
3. 以兼容优先进行实现：
   - keep existing API behavior unless user requests breaking changes
   - keep SSE event compatibility for chat stream changes
   - keep RAG retrieval log observability when modifying retrieval pipeline
4. 验证并说明：
   - state which baseline constraints were applied
   - call out any intentional deviation and why

## 强约束

- 不得将权限校验下放为仅前端控制。
- 不得绕过后端角色校验实现管理端能力。
- 不得引入临时返回格式，统一使用 `Result`。
- 不得在上传接口中阻塞长耗时文档处理。
- 新增 RAG 运行参数若属于可调项，必须进入 AI 配置体系。

## 工程约束

- 前端只做体验层门禁，后端是最终权限裁决点。
- 保持聊天流式协议稳定（`rewrite/retrieval/rerank/token/done`）。
- 保持知识库状态机语义稳定：`uploading/processing/ready/failed`。
- 除非用户明确接受，不做破坏性 API 变更。

## 维护规则

- 将本 `SKILL.md` 作为架构规则的唯一基线文档。
- 出现以下变化时，必须同步更新本文件：
  - 新增核心模块或中间件
  - API 协议或路径变化
  - 鉴权模型变化
  - RAG 阶段变化
  - 存储边界变化（MySQL/Redis/Milvus/文件）

