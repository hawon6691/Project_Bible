import { getMe, getProfile, getUsers, updateMe } from "./user.service.js";
import { toUserDto, toUserProfileDto } from "./user.mapper.js";
import { success } from "../utils/response.js";

export async function getMeController(req, res) {
  const data = await getMe(req.user);
  res.status(200).json(success(toUserDto(data)));
}

export async function updateMeController(req, res) {
  const data = await updateMe(req.user.id, req.body);
  res.status(200).json(success(toUserDto(data)));
}

export async function getUsersController(req, res) {
  const { items, meta } = await getUsers(req.query);
  res.status(200).json(success(items.map(toUserDto), meta));
}

export async function getProfileController(req, res) {
  const data = await getProfile(req.params.id);
  res.status(200).json(success(toUserProfileDto(data)));
}
