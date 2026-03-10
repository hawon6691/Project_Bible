<?php

namespace App\Http\Controllers\Api\V1\Media;

use OpenApi\Attributes as OA;

use App\Http\Controllers\Api\V1\ApiController;
use App\Modules\Media\Requests\CreatePresignedUrlRequest;
use App\Modules\Media\Requests\UploadMediaRequest;
use App\Modules\Media\Services\MediaService;
use Illuminate\Http\Request;

#[OA\Tag(name: 'Media')]
class MediaController extends ApiController
{
    public function __construct(private readonly MediaService $service) {}

    public function upload(UploadMediaRequest $request)
    {
        return $this->success($this->service->upload($request->user(), $request->validated()), [], 201);
    }

    public function presignedUrl(CreatePresignedUrlRequest $request)
    {
        return $this->success($this->service->presignedUrl($request->validated()), [], 201);
    }

    public function stream(int $id)
    {
        return $this->success($this->service->stream($id));
    }

    public function destroy(Request $request, int $id)
    {
        return $this->success($this->service->delete($request->user(), $id));
    }

    public function metadata(int $id)
    {
        return $this->success($this->service->metadata($id));
    }
}
