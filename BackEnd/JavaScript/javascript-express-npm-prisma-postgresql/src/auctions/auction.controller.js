import {
  cancelAuction,
  createAuctionRequest,
  createBid,
  deleteBid,
  getAuction,
  getAuctions,
  selectBid,
  updateBid,
} from "./auction.service.js";
import { success } from "../utils/response.js";

export async function getAuctionsController(req, res) {
  const { items, meta } = await getAuctions(req.query);
  res.status(200).json(success(items, meta));
}

export async function getAuctionController(req, res) {
  const data = await getAuction(req.params.id);
  res.status(200).json(success(data));
}

export async function createAuctionController(req, res) {
  const data = await createAuctionRequest(req.user.id, req.body);
  res.status(201).json(success(data));
}

export async function createAuctionBidController(req, res) {
  const data = await createBid(req.user.id, req.params.id, req.body);
  res.status(201).json(success(data));
}

export async function selectAuctionBidController(req, res) {
  const data = await selectBid(req.user.id, req.params.id, req.params.bidId);
  res.status(200).json(success(data));
}

export async function cancelAuctionController(req, res) {
  const data = await cancelAuction(req.user.id, req.params.id);
  res.status(200).json(success(data));
}

export async function updateAuctionBidController(req, res) {
  const data = await updateBid(req.user.id, req.params.id, req.params.bidId, req.body);
  res.status(200).json(success(data));
}

export async function deleteAuctionBidController(req, res) {
  const data = await deleteBid(req.user.id, req.params.id, req.params.bidId);
  res.status(200).json(success(data));
}
