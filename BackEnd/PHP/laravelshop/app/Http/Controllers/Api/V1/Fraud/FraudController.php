<?php

namespace App\Http\Controllers\Api\V1\Fraud;

use App\Http\Controllers\Api\V1\ApiController;
use App\Modules\Fraud\Services\FraudService;

class FraudController extends ApiController
{
    public function __construct(
        private readonly FraudService $fraudService,
    ) {}

    public function alerts()
    {
        return $this->success($this->fraudService->getAlerts(request()->query('status')));
    }

    public function approve(int $id)
    {
        return $this->success($this->fraudService->approveAlert(request()->user(), $id));
    }

    public function reject(int $id)
    {
        return $this->success($this->fraudService->rejectAlert(request()->user(), $id));
    }

    public function realPrice(int $id)
    {
        return $this->success($this->fraudService->getRealPrice($id, request()->query('sellerId') ? (int) request()->query('sellerId') : null));
    }

    public function effectivePrices(int $productId)
    {
        return $this->success($this->fraudService->getEffectivePrices($productId));
    }

    public function anomalies(int $productId)
    {
        return $this->success($this->fraudService->detectAnomalies($productId));
    }

    public function scan(int $productId)
    {
        return $this->success($this->fraudService->detectAnomalies($productId, true));
    }

    public function flags(int $productId)
    {
        return $this->success($this->fraudService->getFlags($productId));
    }
}
