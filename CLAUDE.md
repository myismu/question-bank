# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 仓库定位

题库业务系统仓库：只包含题库业务侧代码（`question-bank-service` 与两个前端）。

用户中心、文件服务、统一网关、公共库与契约库在平台仓库 **cloud-platform**（`github.com/myismu/cloud-platform`）—— 通过**接口调用**（部署的服务）与 **Maven 制品**（common / api）使用，**本仓库不包含其代码**。修改平台能力时去平台仓库改，不要在本仓库复制。

## 模块

| 模块 | 职责 | 端口 |
|---|---|---|
| `service/question-bank-service` | 题库服务：题库、题目、试卷管理，消费用户注册事件 | 8002 |
| `Web/QuestionBank-Web` | 学生端（练习 / 考试 / 错题本 / 统计） | 3334 |
| `Web/QuestionBank-Admin` | 管理端（题库 / 用户 / 日志 / 统计） | 3335 |

## 依赖的平台制品

- 父 pom：`com.exambaby:cloud:1.0.0`（来自 GitHub Packages，`<relativePath/>` 强制走制品仓库）
- 公共库 `com.exambaby:common`、契约库 `com.exambaby:api`（版本由父 pom 的 `platform.libs.version` 统一管理）
- 本地：`~/.m2/settings.xml` 声明制品仓库；CI：`mvn -s .mvn/ci-settings.xml`

## 配置与依赖

- 运行时配置来自 **Nacos**（平台提供的开发命名空间），`naocs/nacos-dev/question-bank-service.yaml` 为模板。
- 中间件与平台服务（Nacos / MySQL / Redis / 网关 / 用户中心 / 文件服务）由平台仓库提供；**本地开发先起平台，再起业务服务**。
- 业务数据库脚本：`SQL/question_bank.sql`。
- RocketMQ 用于用户注册事件：`user-hub-service`（平台）发送 `user-register-topic`，本仓库的题库服务消费。

## 构建与启动

```bash
mvn clean package
# 启动 QuestionBankApplication（网关等平台服务需已就绪）
```

前端：各目录 `pnpm install && pnpm dev`。

## 关键机制

- **网关鉴权**由平台统一提供（`AuthFilter` + Redis 在线校验），本仓库不实现鉴权。
- **调用用户中心**：使用平台契约库 `api` 中的 `UserHubClient`（Feign），不要手写接口。

## QuestionBank-Admin

### Common Commands

Run all commands from `Web/QuestionBank-Admin`.

```bash
pnpm install
pnpm dev
pnpm build
pnpm build:staging
pnpm preview
pnpm lint
pnpm test
```

Notes:
- `pnpm dev` starts Vite on port `3335` and proxies `/api` to `http://127.0.0.1:8080`（平台网关）。
- Production build runs `vue-tsc && vite build`; staging build uses `--mode staging`.
- Vitest is configured in `vite.config.ts` with `happy-dom` and `tests/**/*.test.{ts,js}`.
- 原 Husky 提交钩子已移除（`prepare` 脚本与 `.husky` 目录已删除），提交前请手动执行 `pnpm lint` 与类型检查。

### High-Level Architecture

This app is a Vue 3 + Vite + TypeScript admin frontend derived from `v3-admin-vite`, adapted into a question-bank management system.

- App bootstrap lives in `src/main.ts`: create app, install shared plugins, attach Pinia and Vue Router, then mount after `router.isReady()`.
- Global plugin registration is centralized in `src/plugins/index.ts`（Element Plus 图标、权限指令、SVG 图标、VXE Table）。
- Routing is defined in `src/router/index.ts`（多数业务页面在 `constantRoutes`：系统日志、题库管理、学习统计等）。
- Route access control in `src/router/guard.ts`: token 来自 cookie，首次受保护导航时懒加载用户信息，通过权限 store 注入路由。
- State with Pinia in `src/pinia`；`stores/user.ts` 管 token/roles/用户名/logout 与 `getInfo()`。
- HTTP 集中在 `src/http/axios.ts`（`request()` 注入 Authorization、处理业务 code 与全局错误提示）。
- API 封装与功能同目录（`src/pages/*/apis`）。
- 布局壳在 `src/layouts`；通用代码在 `src/common`。

### Build and Tooling Details

- Path aliases: `@` -> `src`, `@@` -> `src/common`。
- Auto-import 扫描 `src/pinia/stores` 与 `src/common/composables`。
- 手工分包：Vue core / Element Plus / VXE Table。
- 注意：`types/auto/*.d.ts` 为自动生成且**被跟踪**，新增自动导入用法后需重新生成并提交（否则 CI 的类型检查会失败）。
- 注意：未被任何模块引用的 composable 不会被打包扫描，其中的自动导入名（如 `ElLoading`）需显式 import。

### Existing Tests

测试覆盖较浅（模板级）。修改练习、考试、统计等交互页面后需针对性验证。

### Upstream Context

继承 `v3-admin-vite` 的约定：Composition API + `script setup`、Element Plus、Pinia、Vue Router、UnoCSS、SCSS 主题变量、多布局与角色权限路由。

## QuestionBank-Web

### Common Commands

Run all commands from `Web/QuestionBank-Web`.

```bash
pnpm install
pnpm dev
pnpm build
pnpm build:staging
pnpm preview
pnpm lint
pnpm test
```

Notes:
- `pnpm dev` starts Vite on port `3334` and proxies `/api` to `http://127.0.0.1:8080`（平台网关）。
- 与管理端同模板同工具链。
- 原 Husky 提交钩子已移除，提交前手动执行 `pnpm lint` 与类型检查。

### High-Level Architecture

学生端：围绕学习、练习与个人题库管理。

- 启动模式与管理端一致（`src/main.ts`）。
- 路由三大块：`/user/*`（认证页）、`/home`（学习面板）、`/question-bank/*`（题库工作流）。
- `src/pages/question-bank/views/practice.vue`：练习状态复杂（答题历史、进度、活动记录持久化、切页保存）。
- 学习统计在 `src/pages/home/views/statistics.vue`（ECharts）。
- 认证流：Pinia user store + cookie token + 路由守卫（与管理端相同模式）。

### Build and Tooling Details

- Path aliases 同上；Vite 自动导入与组件自动注册同上。
- 依赖中额外包含 `echarts`。
- 结构与管理端接近，跨端移植修改前先核对端口、路由名与页面职责。
