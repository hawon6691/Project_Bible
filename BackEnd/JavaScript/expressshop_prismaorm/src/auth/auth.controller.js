import {
  login,
  logout,
  refreshTokens,
  sanitizeAuthUser,
  signup,
} from "./auth.service.js";
import {
  toAuthTokenDto,
  toAuthUserDto,
  toSignupResultDto,
} from "./auth.mapper.js";
import { success } from "../utils/response.js";

export async function signupController(req, res) {
  const data = await signup(req.body);
  res.status(201).json(success(toSignupResultDto(data)));
}

export async function loginController(req, res) {
  const data = await login(req.body);
  res.status(200).json(success(toAuthTokenDto(data)));
}

export async function logoutController(req, res) {
  const data = await logout(req.user.id);
  res.status(200).json(success(data));
}

export async function refreshController(req, res) {
  const data = await refreshTokens(req.body);
  res.status(200).json(success(toAuthTokenDto(data)));
}

export async function meController(req, res) {
  res.status(200).json(success(toAuthUserDto(sanitizeAuthUser(req.user))));
}
