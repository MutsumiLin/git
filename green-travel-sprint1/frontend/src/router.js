import { createRouter, createWebHistory } from 'vue-router'
import { auth, ensureSession, homeFor } from './stores/auth.js'
import LoginView from './views/LoginView.vue'
import DashboardView from './views/DashboardView.vue'
import TripsView from './views/TripsView.vue'
import WalletView from './views/WalletView.vue'
import CatalogView from './views/CatalogView.vue'
import WorkspaceView from './views/WorkspaceView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', component: LoginView, meta: { public: true } },
    { path: '/', component: DashboardView, meta: { role: 'USER' } },
    { path: '/trips', component: TripsView, meta: { role: 'USER' } },
    { path: '/wallet', component: WalletView, meta: { role: 'USER' } },
    { path: '/catalog', component: CatalogView, meta: { role: 'USER' } },
    { path: '/workspace', component: WorkspaceView, meta: { workspace: true } },
    { path: '/:pathMatch(.*)*', redirect: '/' },
  ],
})

router.beforeEach(async to => {
  await ensureSession()
  if (!auth.user) return to.meta.public ? true : '/login'
  if (to.path === '/login') return homeFor(auth.user)
  if (to.meta.role && auth.user.role !== to.meta.role) return '/workspace'
  if (to.meta.workspace && auth.user.role === 'USER') return '/'
  return true
})
export default router
