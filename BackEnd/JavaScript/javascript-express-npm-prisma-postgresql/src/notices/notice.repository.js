import { prisma } from "../prisma.js";

export function findNotices(page, limit) {
  return Promise.all([
    prisma.notice.findMany({
      skip: (page - 1) * limit,
      take: limit,
      orderBy: [{ isPinned: "desc" }, { createdAt: "desc" }],
    }),
    prisma.notice.count(),
  ]);
}

export function findNoticeById(noticeId) {
  return prisma.notice.findUnique({
    where: { id: Number(noticeId) },
  });
}

export function incrementNoticeViewCount(noticeId) {
  return prisma.notice.update({
    where: { id: Number(noticeId) },
    data: { viewCount: { increment: 1 } },
  });
}
