# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 仓库定位

在线题库系统：Spring Cloud 微服务后端（Java 17）+ 两个独立的 Vue 3 前端（学生端 `Web/QuestionBank-Web`、管理端 `Web/QuestionBank-Admin`）。本仓库自包含（AI-PPT 系统在独立的 ai-ppt 仓库，不在本仓库内）。

## 后端微服务

| 模块 | 职责 | 端口 |
|---|---|---|
| `gateway` | 网关：路由、JWT 鉴权、CORS、请求日志 | 8080 |
| `service/user-hub-service` | 用户服务：登录、注册、用户/角色/路由管理 | 8001 |
| `service/question-bank-service` | 题库服务：题库、题目、试卷管理，含 RocketMQ 生产/消费 | 8002 |
| `service/file-service` | 文件服务：上传与静态资源 | 8003 |
| `common` | 公共模块：JwtUtil、统一返回、全局异常、登录头过滤器 | - |
| `api` | Feign 客户端接口定义 | - |

### 配置与依赖

- 运行时配置全部来自 **Nacos**，本地 `application-dev.yml` 只保留端口与 Nacos 地址。
- 配置模板在 `naocs/nacos-dev/`，按 Data ID 导入 Nacos 对应命名空间。仓库内敏感值均为占位符（如 `${MYSQL_PWD:}`、`${JWT_SECRET:}`），真实值通过环境变量注入，**不要提交任何真实密钥**。
- 中间件依赖：MySQL（`user_hub` / `question_bank` 库）、Redis、Nacos；RocketMQ 用于用户注册后的事件通知（`user-register-topic`）；Seata 已在配置中启用（分布式事务演示）。
- SQL 脚本在 `SQL/`：`user_hub.sql`、`question_bank.sql`、`seata.sql`。

### 构建与启动

```bash
mvn clean install    # 先安装 common/api 到本地仓库
```

再依次启动各模块启动类：`GatewayApplication` → `UserHubApplication` → `FileApplication` → `QuestionBankApplication`。

### 关键机制

- **网关鉴权**：`gateway` 的 `AuthFilter` 校验 JWT（`common/JwtUtil`），并检查 Redis 在线状态（`online:{systemCode}:{userId}` 必须存在且与 token 一致）。免认证白名单见 `AuthFilter.WHITE_LIST`（含公开题库查询接口）。
- **JWT 密钥**：`user-hub-service` 签发、`gateway` 校验，两边共用 Nacos 下发的 `jwt.secret`，修改必须全服务同步。
- **消息解耦**：用户注册成功后，`user-hub-service` 向 RocketMQ 的 `user-register-topic` 发送消息，`question-bank-service` 消费后为新用户初始化数据。

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
- `pnpm dev` starts Vite on port `3335` and proxies `/api` to `http://127.0.0.1:8080`.
- Production build runs `vue-tsc && vite build`; staging build uses `--mode staging`.
- Vitest is configured in `vite.config.ts` with `happy-dom` and `tests/**/*.test.{ts,js}`.
- 原 Husky 提交钩子已随并入单仓移除（`prepare` 脚本与 `.husky` 目录已删除），提交前请手动执行 `pnpm lint` 与类型检查。

### High-Level Architecture

This app is a Vue 3 + Vite + TypeScript admin frontend derived from `v3-admin-vite`, but it has been adapted into a question-bank management system for administrators.

- App bootstrap lives in `src/main.ts`: create app, install shared plugins, attach Pinia and Vue Router, then mount after `router.isReady()`.
- Global plugin registration is centralized in `src/plugins/index.ts`, which wires Element Plus icon components, the permission directive, SVG icon support, and VXE Table once at startup.
- Routing is defined in `src/router/index.ts`. Most business pages are currently in `constantRoutes`, organized by domains like system logs, question bank management, and learning statistics.
- Route access control is enforced in `src/router/guard.ts`: token comes from cookies, user info is fetched lazily on first guarded navigation, and allowed routes are injected through the permission store.
- `src/router/config.ts` has `dynamic: true`, so permission flow matters even though most route declarations are static; the guard still loads roles and calls the permission store before allowing navigation.
- State is organized with Pinia in `src/pinia`. `stores/user.ts` owns token, roles, username, logout/reset behavior, and `getInfo()`, which calls the current-user API and supplies fallback roles from router config.
- HTTP access is centralized in `src/http/axios.ts`. All page-level API modules call the exported `request()` helper, which injects `Authorization`, merges defaults, unwraps backend `code`, and handles auth/network failures with global `ElMessage` feedback.
- Backend-facing API wrappers are colocated with features: shared auth/user calls live in `src/common/apis`, while page-specific endpoints live under each page directory such as `src/pages/questionBanks/apis` or `src/pages/activityRecords/apis`.
- Business screens are feature-first under `src/pages`. Each major route usually has its own page component plus a local `apis/` folder, so when changing a feature, start from the page directory rather than searching globally.
- Layout shell code is under `src/layouts`, with multiple layout modes and shared pieces like sidebar, breadcrumb, tags view, and settings drawer.
- Shared reusable code is under `src/common`: assets, composables, utility functions, generic components, and cache helpers used across pages and stores.

