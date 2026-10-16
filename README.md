# Question Bank 在线题库系统

基于 Spring Cloud 微服务 + Vue 3 的在线题库平台，包含题库管理、在线练习、错题本与学习统计能力，分为学生端与管理端两个前端。

## 架构说明（重要）

本仓库只包含题库**业务侧**的代码。用户中心、文件服务、统一网关、公共库与契约库由平台仓库 [cloud-platform](../cloud-platform)（GitHub: `myismu/cloud-platform`）提供：

| 能力 | 来源（平台仓库） | 本仓库的使用方式 |
|---|---|---|
| 用户中心（认证 / 用户 / 角色） | `user-hub-service` | 接口调用（经统一网关） |
| 文件服务（上传 / 静态资源） | `file-service` | 接口调用（经统一网关） |
| 统一网关（路由 / 鉴权 / CORS） | `gateway` | 所有请求的统一入口 |
| 公共库 `common`、契约库 `api` | 发布的 Maven 制品 | `pom.xml` 依赖 |

平台代码只有一份源码（在 cloud-platform 仓库），本仓库不复制、不维护其实现。

## 项目能力

- 学生端：公开题库浏览、个人题库管理、在线练习、考试、错题本、学习统计
- 管理端：题库管理、题目管理、用户与角色管理、系统日志、学习数据统计
- 后端：题库/题目/试卷管理、消息队列解耦（消费用户注册事件）

## 技术栈

### 后端

- Java 17 · Spring Boot 3.5 · Spring Cloud 2025 · Spring Cloud Alibaba 2025
- Nacos（服务发现 + 配置中心）· Sentinel · Seata · RocketMQ
- MyBatis-Plus · MySQL · Redis · JWT

### 前端

- Vue 3 + TypeScript + Vite · Element Plus · Pinia · Vue Router · ECharts

## 目录结构

```text
.
├── Web/
│   ├── QuestionBank-Web/        # 学生端（端口 3334）
│   └── QuestionBank-Admin/      # 管理端（端口 3335）
├── service/
│   └── question-bank-service/   # 题库服务（端口 8002）
├── SQL/                         # 业务数据库脚本（question_bank）
├── naocs/nacos-dev/             # 业务服务的 Nacos 配置模板
├── .mvn/ci-settings.xml         # CI 专用 Maven 配置（声明平台制品仓库）
└── pom.xml                      # Maven 父工程（继承平台父 pom）
```

## 本地启动

### 0. 前置：启动平台（含中间件）

```bash
cd ../cloud-platform
docker-compose up -d --build    # Nacos / MySQL / Redis / 网关 / 用户中心 / 文件服务
```

### 1. 启动业务服务

```bash
mvn clean package
```

启动 `QuestionBankApplication`（端口 8002）；Nacos 配置从平台开发命名空间读取（本仓库 `naocs/nacos-dev/question-bank-service.yaml` 为模板）。

### 2. 启动前端

```bash
cd Web/QuestionBank-Web    # 学生端 → http://127.0.0.1:3334
pnpm install && pnpm dev
```

```bash
cd Web/QuestionBank-Admin  # 管理端 → http://127.0.0.1:3335
pnpm install && pnpm dev
```

两个前端的 `/api` 请求都代理到平台网关 `http://127.0.0.1:8080`。

## Maven 依赖说明

父 pom（`com.exambaby:cloud`）、公共库 `common`、契约库 `api` 来自平台发布的 GitHub Packages 制品：

- 本地开发：`~/.m2/settings.xml` 中声明制品仓库（配置片段见 cloud-platform 的 README；包为公开，拉取无需认证）
- CI：使用 `.mvn/ci-settings.xml`

## 配置安全说明

- 仓库内敏感值均为占位符，真实值通过环境变量或 Nacos 私有配置注入
- 不提交真实 `.env`、数据库密码

## 说明

两个前端基于 `v3-admin-vite` 模板二次开发，保留了其布局、权限路由与工具链约定。本项目采用 GPL-3.0 许可证。
