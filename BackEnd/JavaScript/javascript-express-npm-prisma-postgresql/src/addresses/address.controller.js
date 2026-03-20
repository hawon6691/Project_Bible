import {
  createAddress,
  deleteAddress,
  getAddresses,
  updateAddress,
} from "./address.service.js";
import { toAddressDto } from "./address.mapper.js";
import { success } from "../utils/response.js";

export async function getAddressesController(req, res) {
  const { items, meta } = await getAddresses(req.user.id);
  res.status(200).json(success(items.map(toAddressDto), meta));
}

export async function createAddressController(req, res) {
  const data = await createAddress(req.user.id, req.body);
  res.status(201).json(success(toAddressDto(data)));
}

export async function updateAddressController(req, res) {
  const data = await updateAddress(req.user.id, req.params.id, req.body);
  res.status(200).json(success(toAddressDto(data)));
}

export async function deleteAddressController(req, res) {
  const data = await deleteAddress(req.user.id, req.params.id);
  res.status(200).json(success(data));
}
