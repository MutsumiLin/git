<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { api } from '../api/client.js'
import { auth } from '../stores/auth.js'
import GreenIcon from '../components/GreenIcon.vue'
const router = useRouter(), items=ref([]), selected=ref(null), error=ref(''),busy=ref(false),quantity=ref(1),page=ref(0),total=ref(0),message=ref('')
const key=`green-redemption-pending-${auth.user.id}`
let saved=null;try{saved=JSON.parse(localStorage.getItem(key)||'null')}catch{localStorage.removeItem(key)}
const pending=ref(saved)
async function load(next=page.value){busy.value=true;error.value='';try{const r=await api.products(next);items.value=r.items;total.value=r.total;page.value=next}catch(e){error.value=e.message}finally{busy.value=false}}
async function detail(id){error.value='';try{selected.value=await api.product(id);quantity.value=1}catch(e){error.value=e.message}}
async function redeem(){
 if(busy.value)return
 if(!pending.value){
  if(!selected.value || !Number.isInteger(quantity.value)||quantity.value<1||quantity.value>10){error.value='数量须为 1 至 10 的整数';return}
  pending.value={clientRequestId:crypto.randomUUID(),productId:selected.value.id,quantity:quantity.value,expectedPointsPrice:selected.value.pointsPrice}
  try{localStorage.setItem(key,JSON.stringify(pending.value))}catch{pending.value=null;error.value='浏览器无法保存请求编号，请允许本地存储后重试';return}
 }
 busy.value=true;error.value=''
 try{const r=await api.redeem(pending.value);pending.value=null;localStorage.removeItem(key);selected.value=null;message.value=`兑换成功，订单 #${r.order.id}，请在我的兑换中查看核销码`;await load()}
 catch(e){error.value=e.message;if(e.status>=400&&e.status<500){pending.value=null;localStorage.removeItem(key)}}
 finally{busy.value=false}
}
onMounted(()=>load())
</script>
<template>
 <div class="page-title"><div><span class="eyebrow">绿色好物</span><h1>积分商城</h1><p>把绿色出行的积累，兑换成生活里的小改变。</p></div><button class="button secondary" @click="router.push('/orders')">我的兑换</button></div>
 <p v-if="message" class="notice success" role="status">{{message}}</p><p v-if="error" class="notice error" role="alert">{{error}}</p>
 <div v-if="pending" class="notice">有一笔兑换结果尚未确认。请保留原请求编号重试。<button class="button secondary" :disabled="busy" @click="redeem">查询并重试原兑换</button></div>
 <div v-if="!items.length" class="card empty-state">{{busy?'正在加载…':'暂无上架商品'}}</div>
 <section class="product-grid"><article v-for="(item,index) in items" :key="item.id" class="product-card"><div :class="['product-art',`art-${index%3}`]"><GreenIcon :name="item.sku.includes('BIKE')?'bike':item.sku.includes('CUP')?'cup':'bag'"/><span>{{item.category}}</span></div><div class="product-content"><span class="muted small">{{item.merchantName}}</span><h2>{{item.name}}</h2><p>{{item.description}}</p><div class="product-price"><strong>{{item.pointsPrice}}<small> 积分</small></strong><span class="muted small">库存 {{item.stock}}</span></div><button class="button secondary full" :disabled="busy||!!pending" @click="detail(item.id)">查看与兑换</button></div></article></section>
 <div class="pagination"><button class="button secondary" :disabled="busy||page===0" @click="load(page-1)">上一页</button><span>第 {{page+1}} 页</span><button class="button secondary" :disabled="busy||(page+1)*20>=total" @click="load(page+1)">下一页</button></div>
 <div v-if="selected" class="modal-backdrop" @click.self="!busy&&(selected=null)"><section class="modal" role="dialog" aria-modal="true" aria-labelledby="product-title"><button class="modal-close text-button" :disabled="busy" aria-label="关闭" @click="selected=null">×</button><span class="eyebrow green">{{selected.category}}</span><h2 id="product-title">{{selected.name}}</h2><p>{{selected.description}}</p><dl class="detail-list"><div><dt>提供商户</dt><dd>{{selected.merchantName}}</dd></div><div><dt>单件所需积分</dt><dd>{{selected.pointsPrice}} 分</dd></div><div><dt>当前库存</dt><dd>{{selected.stock}}</dd></div></dl><label for="qty">兑换数量</label><input id="qty" v-model.number="quantity" type="number" min="1" :max="Math.min(10,selected.stock)" :disabled="busy||!!pending"/><p>合计 {{selected.pointsPrice*quantity}} 积分</p><p v-if="error" class="notice error" role="alert">{{error}}</p><button class="button primary full" :disabled="busy||selected.stock===0" @click="redeem">{{busy?'正在确认…':pending?'重试原兑换':'确认兑换'}}</button></section></div>
</template>
