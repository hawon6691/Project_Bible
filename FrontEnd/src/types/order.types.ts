export enum OrderStatus {
  PENDING = 'PENDING',
  PAID = 'PAID',
  PREPARING = 'PREPARING',
  SHIPPING = 'SHIPPING',
  DELIVERED = 'DELIVERED',
  CANCELLED = 'CANCELLED',
  REFUND_REQUESTED = 'REFUND_REQUESTED',
  REFUNDED = 'REFUNDED',
}

export interface Order {
  id: number;
  orderNumber: string;
  status: OrderStatus;
  totalAmount: number;
  discountAmount: number;
  shippingFee: number;
  finalAmount: number;
  items: OrderItem[];
  shippingAddress: Address;
  createdAt: string;
  paidAt?: string;
}

export interface OrderItem {
  id: number;
  productId: number;
  productName: string;
  thumbnailUrl: string;
  quantity: number;
  unitPrice: number;
  totalPrice: number;
  selectedOptions?: string;
  sellerId: number;
  sellerName: string;
}

export interface Address {
  id: number;
  name: string;
  phone: string;
  zipCode: string;
  address: string;
  addressDetail: string;
  isDefault: boolean;
}

export interface CartItem {
  id: number;
  productId: number;
  productName: string;
  thumbnailUrl: string;
  price: number;
  quantity: number;
  selectedOptions?: string;
  sellerId: number;
  sellerName: string;
}

export interface CreateOrderRequest {
  items: { cartItemId: number; quantity: number }[];
  addressId: number;
  couponId?: number;
  usePoints?: number;
  paymentMethod: string;
}

export interface Review {
  id: number;
  productId: number;
  productName: string;
  userId: number;
  userName: string;
  rating: number;
  title: string;
  content: string;
  images: string[];
  helpfulCount: number;
  createdAt: string;
}

export interface Coupon {
  id: number;
  code: string;
  name: string;
  discountType: 'PERCENT' | 'FIXED';
  discountValue: number;
  minOrderAmount: number;
  maxDiscountAmount?: number;
  expiresAt: string;
  isUsed: boolean;
}
