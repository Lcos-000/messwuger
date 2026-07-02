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
    path: ROUTE_PATHS.ADMIN,
    name: ROUTE_NAMES.ADMIN,
    component: () => import('../views/Admin.vue'),
    meta: { title: '管理员控制台', requiresAuth: true, adminOnly: true, index: 1 }
  },
  {
    path: ROUTE_PATHS.SCHEDULE,
    name: ROUTE_NAMES.SCHEDULE,
    component: () => import('../views/Schedule.vue'),
    meta: { title: '我的课表', requiresAuth: true, index: 2 }
  },
  {
    path: ROUTE_PATHS.PROFILE,
    name: ROUTE_NAMES.PROFILE,
    component: () => import('../views/Profile.vue'),
    meta: { title: '个人中心', requiresAuth: true, index: 3 }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  if (to.meta.title) {
    document.title = to.meta.title
  }

  const token = localStorage.getItem(STORAGE_KEYS.TOKEN)
  const loginMode = localStorage.getItem(STORAGE_KEYS.LOGIN_MODE)

  if (to.meta.requiresAuth) {
    if (token) {
      if (to.meta.adminOnly && loginMode !== 'admin') {
        next(ROUTE_PATHS.SCHEDULE)
        return
      }
      next()
    } else {
      next(ROUTE_PATHS.LOGIN)
    }
  } else {
    if (to.path === ROUTE_PATHS.LOGIN && token) {
      next(loginMode === 'admin' ? ROUTE_PATHS.ADMIN : ROUTE_PATHS.SCHEDULE)
    } else {
      next()
    }
  }
})

export default router
