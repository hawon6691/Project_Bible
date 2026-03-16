export function toNewsCategoryDto(item) {
  return {
    id: item.id,
    name: item.name,
    slug: item.slug,
    createdAt: item.createdAt,
    updatedAt: item.updatedAt,
  };
}

export function toNewsDto(item) {
  return {
    id: item.id,
    title: item.title,
    content: item.content,
    categoryId: item.categoryId,
    thumbnailUrl: item.thumbnailUrl,
    viewCount: item.viewCount,
    createdAt: item.createdAt,
    updatedAt: item.updatedAt,
    category: item.category ? toNewsCategoryDto(item.category) : undefined,
  };
}

export function toNewsDetailDto(item) {
  return {
    ...toNewsDto(item),
    relatedProducts: (item.products ?? []).map((item) => item.product ?? undefined).filter(Boolean),
  };
}
