import {
  autocomplete,
  clearRecentKeywords,
  getPopularKeywords,
  getRecentKeywords,
  removeRecentKeyword,
  getSearchIndexStatus,
  getSearchOutboxSummary,
  getSearchWeights,
  reindexAllProducts,
  reindexProduct,
  requeueFailedSearchOutbox,
  saveRecentKeyword,
  search,
  updateSearchPreferences,
  updateSearchWeights,
} from "./search.service.js";
import {
  toAutocompleteCategoryDto,
  toAutocompleteProductDto,
  toPopularKeywordDto,
  toRecentKeywordDto,
  toSearchHitDto,
  toSearchWeightDto,
} from "./search.mapper.js";
import { success } from "../utils/response.js";

export async function searchController(req, res) {
  const { data, meta } = await search(req.query);
  res.status(200).json(
    success(
      {
        ...data,
        hits: data.hits.map(toSearchHitDto),
      },
      meta,
    ),
  );
}

export async function autocompleteController(req, res) {
  const data = await autocomplete(req.query);
  res.status(200).json(
    success({
      keywords: data.keywords,
      products: data.products.map(toAutocompleteProductDto),
      categories: data.categories.map(toAutocompleteCategoryDto),
    }),
  );
}

export async function popularSearchKeywordsController(req, res) {
  const data = await getPopularKeywords(req.query);
  res.status(200).json(success(data.map(toPopularKeywordDto)));
}

export async function saveRecentSearchController(req, res) {
  const data = await saveRecentKeyword(req.user.id, req.body);
  res.status(201).json(success(data.map(toRecentKeywordDto)));
}

export async function getRecentSearchController(req, res) {
  const data = await getRecentKeywords(req.user.id);
  res.status(200).json(success(data.map(toRecentKeywordDto)));
}

export async function deleteRecentSearchController(req, res) {
  const data = await removeRecentKeyword(req.user.id, req.params.id);
  res.status(200).json(success(data));
}

export async function clearRecentSearchController(req, res) {
  const data = await clearRecentKeywords(req.user.id);
  res.status(200).json(success(data));
}

export async function updateSearchPreferencesController(req, res) {
  const data = await updateSearchPreferences(req.user.id, req.body);
  res.status(200).json(success(data));
}

export async function getSearchWeightsController(_req, res) {
  const data = await getSearchWeights();
  res.status(200).json(success(toSearchWeightDto(data)));
}

export async function updateSearchWeightsController(req, res) {
  const data = await updateSearchWeights(req.body);
  res.status(200).json(success(toSearchWeightDto(data)));
}

export async function searchIndexStatusController(_req, res) {
  const data = await getSearchIndexStatus();
  res.status(200).json(success(data));
}

export async function reindexAllProductsController(_req, res) {
  const data = await reindexAllProducts();
  res.status(201).json(success(data));
}

export async function reindexProductController(req, res) {
  const data = await reindexProduct(req.params.id);
  res.status(201).json(success(data));
}

export async function searchOutboxSummaryController(_req, res) {
  const data = await getSearchOutboxSummary();
  res.status(200).json(success(data));
}

export async function requeueFailedSearchOutboxController(req, res) {
  const data = await requeueFailedSearchOutbox(req.query);
  res.status(200).json(success(data));
}
