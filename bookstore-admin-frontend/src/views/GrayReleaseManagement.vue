 <template>
   <div class="gray-release-page">
     <n-space vertical size="large">
       <n-card title="灰度发布管理">
         <template #header-extra>
           <n-space>
             <n-button type="primary" size="small" @click="fetchRules">
               <template #icon><n-icon><RefreshOutline /></n-icon></template>
               刷新
             </n-button>
             <n-button size="small" @click="refreshGrayConfig">
               <template #icon><n-icon><SyncOutline /></n-icon></template>
               触发刷新
             </n-button>
           </n-space>
         </template>
         <n-alert type="info" :bordered="false" style="margin-bottom: 16px">
           灰度规则通过 Nacos 配置中心管理（<code>bookstore.gray-release</code>），
           修改 Nacos 中的配置后点击「触发刷新」即可生效，无需重启服务。
         </n-alert>
         <n-empty v-if="!loading && rules.length === 0" description="暂无灰度规则" />
         <n-table v-else :bordered="true" :single-line="false">
           <thead>
             <tr>
               <th>规则名称</th>
               <th>目标服务</th>
               <th>灰度版本</th>
               <th>灰度比例</th>
               <th>白名单用户</th>
               <th>灰度标签</th>
             </tr>
           </thead>
           <tbody>
             <tr v-for="rule in rules" :key="rule.name">
               <td><n-tag type="warning" size="small">{{ rule.name }}</n-tag></td>
               <td><n-tag type="info" size="small">{{ rule.serviceName }}</n-tag></td>
               <td><n-tag type="success" size="small">{{ rule.version }}</n-tag></td>
               <td>{{ rule.percentage }}%</td>
               <td>
                 <n-space>
                   <n-tag v-for="uid in rule.whitelistUserIds.slice(0, 5)" :key="uid" size="tiny" round>
                     {{ uid }}
                   </n-tag>
                   <n-tag v-if="rule.whitelistUserIds.length > 5" size="tiny" round>
                     +{{ rule.whitelistUserIds.length - 5 }}
                   </n-tag>
                   <span v-if="rule.whitelistUserIds.length === 0" class="text-muted">—</span>
                 </n-space>
               </td>
               <td>
                 <n-tag v-for="tag in rule.grayTags" :key="tag" size="small" round>{{ tag }}</n-tag>
                 <span v-if="rule.grayTags.length === 0" class="text-muted">—</span>
               </td>
             </tr>
           </tbody>
         </n-table>
       </n-card>

       <n-card title="配置示例（Nacos 配置中心）">
         <n-code :code="nacosConfigExample" language="yaml" />
       </n-card>
     </n-space>
   </div>
 </template>

 <script setup lang="ts">
 import { ref, onMounted } from 'vue'
 import { NButton, NCard, NTag, NTable, NSpace, NEmpty, NAlert, NCode, NIcon, useMessage } from 'naive-ui'
 import { RefreshOutline, SyncOutline } from '@vicons/ionicons5'
 import axios from 'axios'

 const message = useMessage()
 const loading = ref(false)
 const rules = ref<any[]>([])

 const nacosConfigExample = `bookstore:
   gray-release:
     enabled: true
     rules:
       - name: "product-new-feature"
         service-name: "bookstore-product"
         version: "v2"
         percentage: 10
         whitelist-user-ids: ["101", "102"]
         gray-tags: ["beta", "canary"]

       - name: "order-new-pipeline"
         service-name: "bookstore-order"
         version: "v2"
         percentage: 5`

 const apiBase = import.meta.env.VITE_API_GATEWAY_URL || 'http://localhost:8080'

 const fetchRules = async () => {
   loading.value = true
   try {
     const resp = await axios.get(`${apiBase}/admin/api/gray-release`)
     rules.value = resp.data?.data || []
     message.success('灰度规则已刷新')
   } catch (e: any) {
     message.error('获取灰度规则失败: ' + (e.message || ''))
   } finally {
     loading.value = false
   }
 }

 const refreshGrayConfig = async () => {
   try {
     await axios.post(`${apiBase}/admin/api/gray-release/refresh`)
     message.success('已触发 Nacos 灰度配置刷新')
   } catch (e: any) {
     message.error('触发刷新失败: ' + (e.message || ''))
   }
 }

 onMounted(fetchRules)
 </script>

 <style scoped>
 .gray-release-page {
   max-width: 1200px;
 }
 .text-muted {
   color: #64748b;
   font-size: 13px;
 }
 </style>
