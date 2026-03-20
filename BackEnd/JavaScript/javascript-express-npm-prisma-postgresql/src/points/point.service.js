import {
  createPointTransaction,
  findPointTransactions,
  findUserById,
  updateUserPoint,
} from "./point.repository.js";
import { badRequest, notFound } from "../utils/http-error.js";

export async function getPointBalance(user) {
  return { balance: user.point };
}

export async function getPointTransactions(userId) {
  const items = await findPointTransactions(userId);
  return { items, meta: { total: items.length } };
}

export async function grantPoints(adminId, payload) {
  const { userId, amount, description } = payload ?? {};
  if (!userId || !amount || !description) {
    throw badRequest("userId, amount, description are required");
  }
  const user = await findUserById(userId);
  if (!user) throw notFound("User not found");
  const balance = user.point + Number(amount);
  await updateUserPoint(user.id, balance);
  return createPointTransaction({
    userId: user.id,
    type: "ADMIN_GRANT",
    amount: Number(amount),
    balance,
    description,
    referenceType: "ADMIN",
    referenceId: adminId,
  });
}
