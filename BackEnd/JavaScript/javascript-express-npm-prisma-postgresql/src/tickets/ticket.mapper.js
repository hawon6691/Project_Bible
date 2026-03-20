export function toTicketReplyDto(item) {
  return {
    id: item.id,
    ticketId: item.ticketId,
    userId: item.userId,
    content: item.content,
    isAdmin: item.isAdmin,
    createdAt: item.createdAt,
    user: item.user ?? undefined,
  };
}

export function toTicketDto(item) {
  return {
    id: item.id,
    ticketNumber: item.ticketNumber,
    userId: item.userId,
    category: item.category,
    title: item.title,
    content: item.content,
    status: item.status,
    attachmentUrls: item.attachmentUrls,
    createdAt: item.createdAt,
    updatedAt: item.updatedAt,
    user: item.user ?? undefined,
  };
}

export function toTicketDetailDto(item) {
  return {
    ...toTicketDto(item),
    replies: (item.replies ?? []).map(toTicketReplyDto),
  };
}
