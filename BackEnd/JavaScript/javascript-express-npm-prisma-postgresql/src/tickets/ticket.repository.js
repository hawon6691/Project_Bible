import { prisma } from "../prisma.js";

export function findSupportTickets(userId) {
  return prisma.supportTicket.findMany({
    where: { userId },
    orderBy: { id: "desc" },
  });
}

export function createSupportTicket(data) {
  return prisma.supportTicket.create({ data });
}

export function findSupportTicket(userId, ticketId) {
  return prisma.supportTicket.findFirst({
    where: { id: Number(ticketId), userId },
    include: {
      replies: {
        include: {
          user: {
            select: { id: true, name: true, role: true },
          },
        },
        orderBy: { id: "asc" },
      },
    },
  });
}

export function findSupportTicketById(ticketId) {
  return prisma.supportTicket.findUnique({
    where: { id: Number(ticketId) },
  });
}

export function createTicketReply(data) {
  return prisma.ticketReply.create({ data });
}

export function findAdminSupportTickets() {
  return prisma.supportTicket.findMany({
    include: {
      user: {
        select: { id: true, email: true, name: true },
      },
    },
    orderBy: { id: "desc" },
  });
}

export function updateSupportTicket(ticketId, data) {
  return prisma.supportTicket.update({
    where: { id: Number(ticketId) },
    data,
  });
}
