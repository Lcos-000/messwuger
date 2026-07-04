import { createRouter, createWebHistory } from 'vue-router'
import { ROUTE_NAMES, ROUTE_PATHS } from '@/config'
import { getDefaultAuthedRoute, hasAdminSession, hasSessionForPath, hasUserSession } from '@/utils/auth'

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
    path: ROUTE_PATHS.GRADES,
    name: ROUTE_NAMES.GRADES,
    component: () => import('../views/Grades.vue'),
    meta: { title: '我的成绩', requiresAuth: true, index: 3 }
  },
  {
    path: ROUTE_PATHS.PROFILE,
    name: ROUTE_NAMES.PROFILE,
    component: () => import('../views/Profile.vue'),
    meta: { title: '个人中心', requiresAuth: true, index: 4 }
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

  const hasUserToken = hasUserSession()
  const hasAdminToken = hasAdminSession()

  if (to.meta.requiresAuth) {
    if (!hasSessionForPath(to.path)) {
      next(ROUTE_PATHS.LOGIN)
      return
    }
    if (to.meta.adminOnly && !hasAdminToken) {
      next(hasUserToken ? ROUTE_PATHS.SCHEDULE : ROUTE_PATHS.LOGIN)
      return
    }
    next()
  } else {
    if (to.path === ROUTE_PATHS.LOGIN && (hasUserToken || hasAdminToken)) {
      next(getDefaultAuthedRoute())
      return
    }
    next()
  }
})

export default router
