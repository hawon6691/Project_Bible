function toFriendUserDto(user) {
  return user
    ? {
        id: user.id,
        email: user.email,
        name: user.name,
        nickname: user.nickname,
        profileImageUrl: user.profileImageUrl,
        role: user.role,
      }
    : undefined;
}

export function toFriendshipDto(item, currentUserId) {
  const friend = item.requesterId === currentUserId ? item.addressee : item.requester;
  return {
    id: item.id,
    status: item.status,
    createdAt: item.createdAt,
    updatedAt: item.updatedAt,
    friend: toFriendUserDto(friend),
  };
}

export function toFriendRequestDto(item) {
  return {
    id: item.id,
    status: item.status,
    createdAt: item.createdAt,
    requester: toFriendUserDto(item.requester),
    addressee: toFriendUserDto(item.addressee),
  };
}

export function toFriendActivityDto(item) {
  return {
    id: item.id,
    userId: item.userId,
    type: item.type,
    message: item.message,
    metadata: item.metadata,
    createdAt: item.createdAt,
    user: toFriendUserDto(item.user),
  };
}
