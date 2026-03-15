export function toFaqDto(item) {
  return {
    id: item.id,
    category: item.category,
    question: item.question,
    answer: item.answer,
    sortOrder: item.sortOrder,
    isActive: item.isActive,
    createdAt: item.createdAt,
    updatedAt: item.updatedAt,
  };
}
