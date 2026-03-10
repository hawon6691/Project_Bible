<?php

namespace App\Http\Controllers\Api\V1\Image;

use App\Http\Controllers\Api\V1\ApiController;
use App\Modules\Image\Requests\UploadImageRequest;
use App\Modules\Image\Services\ImageService;
use OpenApi\Attributes as OA;

#[OA\Tag(name: 'Image')]
class ImageController extends ApiController
{
    public function __construct(
        private readonly ImageService $imageService,
    ) {}

    public function upload(UploadImageRequest $request)
    {
        return $this->success($this->imageService->upload($request->user(), $request->file('file'), $request->string('category')->toString()), status: 201);
    }

    public function variants(int $id)
    {
        return $this->success($this->imageService->variants($id));
    }

    public function destroy(int $id)
    {
        return $this->success($this->imageService->remove(request()->user(), $id));
    }
}
