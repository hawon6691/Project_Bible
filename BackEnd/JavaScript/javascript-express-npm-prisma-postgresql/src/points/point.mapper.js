export function toPointBalanceDto(item) {
  return { balance: item.balance };
}

export function toPointTransactionDto(item) {
  return {
    id: item.id,
    userId: item.userId,
    type: item.type,
    amount: item.amount,
    balance: item.balance,
    description: item.description,
    referenceType: item.referenceType,
    referenceId: item.referenceId,
    createdAt: item.createdAt,
  };
}
