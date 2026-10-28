<script setup>
import { onMounted, onUnmounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { auth, signOut } from './stores/auth.js'
import AppShell from './components/AppShell.vue'

const router = useRouter()
const message = ref('')
async function logout() {
  try { await signOut(); await router.replace('/login') }
  catch (error) { message.value = error.message }
}
function expired() {
  auth.user = null
  if (router.currentRoute.value.path !== '/login') router.replace('/login')
}
onMounted(() => window.addEventListener('session-expired', expired))
onUnmounted(() => window.removeEventListener('session-expired', expired))
</script>

<template>
  <AppShell v-if="auth.user" :user="auth.user" @logout="logout">
    <p v-if="message" class="notice error" role="alert">{{ message }} <button class="text-button" @click="message = ''">关闭</button></p>
    <RouterView :key="auth.user.id + $route.path" />
  </AppShell>
  <RouterView v-else />
</template>
