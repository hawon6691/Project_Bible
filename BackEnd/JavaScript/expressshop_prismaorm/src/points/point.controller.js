import {
  getPointBalance,
  getPointTransactions,
  grantPoints,
} from "./point.service.js";
import { toPointBalanceDto, toPointTransactionDto } from "./point.mapper.js";
import { success } from "../utils/response.js";

export async function getPointBalanceController(req, res) {
  const data = await getPointBalance(req.user);
  res.status(200).json(success(toPointBalanceDto(data)));
}

export async function getPointTransactionsController(req, res) {
  const { items, meta } = await getPointTransactions(req.user.id);
  res.status(200).json(success(items.map(toPointTransactionDto), meta));
}

export async function grantPointsController(req, res) {
  const data = await grantPoints(req.user.id, req.body);
  res.status(201).json(success(toPointTransactionDto(data)));
}
