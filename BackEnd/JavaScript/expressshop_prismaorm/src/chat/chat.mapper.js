export function toChatRoomDto(item) {
  return {
    id: item.id,
    name: item.name,
    createdBy: item.createdBy,
    isPrivate: item.isPrivate,
    createdAt: item.createdAt,
    updatedAt: item.updatedAt,
    creator: item.creator ?? undefined,
    members: (item.members ?? []).map((member) => ({
      id: member.id,
      roomId: member.roomId,
      userId: member.userId,
      joinedAt: member.joinedAt,
      user: member.user ?? undefined,
    })),
  };
}

export function toChatMessageDto(item) {
  return {
    id: item.id,
    roomId: item.roomId,
    senderId: item.senderId,
    message: item.message,
    createdAt: item.createdAt,
    updatedAt: item.updatedAt,
    sender: item.sender ?? undefined,
  };
}
