 <template>
   <div class="api-docs-container">
     <div class="api-docs-header">
       <div class="header-info">
         <h2>API 文档</h2>
         <p>BookVerse 在线书店 — 后台管理系统 API 接口文档（通过 Swagger UI 自动生成）</p>
       </div>
       <div class="header-actions">
         <n-select
           :value="selectedGroup"
           :options="groupOptions"
           size="small"
           style="width: 200px"
           @update:value="handleGroupChange"
         />
         <n-button size="small" @click="refreshIframe">
           <template #icon><n-icon><RefreshOutline /></n-icon></template>
           刷新文档
         </n-button>
         <n-button size="small" type="primary" @click="openInNewTab">
           <template #icon><n-icon><OpenOutline /></n-icon></template>
           新窗口打开
         </n-button>
       </div>
     </div>
     <div class="api-docs-frame-wrapper">
       <n-spin :show="loading">
         <iframe
           ref="iframeRef"
           :src="swaggerUrl"
           class="api-docs-frame"
           frameborder="0"
           @load="onFrameLoad"
         />
       </n-spin>
     </div>
   </div>
 </template>

 <script setup lang="ts">
 import { ref, computed } from 'vue'
 import { NButton, NIcon, NSelect, NSpin } from 'naive-ui'
 import { RefreshOutline, OpenOutline } from '@vicons/ionicons5'

 const iframeRef = ref<HTMLIFrameElement | null>(null)
 const loading = ref(true)
 const selectedGroup = ref('all')

 const gatewayUrl = computed(() => {
   const { protocol, hostname } = window.location
   // Gateway 在端口 8080，后端 API 文档通过网关访问
   return `${protocol}//${hostname}:8080`
 })

 const groupOptions = [
   { label: '全部接口', value: 'all' },
   { label: '管理后台 API', value: '管理后台API' },
   { label: '服务代理 API', value: '服务代理' },
 ]

 const swaggerUrl = computed(() => {
   if (selectedGroup.value === 'all') {
     return `${gatewayUrl.value}/swagger-ui.html`
   }
   return `${gatewayUrl.value}/swagger-ui.html?group=${encodeURIComponent(selectedGroup.value)}`
 })

 const refreshIframe = () => {
   if (iframeRef.value) {
     loading.value = true
     iframeRef.value.src = swaggerUrl.value
   }
 }

 const openInNewTab = () => {
   window.open(swaggerUrl.value, '_blank')
 }

 const onFrameLoad = () => {
   loading.value = false
 }

 const handleGroupChange = (value: string) => {
   selectedGroup.value = value
   refreshIframe()
 }
 </script>

 <style scoped>
 .api-docs-container {
   height: calc(100vh - 128px);
   display: flex;
   flex-direction: column;
 }

 .api-docs-header {
   display: flex;
   align-items: center;
   justify-content: space-between;
   padding: 16px 24px;
   background: rgba(10, 14, 26, 0.8);
   border-radius: 12px;
   margin-bottom: 16px;
   border: 1px solid rgba(99, 102, 241, 0.15);
 }

 .header-info h2 {
   margin: 0 0 4px 0;
   font-size: 20px;
   color: #f1f5f9;
 }

 .header-info p {
   margin: 0;
   font-size: 13px;
   color: #64748b;
 }

 .header-actions {
   display: flex;
   align-items: center;
   gap: 12px;
 }

 .api-docs-frame-wrapper {
   flex: 1;
   position: relative;
   border-radius: 12px;
   overflow: hidden;
   border: 1px solid rgba(99, 102, 241, 0.15);
   background: #fff;
 }

 .api-docs-frame {
   width: 100%;
   height: 100%;
   border: none;
 }
 </style>
