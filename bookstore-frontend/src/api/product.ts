import http from './index'
import type { ProductVO, Category, PageResult } from '@/types'

export interface ProductListParams {
  pageNum?: number
  pageSize?: number
  keyword?: string
  category?: number | string
  sort?: string
}

export async function getProductList(params: ProductListParams): Promise<PageResult<ProductVO>> {
  const res = await http.get('/api/products', { params }) as any
  if (res && res.records) {
    return res as PageResult<ProductVO>
  }
  return res as PageResult<ProductVO>
}

export function getProduct(id: string | number): Promise<ProductVO> {
  return http.get(`/api/products/${id}`)
}

export function getRecommendProducts(limit: number = 8): Promise<ProductVO[]> {
  return http.get('/api/products/recommend', { params: { limit } })
}

export function getHotProducts(limit: number = 8): Promise<ProductVO[]> {
  return http.get('/api/products/hot', { params: { limit } })
}

export async function searchProducts(keyword: string, pageNum: number = 1, pageSize: number = 12): Promise<ProductVO[]> {
  try {
    return await http.get('/api/products/search', { params: { keyword, pageNum, pageSize } })
  } catch {
    return []
  }
}

export function getCategoryList(): Promise<Category[]> {
  return http.get('/api/products/categories')
}
