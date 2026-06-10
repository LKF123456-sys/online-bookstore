<template>
  <DefaultLayout>
    <div class="page-container">
      <h1 class="section-title">结算</h1>

      <n-spin :show="loading">
        <n-grid :x-gap="24" :cols="3" responsive="screen" item-responsive>
          <!-- Order Form -->
          <n-gi span="3 m:2">
            <n-space vertical :size="16">
              <!-- Shipping Address -->
              <n-card title="收货地址">
                <n-form
                  ref="shippingFormRef"
                  :model="shippingForm"
                  :rules="addressRules"
                  label-placement="left"
                  label-width="100px"
                >
                  <n-form-item label="收件人" path="name">
                    <n-input v-model:value="shippingForm.name" placeholder="收件人姓名" />
                  </n-form-item>
                  <n-form-item label="联系电话" path="phone">
                    <n-input v-model:value="shippingForm.phone" placeholder="联系电话" />
                  </n-form-item>
                  <n-form-item label="详细地址" path="address">
                    <n-input v-model:value="shippingForm.address" type="textarea" :rows="2" placeholder="详细收货地址" />
                  </n-form-item>
                </n-form>
              </n-card>

              <!-- Coupon -->
              <n-card title="优惠券">
                <n-space align="center" :size="12">
                  <n-select
                    v-model:value="selectedCouponId"
                    :options="couponOptions"
                    placeholder="选择优惠券（可选）"
                    clearable
                    style="width: 300px"
                  />
                  <span v-if="selectedCoupon" class="discount-info">
                    省 ${{ discountAmount.toFixed(2) }}
                  </span>
                </n-space>
              </n-card>

              <!-- Order Items -->
              <n-card title="订单商品">
                <n-data-table
                  :columns="itemColumns"
                  :data="cartStore.items"
                  :bordered="false"
                  size="small"
                />
              </n-card>
            </n-space>
          </n-gi>

          <!-- Order Summary -->
          <n-gi span="3 m:1">
            <n-card title="订单汇总" class="summary-card">
              <n-space vertical :size="12">
                <div class="summary-line">
                  <span>商品 ({{ cartStore.itemCount }} 件)：</span>
                  <span>${{ cartStore.totalPrice.toFixed(2) }}</span>
                </div>
                <div class="summary-line">
                  <span>运费：</span>
                  <span style="color: #18a058">免运费</span>
                </div>
                <div v-if="selectedCoupon" class="summary-line discount">
                  <span>优惠：</span>
                  <span>-${{ discountAmount.toFixed(2) }}</span>
                </div>
                <n-divider />
                <div class="summary-line total">
                  <span>合计：</span>
                  <span>${{ totalAmount.toFixed(2) }}</span>
                </div>
                <n-button
                  type="primary"
                  block
                  size="large"
                  :loading="submitting"
                  @click="handleSubmitOrder"
                >
                  提交订单
                </n-button>
              </n-space>
            </n-card>
          </n-gi>
        </n-grid>
      </n-spin>
    </div>
  </DefaultLayout>
</template>

