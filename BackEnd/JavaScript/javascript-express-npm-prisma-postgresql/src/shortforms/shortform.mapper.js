export function toShortformDto(item) {
  return {
    id: item.id,
    userId: item.userId,
    title: item.title,
    videoUrl: item.videoUrl,
    thumbnailUrl: item.thumbnailUrl,
    durationSec: item.durationSec,
    viewCount: item.viewCount,
    likeCount: item.likeCount,
    commentCount: item.commentCount,
    transcodeStatus: item.transcodeStatus,
    transcodedVideoUrl: item.transcodedVideoUrl,
    transcodeError: item.transcodeError,
    transcodedAt: item.transcodedAt,
    createdAt: item.createdAt,
    updatedAt: item.updatedAt,
    user: item.user ?? undefined,
    productIds: (item.products ?? []).map((item) => item.productId),
    products: (item.products ?? []).map((item) => item.product ?? undefined).filter(Boolean),
  };
}

export function toShortformCommentDto(item) {
  return {
    id: item.id,
    shortformId: item.shortformId,
    userId: item.userId,
    content: item.content,
    createdAt: item.createdAt,
    updatedAt: item.updatedAt,
    user: item.user ?? undefined,
  };
}
