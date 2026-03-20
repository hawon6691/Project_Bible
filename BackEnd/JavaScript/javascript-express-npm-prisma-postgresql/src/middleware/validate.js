import { badRequest } from "../utils/http-error.js";

export function validate(validator) {
  return (req, _res, next) => {
    const message = validator(req);
    if (message) {
      throw badRequest(message);
    }
    next();
  };
}
