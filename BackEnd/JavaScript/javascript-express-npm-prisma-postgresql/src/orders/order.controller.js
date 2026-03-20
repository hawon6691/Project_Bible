import {
  cancelOrder,
  createOrder,
  getAdminOrders,
  getOrder,
  getOrders,
  updateOrderStatus,
} from "./order.service.js";
import { toOrderDetailDto, toOrderSummaryDto } from "./order.mapper.js";
import { success } from "../utils/response.js";

export async function getOrdersController(req, res) {
  const { items, meta } = await getOrders(req.user.id);
  res.status(200).json(success(items.map(toOrderSummaryDto), meta));
}

export async function getOrderController(req, res) {
  const data = await getOrder(req.user.id, req.params.id);
  res.status(200).json(success(toOrderDetailDto(data)));
}

export async function createOrderController(req, res) {
  const data = await createOrder(req.user.id, req.body);
  res.status(201).json(success(toOrderDetailDto(data)));
}

export async function cancelOrderController(req, res) {
  const data = await cancelOrder(req.user.id, req.params.id);
  res.status(200).json(success(data));
}

export async function getAdminOrdersController(_req, res) {
  const { items, meta } = await getAdminOrders();
  res.status(200).json(success(items.map(toOrderSummaryDto), meta));
}

export async function updateOrderStatusController(req, res) {
  const data = await updateOrderStatus(req.params.id, req.body);
  res.status(200).json(success(toOrderDetailDto(data)));
}
