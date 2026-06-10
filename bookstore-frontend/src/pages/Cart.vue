<template>
  <DefaultLayout>
    <div class="page-container">
      <h1 class="section-title">购物车</h1>

      <n-spin :show="loading">
        <template v-if="cartStore.items.length > 0">
          <n-card>
            <n-data-table
              :columns="columns"
              :data="cartStore.items"
              :bordered="false"
              :single-line="false"
            />
          </n-card>

          <!-- Cart Summary -->
          <n-card style="margin-top: 16px">
            <div class="cart-summary">
              <div class="summary-row">
                <span>小计 ({{ cartStore.itemCount }} 件商品)：</span>
                <span class="total-price">${{ cartStore.totalPrice.toFixed(2) }}</span>
              </div>
              <n-space :size="12" style="margin-top: 16px; justify-content: flex-end">
                <n-popconfirm @positive-click="handleClearCart">
                  <template #trigger>
                    <n-button type="default">清空购物车</n-button>
                  </template>
                  确定要清空购物车吗？
                </n-popconfirm>
                <n-button type="primary" size="large" @click="$router.push('/checkout')">
                  去结算
                </n-button>
              </n-space>
            </div>
          </n-card>
        </template>

        <n-empty v-else-if="!loading" description="购物车是空的" size="huge">
          <template #extra>
            <n-button type="primary" @click="$router.push('/products')">继续购物</n-button>
          </template>
        </n-empty>
      </n-spin>
    </div>
  </DefaultLayout>
</template>

<script setup lang="ts">
import { ref, h, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { NButton, NInputNumber, NSpace, NImage, NTag } from 'naive-ui'
import type { DataTableColumns } from 'naive-ui'
import DefaultLayout from '@/layouts/DefaultLayout.vue'
import type { CartItem } from '@/types'
import { useCartStore } from '@/stores/cart'
import { getCart, updateCartItem, removeCartItem, clearCart } from '@/api/cart'

const router = useRouter()
const cartStore = useCartStore()
const loading = ref(false)

const placeholderImage = 'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" width="60" height="80" fill="%23ddd"%3E%3Crect width="60" height="80"/%3E%3C/svg%3E'

const columns: DataTableColumns<CartItem> = [
  {
    title: '商品',
    key: 'product',
    width: 350,
    render(row) {
      const product = row.product
      const img = row.imageUrl || row.image || product?.image || placeholderImage
      const title = row.name || row.title || product?.name || product?.title || '未知商品'
      return h('div', { style: 'display: flex; align-items: center; gap: 12px' }, [
        h(NImage, {
          src: img,
          'fallback-src': placeholderImage,
          width: 60,
          height: 80,
          'object-fit': 'cover',
          'preview-disabled': true,
          lazy: true,
          style: 'border-radius: 4px; flex-shrink: 0',
        }),
        h('div', {}, [
          h('div', {
            style: 'font-weight: 600; cursor: pointer; color: #333',
            onClick: () => router.push(`/product/${row.productid || row.productId}`),
          }, title),
        ]),
      ])
    },
  },
  {
    title: '价格',
    key: 'price',
    width: 120,
    render(row) {
      const price = row.price || 0
      return h('span', { class: 'price' }, `$${Number(price).toFixed(2)}`)
    },
  },
  {
    title: '数量',
    key: 'quantity',
    width: 160,
    render(row) {
      const maxStock = 99
      return h(NInputNumber, {
        value: row.quantity,
        min: 1,
        max: maxStock,
        size: 'small',
        style: 'width: 120px',
        onUpdateValue: async (val: number | null) => {
          if (val && val > 0) {
            try {
              const pid = row.productid || row.productId
              await updateCartItem(pid as any, val)
              cartStore.updateItem(pid as any, val)
            } catch {
              // handled
            }
          }
        },
      })
    },
  },
  {
    title: '小计',
    key: 'subtotal',
    width: 120,
    render(row) {
      const subtotal = row.subtotal != null ? Number(row.subtotal) : (row.price || 0) * row.quantity
      return h('span', { style: 'font-weight: 600; color: #e8803f' }, `$${Number(subtotal).toFixed(2)}`)
    },
  },
  {
    title: '操作',
    key: 'action',
    width: 100,
    render(row) {
      return h(NButton, {
        type: 'error',
        size: 'small',
        quaternary: true,
        onClick: async () => {
          try {
            const pid = row.productid || row.productId
            await removeCartItem(pid as any)
            cartStore.removeItem(pid as any)
            window.$message?.success('商品已移除')
          } catch {
            // handled
          }
        },
      }, { default: () => '删除' })
    },
  },
]

async function loadCart() {
  loading.value = true
  try {
    const cart = await getCart()
    cartStore.setItems(cart.items || [])
  } catch {
    // handled
  } finally {
    loading.value = false
  }
}

async function handleClearCart() {
  try {
    await clearCart()
    cartStore.clearItems()
    window.$message?.success('购物车已清空')
  } catch {
    // handled
  }
}

onMounted(() => {
  loadCart()
})
</script>

<style scoped>
.cart-summary {
  text-align: right;
}

.summary-row {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 12px;
  font-size: 1.1rem;
}

.total-price {
  font-size: 1.5rem;
  font-weight: 700;
  color: #e8803f;
}
</style>
