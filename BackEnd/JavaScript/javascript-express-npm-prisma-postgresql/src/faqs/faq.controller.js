import { createFaq, getFaqs } from "./faq.service.js";
import { toFaqDto } from "./faq.mapper.js";
import { success } from "../utils/response.js";

export async function getFaqsController(req, res) {
  const { items, meta } = await getFaqs(req.query);
  res.status(200).json(success(items.map(toFaqDto), meta));
}

export async function createFaqController(req, res) {
  const data = await createFaq(req.body);
  res.status(201).json(success(toFaqDto(data)));
}
