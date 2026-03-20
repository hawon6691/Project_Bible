export function toInquiryDto(item) {
  return {
    id: item.id,
    productId: item.productId,
    userId: item.userId,
    title: item.title,
    content: item.content,
    isSecret: item.isSecret,
    answer: item.answer,
    answeredBy: item.answeredBy,
    answeredAt: item.answeredAt,
    createdAt: item.createdAt,
    updatedAt: item.updatedAt,
    user: item.user ?? undefined,
    answeredUser: item.answeredUser ?? undefined,
  };
}
