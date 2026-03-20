export function toPaymentDto(item) {
  return {
    id: item.id,
    orderId: item.orderId,
    method: item.method,
    amount: item.amount,
    status: item.status,
    paidAt: item.paidAt,
    refundedAt: item.refundedAt,
  };
}
