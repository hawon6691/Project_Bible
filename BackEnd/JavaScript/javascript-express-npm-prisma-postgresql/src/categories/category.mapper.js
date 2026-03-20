export function toCategoryDto(item) {
  return {
    id: item.id,
    name: item.name,
    parentId: item.parentId,
    sortOrder: item.sortOrder,
    createdAt: item.createdAt,
    updatedAt: item.updatedAt,
  };
}
