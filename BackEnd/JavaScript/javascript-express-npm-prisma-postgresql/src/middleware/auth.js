import { prisma } from "../prisma.js";
import { verifyAccessToken } from "../auth/token.service.js";
import { asyncHandler } from "../utils/async-handler.js";
import { forbidden, unauthorized } from "../utils/http-error.js";

export const requireAuth = asyncHandler(async (req, _res, next) => {
  const header = req.headers.authorization;
  if (!header?.startsWith("Bearer ")) {
    throw unauthorized();
  }

  const token = header.slice("Bearer ".length).trim();
  const payload = verifyAccessToken(token);
  const user = await prisma.user.findUnique({
    where: { id: Number(payload.sub) },
  });

  if (!user || user.deletedAt) {
    throw unauthorized("User not found");
  }

  req.user = user;
  next();
});

export function requireRole(...roles) {
  return (req, _res, next) => {
    if (!req.user) {
      throw unauthorized();
    }

    if (!roles.includes(req.user.role)) {
      throw forbidden();
    }

    next();
  };
}
