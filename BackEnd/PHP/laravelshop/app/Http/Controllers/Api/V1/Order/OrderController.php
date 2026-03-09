<?php

namespace App\Http\Controllers\Api\V1\Order;

use App\Http\Controllers\Api\V1\ApiController;
use App\Modules\Order\Requests\CreateOrderRequest;
use App\Modules\Order\Requests\ListOrdersRequest;
use App\Modules\Order\Requests\UpdateOrderStatusRequest;
use App\Modules\Order\Services\OrderService;

class OrderController extends ApiController
{
    public function __construct(
        private readonly OrderService $orderService,
    ) {}

    public function store(CreateOrderRequest $request)
    {
        return $this->success($this->orderService->create($request->user(), $request->validated()), status: 201);
    }

    public function index(ListOrdersRequest $request)
    {
        return $this->success($this->orderService->listForUser($request->user(), $request->validated()));
    }

    public function show(int $id)
    {
        return $this->success($this->orderService->detailForUser(request()->user(), $id));
    }

    public function cancel(int $id)
    {
        return $this->success($this->orderService->cancel(request()->user(), $id));
    }

    public function adminIndex(ListOrdersRequest $request)
    {
        return $this->success($this->orderService->listForAdmin($request->user(), $request->validated()));
    }

    public function adminUpdateStatus(UpdateOrderStatusRequest $request, int $id)
    {
        return $this->success($this->orderService->updateStatus($request->user(), $id, $request->validated()['status']));
    }
}
