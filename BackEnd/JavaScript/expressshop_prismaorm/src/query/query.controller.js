import {
  findQueryProductDetail,
  findQueryProducts,
  rebuildQueryProducts,
  syncQueryProduct,
} from "./query.service.js";
import { success } from "../utils/response.js";

export async function findQueryProductsController(req, res) {
  const { data, meta } = await findQueryProducts(req.query);
  res.status(200).json(success(data, meta));
}

export async function findQueryProductDetailController(req, res) {
  const data = await findQueryProductDetail(req.params.productId);
  res.status(200).json(success(data));
}

export async function syncQueryProductController(req, res) {
  const data = await syncQueryProduct(req.params.productId);
  res.status(200).json(success(data));
}

export async function rebuildQueryProductsController(_req, res) {
  const data = await rebuildQueryProducts();
  res.status(200).json(success(data));
}
