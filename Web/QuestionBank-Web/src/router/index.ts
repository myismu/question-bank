import type { RouteRecordRaw } from "vue-router"
import { createRouter } from "vue-router"
import { routerConfig } from "@/router/config"
import { registerNavigationGuard } from "@/router/guard"
import { flatMultiLevelRoutes } from "./helper"

const Layouts = () => import("@/layouts/index.vue")

/**
 * @name 常驻路由
 * @description 除了 redirect/403/404/login 等隐藏页面，其他页面建议设置唯一的 Name 属性
 */
export const constantRoutes: RouteRecordRaw[] = [
  {
    path: "/redirect",
    component: Layouts,
    meta: {
      hidden: true
    },
    children: [
      {
        path: ":path(.*)",
        component: () => import("@/pages/redirect/index.vue")
      }
    ]
  },
  {
    path: "/403",
    component: () => import("@/pages/error/403.vue"),
    meta: {
      hidden: true
    }
  },
  {
    path: "/404",
    component: () => import("@/pages/error/404.vue"),
    meta: {
      hidden: true
    },
    alias: "/:pathMatch(.*)*"
  },
  {
    path: "/user",
    redirect: "/user/login",
    children: [
      {
        path: "login",
        component: () => import("@/pages/login/index.vue"),
        name: "Login",
        meta: {
          title: "登录",
          affix: true
        }
      },

      {
        path: "registration",
        component: () => import("@/pages/login/views/Registration.vue"),
        name: "Registration",
        meta: {
          title: "注册",
          affix: true
        }
      }

    ]
  },
  {
    path: "/",
    component: Layouts,
    redirect: "/home",
    children: [
      {
        path: "home",
        component: () => import("@/pages/home/index.vue"),
        name: "Home",
        meta: {
          title: "首页",
          affix: true
        }
      },
      {
        path: "statistics",
        component: () => import("@/pages/home/views/statistics.vue"),
        name: "Statistics",
        meta: { title: "学习统计", affix: true }
      },
      {
        path: "wrongQuestions",
        component: () => import("@/pages/home/views/wrongQuestions.vue"),
        name: "WrongQuestions",
        meta: { title: "错题表", affix: true }
      }

    ]
  },
  {
    path: "/question-bank",
    component: Layouts,
    redirect: "/question-bank/main",
    children: [
      {
        path: "main",
        component: () => import("@/pages/question-bank/index.vue"),
        name: "QuestionBank",
        meta: { title: "题库管理", affix: true }
      },
      {
        path: "createBank",
        component: () => import("@/pages/question-bank/views/createBank.vue"),
        name: "CreateBank",
        meta: { title: "创建题库", affix: true }
      },
      {
        path: "bankDetail",
        component: () => import("@/pages/question-bank/views/bankDetail.vue"),
        name: "BankDetail",
        meta: { title: "题库详情", affix: true }
      },
      {
        path: "practice",
        component: () => import("@/pages/question-bank/views/practice.vue"),
        name: "Practice",
        meta: { title: "练习模式", affix: true }
      },
      {
        path: "exam",
        component: () => import("@/pages/question-bank/views/Exam.vue"),
        name: "Exam",
        meta: { title: "模拟考试", affix: true }
      },
      {
        path: "bankManage",
        component: () => import("@/pages/question-bank/views/bankManage.vue"),
        name: "BankManage",
        meta: { title: "题库管理", affix: true }
      }

    ]
  }

]

/**
 * @name 动态路由
 * @description 用来放置有权限 (Roles 属性) 的路由
 * @description 必须带有唯一的 Name 属性
 */
export const dynamicRoutes: RouteRecordRaw[] = [

]

/** 路由实例 */
export const router = createRouter({
  history: routerConfig.history,
  routes: routerConfig.thirdLevelRouteCache ? flatMultiLevelRoutes(constantRoutes) : constantRoutes
})

/** 重置路由 */
export function resetRouter() {
  try {
    // 注意：所有动态路由路由必须带有 Name 属性，否则可能会不能完全重置干净
    router.getRoutes().forEach((route) => {
      const { name, meta } = route
      if (name && meta.roles?.length) {
        router.hasRoute(name) && router.removeRoute(name)
      }
    })
  } catch {
    // 强制刷新浏览器也行，只是交互体验不是很好
    location.reload()
  }
}

// 注册路由导航守卫
registerNavigationGuard(router)
