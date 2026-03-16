export function toAuthUserDto(user) {
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
  };
}

export function toSignupResultDto(result) {
  return {
    id: result.id,
    email: result.email,
    name: result.name,
    message: result.message,
  };
}

export function toAuthTokenDto(tokens) {
  return {
    accessToken: tokens.accessToken,
    refreshToken: tokens.refreshToken,
    expiresIn: tokens.expiresIn,
  };
}

export function toAuthMessageDto(result) {
  return {
    message: result.message,
    ...(result.verified !== undefined ? { verified: result.verified } : {}),
    ...(result.resetToken ? { resetToken: result.resetToken } : {}),
  };
}
