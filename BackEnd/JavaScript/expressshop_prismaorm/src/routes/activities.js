import { Router } from "express";

import {
  clearRecentViewsController,
  clearSearchHistoriesController,
  deleteSearchHistoryController,
  getRecentViewsController,
  getSearchHistoriesController,
} from "../activity/activity.controller.js";
import { requireAuth } from "../middleware/auth.js";
import { asyncHandler } from "../utils/async-handler.js";

const router = Router();

router.get("/activity/views", requireAuth, asyncHandler(getRecentViewsController));
router.delete("/activity/views", requireAuth, asyncHandler(clearRecentViewsController));
router.get("/activity/searches", requireAuth, asyncHandler(getSearchHistoriesController));
router.delete("/activity/searches", requireAuth, asyncHandler(clearSearchHistoriesController));
router.delete("/activity/searches/:id", requireAuth, asyncHandler(deleteSearchHistoryController));

export default router;
