import {
  createComment as createCommentRecord,
  createPost as createPostRecord,
  findBoards,
  findPostById,
  findPosts,
  incrementPostCommentCount,
  incrementPostViewCount,
} from "./board.repository.js";
import { badRequest, notFound } from "../utils/http-error.js";

export async function getBoards() {
  const items = await findBoards();
  return { items, meta: { total: items.length } };
}

export async function getPosts(query) {
  const items = await findPosts(query.boardId);
  return { items, meta: { total: items.length } };
}

export async function createPost(userId, payload) {
  const { boardId, title, content } = payload ?? {};
  if (!boardId || !title || !content) {
    throw badRequest("boardId, title, content are required");
  }
  return createPostRecord({
    boardId: Number(boardId),
    userId,
    title,
    content,
  });
}

export async function getPost(postId) {
  const item = await findPostById(postId);
  if (!item || item.deletedAt) {
    throw notFound("Post not found");
  }
  await incrementPostViewCount(postId);
  return { ...item, viewCount: item.viewCount + 1 };
}

export async function createComment(userId, postId, payload) {
  const { content, parentId } = payload ?? {};
  if (!content) {
    throw badRequest("content is required");
  }
  const created = await createCommentRecord({
    postId: Number(postId),
    userId,
    parentId: parentId ? Number(parentId) : null,
    content,
  });
  await incrementPostCommentCount(postId);
  return created;
}
