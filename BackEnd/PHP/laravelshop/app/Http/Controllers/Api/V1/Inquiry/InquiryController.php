<?php

namespace App\Http\Controllers\Api\V1\Inquiry;

use App\Http\Controllers\Api\V1\ApiController;
use App\Modules\Inquiry\Requests\AnswerInquiryRequest;
use App\Modules\Inquiry\Requests\StoreInquiryRequest;
use App\Modules\Inquiry\Services\InquiryService;

class InquiryController extends ApiController
{
    public function __construct(
        private readonly InquiryService $inquiryService,
    ) {}

    public function index(int $productId)
    {
        return $this->success($this->inquiryService->listByProduct($productId));
    }

    public function store(StoreInquiryRequest $request, int $productId)
    {
        return $this->success($this->inquiryService->create($request->user(), $productId, [
            ...$request->validated(),
            'isSecret' => (bool) $request->input('isSecret', $request->input('is_secret', false)),
        ]), status: 201);
    }

    public function answer(AnswerInquiryRequest $request, int $id)
    {
        return $this->success($this->inquiryService->answer($request->user(), $id, $request->validated()));
    }

    public function mine()
    {
        return $this->success($this->inquiryService->myInquiries(request()->user()));
    }

    public function destroy(int $id)
    {
        return $this->success($this->inquiryService->delete(request()->user(), $id));
    }
}
