import { Router } from "express";

import {
  autocompleteController,
  clearRecentSearchController,
  deleteRecentSearchController,
  getRecentSearchController,
  getSearchWeightsController,
  popularSearchKeywordsController,
  reindexAllProductsController,
  reindexProductController,
  requeueFailedSearchOutboxController,
  saveRecentSearchController,
  searchController,
  searchIndexStatusController,
  searchOutboxSummaryController,
  updateSearchPreferencesController,
  updateSearchWeightsController,
} from "../search/search.controller.js";
import {
  validatePopularSearchQuery,
  validateSaveRecentSearch,
  validateSearchAutocompleteQuery,
  validateSearchQuery,
  validateUpdateSearchPreference,
  validateUpdateSearchWeights,
} from "../search/search.validator.js";
import { requireAuth, requireRole } from "../middleware/auth.js";
import { validate } from "../middleware/validate.js";
import { asyncHandler } from "../utils/async-handler.js";

const router = Router();

router.get("/search", validate(validateSearchQuery), asyncHandler(searchController));
router.get("/search/autocomplete", validate(validateSearchAutocompleteQuery), asyncHandler(autocompleteController));
router.get("/search/popular", validate(validatePopularSearchQuery), asyncHandler(popularSearchKeywordsController));

router.post("/search/recent", requireAuth, validate(validateSaveRecentSearch), asyncHandler(saveRecentSearchController));
router.get("/search/recent", requireAuth, asyncHandler(getRecentSearchController));
router.delete("/search/recent/:id", requireAuth, asyncHandler(deleteRecentSearchController));
router.delete("/search/recent", requireAuth, asyncHandler(clearRecentSearchController));
router.patch(
  "/search/preferences",
  requireAuth,
  validate(validateUpdateSearchPreference),
  asyncHandler(updateSearchPreferencesController),
);

router.get("/search/admin/weights", requireAuth, requireRole("ADMIN"), asyncHandler(getSearchWeightsController));
router.patch(
  "/search/admin/weights",
  requireAuth,
  requireRole("ADMIN"),
  validate(validateUpdateSearchWeights),
  asyncHandler(updateSearchWeightsController),
);
router.get("/search/admin/index/status", requireAuth, requireRole("ADMIN"), asyncHandler(searchIndexStatusController));
router.post("/search/admin/index/reindex", requireAuth, requireRole("ADMIN"), asyncHandler(reindexAllProductsController));
router.post(
  "/search/admin/index/products/:id/reindex",
  requireAuth,
  requireRole("ADMIN"),
  asyncHandler(reindexProductController),
);
router.get(
  "/search/admin/index/outbox/summary",
  requireAuth,
  requireRole("ADMIN"),
  asyncHandler(searchOutboxSummaryController),
);
router.post(
  "/search/admin/index/outbox/requeue-failed",
  requireAuth,
  requireRole("ADMIN"),
  asyncHandler(requeueFailedSearchOutboxController),
);

export default router;
