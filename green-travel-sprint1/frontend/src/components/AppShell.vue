<script setup>
import { computed } from 'vue'
import GreenIcon from './GreenIcon.vue'

const props = defineProps({ user: { type: Object, required: true } })
defineEmits(['logout'])
const roleName = computed(() => ({ USER: '市民用户', ADMIN: '运营管理员', MERCHANT: '商户' })[props.user.role])
const navigation = computed(() => props.user.role === 'USER' ? [
  { path: '/', title: '出行概览', icon: 'home' },
  { path: '/trips', title: '我的行程', icon: 'route' },
  { path: '/wallet', title: '积分账户', icon: 'coins' },
  { path: '/catalog', title: '积分商城', icon: 'shop' },
] : [{ path: '/workspace', title: props.user.role === 'ADMIN' ? '运营工作台' : '商户工作台', icon: 'grid' }])
</script>

<template>
  <div class="app-shell">
    <aside class="sidebar">
      <RouterLink :to="user.role === 'USER' ? '/' : '/workspace'" class="brand"><span class="brand-mark"><GreenIcon name="leaf" /></span><div><strong>青行积分</strong><small>GREEN JOURNEY</small></div></RouterLink>
      <span class="nav-caption">{{ roleName }}空间</span>
      <nav aria-label="主导航"><RouterLink v-for="item in navigation" :key="item.path" :to="item.path" :exact-active-class="'active'"><GreenIcon :name="item.icon" />{{ item.title }}</RouterLink></nav>
      <div class="sidebar-note"><GreenIcon name="leaf" /><strong>记录今天的绿色选择</strong><p>公交、地铁、骑行<br>让每一段出行都可回顾</p></div>
      <div class="sidebar-footer"><span class="dot"></span> 第一轮迭代 · 教学模拟</div>
    </aside>
    <div class="main-column">
      <header class="topbar"><div class="breadcrumb">城市绿色出行 <span>/</span> {{ roleName }}平台</div><div class="account-menu"><span class="avatar">{{ user.username[0].toUpperCase() }}</span><div><strong>{{ user.username }}</strong><small>{{ roleName }}</small></div><button class="icon-button" title="退出登录" aria-label="退出登录" @click="$emit('logout')"><GreenIcon name="logout" /></button></div></header>
      <main class="page-content"><slot /></main>
      <footer class="page-footer">青行积分 · 软件工程课程实验 <span>减排量为教学模拟估算</span></footer>
    </div>
  </div>
</template>

<style scoped>
.app-shell{display:grid;grid-template-columns:235px minmax(0,1fr);min-height:100vh}.sidebar{position:sticky;top:0;height:100vh;padding:32px 22px 22px;background:#fff;border-right:1px solid var(--line);display:flex;flex-direction:column}.brand{text-decoration:none;color:var(--ink);gap:11px}.brand strong{font-size:21px;letter-spacing:1px}.brand small{display:block;font-size:9px;letter-spacing:1.8px;margin-top:5px;color:#77857e}.nav-caption{font-size:11px;color:#9aa39d;letter-spacing:1px;margin:47px 14px 14px}nav{display:grid;gap:7px}nav a{display:flex;align-items:center;gap:13px;padding:14px;border-radius:9px;text-decoration:none;color:#6b776f;font-size:14px}nav a svg{width:20px;height:20px}nav a:hover{background:#f5f8f6}nav a.active{color:var(--green);background:#eaf4ee;font-weight:700}.sidebar-note{margin-top:auto;padding:21px 13px;border-top:1px solid var(--line);color:var(--green)}.sidebar-note>svg{width:27px;height:27px;margin-bottom:12px}.sidebar-note strong{display:block;font-size:12px}.sidebar-note p{font-size:11px;line-height:1.8;color:var(--muted);margin-bottom:0}.sidebar-footer{font-size:10px;color:#8b968e;border-top:1px solid var(--line);padding-top:20px}.dot{display:inline-block;width:6px;height:6px;border-radius:50%;background:#3a946b;margin-right:5px}.main-column{min-width:0;display:flex;flex-direction:column}.topbar{height:83px;background:#fff;border-bottom:1px solid var(--line);display:flex;align-items:center;justify-content:space-between;padding:0 39px}.breadcrumb{font-size:12px;color:#66756b}.breadcrumb span{margin:0 15px;color:#c4ccc7}.account-menu{display:flex;gap:12px;align-items:center}.avatar{display:grid;place-items:center;border-radius:50%;width:36px;height:36px;background:#e5efe8;color:var(--green);font-size:12px;font-weight:700}.account-menu strong{font-size:12px;display:block}.account-menu small{font-size:10px;color:var(--muted)}.account-menu button{margin-left:12px}.page-content{padding:35px 39px;max-width:1500px;width:100%;margin:0 auto;flex:1}.page-footer{padding:0 39px 25px;font-size:10px;color:#8b968f;display:flex;justify-content:space-between}@media(max-width:1000px){.app-shell{grid-template-columns:190px 1fr}.sidebar{padding:24px 14px}.page-content{padding:25px}.topbar{padding:0 25px}.page-footer{padding-left:25px;padding-right:25px}}@media(max-width:720px){.app-shell{display:block}.sidebar{height:auto;position:relative;padding:16px;border-right:0;border-bottom:1px solid var(--line)}.sidebar .brand{margin-bottom:17px}.nav-caption,.sidebar-note,.sidebar-footer{display:none}nav{display:flex;overflow-x:auto;gap:5px}nav a{white-space:nowrap;padding:10px;font-size:12px}nav a svg{width:16px;height:16px}.topbar{height:68px;padding:0 18px}.breadcrumb{font-size:11px}.account-menu small{display:none}.page-content{padding:24px 16px}.page-footer{padding:0 16px 20px;line-height:1.6;gap:20px}}
</style>
