<?php

namespace App\Http\Controllers\Api\V1\Payment;

use App\Http\Controllers\Api\V1\ApiController;
use App\Modules\Payment\Requests\CreatePaymentRequest;
use App\Modules\Payment\Services\PaymentService;

class PaymentController extends ApiController
{
    public function __construct(
        private readonly PaymentService $paymentService,
    ) {
    }

    public function store(CreatePaymentRequest $request)
    {
        return $this->success($this->paymentService->create($request->user(), $request->validated()), status: 201);
    }

    public function show(int $id)
    {
        return $this->success($this->paymentService->detail(request()->user(), $id));
    }

    public function refund(int $id)
    {
        return $this->success($this->paymentService->refund(request()->user(), $id));
    }
}
