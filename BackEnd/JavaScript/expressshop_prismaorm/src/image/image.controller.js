import { success } from "../utils/response.js";
import { deleteImage, getImageVariants, uploadImage } from "./image.service.js";

export async function uploadImageController(req, res) {
  const data = await uploadImage(req.user.id, req.file, req.body);
  res.status(201).json(success(data));
}

export async function getImageVariantsController(req, res) {
  const data = await getImageVariants(req.params.id);
  res.status(200).json(success(data));
}

export async function deleteImageController(req, res) {
  const data = await deleteImage(req.params.id);
  res.status(200).json(success(data));
}
