export type Role = 'CUSTOMER' | 'ADMIN'
export type OrderStatus = 'PENDING' | 'PROCESSING' | 'SHIPPED' | 'DELIVERED' | 'CANCELLED'
export type PaymentStatus = 'PENDING' | 'SUCCESS' | 'FAILED' | 'REFUNDED'
export type PaymentMethod = 'CREDIT_CARD' | 'DEBIT_CARD'
export type RewardTransactionType = 'EARN' | 'REDEEM' | 'REFUND'

export interface UserResponse {
  id: number
  firstName: string
  lastName: string
  email: string
  phone?: string
  role: Role
  createdAt: string
}

export interface AuthResponse {
  accessToken: string
  tokenType: string
  expiresIn: number
  user: UserResponse
}

export interface CategoryResponse {
  id: number
  name: string
  description?: string
  createdAt: string
}

export interface BrandResponse {
  id: number
  name: string
  description?: string
  createdAt: string
}

export interface BookResponse {
  id: number
  title: string
  author: string
  description?: string
  isbn?: string
  price: number
  stockQuantity: number
  imageUrl?: string
  category?: CategoryResponse
  brand?: BrandResponse
  available: boolean
  createdAt: string
  updatedAt: string
  tentativeDeliveryDate?: string
}

export interface CartItemResponse {
  itemId: number
  bookId: number
  bookTitle: string
  bookAuthor: string
  imageUrl?: string
  priceAtAdd: number
  currentPrice: number
  quantity: number
  lineTotal: number
}

export interface CartResponse {
  cartId: number
  items: CartItemResponse[]
  subtotal: number
  itemCount: number
}

export interface AddressResponse {
  id: number
  fullName: string
  line1: string
  line2?: string
  city: string
  state: string
  postalCode: string
  country: string
  phone: string
  isDefault: boolean
  createdAt: string
}

export interface AddressRequest {
  fullName: string
  line1: string
  line2?: string
  city: string
  state: string
  postalCode: string
  country: string
  phone: string
}

export interface OrderItemResponse {
  id: number
  bookId: number
  bookTitleSnapshot: string
  bookAuthorSnapshot: string
  priceSnapshot: number
  quantity: number
  lineTotal: number
}

export interface PaymentResponse {
  id: number
  paymentMethod: PaymentMethod
  maskedCardNumber?: string
  cardHolderName?: string
  transactionRef?: string
  amount: number
  status: PaymentStatus
  initiatedAt: string
  completedAt?: string
}

export interface OrderResponse {
  id: number
  orderNumber: string
  status: OrderStatus
  subtotal: number
  rewardDiscount: number
  deliveryCharge: number
  totalAmount: number
  addressSnapshot: string
  placedAt: string
  items: OrderItemResponse[]
  payment?: PaymentResponse
}

export interface RewardTransactionResponse {
  id: number
  type: RewardTransactionType
  points: number
  description: string
  orderId?: number
  createdAt: string
}

export interface RewardResponse {
  balance: number
  transactions: RewardTransactionResponse[]
}

export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
  size: number
}

export interface ErrorResponse {
  timestamp: string
  status: number
  error: string
  message: string
  path: string
  errors?: { field: string; message: string }[]
}

export interface CheckoutRequest {
  addressId: number
  paymentMethod: PaymentMethod
  maskedCardNumber?: string
  cardHolderName?: string
  rewardPointsToRedeem?: number
}

export interface BookRequest {
  title: string
  author: string
  description?: string
  isbn?: string
  price: number
  stockQuantity: number
  imageUrl?: string
  categoryId?: number
  brandId?: number
}
