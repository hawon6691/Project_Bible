import {
  createOrder as createOrderRecord,
  findAdminOrders,
  findOrder,
  findProductsForOrder,
  findUserOrder,
  findUserOrders,
  updateOrderById,
} from "./order.repository.js";
import { badRequest, notFound } from "../utils/http-error.js";

export async function getOrders(userId) {
  const items = await findUserOrders(userId);
  return { items, meta: { total: items.length } };
}

export async function getOrder(userId, orderId) {
  const item = await findOrder(userId, orderId);
  if (!item) throw notFound("Order not found");
  return item;
}

export async function createOrder(userId, payload) {
  const {
    recipientName,
    recipientPhone,
    zipCode,
    address,
    addressDetail,
    memo,
    pointUsed,
    items,
  } = payload ?? {};

  if (
    !recipientName ||
    !recipientPhone ||
    !zipCode ||
    !address ||
    !Array.isArray(items) ||
    items.length === 0
  ) {
    throw badRequest("recipient info and items are required");
  }

  const productIds = items.map((item) => Number(item.productId));
  const products = await findProductsForOrder(productIds);
  const productMap = new Map(products.map((product) => [product.id, product]));
  let totalAmount = 0;

  const orderItems = items.map((item) => {
    const product = productMap.get(Number(item.productId));
    if (!product) throw badRequest(`Product not found: ${item.productId}`);
    const quantity = Number(item.quantity ?? 1);
    const unitPrice = product.discountPrice ?? product.price;
    const totalPrice = unitPrice * quantity;
    totalAmount += totalPrice;
    return {
      productId: product.id,
      sellerId: item.sellerId ? Number(item.sellerId) : null,
      productName: product.name,
      sellerName: item.sellerName ?? null,
      selectedOptions: item.selectedOptions ?? null,
      quantity,
      unitPrice,
      totalPrice,
    };
  });

  const usedPoint = Number(pointUsed ?? 0);
  const finalAmount = Math.max(totalAmount - usedPoint, 0);
  const orderNumber = `ORD-${Date.now()}`;

  return createOrderRecord({
    orderNumber,
    userId,
    status: "PENDING",
    totalAmount,
    pointUsed: usedPoint,
    finalAmount,
    recipientName,
    recipientPhone,
    zipCode,
    address,
    addressDetail: addressDetail ?? null,
    memo: memo ?? null,
    orderItems: { create: orderItems },
  });
}

export async function cancelOrder(userId, orderId) {
  const existing = await findUserOrder(userId, orderId);
  if (!existing) throw notFound("Order not found");
  return updateOrderById(existing.id, { status: "CANCELED" });
}

export async function getAdminOrders() {
  const items = await findAdminOrders();
  return { items, meta: { total: items.length } };
}

export async function updateOrderStatus(orderId, payload) {
  const status = payload?.status;
  if (!status) throw badRequest("status is required");
  return updateOrderById(orderId, { status });
}
