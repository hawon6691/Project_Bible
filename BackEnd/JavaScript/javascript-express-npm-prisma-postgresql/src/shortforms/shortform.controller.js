import {
  createComment,
  createShortformItem,
  deleteShortform,
  getComments,
  getShortformDetail,
  getShortformFeed,
  getShortformRanking,
  getShortformsByUser,
  getTranscodeStatus,
  retryTranscode,
  toggleShortformLike,
} from "./shortform.service.js";
import { toShortformCommentDto, toShortformDto } from "./shortform.mapper.js";
import { success } from "../utils/response.js";

export async function createShortformController(req, res) {
  const data = await createShortformItem(req.user.id, req.body);
  res.status(201).json(success(toShortformDto(data)));
}

export async function getShortformsController(req, res) {
  const { items, meta } = await getShortformFeed(req.query);
  res.status(200).json(success(items.map(toShortformDto), meta));
}

export async function getShortformController(req, res) {
  const data = await getShortformDetail(req.params.id);
  res.status(200).json(success(toShortformDto(data)));
}

export async function toggleShortformLikeController(req, res) {
  const data = await toggleShortformLike(req.user.id, req.params.id);
  res.status(200).json(success(data));
}

export async function createShortformCommentController(req, res) {
  const data = await createComment(req.user.id, req.params.id, req.body);
  res.status(201).json(success(toShortformCommentDto(data)));
}

export async function getShortformCommentsController(req, res) {
  const { items, meta } = await getComments(req.params.id, req.query);
  res.status(200).json(success(items.map(toShortformCommentDto), meta));
}

export async function getShortformRankingController(req, res) {
  const { items, meta } = await getShortformRanking(req.query);
  res.status(200).json(success(items.map(toShortformDto), meta));
}

export async function getTranscodeStatusController(req, res) {
  const data = await getTranscodeStatus(req.params.id);
  res.status(200).json(success(data));
}

export async function retryTranscodeController(req, res) {
  const data = await retryTranscode(req.user, req.params.id);
  res.status(200).json(success(data));
}

export async function deleteShortformController(req, res) {
  const data = await deleteShortform(req.user, req.params.id);
  res.status(200).json(success(data));
}

export async function getUserShortformsController(req, res) {
  const { items, meta } = await getShortformsByUser(req.params.userId, req.query);
  res.status(200).json(success(items.map(toShortformDto), meta));
}
