<script setup>
import { computed, reactive, ref } from 'vue'
import { api } from '../api/client.js'
import { estimate, localInput, validDistance } from '../utils/format.js'
import GreenIcon from './GreenIcon.vue'

const props = defineProps({ userId: { type: Number, required: true }, rule: Object })
const emit = defineEmits(['reported'])
const key = `green-travel.pending.${props.userId}`
let restored = null
try { restored = JSON.parse(sessionStorage.getItem(key) || 'null') } catch { /* 存储不可用时仍保留内存中的重试编号 */ }
const form = reactive({ mode: restored?.mode || 'BUS', distance: restored?.distanceKm || '10.00', completedAt: restored?.completedAt ? localInput(new Date(restored.completedAt)) : localInput() })
const pending = ref(restored)
const busy = ref(false)
const error = ref('')
const success = ref('')
const preview = computed(() => estimate(form.distance, form.mode, props.rule))
function remember(payload) { try { payload ? sessionStorage.setItem(key, JSON.stringify(payload)) : sessionStorage.removeItem(key) } catch { /* 内存中的 pending 仍有效 */ } }
async function submit() {
  error.value = ''; success.value = ''
  if (!pending.value) {
    if (!validDistance(form.distance)) { error.value = '里程应为 0.01—100.00 公里，最多两位小数'; return }
    const completed = new Date(form.completedAt)
    if (!form.completedAt || !Number.isFinite(completed.getTime()) || completed > new Date()) { error.value = '请选择有效且不晚于当前时间的完成时间'; return }
    pending.value = { clientTripId: crypto.randomUUID(), mode: form.mode, distanceKm: form.distance, completedAt: completed.toISOString() }
    remember(pending.value)
  }
  busy.value = true
  try {
    const result = await api.report(pending.value)
    success.value = result.duplicate ? `已找到原行程，奖励 ${result.trip.earnedPoints} 分，未重复发放。` : `行程记录成功，获得 ${result.trip.earnedPoints} 积分。`
    pending.value = null; remember(null); form.completedAt = localInput()
    emit('reported', result)
  } catch (e) {
    error.value = e.message
    if (e.status >= 400 && e.status < 500) { pending.value = null; remember(null) }
  } finally { busy.value = false }
}
</script>

<template>
  <form class="trip-form" @submit.prevent="submit">
    <div class="section-heading"><div><span class="eyebrow green">记录一段新行程</span><h2>绿色出行上报</h2></div><span class="pill">模拟采集</span></div>
    <p v-if="error" class="notice error" role="alert">{{ error }}</p><p v-if="success" class="notice success" role="status">{{ success }}</p>
    <fieldset :disabled="busy || !!pending" class="mode-fieldset"><legend>选择出行方式</legend><div class="mode-options">
      <label v-for="item in [{ id: 'BUS', label: '公交' }, { id: 'METRO', label: '地铁' }, { id: 'BIKE', label: '骑行' }]" :key="item.id" :class="['mode-option', { selected: form.mode === item.id }]">
        <input v-model="form.mode" type="radio" name="mode" :value="item.id" /><GreenIcon :name="item.id.toLowerCase()" /><span>{{ item.label }}</span>
      </label>
    </div></fieldset>
    <div class="form-columns"><div><label for="distance">行程里程 <span class="muted">公里</span></label><input id="distance" v-model="form.distance" inputmode="decimal" placeholder="例如 10.00" :disabled="busy || !!pending" /></div><div><label for="completed">完成时间</label><input id="completed" v-model="form.completedAt" type="datetime-local" :disabled="busy || !!pending" /></div></div>
    <div class="estimate"><span>本次预计奖励</span><strong>{{ preview ? preview.points : '—' }} <small>积分</small></strong><span>模拟减排 {{ preview ? preview.carbon.toFixed(3) : '—' }} kgCO2e</span></div>
    <p class="form-note">按 {{ rule?.versionName || '当前规则' }} 估算，实际奖励以系统保存结果为准。里程支持 0.01—100.00 公里。</p>
    <p v-if="pending && !busy" class="notice">本次结果尚未确认，已保留原编号。重试会查询或受理同一行程，不重复发分。</p>
    <button class="button primary full" :disabled="busy || !rule"><GreenIcon name="plus" />{{ busy ? '正在确认行程…' : pending ? '使用原编号重试' : '提交行程并领取积分' }}</button>
  </form>
</template>
