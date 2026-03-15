import { createInquiry, getInquiries } from "./inquiry.service.js";
import { toInquiryDto } from "./inquiry.mapper.js";
import { success } from "../utils/response.js";

export async function getInquiriesController(req, res) {
  const { items, meta } = await getInquiries(req.params.productId);
  res.status(200).json(success(items.map(toInquiryDto), meta));
}

export async function createInquiryController(req, res) {
  const data = await createInquiry(req.user.id, req.params.productId, req.body);
  res.status(201).json(success(toInquiryDto(data)));
}
