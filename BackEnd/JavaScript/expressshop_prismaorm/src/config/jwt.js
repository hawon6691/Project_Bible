export const jwtConfig = {
  accessSecret: process.env.JWT_ACCESS_SECRET ?? "pbshop-access-secret",
  refreshSecret: process.env.JWT_REFRESH_SECRET ?? "pbshop-refresh-secret",
  accessExpiresIn: "1h",
  refreshExpiresIn: "7d",
};
