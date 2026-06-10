// API Response wrapper
export interface Result<T> {
  code: number
  message: string
  data: T
}

// Pagination
export interface PageResult<T> {
  records: T[]
  list?: T[]
  total: number
  pageNum: number
  pageSize: number
  totalPages?: number
  pages?: number
}

// User
export interface UserVO {
  id: number
  username: string
  email: string
  phone: string
  firstname: string
  lastname: string
  addr1: string
  addr2: string
  city: string
  state: string
  zip: string
  country: string
  role: string
  createdAt: string
  updatedAt: string
}

export interface LoginRequest {
  username: string
  password: string
}

export interface LoginResponse {
  token: string
  user: UserVO
}

export interface RegisterRequest {
  username: string
  password: string
  email: string
  phone: string
}

export interface UpdatePasswordRequest {
  oldPassword: string
  newPassword: string
}

export interface UpdateProfileRequest {
  email?: string
  firstname?: string
  lastname?: string
  phone?: string
  addr1?: string
  addr2?: string
  city?: string
  state?: string
  zip?: string
  country?: string
}

// Product
export interface ProductVO {
  id?: number
  productid?: string
  title?: string
  name?: string
  author?: string
  isbn?: string
  publisher?: string
  publishDate?: string
  price: number
  salePrice?: number
  sale_price?: number
  description?: string
  descn?: string
  image?: string
  images?: string
  categoryId?: number
  category?: string
  categoryName?: string
  categoryname?: string
  stock: number
  sales: number
  rating?: number
  ratingCount?: number
  rating_count?: number
  status: number
  is_recommend?: number
  createdAt?: string
  updatedAt?: string
}

// Category
export interface Category {
  id?: number | string
  categoryid?: string
  name?: string
  categoryname?: string
  categorydesc?: string
  parentId?: number
  sort?: number
  children?: Category[]
}

// Cart
export interface CartItem {
  itemid?: number
  itemid_str?: string
  cartid?: number
  productId?: number
  productid?: string
  quantity: number
  product?: ProductVO
  title?: string
  name?: string
  price?: number
  salePrice?: number
  sale_price?: number
  image?: string
  imageUrl?: string
  subtotal?: number
  stock?: number
}

export interface CartVO {
  cartid?: number
  items: CartItem[]
}

export interface AddToCartRequest {
  productId: number
  quantity: number
}

// Order
export interface OrderItemVO {
  id?: number
  orderitemid?: string
  orderId?: number
  orderid?: string
  productId?: number
  productid?: string
  quantity: number
  price: number
  product?: ProductVO
  title?: string
  name?: string
  productName?: string
  image?: string
}

export interface OrderVO {
  id?: number
  orderid?: string
  orderNo?: string
  orderno?: string
  userId?: number
  userid?: string
  status: string | number
  statusText?: string
  totalprice?: number
  originalprice?: number
  totalAmount?: number
  totalamount?: number
  discountAmount?: number
  discountamount?: number
  payAmount?: number
  payamount?: number
  couponname?: string
  orderdate?: string
  billingAddress?: string
  billingaddress?: string
  shippingAddress?: string
  shippingaddress?: string
  billtofirstname?: string
  billtolastname?: string
  billaddr1?: string
  billaddr2?: string
  billcity?: string
  billstate?: string
  billzip?: string
  billcountry?: string
  shiptofirstname?: string
  shiptolastname?: string
  shipaddr1?: string
  shipaddr2?: string
  shipcity?: string
  shipstate?: string
  shipzip?: string
  shipcountry?: string
  courier?: string
  creditcard?: string
  exprdate?: string
  cardtype?: string
  locale?: string
  couponId?: number
  couponid?: string
  paymentMethod?: string
  paymentmethod?: string
  paymentTime?: string
  paymenttime?: string
  shippingTime?: string
  shippingtime?: string
  completionTime?: string
  completiontime?: string
  cancelTime?: string
  canceltime?: string
  cancelReason?: string
  cancelreason?: string
  items: OrderItemVO[]
  createdAt?: string
  updatedAt?: string
  created_at?: string
  updated_at?: string
}

export interface CreateOrderRequest {
  items: { productId: string | number; quantity: number }[]
  billingAddress: string
  shippingAddress: string
  couponId?: number
}

// Review
export interface ReviewVO {
  id?: number
  reviewid?: string
  userId?: number
  userid?: string
  productId?: number
  productid?: string
  orderId?: number
  orderid?: string
  rating: number
  content: string
  username?: string
  product?: ProductVO
  productTitle?: string
  producttitle?: string
  productImage?: string
  productimage?: string
  createdAt?: string
  created_at?: string
}

export interface CreateReviewRequest {
  productId: number
  orderId: number
  rating: number
  content: string
}

// Coupon
export interface CouponVO {
  id?: number
  couponid?: string
  name?: string
  couponname?: string
  type: number
  discount: number
  minAmount?: number
  minamount?: number
  startTime?: string
  start_time?: string
  endTime?: string
  end_time?: string
  totalCount?: number
  total_count?: number
  claimedCount?: number
  claimed_count?: number
  status: number
  claimed?: boolean
}

// Message
export interface MessageVO {
  id: number
  userId: number
  title: string
  content: string
  type: number
  isRead: boolean
  createdAt: string
}

// Announcement
export interface AnnouncementVO {
  id: number
  title: string
  content: string
  startTime: string
  endTime: string
  status: number
  createdAt: string
}
