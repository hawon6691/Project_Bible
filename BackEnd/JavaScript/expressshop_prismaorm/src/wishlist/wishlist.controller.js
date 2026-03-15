import { getWishlist, removeWishlist, toggleWishlist } from "./wishlist.service.js";
import { toWishlistItemDto } from "./wishlist.mapper.js";
import { success } from "../utils/response.js";

export async function getWishlistController(req, res) {
  const { items, meta } = await getWishlist(req.user.id, req.query);
  res.status(200).json(success(items.map(toWishlistItemDto), meta));
}

export async function toggleWishlistController(req, res) {
  const data = await toggleWishlist(req.user.id, req.params.productId);
  res.status(data.wishlisted ? 201 : 200).json(success(data));
}

export async function removeWishlistController(req, res) {
  const data = await removeWishlist(req.user.id, req.params.productId);
  res.status(200).json(success(data));
}
