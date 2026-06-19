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
// Backend Product entity fields: productid, category, name, descn, author, price, image, stock, sales, isRecommend, status
// ProductVO adds @JsonProperty/compat: id, description, imageUrl
export interface ProductVO {
  productid?: string
  id?: number                  // @JsonProperty alias for productid
  name?: string
  category?: string
  descn?: string
  description?: string         // @JsonProperty alias for descn
  author?: string
  price: number
  image?: string
  imageUrl?: string            // @JsonProperty alias for image
  stock: number
  sales: number
  isRecommend?: number
  status: number
}

// Category
export interface Category {
  categoryid?: string
  categoryname?: string
  categorydesc?: string
  parentId?: number
  sort?: number
  children?: Category[]
}

// Cart
export interface CartItem {
  itemid?: number
  cartid?: number
  productid?: string
  quantity: number
  product?: ProductVO
  name?: string
  price?: number
  image?: string
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
  orderitemid?: string
  orderid?: string
  productid?: string
  quantity: number
  price: number
  product?: ProductVO
  name?: string
  image?: string
}

// Backend Orders entity fields (all lowercase): orderid, userid, orderdate, totalprice, originalprice,
// discountamount, couponname, status, billtofirstname, billtolastname, billaddr1, billaddr2, billcity,
// billstate, billzip, billcountry, shipaddr1, shipaddr2, shipcity, shipstate, shipzip, shipcountry,
// shiptofirstname, shiptolastname, courier, creditcard, exprdate, cardtype, locale
// OrderVO adds @JsonProperty: shippingAddress, billingAddress, totalAmount
export interface OrderVO {
  orderid?: string
  userid?: string
  status: string | number
  totalprice?: number
  originalprice?: number
  totalAmount?: number          // @JsonProperty alias
  discountamount?: number
  couponname?: string
  orderdate?: string
  billingAddress?: string       // @JsonProperty alias
  shippingAddress?: string      // @JsonProperty alias
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
  items: OrderItemVO[]
}

export interface CreateOrderRequest {
  items: { productId: string | number; quantity: number }[]
  billingAddress: string
  shippingAddress: string
  couponId?: number
}

// Review
export interface ReviewVO {
  reviewid?: string
  userid?: string
  productid?: string
  orderid?: string
  rating: number
  content: string
  username?: string
  product?: ProductVO
  createdAt?: string
}

export interface CreateReviewRequest {
  productId: number
  orderId: number
  rating: number
  content: string
}

// Coupon
export interface CouponVO {
  couponid?: string
  couponname?: string
  type: number
  discount: number
  minamount?: number
  starttime?: string
  endtime?: string
  totalcount?: number
  claimedcount?: number
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
