export function toBoardDto(item) {
  return {
    id: item.id,
    name: item.name,
    slug: item.slug,
    sortOrder: item.sortOrder,
    isActive: item.isActive,
  };
}

export function toPostSummaryDto(item) {
  return {
    id: item.id,
    boardId: item.boardId,
    userId: item.userId,
    title: item.title,
    content: item.content,
    viewCount: item.viewCount,
    likeCount: item.likeCount,
    commentCount: item.commentCount,
    createdAt: item.createdAt,
    updatedAt: item.updatedAt,
    deletedAt: item.deletedAt,
    board: item.board ?? undefined,
    user: item.user ?? undefined,
  };
}

export function toCommentDto(item) {
  return {
    id: item.id,
    postId: item.postId,
    userId: item.userId,
    parentId: item.parentId,
    content: item.content,
    createdAt: item.createdAt,
    updatedAt: item.updatedAt,
    user: item.user ?? undefined,
  };
}

export function toPostDetailDto(item) {
  return {
    ...toPostSummaryDto(item),
    comments: (item.comments ?? []).map(toCommentDto),
  };
}
