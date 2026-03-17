import {
  createAdminSeller,
  createSellerReview,
  deleteAdminSeller,
  deleteSellerReview,
  getSeller,
  getSellerReviews,
  getSellerTrust,
  getSellers,
  updateSellerReview,
  updateAdminSeller,
} from "./seller.service.js";
import { toSellerDto } from "./seller.mapper.js";
import { success } from "../utils/response.js";

export async function getSellersController(req, res) {
  const { items, meta } = await getSellers(req.query);
  res.status(200).json(success(items.map(toSellerDto), meta));
}

export async function getSellerController(req, res) {
  const data = await getSeller(req.params.id);
  res.status(200).json(success(toSellerDto(data)));
}

export async function getSellerTrustController(req, res) {
  const data = await getSellerTrust(req.params.id);
  res.status(200).json(success(data));
}

export async function getSellerReviewsController(req, res) {
  const { items, meta } = await getSellerReviews(req.params.id, req.query);
  res.status(200).json(success(items, meta));
}

export async function createSellerReviewController(req, res) {
  const data = await createSellerReview(req.user.id, req.params.id, req.body);
  res.status(201).json(success(data));
}

export async function createSellerController(req, res) {
  const data = await createAdminSeller(req.body);
  res.status(201).json(success(toSellerDto(data)));
}

export async function updateSellerController(req, res) {
  const data = await updateAdminSeller(req.params.id, req.body);
  res.status(200).json(success(toSellerDto(data)));
}

export async function deleteSellerController(req, res) {
  const data = await deleteAdminSeller(req.params.id);
  res.status(200).json(success(data));
}

export async function updateSellerReviewController(req, res) {
  const data = await updateSellerReview(req.user, req.params.id, req.body ?? {});
  res.status(200).json(success(data));
}

export async function deleteSellerReviewController(req, res) {
  const data = await deleteSellerReview(req.user, req.params.id);
  res.status(200).json(success(data));
}
