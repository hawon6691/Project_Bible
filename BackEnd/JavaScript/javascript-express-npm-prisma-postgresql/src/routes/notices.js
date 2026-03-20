import { Router } from "express";

import {
  getNoticeController,
  getNoticesController,
} from "../notices/notice.controller.js";
import { asyncHandler } from "../utils/async-handler.js";

const router = Router();

router.get("/notices", asyncHandler(getNoticesController));
router.get("/notices/:id", asyncHandler(getNoticeController));

export default router;
