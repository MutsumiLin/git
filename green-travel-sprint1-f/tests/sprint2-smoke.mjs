import assert from 'node:assert/strict'
import {randomUUID} from 'node:crypto'
import {readFile,writeFile,mkdir} from 'node:fs/promises'
import {dirname} from 'node:path'
const args=process.argv.slice(2),opt=k=>{const i=args.indexOf(k);return i<0?null:args[i+1]}
const base=(opt('--base-url')||'http://127.0.0.1:8080').replace(/\/$/,''),password=process.env.DEMO_PASSWORD||'GreenDemo!2026',results=[]
class Client{
 cookies=new Map();csrf=null
 async request(path,{method='GET',body,form,status=200}={}){
  const headers={Accept:'application/json'}
  if(this.cookies.size)headers.Cookie=[...this.cookies].map(([k,v])=>`${k}=${v}`).join('; ')
  if(method!=='GET'&&this.csrf)headers[this.csrf.headerName]=this.csrf.token
  if(body!==undefined)headers['Content-Type']='application/json'
  if(form)headers['Content-Type']='application/x-www-form-urlencoded'
  const r=await fetch(base+path,{method,headers,body:form?new URLSearchParams(form):body===undefined?undefined:JSON.stringify(body),signal:AbortSignal.timeout(15000)})
  for(const c of r.headers.getSetCookie()){const pair=c.split(';')[0],i=pair.indexOf('=');this.cookies.set(pair.slice(0,i),pair.slice(i+1))}
  const json=await r.json();assert.equal(r.status,status,`${path}: ${json.message}`);return json.data
 }
 async login(name){this.csrf=await this.request('/api/auth/csrf');await this.request('/api/auth/login',{method:'POST',form:{username:name,password}});this.csrf=await this.request('/api/auth/csrf')}
}
async function check(name,fn){await fn();results.push({name,result:'PASS'});console.log('PASS '+name)}
async function main(){
 const user=new Client();await user.login('citizen')
 const restart=opt('--verify-restart')
 if(restart){const prior=JSON.parse(await readFile(restart,'utf8'));await check('重启保留余额订单库存和核销状态',async()=>{assert.equal((await user.request('/api/me/points')).balance,prior.balance);assert.equal((await user.request(`/api/me/redemptions/${prior.orderId}`)).status,'REDEEMED');assert.equal((await user.request(`/api/products/${prior.productId}`)).stock,prior.stock)});await check('重启后原兑换请求仍只返回原订单',async()=>{const r=await user.request('/api/me/redemptions',{method:'POST',body:prior.payload});assert.equal(r.duplicate,true);assert.equal(r.order.id,prior.orderId);assert.equal((await user.request('/api/me/points')).balance,prior.balance)});return{results,source:restart}}
 const other=new Client(),merchant=new Client(),otherMerchant=new Client(),admin=new Client()
 await Promise.all([other.login('citizen2'),merchant.login('merchant'),otherMerchant.login('merchant2'),admin.login('operator')])
 const before=(await user.request('/api/me/points')).balance
 await check('行程奖励为后续兑换提供真实积分',async()=>{await user.request('/api/trips',{method:'POST',body:{clientTripId:randomUUID(),mode:'BIKE',distanceKm:'100.00',completedAt:new Date(Date.now()-300000).toISOString()},status:201});assert.equal((await user.request('/api/me/points')).balance,before+2000)})
 const product=(await user.request('/api/products')).items.find(p=>p.sku==='ECO-BAG');assert.ok(product);assert.ok(product.stock>=2)
 const payload={clientRequestId:randomUUID(),productId:product.id,quantity:2,expectedPointsPrice:product.pointsPrice}
 let order
 await check('创建兑换同时扣分扣库存',async()=>{order=(await user.request('/api/me/redemptions',{method:'POST',body:payload,status:201})).order;assert.equal((await user.request('/api/me/points')).balance,before+2000-product.pointsPrice*2);assert.equal((await user.request(`/api/products/${product.id}`)).stock,product.stock-2)})
 await check('重复兑换不再次扣分且内容冲突409',async()=>{const r=await user.request('/api/me/redemptions',{method:'POST',body:payload});assert.equal(r.duplicate,true);await user.request('/api/me/redemptions',{method:'POST',body:{...payload,quantity:1},status:409})})
 await check('本人和本店范围由会话决定',async()=>{await other.request(`/api/me/redemptions/${order.id}`,{status:404});await otherMerchant.request('/api/merchant/redemptions/verify',{method:'POST',body:{verifyCode:order.verifyCode},status:404});await user.request('/api/admin/products',{status:403})})
 await check('重复取消只退一次积分恢复一次库存',async()=>{for(let i=0;i<2;i++)await user.request(`/api/me/redemptions/${order.id}/cancel`,{method:'POST'});assert.equal((await user.request('/api/me/points')).balance,before+2000);assert.equal((await user.request(`/api/products/${product.id}`)).stock,product.stock)})
 const second={...payload,clientRequestId:randomUUID(),quantity:1};order=(await user.request('/api/me/redemptions',{method:'POST',body:second,status:201})).order
 await check('商户重复核销不再次扣分且已核销不能取消',async()=>{for(let i=0;i<2;i++)await merchant.request('/api/merchant/redemptions/verify',{method:'POST',body:{verifyCode:order.verifyCode}});await user.request(`/api/me/redemptions/${order.id}/cancel`,{method:'POST',status:409});assert.equal((await user.request('/api/me/points')).balance,before+2000-product.pointsPrice)})
 await check('账本包含消费和退款',async()=>{const ledger=(await user.request('/api/me/point-ledger?size=100')).items;assert.ok(ledger.some(l=>l.bizType==='REDEMPTION_DEBIT'&&l.bizId===order.id));assert.ok(ledger.some(l=>l.bizType==='REDEMPTION_REFUND'))})
 await check('运营新规则发布后历史奖励保持',async()=>{const old=(await user.request('/api/me/trips')).items[0];await admin.request('/api/admin/carbon-rules',{method:'POST',body:{versionName:'smoke-'+Date.now(),carFactor:'0.2',busFactor:'0.07',metroFactor:'0.05',bikeFactor:'0',pointsPerKg:100}});assert.equal((await user.request(`/api/me/trips/${old.id}`)).earnedPoints,old.earnedPoints)})
 await check('未交付的清算接口不返回成功',async()=>{await admin.request('/api/admin/settlements',{status:404})})
 return {results,payload:second,orderId:order.id,productId:product.id,balance:(await user.request('/api/me/points')).balance,stock:(await user.request(`/api/products/${product.id}`)).stock}
}
try{const result=await main();if(opt('--output')){await mkdir(dirname(opt('--output')),{recursive:true});await writeFile(opt('--output'),JSON.stringify(result,null,2)+'\n')}console.log(`${results.length} 项第二轮HTTP验证通过`)}catch(e){console.error('FAIL '+e.message);process.exitCode=1}
