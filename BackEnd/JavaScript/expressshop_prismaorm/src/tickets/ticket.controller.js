import {
  createSupportReply,
  createSupportTicket,
  getAdminSupportTickets,
  getSupportTicket,
  getSupportTickets,
  updateSupportTicketStatus,
} from "./ticket.service.js";
import { toTicketDetailDto, toTicketDto, toTicketReplyDto } from "./ticket.mapper.js";
import { success } from "../utils/response.js";

export async function getSupportTicketsController(req, res) {
  const { items, meta } = await getSupportTickets(req.user.id);
  res.status(200).json(success(items.map(toTicketDto), meta));
}

export async function createSupportTicketController(req, res) {
  const data = await createSupportTicket(req.user.id, req.body);
  res.status(201).json(success(toTicketDto(data)));
}

export async function getSupportTicketController(req, res) {
  const data = await getSupportTicket(req.user.id, req.params.id);
  res.status(200).json(success(toTicketDetailDto(data)));
}

export async function createSupportReplyController(req, res) {
  const data = await createSupportReply(req.user, req.params.id, req.body);
  res.status(201).json(success(toTicketReplyDto(data)));
}

export async function getAdminSupportTicketsController(_req, res) {
  const { items, meta } = await getAdminSupportTickets();
  res.status(200).json(success(items.map(toTicketDto), meta));
}

export async function updateSupportTicketStatusController(req, res) {
  const data = await updateSupportTicketStatus(req.params.id, req.body);
  res.status(200).json(success(toTicketDetailDto(data)));
}
