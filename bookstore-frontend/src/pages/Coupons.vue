<template>
  <DefaultLayout>
    <div class="page-container">
      <h1 class="section-title">优惠券</h1>

      <n-card>
        <n-tabs v-model:value="activeTab" type="line" @update:value="handleTabChange">
          <n-tab-pane name="available" tab="可领取优惠券">
            <n-spin :show="availableLoading">
              <div v-if="availableCoupons.length > 0" class="coupons-grid">
                <n-card
                  v-for="coupon in availableCoupons"
                  :key="coupon.id"
                  class="coupon-card"
                  size="small"
                >
                  <div class="coupon-value">
                    <span class="coupon-amount">${{ coupon.discount }}</span>
                    <span class="coupon-condition">Min. ${{ coupon.minamount || coupon.minAmount }}</span>
                  </div>
                  <div class="coupon-info">
                    <h3>{{ coupon.couponname || coupon.name }}</h3>
                    <p class="coupon-period">{{ formatDate(coupon.start_time || coupon.startTime || '') }} - {{ formatDate(coupon.end_time || coupon.endTime || '') }}</p>
                    <p class="coupon-remaining">剩余 {{ (coupon.total_count || coupon.totalCount || 0) - (coupon.claimed_count || coupon.claimedCount || 0) }} 张</p>
                  </div>
                  <n-button
                    type="primary"
                    size="small"
                    :loading="claimingId === (coupon.couponid || coupon.id)"
                    :disabled="coupon.claimed"
                    @click="handleClaim(coupon)"
                  >
                    {{ coupon.claimed ? '已领取' : '领取' }}
                  </n-button>
                </n-card>
              </div>
              <n-empty v-else-if="!availableLoading" description="暂无可领取的优惠券" />
            </n-spin>

            <!-- Pagination -->
            <div v-if="availableTotal > availablePageSize" class="pagination-wrap">
              <n-pagination
                v-model:page="availablePageNum"
                :page-count="Math.ceil(availableTotal / availablePageSize)"
                @update:page="fetchAvailableCoupons"
              />
            </div>
          </n-tab-pane>

          <n-tab-pane name="mine" tab="我的优惠券">
            <n-spin :show="myLoading">
              <div v-if="myCoupons.length > 0" class="coupons-grid">
                <n-card
                  v-for="coupon in myCoupons"
                  :key="coupon.id"
                  class="coupon-card my-coupon"
                  size="small"
                >
                  <div class="coupon-value">
                    <span class="coupon-amount">${{ coupon.discount }}</span>
                    <span class="coupon-condition">Min. ${{ coupon.minamount || coupon.minAmount }}</span>
                  </div>
                  <div class="coupon-info">
                    <h3>{{ coupon.couponname || coupon.name }}</h3>
                    <p class="coupon-period">{{ formatDate(coupon.start_time || coupon.startTime || '') }} - {{ formatDate(coupon.end_time || coupon.endTime || '') }}</p>
                    <n-tag
                      :type="isCouponValid(coupon) ? 'success' : 'error'"
                      size="tiny"
                    >
                      {{ isCouponValid(coupon) ? '有效' : '已过期' }}
                    </n-tag>
                  </div>
                  <n-button
                    type="primary"
                    size="small"
                    :disabled="!isCouponValid(coupon)"
                    @click="$router.push('/products')"
                  >
                    去购物
                  </n-button>
                </n-card>
              </div>
              <n-empty v-else-if="!myLoading" description="您还没有领取过优惠券" />
            </n-spin>
          </n-tab-pane>
        </n-tabs>
      </n-card>
    </div>
  </DefaultLayout>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import DefaultLayout from '@/layouts/DefaultLayout.vue'
import type { CouponVO } from '@/types'
import { getCouponList, claimCoupon, getMyCoupons } from '@/api/coupon'

const activeTab = ref('available')

// Available coupons
const availableCoupons = ref<CouponVO[]>([])
const availableLoading = ref(false)
const availablePageNum = ref(1)
const availablePageSize = ref(10)
const availableTotal = ref(0)
const claimingId = ref<number | null>(null)

// My coupons
const myCoupons = ref<CouponVO[]>([])
const myLoading = ref(false)

function formatDate(dateStr: string) {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleDateString()
}

function isCouponValid(coupon: CouponVO): boolean {
  return new Date(coupon.endTime || coupon.end_time || '') > new Date()
}

function handleTabChange(tab: string) {
  if (tab === 'available') {
    fetchAvailableCoupons()
  } else {
    fetchMyCoupons()
  }
}

async function fetchAvailableCoupons() {
  availableLoading.value = true
  try {
    const res = await getCouponList(availablePageNum.value, availablePageSize.value)
    availableCoupons.value = res.records || res.list || []
    availableTotal.value = res.total || 0
  } catch {
    availableCoupons.value = []
    availableTotal.value = 0
  } finally {
    availableLoading.value = false
  }
}

async function fetchMyCoupons() {
  myLoading.value = true
  try {
    myCoupons.value = await getMyCoupons() || []
  } catch {
    myCoupons.value = []
  } finally {
    myLoading.value = false
  }
}

async function handleClaim(coupon: CouponVO) {
  claimingId.value = Number(coupon.couponid || coupon.id || 0)
  try {
    await claimCoupon((coupon.couponid || coupon.id) as any)
    coupon.claimed = true
    window.$message?.success('优惠券领取成功！')
  } catch {
    // handled
  } finally {
    claimingId.value = null
  }
}

onMounted(() => {
  fetchAvailableCoupons()
})
</script>

<style scoped>
.coupons-grid {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 16px;
}

.coupon-card {
  display: flex;
  align-items: center;
}

.coupon-card :deep(.n-card__content) {
  display: flex;
  align-items: center;
  gap: 20px;
  width: 100%;
}

.coupon-value {
  text-align: center;
  min-width: 100px;
  padding: 8px 16px;
  background: linear-gradient(135deg, #18a058 0%, #2080f0 100%);
  border-radius: 8px;
  color: white;
}

.my-coupon .coupon-value {
  background: linear-gradient(135deg, #f0a020 0%, #e8803f 100%);
}

.coupon-amount {
  display: block;
  font-size: 1.8rem;
  font-weight: 700;
}

.coupon-condition {
  font-size: 0.8rem;
  opacity: 0.9;
}

.coupon-info {
  flex: 1;
}

.coupon-info h3 {
  font-size: 1rem;
  margin-bottom: 4px;
}

.coupon-period {
  color: #999;
  font-size: 0.85rem;
  margin-bottom: 4px;
}

.coupon-remaining {
  color: #666;
  font-size: 0.85rem;
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}
</style>
