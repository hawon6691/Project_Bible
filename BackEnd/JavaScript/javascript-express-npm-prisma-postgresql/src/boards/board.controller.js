import {
  createComment,
  createPost,
  getBoards,
  getPost,
  getPosts,
} from "./board.service.js";
import {
  toBoardDto,
  toCommentDto,
  toPostDetailDto,
  toPostSummaryDto,
} from "./board.mapper.js";
import { success } from "../utils/response.js";

export async function getBoardsController(_req, res) {
  const { items, meta } = await getBoards();
  res.status(200).json(success(items.map(toBoardDto), meta));
}

export async function getPostsController(req, res) {
  const { items, meta } = await getPosts(req.query);
  res.status(200).json(success(items.map(toPostSummaryDto), meta));
}

export async function createPostController(req, res) {
  const data = await createPost(req.user.id, req.body);
  res.status(201).json(success(toPostSummaryDto(data)));
}

export async function getPostController(req, res) {
  const data = await getPost(req.params.id);
  res.status(200).json(success(toPostDetailDto(data)));
}

export async function createCommentController(req, res) {
  const data = await createComment(req.user.id, req.params.id, req.body);
  res.status(201).json(success(toCommentDto(data)));
}
