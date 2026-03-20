import {
  createSupportTicket as createSupportTicketRecord,
  createTicketReply,
  findAdminSupportTickets,
  findSupportTicket,
  findSupportTicketById,
  findSupportTickets,
  updateSupportTicket,
} from "./ticket.repository.js";
import { badRequest, notFound } from "../utils/http-error.js";

export async function getSupportTickets(userId) {
  const items = await findSupportTickets(userId);
  return { items, meta: { total: items.length } };
}

export async function createSupportTicket(userId, payload) {
  const { category, title, content, attachmentUrls } = payload ?? {};
  if (!category || !title || !content) {
    throw badRequest("category, title, content are required");
  }
  return createSupportTicketRecord({
    ticketNumber: `TCK-${Date.now()}`,
    userId,
    category,
    title,
    content,
    attachmentUrls: attachmentUrls ?? null,
  });
}

export async function getSupportTicket(userId, ticketId) {
  const item = await findSupportTicket(userId, ticketId);
  if (!item) {
    throw notFound("Support ticket not found");
  }
  return item;
}

export async function createSupportReply(user, ticketId, payload) {
  const { content } = payload ?? {};
  if (!content) {
    throw badRequest("content is required");
  }
  const item = await findSupportTicketById(ticketId);
  if (!item) {
    throw notFound("Support ticket not found");
  }
  if (item.userId !== user.id && user.role !== "ADMIN") {
    throw notFound("Support ticket not found");
  }
  return createTicketReply({
    ticketId: Number(ticketId),
    userId: user.id,
    content,
    isAdmin: user.role === "ADMIN",
  });
}

export async function getAdminSupportTickets() {
  const items = await findAdminSupportTickets();
  return { items, meta: { total: items.length } };
}

export async function updateSupportTicketStatus(ticketId, payload) {
  const { status } = payload ?? {};
  if (!status) {
    throw badRequest("status is required");
  }
  return updateSupportTicket(ticketId, { status });
}
