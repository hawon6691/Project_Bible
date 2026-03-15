import {
  closeRoom,
  createRoom,
  getRoomMessages,
  getRooms,
} from "./chat.service.js";
import { toChatMessageDto, toChatRoomDto } from "./chat.mapper.js";
import { success } from "../utils/response.js";

export async function createRoomController(req, res) {
  const data = await createRoom(req.user, req.body);
  res.status(201).json(success(toChatRoomDto(data)));
}

export async function getRoomsController(req, res) {
  const { items, meta } = await getRooms(req.user);
  res.status(200).json(success(items.map(toChatRoomDto), meta));
}

export async function getRoomMessagesController(req, res) {
  const { items, meta } = await getRoomMessages(req.user, req.params.id, req.query);
  res.status(200).json(success(items.map(toChatMessageDto), meta));
}

export async function closeRoomController(req, res) {
  const data = await closeRoom(req.user, req.params.id);
  res.status(200).json(success(toChatRoomDto(data)));
}
