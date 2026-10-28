<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { auth, homeFor, signIn } from '../stores/auth.js'
import GreenIcon from '../components/GreenIcon.vue'

const router = useRouter()
const username = ref('citizen')
const password = ref('')
const busy = ref(false)
const error = ref('')
async function submit() {
  if (!username.value.trim() || !password.value) { error.value = '请输入账号和密码'; return }
  busy.value = true; error.value = ''
  try { const user = await signIn(username.value.trim(), password.value); await router.replace(homeFor(user)) }
  catch (e) { error.value = e.message }
  finally { busy.value = false }
}
</script>

<template>
  <main class="login-page">
    <section class="login-story">
      <div class="brand"><span class="brand-mark"><GreenIcon name="leaf" /></span><strong>青行积分</strong></div>
      <span class="eyebrow">城市绿色出行计划</span>
      <h1>每一段绿色行程<br>都有可见的积累。</h1>
      <p>记录公交、地铁与骑行行程，查看模拟减排量，积累属于自己的绿色积分。</p>
      <div class="journey-art" aria-hidden="true"><span>BUS</span><i></i><span>METRO</span><i></i><span>BIKE</span></div>
      <small>软件工程课程实验 · 第一轮迭代 · 教学模拟数据</small>
    </section>
    <section class="login-panel">
      <form class="login-form" @submit.prevent="submit">
        <span class="eyebrow green">欢迎回来</span><h2>登录青行</h2><p class="muted">使用小组提供的体验账号进入平台</p>
        <p v-if="error || auth.connectionError" class="notice error" role="alert">{{ error || auth.connectionError }}</p>
        <label for="username">账号</label><input id="username" v-model="username" autocomplete="username" maxlength="40" :disabled="busy" />
        <label for="password">密码</label><input id="password" v-model="password" type="password" autocomplete="current-password" :disabled="busy" />
        <button class="button primary login-submit" :disabled="busy">{{ busy ? '正在登录…' : '登录平台' }}<GreenIcon name="arrow" /></button>
        <div class="login-help"><strong>角色体验账号</strong><p>市民 citizen · 运营 operator · 商户 merchant</p><p>初始演示密码：GreenDemo!2026</p><small>仅在启用演示数据的环境中有效。</small></div>
      </form>
    </section>
  </main>
</template>
