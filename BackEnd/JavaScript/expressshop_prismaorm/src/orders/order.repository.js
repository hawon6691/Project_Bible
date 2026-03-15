import { prisma } from "../prisma.js";

function orderSummarySelect() {
  return {
    id: true,
    orderNumber: true,
    status: true,
    totalAmount: true,
    pointUsed: true,
    finalAmount: true,
    recipientName: true,
    createdAt: true,
    updatedAt: true,
  };
}

export function orderDetailInclude() {
  return {
    orderItems: { orderBy: { id: "asc" } },
    payments: { orderBy: { id: "asc" } },
  };
}

export function findProductsForOrder(productIds) {
  return prisma.product.findMany({ where: { id: { in: productIds } } });
}

export function findUserOrders(userId) {
  return prisma.order.findMany({
    where: { userId },
    select: orderSummarySelect(),
    orderBy: { createdAt: "desc" },
  });
}

export function findOrder(userId, orderId) {
  return prisma.order.findFirst({
    where: { id: Number(orderId), userId },
    include: orderDetailInclude(),
  });
}

export function createOrder(data) {
  return prisma.order.create({
    data,
    include: orderDetailInclude(),
  });
}

export function updateOrderById(orderId, data) {
  return prisma.order.update({
    where: { id: Number(orderId) },
    data,
    include: orderDetailInclude(),
  });
}

export function findUserOrder(userId, orderId) {
  return prisma.order.findFirst({
    where: { id: Number(orderId), userId },
  });
}

export function findAdminOrders() {
  return prisma.order.findMany({
    select: {
      ...orderSummarySelect(),
      user: { select: { id: true, email: true, name: true } },
    },
    orderBy: { createdAt: "desc" },
  });
}
