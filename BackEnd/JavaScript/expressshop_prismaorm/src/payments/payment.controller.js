import { createPayment, getPayment, refundPayment } from "./payment.service.js";
import { toPaymentDto } from "./payment.mapper.js";
import { success } from "../utils/response.js";

export async function createPaymentController(req, res) {
  const data = await createPayment(req.user.id, req.body);
  res.status(201).json(success(toPaymentDto(data)));
}

export async function getPaymentController(req, res) {
  const data = await getPayment(req.user.id, req.params.id);
  res.status(200).json(success(toPaymentDto(data)));
}

export async function refundPaymentController(req, res) {
  const data = await refundPayment(req.user.id, req.params.id);
  res.status(200).json(success(toPaymentDto(data)));
}
