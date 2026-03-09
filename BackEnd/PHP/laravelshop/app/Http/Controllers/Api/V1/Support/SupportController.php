<?php

namespace App\Http\Controllers\Api\V1\Support;

use App\Http\Controllers\Api\V1\ApiController;
use App\Modules\Support\Requests\ReplySupportTicketRequest;
use App\Modules\Support\Requests\StoreSupportTicketRequest;
use App\Modules\Support\Requests\UpdateSupportTicketStatusRequest;
use App\Modules\Support\Services\SupportService;

class SupportController extends ApiController
{
    public function __construct(
        private readonly SupportService $supportService,
    ) {
    }

    public function index()
    {
        return $this->success($this->supportService->listForUser(request()->user()));
    }

    public function store(StoreSupportTicketRequest $request)
    {
        return $this->success($this->supportService->create($request->user(), $request->validated()), status: 201);
    }

    public function show(int $id)
    {
        return $this->success($this->supportService->show(request()->user(), $id));
    }

    public function reply(ReplySupportTicketRequest $request, int $id)
    {
        return $this->success($this->supportService->reply($request->user(), $id, $request->validated()));
    }

    public function adminIndex()
    {
        return $this->success($this->supportService->adminList(request()->query('status')));
    }

    public function adminUpdateStatus(UpdateSupportTicketStatusRequest $request, int $id)
    {
        return $this->success($this->supportService->adminUpdateStatus($request->user(), $id, $request->validated()));
    }
}
