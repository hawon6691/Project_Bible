export function toOrderSummaryDto(item) {
  return {
    id: item.id,
    orderNumber: item.orderNumber,
    status: item.status,
    totalAmount: item.totalAmount,
    pointUsed: item.pointUsed,
    finalAmount: item.finalAmount,
    recipientName: item.recipientName,
    createdAt: item.createdAt,
    updatedAt: item.updatedAt,
    user: item.user ?? undefined,
  };
}

export function toOrderDetailDto(item) {
  return {
    ...toOrderSummaryDto(item),
    recipientPhone: item.recipientPhone,
    zipCode: item.zipCode,
    address: item.address,
    addressDetail: item.addressDetail,
    memo: item.memo,
    orderItems: item.orderItems ?? [],
    payments: item.payments ?? [],
  };
}
