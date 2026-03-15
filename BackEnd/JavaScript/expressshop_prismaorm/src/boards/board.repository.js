import { prisma } from "../prisma.js";

export function findBoards() {
  return prisma.board.findMany({
    where: { isActive: true },
    orderBy: [{ sortOrder: "asc" }, { id: "asc" }],
  });
}

export function findPosts(boardId) {
  return prisma.post.findMany({
    where: {
      ...(boardId ? { boardId: Number(boardId) } : {}),
      deletedAt: null,
    },
    include: {
      board: { select: { id: true, name: true, slug: true } },
      user: { select: { id: true, name: true, nickname: true } },
    },
    orderBy: { id: "desc" },
  });
}

export function createPost(data) {
  return prisma.post.create({ data });
}

export function findPostById(postId) {
  return prisma.post.findUnique({
    where: { id: Number(postId) },
    include: {
      board: true,
      user: { select: { id: true, name: true, nickname: true } },
      comments: {
        where: { deletedAt: null },
        include: {
          user: { select: { id: true, name: true, nickname: true } },
        },
        orderBy: { id: "asc" },
      },
    },
  });
}

export function incrementPostViewCount(postId) {
  return prisma.post.update({
    where: { id: Number(postId) },
    data: { viewCount: { increment: 1 } },
  });
}

export function createComment(data) {
  return prisma.comment.create({ data });
}

export function incrementPostCommentCount(postId) {
  return prisma.post.update({
    where: { id: Number(postId) },
    data: { commentCount: { increment: 1 } },
  });
}
