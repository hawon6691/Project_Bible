<?php

namespace App\Http\Controllers\Api\V1\AdminSettings;

use OpenApi\Attributes as OA;

use App\Http\Controllers\Api\V1\ApiController;
use App\Modules\AdminSettings\Requests\UpdateExtensionsRequest;
use App\Modules\AdminSettings\Requests\UpdateReviewPolicyRequest;
use App\Modules\AdminSettings\Requests\UpdateUploadLimitsRequest;
use App\Modules\AdminSettings\Services\AdminSettingsService;
use Illuminate\Http\Request;

#[OA\Tag(name: 'AdminSettings')]
class AdminSettingsController extends ApiController
{
    public function __construct(private readonly AdminSettingsService $service) {}

    public function extensions(Request $request)
    {
        return $this->success($this->service->extensions($request->user()));
    }

    public function updateExtensions(UpdateExtensionsRequest $request)
    {
        return $this->success($this->service->updateExtensions($request->user(), $request->validated()));
    }

    public function uploadLimits(Request $request)
    {
        return $this->success($this->service->uploadLimits($request->user()));
    }

    public function updateUploadLimits(UpdateUploadLimitsRequest $request)
    {
        return $this->success($this->service->updateUploadLimits($request->user(), $request->validated()));
    }

    public function reviewPolicy(Request $request)
    {
        return $this->success($this->service->reviewPolicy($request->user()));
    }

    public function updateReviewPolicy(UpdateReviewPolicyRequest $request)
    {
        return $this->success($this->service->updateReviewPolicy($request->user(), $request->validated()));
    }
}
