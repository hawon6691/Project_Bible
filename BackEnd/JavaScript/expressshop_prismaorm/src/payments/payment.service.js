import {
  createPayment as createPaymentRecord,
  findPayment,
  findUserOrder,
  updatePayment,
} from "./payment.repository.js";
import { badRequest, notFound } from "../utils/http-error.js";

export async function createPayment(userId, payload) {
  const { orderId, method, amount } = payload ?? {};
  if (!orderId || !method || !amount) throw badRequest("orderId, method, amount are required");
  const order = await findUserOrder(userId, orderId);
  if (!order) throw notFound("Order not found");
  return createPaymentRecord({
    orderId: order.id,
    method,
    amount: Number(amount),
    status: "COMPLETED",
    paidAt: new Date(),
  });
}

export async function getPayment(userId, paymentId) {
  const payment = await findPayment(userId, paymentId);
  if (!payment) throw notFound("Payment not found");
  return payment;
}

export async function refundPayment(userId, paymentId) {
  const payment = await findPayment(userId, paymentId);
  if (!payment) throw notFound("Payment not found");
  return updatePayment(payment.id, { status: "REFUNDED", refundedAt: new Date() });
}