<script setup lang="ts">
import { ref, reactive, computed, h, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { NImage } from 'naive-ui'
import type { DataTableColumns, SelectOption, FormInst, FormRules } from 'naive-ui'
import DefaultLayout from '@/layouts/DefaultLayout.vue'
import type { CartItem, CouponVO } from '@/types'
import { useCartStore } from '@/stores/cart'
import { useAuthStore } from '@/stores/auth'
import { createOrder } from '@/api/order'
import { getCart, clearCart } from '@/api/cart'
import { getMyCoupons } from '@/api/coupon'

const router = useRouter()
const cartStore = useCartStore()
const authStore = useAuthStore()

const loading = ref(false)
const submitting = ref(false)
const selectedCouponId = ref<number | null>(null)
const coupons = ref<CouponVO[]>([])
const shippingFormRef = ref<FormInst | null>(null)

const shippingForm = reactive({
  name: '',
  phone: '',
  address: '',
})

const addressRules: FormRules = {
  name: { required: true, message: '请输入收件人姓名', trigger: 'blur' },
  phone: { required: true, message: '请输入联系电话', trigger: 'blur' },
  address: { required: true, message: '请输入收货地址', trigger: 'blur' },
}

const placeholderImage = 'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" width="40" height="56" fill="%23ddd"%3E%3Crect width="40" height="56"/%3E%3C/svg%3E'

const itemColumns: DataTableColumns<CartItem> = [
  {
    title: '商品',
    key: 'product',
    render(row) {
      const img = row.imageUrl || row.image || row.product?.image || placeholderImage
      const title = row.name || row.title || row.product?.name || row.product?.title || '商品'
      return h('div', { style: 'display: flex; align-items: center; gap: 8px' }, [
        h(NImage, {
          src: img,
          width: 40,
          height: 56,
          'object-fit': 'cover',
          'preview-disabled': true,
          'fallback-src': placeholderImage,
          lazy: true,
        }),
        h('span', {}, title),
      ])
    },
  },
  {
    title: '价格',
    key: 'price',
    width: 80,
    render(row) {
      const price = row.price || 0
      return h('span', {}, `$${Number(price).toFixed(2)}`)
    },
  },
  {
    title: '数量',
    key: 'quantity',
    width: 60,
  },
  {
    title: '小计',
    key: 'subtotal',
    width: 90,
    render(row) {
      const subtotal = row.subtotal != null ? Number(row.subtotal) : (row.price || 0) * row.quantity
      return h('span', { style: 'color: #e8803f; font-weight: 600' }, `$${Number(subtotal).toFixed(2)}`)
    },
  },
]

const couponOptions = computed<SelectOption[]>(() => {
  return coupons.value
    .map(c => ({
      label: `${c.couponname || c.name} - $${c.discount} off (min $${c.minamount || c.minAmount})`,
      value: c.couponid || c.id,
    }))
})

const selectedCoupon = computed(() => {
  if (!selectedCouponId.value) return null
  return coupons.value.find(c => c.id === selectedCouponId.value) || null
})

const discountAmount = computed(() => {
  if (!selectedCoupon.value) return 0
  if (cartStore.totalPrice >= (selectedCoupon.value.minamount || selectedCoupon.value.minAmount || 0)) {
    return selectedCoupon.value.discount
  }
  return 0
})

const totalAmount = computed(() => {
  return Math.max(0, cartStore.totalPrice - discountAmount.value)
})

async function handleSubmitOrder() {
  try {
    await shippingFormRef.value?.validate()
  } catch {
    return
  }

  if (cartStore.items.length === 0) {
    window.$message?.warning('购物车为空')
    return
  }

  submitting.value = true
  try {
    const shippingAddress = `${shippingForm.name}|${shippingForm.phone}|${shippingForm.address}`
    const order = await createOrder({
      items: cartStore.items.map(item => ({
        productId: item.productId || item.productid || '',
        quantity: item.quantity,
      })),
      billingAddress: shippingAddress,
      shippingAddress: shippingAddress,
      couponId: selectedCouponId.value || undefined,
    })

    window.$message?.success('订单提交成功！')
    // 清空后端购物车
    try {
      await clearCart()
    } catch {
      // ignore
    }
    cartStore.clearItems()

    router.push(`/order/${order.orderid || order.id}`)
  } catch (e: any) {
    window.$message?.error(e?.response?.data?.message || e?.message || '订单提交失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  if (cartStore.items.length === 0) {
    loading.value = true
    try {
      const cart = await getCart()
      cartStore.setItems(cart.items || [])
    } catch {
      // ignore
    }
    loading.value = false
  }

  // Pre-fill from user profile
  if (authStore.user) {
    shippingForm.name = [authStore.user.firstname, authStore.user.lastname].filter(Boolean).join(' ') || authStore.user.username
    shippingForm.phone = authStore.user.phone || ''
    shippingForm.address = [authStore.user.addr1, authStore.user.city, authStore.user.state, authStore.user.zip, authStore.user.country].filter(Boolean).join(', ')
  }

  // Load coupons
  try {
    coupons.value = await getMyCoupons() || []
  } catch {
    coupons.value = []
  }
})
</script>

<style scoped>
.summary-card {
  position: sticky;
  top: 80px;
}

.summary-line {
  display: flex;
  justify-content: space-between;
  font-size: 0.95rem;
}

.summary-line.discount span:last-child {
  color: #18a058;
}

.summary-line.total {
  font-size: 1.2rem;
  font-weight: 700;
}

.summary-line.total span:last-child {
  color: #e8803f;
}

.discount-info {
  color: #18a058;
  font-weight: 500;
}
</style>
