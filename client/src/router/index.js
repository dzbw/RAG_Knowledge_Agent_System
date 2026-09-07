import { unref } from 'vue'
import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { public: true },
  },
  {
    path: '/admin',
    component: () => import('../layouts/AdminLayout.vue'),
    meta: { requiresAuth: true, role: 'ADMIN' },
    children: [
      { path: '', redirect: '/admin/dashboard' },
      { path: 'dashboard', name: 'AdminDashboard', component: () => import('../views/admin/Dashboard.vue') },
      { path: 'users', name: 'AdminUsers', component: () => import('../views/admin/UserManage.vue') },
      { path: 'categories', name: 'AdminCategories', component: () => import('../views/admin/CategoryManage.vue') },
      { path: 'documents', name: 'AdminDocuments', component: () => import('../views/admin/DocumentManage.vue') },
      { path: 'chat-test', name: 'AdminChatTest', component: () => import('../views/admin/ChatTest.vue') },
      { path: 'finance/invoice-in', name: 'AdminInvoiceIn', component: () => import('../views/admin/InvoiceInManage.vue'), meta: { title: '进项发票入账' } },
      {
        path: 'profile',
        name: 'AdminProfile',
        component: () => import('../views/user/Profile.vue'),
        meta: { title: '个人中心' },
      },
    ],
  },
  {
    path: '/user',
    component: () => import('../layouts/UserLayout.vue'),
    meta: { requiresAuth: true, role: 'USER' },
    children: [
      { path: '', redirect: '/user/chat' },
      { path: 'chat', name: 'UserChat', component: () => import('../views/user/ChatHome.vue') },
      { path: 'history', name: 'UserHistory', component: () => import('../views/user/History.vue') },
      { path: 'profile', name: 'UserProfile', component: () => import('../views/user/Profile.vue') },
    ],
  },
  { path: '/', redirect: '/login' },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to, _from, next) => {
  const store = useUserStore()
  let token = String(unref(store.token) || '').trim()
  let user = unref(store.user)
  let role = user?.role

  /** 有 token 却无 role（本地数据损坏或旧版本缓存），清掉以免 /login ↔ /user 死循环 */
  if (token && !role) {
    store.logout()
    token = ''
    user = null
    role = undefined
  }

  if (to.meta.public) {
    if (token && to.path === '/login' && role) {
      const home = role === 'ADMIN' ? '/admin/dashboard' : '/user/chat'
      return next({ path: home, replace: true })
    }
    return next()
  }

  if (to.meta.requiresAuth && !token) {
    return next('/login')
  }

  /** 路由要求 ADMIN/USER 与当前身份不一致时，只跳到「另一套」布局的根，且避免重复 next 到当前前缀下 */
  if (to.meta.role && role && role !== to.meta.role) {
    if (role === 'ADMIN') {
      if (to.path.startsWith('/admin')) {
        return next()
      }
      return next({ path: '/admin/dashboard', replace: true })
    }
    if (role === 'USER') {
      if (to.path.startsWith('/user')) {
        return next()
      }
      return next({ path: '/user/chat', replace: true })
    }
    store.logout()
    return next('/login')
  }

  next()
})

export default router