### Build and Tooling Details

- Path aliases in `vite.config.ts`: `@` -> `src`, `@@` -> `src/common`.
- Auto-import scans `src/pinia/stores` and `src/common/composables`, so some store/composable symbols may appear without explicit imports.
- SVG icons are generated from `src/common/assets/icons`, with a special preserve-color directory at `src/common/assets/icons/preserve-color`.
- Manual chunk splitting groups Vue core, Element Plus, and VXE Table into separate bundles.

### Existing Tests

Current test coverage is light and mostly template-level. Existing tests are in `tests/`, including `tests/utils/validate.test.ts`, `tests/components/Notify.test.ts`, and `tests/demo.test.ts`. For feature work, do not assume the current suite protects page-level behavior.

### Upstream Context From README

The app inherits several upstream conventions from `v3-admin-vite`: Vue 3 Composition API with `script setup`, Element Plus UI, Pinia, Vue Router, UnoCSS, SCSS theme variables, multi-layout support, and role-based permission routing. Keep that template heritage in mind before refactoring core layout, router, or store infrastructure.

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
- `pnpm dev` starts Vite on port `3334` and proxies `/api` to `http://127.0.0.1:8080`.
- Build, lint, test, and Vitest configuration are effectively the same shape as the admin app.
- 原 Husky 提交钩子已随并入单仓移除（`prepare` 脚本与 `.husky` 目录已删除），提交前请手动执行 `pnpm lint` 与类型检查。

### High-Level Architecture

This app reuses the same Vue 3/Vite template foundation as the admin app, but the route tree and feature modules are end-user facing around studying, practicing, and managing personal question banks.

- The same bootstrap pattern appears in `src/main.ts`: create app, install shared plugins, then register Pinia and router.
- Routing in `src/router/index.ts` is centered on three areas: auth pages under `/user/*`, learner dashboard pages under `/home` and its child views, and question-bank workflows under `/question-bank/*`.
- `src/pages/home/index.vue` acts as a landing/dashboard page and links users into public banks, learning statistics, and the wrong-question workflow.
- `src/pages/question-bank/index.vue` is the main personal-bank workspace. It loads created and favorited banks, filters them client-side, and routes into create/detail/practice/exam/manage subflows.
- Practice behavior is substantial and stateful in `src/pages/question-bank/views/practice.vue`: it loads questions by bank, tracks answer history and progress, persists activity records through page-local API modules, and saves on navigation or visibility changes.
- Learning analytics live under `src/pages/home/views/statistics.vue`, where ECharts renders trend and progress visualizations based on page-local statistics APIs.
- Shared auth/user flow still comes from Pinia plus cookie-backed router guards. The app keeps the same `src/pinia/stores/user.ts` pattern and `src/common/apis/users/index.ts` current-user request used by the template guard flow.
- API wrappers are split by feature under `src/pages/home/apis` and `src/pages/question-bank/apis`, so feature work usually requires changing both the page view and its colocated API helpers.

### Build and Tooling Details

- Path aliases remain `@` -> `src` and `@@` -> `src/common`.
- Vite auto-import and component auto-registration follow the same upstream pattern as the admin app.
- This app adds `echarts` to the dependency set for the learning-statistics views.
- The structure is close to `QuestionBank-Admin`, so shared fixes can sometimes be ported, but verify ports, route names, and page responsibilities before copying changes.

### Existing Tests

The visible tooling and test layout match the upstream template, which means tests exist but are still sparse relative to the customized learner-facing flows. Treat interactive pages like practice, exam, and statistics as needing targeted verification after changes.
