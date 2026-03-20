export function toNoticeDto(item) {
  return {
    id: item.id,
    title: item.title,
    content: item.content,
    isPinned: item.isPinned,
    viewCount: item.viewCount,
    createdAt: item.createdAt,
    updatedAt: item.updatedAt,
  };
}
