import type { RouteRecordRaw } from "vue-router"
import { routerConfig } from "@/router/config"
import { registerNavigationGuard } from "@/router/guard"
import { createRouter } from "vue-router"
import { flatMultiLevelRoutes } from "./helper"

const Layouts = () => import("@/layouts/index.vue")

/**
 * @name 常驻路由
 * @description 除了 redirect/403/404/login 等隐藏页面，其他页面建议设置唯一的 Name 属性
 */
export const constantRoutes: RouteRecordRaw[] = [
  // ==================== 基础路由 ====================
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
    path: "/login",
    component: () => import("@/pages/login/index.vue"),
    meta: {
      hidden: true
    }
  },
  {
    path: "/",
    component: Layouts,
    redirect: "/dashboard",
    children: [
      {
        path: "dashboard",
        component: () => import("@/pages/dashboard/index.vue"),
        name: "Dashboard",
        meta: {
          title: "首页",
          svgIcon: "dashboard",
          affix: true
        }
      }
    ]
  },

  // ==================== 系统管理 ====================
  {
    path: "/system",
    component: Layouts,
    redirect: "/system/globalLog",
    meta: {
      title: "系统管理",
      svgIcon: "system",
      alwaysShow: true
    },
    children: [
      {
        path: "globalLog",
        component: () => import("@/pages/globalLog/index.vue"),
        name: "globalLog",
        meta: {
          title: "全局日志",
          svgIcon: "login-history"
        }
      }
    ]
  },

  // ==================== 题库管理 ====================
  {
    path: "/questionBank",
    component: Layouts,
    redirect: "/questionBank/questionBanks",
    meta: {
      title: "题库管理",
      svgIcon: "search",
      alwaysShow: true
    },
    children: [
      {
        path: "bankTags",
        component: () => import("@/pages/bankTags/index.vue"),
        name: "bankTags",
        meta: {
          title: "题库标签",
          svgIcon: "role"
        }
      },
      {
        path: "questionChapters",
        component: () => import("@/pages/questionChapters/index.vue"),
        name: "questionChapters",
        meta: {
          title: "章节管理",
          svgIcon: "search"
        }
      },
      {
        path: "questionBanks",
        component: () => import("@/pages/questionBanks/index.vue"),
        name: "questionBanks",
        meta: {
          title: "题库管理",
          svgIcon: "search"
        }
      },
      {
        path: "questions",
        component: () => import("@/pages/questions/index.vue"),
        name: "questions",
        meta: {
          title: "题目管理",
          svgIcon: "search"
        }
      },
      {
        path: "wrongQuestions",
        component: () => import("@/pages/wrongQuestions/index.vue"),
        name: "wrongQuestions",
        meta: {
          title: "错题管理",
          svgIcon: "search"
        }
      }
    ]
  },

  // ==================== 学习统计 ====================
  {
    path: "/statistics",
    component: Layouts,
    redirect: "/statistics/learningStatistics",
    meta: {
      title: "学习统计",
      svgIcon: "dashboard",
      alwaysShow: true
    },
    children: [
      {
        path: "learningStatistics",
        component: () => import("@/pages/learningStatistics/index.vue"),
        name: "LearningStatistics",
        meta: {
          title: "学习统计",
          svgIcon: "dashboard"
        }
      },
      {
        path: "activityAnswers",
        component: () => import("@/pages/activityAnswers/index.vue"),
        name: "activityAnswers",
        meta: {
          title: "答题记录管理",
          svgIcon: "search"
        }
      },
      {
        path: "activityRecords",
        component: () => import("@/pages/activityRecords/index.vue"),
        name: "activityRecords",
        meta: {
          title: "活动记录管理",
          svgIcon: "search"
        }
      }
    ]
  },
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
