<?php

namespace App\Http\Controllers\Api\V1\Spec;

use App\Http\Controllers\Api\V1\ApiController;
use App\Modules\Spec\Requests\ListSpecDefinitionsRequest;
use App\Modules\Spec\Requests\SetProductSpecsRequest;
use App\Modules\Spec\Requests\StoreSpecDefinitionRequest;
use App\Modules\Spec\Requests\UpdateSpecDefinitionRequest;
use App\Modules\Spec\Services\SpecService;

class SpecController extends ApiController
{
    public function __construct(
        private readonly SpecService $specService,
    ) {
    }

    public function listDefinitions(ListSpecDefinitionsRequest $request)
    {
        return $this->success($this->specService->listDefinitions($request->validated()));
    }

    public function storeDefinition(StoreSpecDefinitionRequest $request)
    {
        return $this->success($this->specService->createDefinition($request->user(), $request->validated()), status: 201);
    }

    public function updateDefinition(UpdateSpecDefinitionRequest $request, int $id)
    {
        return $this->success($this->specService->updateDefinition($request->user(), $id, $request->validated()));
    }

    public function deleteDefinition(int $id)
    {
        return $this->success($this->specService->deleteDefinition(request()->user(), $id));
    }

    public function getProductSpecs(int $id)
    {
        return $this->success($this->specService->getProductSpecs($id));
    }

    public function setProductSpecs(SetProductSpecsRequest $request, int $id)
    {
        return $this->success($this->specService->setProductSpecs($request->user(), $id, $request->validated()));
    }
}
