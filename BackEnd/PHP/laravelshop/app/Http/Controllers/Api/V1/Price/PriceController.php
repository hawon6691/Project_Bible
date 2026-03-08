<?php

namespace App\Http\Controllers\Api\V1\Price;

use App\Http\Controllers\Api\V1\ApiController;
use App\Modules\Price\Requests\CreatePriceAlertRequest;
use App\Modules\Price\Requests\PriceHistoryRequest;
use App\Modules\Price\Requests\StorePriceEntryRequest;
use App\Modules\Price\Requests\UpdatePriceEntryRequest;
use App\Modules\Price\Services\PriceService;

class PriceController extends ApiController
{
    public function __construct(
        private readonly PriceService $priceService,
    ) {
    }

    public function listProductPrices(int $id)
    {
        return $this->success($this->priceService->listProductPrices($id));
    }

    public function createProductPrice(StorePriceEntryRequest $request, int $id)
    {
        return $this->success($this->priceService->createPrice($request->user(), $id, $request->validated()), status: 201);
    }

    public function updatePrice(UpdatePriceEntryRequest $request, int $id)
    {
        return $this->success($this->priceService->updatePrice($request->user(), $id, $request->validated()));
    }

    public function deletePrice(int $id)
    {
        return $this->success($this->priceService->deletePrice(request()->user(), $id));
    }

    public function priceHistory(PriceHistoryRequest $request, int $id)
    {
        return $this->success($this->priceService->getPriceHistory($id));
    }

    public function listAlerts()
    {
        return $this->success($this->priceService->listAlerts(request()->user()));
    }

    public function createAlert(CreatePriceAlertRequest $request)
    {
        return $this->success($this->priceService->createAlert($request->user(), $request->validated()), status: 201);
    }

    public function deleteAlert(int $id)
    {
        return $this->success($this->priceService->deleteAlert(request()->user(), $id));
    }
}
