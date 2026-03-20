import { getNotice, getNotices } from "./notice.service.js";
import { toNoticeDto } from "./notice.mapper.js";
import { success } from "../utils/response.js";

export async function getNoticesController(req, res) {
  const { items, meta } = await getNotices(req.query);
  res.status(200).json(success(items.map(toNoticeDto), meta));
}

export async function getNoticeController(req, res) {
  const data = await getNotice(req.params.id);
  res.status(200).json(success(toNoticeDto(data)));
}
