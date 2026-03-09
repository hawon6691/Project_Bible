<?php

namespace App\Http\Controllers\Api\V1\PcBuilder;

use App\Http\Controllers\Api\V1\ApiController;
use App\Modules\PcBuilder\Requests\StoreCompatibilityRuleRequest;
use App\Modules\PcBuilder\Requests\StorePcBuildPartRequest;
use App\Modules\PcBuilder\Requests\StorePcBuildRequest;
use App\Modules\PcBuilder\Requests\UpdatePcBuildRequest;
use App\Modules\PcBuilder\Services\PcBuilderService;
use Illuminate\Http\Request;

class PcBuildController extends ApiController
{
    public function __construct(private readonly PcBuilderService $service)
    {
    }

    public function index(Request $request)
    {
        return $this->success($this->service->listMine($request->user(), (int) $request->query('page', 1), (int) $request->query('limit', 20)));
    }

    public function store(StorePcBuildRequest $request)
    {
        return $this->success($this->service->create($request->user(), $request->validated()), [], 201);
    }

    public function show(int $id)
    {
        return $this->success($this->service->show($id));
    }

    public function update(UpdatePcBuildRequest $request, int $id)
    {
        return $this->success($this->service->update($request->user(), $id, $request->validated()));
    }

    public function destroy(Request $request, int $id)
    {
        return $this->success($this->service->delete($request->user(), $id));
    }

    public function storePart(StorePcBuildPartRequest $request, int $id)
    {
        return $this->success($this->service->addPart($request->user(), $id, $request->validated()), [], 201);
    }

    public function destroyPart(Request $request, int $id, int $partId)
    {
        return $this->success($this->service->removePart($request->user(), $id, $partId));
    }

    public function compatibility(int $id)
    {
        return $this->success($this->service->compatibility($id));
    }

    public function share(Request $request, int $id)
    {
        return $this->success($this->service->share($request->user(), $id));
    }

    public function shared(string $shareCode)
    {
        return $this->success($this->service->showShared($shareCode));
    }

    public function popular(Request $request)
    {
        return $this->success($this->service->popular((int) $request->query('page', 1), (int) $request->query('limit', 20)));
    }

    public function listRules(Request $request)
    {
        return $this->success($this->service->listRules($request->user()));
    }

    public function storeRule(StoreCompatibilityRuleRequest $request)
    {
        return $this->success($this->service->createRule($request->user(), $request->validated()), [], 201);
    }

    public function updateRule(StoreCompatibilityRuleRequest $request, int $id)
    {
        return $this->success($this->service->updateRule($request->user(), $id, $request->validated()));
    }

    public function destroyRule(Request $request, int $id)
    {
        return $this->success($this->service->deleteRule($request->user(), $id));
    }
}
