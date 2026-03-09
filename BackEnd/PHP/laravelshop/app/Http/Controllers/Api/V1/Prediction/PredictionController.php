<?php

namespace App\Http\Controllers\Api\V1\Prediction;

use App\Http\Controllers\Api\V1\ApiController;
use App\Modules\Prediction\Services\PredictionService;

class PredictionController extends ApiController
{
    public function __construct(
        private readonly PredictionService $predictionService,
    ) {}

    public function priceTrend(int $productId)
    {
        return $this->success($this->predictionService->predictProductPrice(
            $productId,
            (int) request()->query('days', 30),
        ));
    }
}
