// API Response wrapper
export interface Result<T = any> {
  code: number
  message: string
  data: T
}

// Page result wrapper
export interface PageResult<T = any> {
  list: T[]
  total: number
  pageNum: number
  pageSize: number
}

// User types
export interface UserVO {
  id: number
  username: string
  nickname: string
  email: string
  phone: string
  avatar: string
  role: string
  status: number
  createTime: string
  updateTime: string
}

export interface LoginRequest {
  username: string
  password: string
}

export interface LoginResponse {
  token: string
  user: UserVO
}

// Product types
export interface ProductVO {
  productid: string
  id: string
  name: string
  author: string
  price: number
  stock: number
  category: string
  categoryName: string
  image: string
  descn: string
  description: string
  sales: number
  is_recommend: number
  status: number
}

export interface ProductForm {
  name: string
  author: string
  price: number
  stock: number
  category: string
  image: string
  descn: string
  description: string
  status: number
}

// Order types
export interface OrderVO {
  id: number
  orderNo: string
  userId: number
  username: string
  totalAmount: number
  status: number
  statusText: string
  address: string
  phone: string
  remark: string
  createTime: string
  payTime: string
  shipTime: string
  completeTime: string
  items: OrderItemVO[]
}

export interface OrderItemVO {
  id: number
  productId: number
  productName: string
  productCover: string
  price: number
  quantity: number
  subtotal: number
}

// Category types
export interface CategoryVO {
  categoryid: string
  id: string
  categoryname: string
  name: string
  categorydesc: string
  description: string
}

// Coupon types
export interface CouponVO {
  id: number
  name: string
  type: number
  discount: number
  minAmount: number
  startTime: string
  endTime: string
  totalCount: number
  remainCount: number
  status: number
  createTime: string
}

export interface CouponForm {
  name: string
  type: number
  discount: number
  minAmount: number
  startTime: string
  endTime: string
  totalCount: number
  status: number
}

// Review types
export interface ReviewVO {
  id: number
  userId: number
  username: string
  productId: number
  productName: string
  rating: number
  content: string
  reply: string
  blocked: boolean
  top: boolean
  createTime: string
}

// Announcement types
export interface AnnouncementVO {
  id: number
  title: string
  content: string
  status: number
  createTime: string
  updateTime: string
}

export interface AnnouncementForm {
  title: string
  content: string
  status: number
}

// Message types
export interface MessageVO {
  id: number
  userId: number
  username: string
  content: string
  type: string
  createTime: string
}

export interface BroadcastRequest {
  content: string
}

// Log types
export interface ApiLogVO {
  id: number
  userId: number
  username: string
  method: string
  path: string
  params: string
  ip: string
  duration: number
  status: number
  createTime: string
}

// Dashboard stats
export interface DashboardStats {
  totalProducts: number
  totalOrders: number
  totalRevenue: number
  totalUsers: number
}

export interface OrderStatusCount {
  status: string
  count: number
}

export interface TopProduct {
  name: string
  sales: number
}
