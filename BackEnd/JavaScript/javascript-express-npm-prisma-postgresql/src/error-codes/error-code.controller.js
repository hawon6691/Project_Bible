import { getErrorCode, getErrorCodes } from "./error-code.service.js";
import { success } from "../utils/response.js";

export function getErrorCodesController(_req, res) {
  res.status(200).json(success(getErrorCodes()));
}

export function getErrorCodeController(req, res) {
  res.status(200).json(success(getErrorCode(req.params.key)));
}
