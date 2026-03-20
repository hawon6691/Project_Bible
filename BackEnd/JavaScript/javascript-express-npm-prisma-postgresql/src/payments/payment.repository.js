import { prisma } from "../prisma.js";

export function findUserOrder(userId, orderId) {
  return prisma.order.findFirst({
    where: { id: Number(orderId), userId },
  });
}

export function createPayment(data) {
  return prisma.payment.create({ data });
}

export function findPayment(userId, paymentId) {
  return prisma.payment.findFirst({
    where: { id: Number(paymentId), order: { userId } },
  });
}

export function updatePayment(paymentId, data) {
  return prisma.payment.update({
    where: { id: Number(paymentId) },
    data,
  });
}
