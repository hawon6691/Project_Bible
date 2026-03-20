import {
  addCartItem,
  clearCart,
  deleteCartItem,
  getCart,
  updateCartItem,
} from "./cart.service.js";
import { toCartItemDto } from "./cart.mapper.js";
import { success } from "../utils/response.js";

export async function getCartController(req, res) {
  const { items, meta } = await getCart(req.user.id);
  res.status(200).json(success(items.map(toCartItemDto), meta));
}

export async function addCartItemController(req, res) {
  const data = await addCartItem(req.user.id, req.body);
  res.status(201).json(success(toCartItemDto(data)));
}

export async function updateCartItemController(req, res) {
  const data = await updateCartItem(req.user.id, req.params.itemId, req.body);
  res.status(200).json(success(toCartItemDto(data)));
}

export async function deleteCartItemController(req, res) {
  const data = await deleteCartItem(req.user.id, req.params.itemId);
  res.status(200).json(success(data));
}

export async function clearCartController(req, res) {
  const data = await clearCart(req.user.id);
  res.status(200).json(success(data));
}
