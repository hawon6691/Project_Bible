import { Router } from "express";

import {
  createCrawlerJobController,
  deleteCrawlerJobController,
  getCrawlerJobsController,
  getCrawlerMonitoringController,
  getCrawlerRunsController,
  runCrawlerJobController,
  triggerCrawlerController,
  updateCrawlerJobController,
} from "../crawler/crawler.controller.js";
import {
  validateCreateCrawlerJob,
  validateTriggerCrawler,
  validateUpdateCrawlerJob,
} from "../crawler/crawler.validator.js";
import { requireAuth, requireRole } from "../middleware/auth.js";
import { validate } from "../middleware/validate.js";
import { asyncHandler } from "../utils/async-handler.js";

const router = Router();

router.get("/crawler/admin/jobs", requireAuth, requireRole("ADMIN"), asyncHandler(getCrawlerJobsController));
router.post(
  "/crawler/admin/jobs",
  requireAuth,
  requireRole("ADMIN"),
  validate(validateCreateCrawlerJob),
  asyncHandler(createCrawlerJobController),
);
router.patch(
  "/crawler/admin/jobs/:id",
  requireAuth,
  requireRole("ADMIN"),
  validate(validateUpdateCrawlerJob),
  asyncHandler(updateCrawlerJobController),
);
router.delete(
  "/crawler/admin/jobs/:id",
  requireAuth,
  requireRole("ADMIN"),
  asyncHandler(deleteCrawlerJobController),
);
router.post(
  "/crawler/admin/jobs/:id/run",
  requireAuth,
  requireRole("ADMIN"),
  asyncHandler(runCrawlerJobController),
);
router.post(
  "/crawler/admin/triggers",
  requireAuth,
  requireRole("ADMIN"),
  validate(validateTriggerCrawler),
  asyncHandler(triggerCrawlerController),
);
router.get("/crawler/admin/runs", requireAuth, requireRole("ADMIN"), asyncHandler(getCrawlerRunsController));
router.get(
  "/crawler/admin/monitoring",
  requireAuth,
  requireRole("ADMIN"),
  asyncHandler(getCrawlerMonitoringController),
);

export default router;
