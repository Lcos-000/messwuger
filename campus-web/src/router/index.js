import { createRouter, createWebHistory } from 'vue-router'
import { ROUTE_NAMES, ROUTE_PATHS, STORAGE_KEYS } from '@/config'

const routes = [
  {
    path: ROUTE_PATHS.ROOT,
    redirect: ROUTE_PATHS.SCHEDULE
  },
  {
    path: ROUTE_PATHS.LOGIN,
    name: ROUTE_NAMES.LOGIN,
    component: () => import('../views/Login.vue'),
    meta: { title: '登录', index: 0 }
  },
  {
    path: ROUTE_PATHS.SCHEDULE,
    name: ROUTE_NAMES.SCHEDULE,
    component: () => import('../views/Schedule.vue'),
    meta: { title: '我的课表', requiresAuth: true, index: 1 }
  },
  {
    path: ROUTE_PATHS.PROFILE,
    name: ROUTE_NAMES.PROFILE,
    component: () => import('../views/Profile.vue'),
    meta: { title: '个人中心', requiresAuth: true, index: 2 }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  // 设置标题
  if (to.meta.title) {
    document.title = to.meta.title
  }

  const token = localStorage.getItem(STORAGE_KEYS.TOKEN)

  if (to.meta.requiresAuth) {
    // 需要登录
    if (token) {
      next()
    } else {
      next(ROUTE_PATHS.LOGIN)
    }
  } else {
    // 不需要登录的页面（如 login 或者 catch-all）
    if (to.path === ROUTE_PATHS.LOGIN && token) {
      next(ROUTE_PATHS.SCHEDULE)
    } else {
      next()
    }
  }
})

export default router


