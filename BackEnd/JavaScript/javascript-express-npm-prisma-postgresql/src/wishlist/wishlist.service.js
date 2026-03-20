import {
  createWishlistItem,
  deleteWishlistItem,
  findWishlist,
  findWishlistItem,
} from "./wishlist.repository.js";

export async function getWishlist(userId, query) {
  const page = Number(query.page ?? 1);
  const limit = Math.min(Number(query.limit ?? 20), 100);
  const [items, total] = await findWishlist(userId, page, limit);
  return { items, meta: { page, limit, total } };
}

export async function toggleWishlist(userId, productId) {
  const existing = await findWishlistItem(userId, productId);
  if (existing) {
    await deleteWishlistItem(userId, productId);
    return { wishlisted: false };
  }
  await createWishlistItem(userId, productId);
  return { wishlisted: true };
}

export async function removeWishlist(userId, productId) {
  await deleteWishlistItem(userId, productId);
  return { message: "Wishlist item deleted" };
}
