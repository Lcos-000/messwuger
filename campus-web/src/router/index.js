import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    redirect: '/schedule'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { title: '登录', index: 0 }
  },
  {
    path: '/schedule',
    name: 'Schedule',
    component: () => import('../views/Schedule.vue'),
    meta: { title: '我的课表', requiresAuth: true, index: 1 }
  },
  {
    path: '/profile',
    name: 'Profile',
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

  const token = localStorage.getItem('campus_token')

  if (to.meta.requiresAuth) {
    // 需要登录
    if (token) {
      next()
    } else {
      next('/login')
    }
  } else {
    // 不需要登录的页面（如 login 或者 catch-all）
    if (to.path === '/login' && token) {
      next('/schedule')
    } else {
      next()
    }
  }
})

export default router


