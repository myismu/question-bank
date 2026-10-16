# Question Bank 在线题库系统

基于 Spring Cloud 微服务 + Vue 3 的在线题库平台，包含题库管理、在线练习、错题本与学习统计能力，分为学生端与管理端两个前端。

## 项目能力

- 学生端：公开题库浏览、个人题库管理、在线练习、考试、错题本、学习统计
- 管理端：题库管理、题目管理、用户与角色管理、系统日志、学习数据统计
- 后端：网关鉴权、服务注册与配置中心、限流熔断、消息队列解耦、分布式事务

## 技术栈

### 后端

- Java 17
- Spring Boot 3.5 / Spring Cloud 2025 / Spring Cloud Alibaba 2025
- Nacos（服务发现 + 配置中心）
- Sentinel（限流熔断）
- Seata（分布式事务）
- RocketMQ（消息队列）
- MyBatis-Plus / MySQL / Redis
- JWT（认证）

### 前端

- Vue 3 + TypeScript + Vite
- Element Plus / Pinia / Vue Router
- ECharts（学习统计）

## 目录结构

```text
.
├── Web/
│   ├── QuestionBank-Web/     # 学生端（Vue 3）
│   └── QuestionBank-Admin/   # 管理端（Vue 3）
├── service/
│   ├── question-bank-service # 题库服务：题库、题目、试卷管理
│   ├── user-hub-service      # 用户服务：认证、用户/角色管理
│   └── file-service          # 文件服务：上传与静态资源
├── gateway/                  # Spring Cloud Gateway 网关
├── common/                   # 公共模块
├── api/                      # Feign 接口定义
├── SQL/                      # 数据库脚本
├── naocs/                    # Nacos 配置模板
└── pom.xml                   # Maven 父工程
```

## 服务端口

| 服务 | 端口 |
|---|---|
| gateway | 8080 |
| user-hub-service | 8001 |
| question-bank-service | 8002 |
| file-service | 8003 |
| QuestionBank-Web（学生端） | 3334 |
| QuestionBank-Admin（管理端） | 3335 |

## 环境要求

- JDK >= 17、Maven >= 3.8
- Node.js >= 20、pnpm
- MySQL、Redis、Nacos、RocketMQ（Seata 可选）

## 快速启动

### 1. 初始化数据库

导入 `SQL/` 下的脚本：

- `SQL/user_hub.sql` → 用户库 `user_hub`
- `SQL/question_bank.sql` → 题库库 `question_bank`
- `SQL/seata.sql` → Seata 服务库（使用 Seata 时）

### 2. 启动基础组件

MySQL、Redis、Nacos（单机模式即可），并按需启动 RocketMQ 与 Seata。

### 3. 导入 Nacos 配置

将 `naocs/nacos-dev/` 下的配置文件按 Data ID 导入 Nacos（Group：`DEFAULT_GROUP`，命名空间按你的环境自行创建）：

- `gateway.yaml`
- `user-hub-service.yaml`
- `question-bank-service.yaml`
- `file-service.yaml`

导入前请替换配置中的占位符（数据库密码、JWT 密钥等），真实值不要提交到仓库。

### 4. 启动后端

```bash
mvn clean install
```

依次启动：`GatewayApplication` → `UserHubApplication` → `FileApplication` → `QuestionBankApplication`。

### 5. 启动前端

```bash
cd Web/QuestionBank-Web   # 学生端，http://127.0.0.1:3334
pnpm install
pnpm dev
```

```bash
cd Web/QuestionBank-Admin # 管理端，http://127.0.0.1:3335
pnpm install
pnpm dev
```

两个前端的 `/api` 请求都代理到网关 `http://127.0.0.1:8080`。

## 配置安全说明

- 仓库内所有敏感项（数据库密码、JWT 密钥等）均为占位符，真实值通过环境变量或 Nacos 私有配置注入。
- 不要提交真实 `.env`、API Key、生产数据库密码。
- 新增配置项时同步更新 `naocs/nacos-dev/` 下的模板。

## 说明

前端基于 `v3-admin-vite` 模板二次开发，保留了其布局、权限路由与工具链约定。
