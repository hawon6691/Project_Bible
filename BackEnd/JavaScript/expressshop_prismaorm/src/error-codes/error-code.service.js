import {
  ERROR_CODE_CATALOG,
  getErrorCodeItem,
} from "./error-code.catalog.js";

export function getErrorCodes() {
  return {
    total: ERROR_CODE_CATALOG.length,
    items: ERROR_CODE_CATALOG,
  };
}

export function getErrorCode(key) {
  return getErrorCodeItem(key);
}
