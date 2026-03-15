import {
  clearCartItems,
  createCartItem,
  deleteCartItemById,
  findCartItemById,
  findCartItems,
  updateCartItemQuantity,
} from "./cart.repository.js";
import { badRequest, notFound } from "../utils/http-error.js";

export async function getCart(userId) {
  const items = await findCartItems(userId);
  return { items, meta: { total: items.length } };
}

export async function addCartItem(userId, payload) {
  const { productId, sellerId, quantity, selectedOptions } = payload ?? {};
  if (!productId || !quantity) throw badRequest("productId and quantity are required");
  return createCartItem({
    userId,
    productId: Number(productId),
    sellerId: sellerId ? Number(sellerId) : null,
    quantity: Number(quantity),
    selectedOptions: selectedOptions ?? null,
  });
}

export async function updateCartItem(userId, itemId, payload) {
  const quantity = Number(payload?.quantity);
  if (!quantity || quantity < 1) throw badRequest("quantity must be greater than 0");
  const updated = await updateCartItemQuantity(userId, itemId, quantity);
  if (updated.count === 0) throw notFound("Cart item not found");
  return findCartItemById(itemId);
}

export async function deleteCartItem(userId, itemId) {
  const deleted = await deleteCartItemById(userId, itemId);
  if (deleted.count === 0) throw notFound("Cart item not found");
  return { message: "Cart item deleted" };
}

export async function clearCart(userId) {
  await clearCartItems(userId);
  return { message: "Cart cleared" };
}
