import {
  createAddress as createAddressRecord,
  deleteAddressById,
  findAddress,
  findAddresses,
  resetDefaultAddresses,
  updateAddressById,
} from "./address.repository.js";
import { badRequest, notFound } from "../utils/http-error.js";

export async function getAddresses(userId) {
  const items = await findAddresses(userId);
  return { items, meta: { total: items.length } };
}

export async function createAddress(userId, payload) {
  const { label, recipientName, phone, zipCode, address, addressDetail, isDefault } =
    payload ?? {};
  if (!label || !recipientName || !phone || !zipCode || !address) {
    throw badRequest("label, recipientName, phone, zipCode, address are required");
  }
  if (isDefault) {
    await resetDefaultAddresses(userId);
  }
  return createAddressRecord({
    userId,
    label,
    recipientName,
    phone,
    zipCode,
    address,
    addressDetail: addressDetail ?? null,
    isDefault: Boolean(isDefault),
  });
}

export async function updateAddress(userId, addressId, payload) {
  const existing = await findAddress(userId, addressId);
  if (!existing) throw notFound("Address not found");
  if (payload?.isDefault) {
    await resetDefaultAddresses(userId);
  }
  return updateAddressById(addressId, {
    label: payload?.label ?? undefined,
    recipientName: payload?.recipientName ?? undefined,
    phone: payload?.phone ?? undefined,
    zipCode: payload?.zipCode ?? undefined,
    address: payload?.address ?? undefined,
    addressDetail: payload?.addressDetail ?? undefined,
    isDefault: typeof payload?.isDefault === "boolean" ? payload.isDefault : undefined,
  });
}

export async function deleteAddress(userId, addressId) {
  const deleted = await deleteAddressById(userId, addressId);
  if (deleted.count === 0) throw notFound("Address not found");
  return { message: "Address deleted" };
}
