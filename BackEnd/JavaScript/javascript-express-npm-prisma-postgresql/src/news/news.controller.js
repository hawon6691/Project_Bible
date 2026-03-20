import {
  createNewsCategoryItem,
  createNewsItem,
  deleteNewsCategoryItem,
  deleteNewsItem,
  getNewsCategories,
  getNewsDetail,
  getNewsList,
  updateNewsItem,
} from "./news.service.js";
import { toNewsCategoryDto, toNewsDetailDto, toNewsDto } from "./news.mapper.js";
import { success } from "../utils/response.js";

export async function getNewsController(req, res) {
  const { items, meta } = await getNewsList(req.query);
  res.status(200).json(success(items.map(toNewsDto), meta));
}

export async function getNewsCategoriesController(_req, res) {
  const { items, meta } = await getNewsCategories();
  res.status(200).json(success(items.map(toNewsCategoryDto), meta));
}

export async function getNewsDetailController(req, res) {
  const data = await getNewsDetail(req.params.id);
  res.status(200).json(success(toNewsDetailDto(data)));
}

export async function createNewsController(req, res) {
  const data = await createNewsItem(req.body);
  res.status(201).json(success(toNewsDetailDto(data)));
}

export async function updateNewsController(req, res) {
  const data = await updateNewsItem(req.params.id, req.body);
  res.status(200).json(success(toNewsDetailDto(data)));
}

export async function deleteNewsController(req, res) {
  const data = await deleteNewsItem(req.params.id);
  res.status(200).json(success(data));
}

export async function createNewsCategoryController(req, res) {
  const data = await createNewsCategoryItem(req.body);
  res.status(201).json(success(toNewsCategoryDto(data)));
}

export async function deleteNewsCategoryController(req, res) {
  const data = await deleteNewsCategoryItem(req.params.id);
  res.status(200).json(success(data));
}
