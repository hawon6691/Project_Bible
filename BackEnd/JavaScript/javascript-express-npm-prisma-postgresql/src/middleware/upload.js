import multer from "multer";

import { badRequest } from "../utils/http-error.js";

const imageUpload = multer({
  storage: multer.memoryStorage(),
  limits: {
    fileSize: 10 * 1024 * 1024,
  },
  fileFilter(_req, file, cb) {
    if (!["image/jpeg", "image/png", "image/webp", "image/gif"].includes(file.mimetype)) {
      cb(badRequest("file type is not allowed"));
      return;
    }

    cb(null, true);
  },
});

export function uploadImageFile(req, res, next) {
  imageUpload.single("file")(req, res, (err) => {
    if (!err) {
      next();
      return;
    }

    if (err instanceof multer.MulterError && err.code === "LIMIT_FILE_SIZE") {
      next(badRequest("file size must not exceed 10MB"));
      return;
    }

    next(err);
  });
}
