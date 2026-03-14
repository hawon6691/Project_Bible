<?php

namespace App\Http\Controllers\Api\V1\I18n;

use App\Http\Controllers\Api\V1\ApiController;
use App\Modules\I18n\Requests\UpsertExchangeRateRequest;
use App\Modules\I18n\Requests\UpsertTranslationRequest;
use App\Modules\I18n\Services\I18nService;
use OpenApi\Attributes as OA;

#[OA\Tag(name: 'I18n')]
class I18nController extends ApiController
{
    public function __construct(
        private readonly I18nService $i18nService,
    ) {}

    public function translations()
    {
        return $this->success($this->i18nService->translations(request()->query('locale'), request()->query('namespace')));
    }

    public function upsertTranslation(UpsertTranslationRequest $request)
    {
        return $this->success($this->i18nService->upsertTranslation($request->user(), $request->validated()));
    }

    public function deleteTranslation(int $id)
    {
        return $this->success($this->i18nService->deleteTranslation(request()->user(), $id));
    }

    public function exchangeRates()
    {
        return $this->success($this->i18nService->exchangeRates());
    }

    public function upsertExchangeRate(UpsertExchangeRateRequest $request)
    {
        return $this->success($this->i18nService->upsertExchangeRate($request->user(), $request->validated()));
    }

    public function convert()
    {
        return $this->success($this->i18nService->convert(
            (float) request()->query('amount'),
            (string) request()->query('from'),
            (string) request()->query('to'),
        ));
    }
}
