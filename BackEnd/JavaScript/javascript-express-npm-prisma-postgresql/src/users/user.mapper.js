export function toUserDto(user) {
  return {
    id: user.id,
    email: user.email,
    name: user.name,
    phone: user.phone,
    role: user.role,
    status: user.status,
    nickname: user.nickname,
    bio: user.bio,
    profileImageUrl: user.profileImageUrl,
    point: user.point,
    preferredLocale: user.preferredLocale,
    preferredCurrency: user.preferredCurrency,
    createdAt: user.createdAt,
    updatedAt: user.updatedAt,
  };
}

export function toUserProfileDto(user) {
  return {
    id: user.id,
    name: user.name,
    nickname: user.nickname,
    bio: user.bio,
    profileImageUrl: user.profileImageUrl,
    role: user.role,
    createdAt: user.createdAt,
  };
}
