import { Router } from "express";

import { getCategoriesController } from "../categories/category.controller.js";
import { asyncHandler } from "../utils/async-handler.js";

const router = Router();

router.get("/categories", asyncHandler(getCategoriesController));

export default router;
