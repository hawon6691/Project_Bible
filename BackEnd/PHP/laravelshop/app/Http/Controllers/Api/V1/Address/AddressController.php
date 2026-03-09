<?php

namespace App\Http\Controllers\Api\V1\Address;

use App\Http\Controllers\Api\V1\ApiController;
use App\Modules\Address\Requests\StoreAddressRequest;
use App\Modules\Address\Requests\UpdateAddressRequest;
use App\Modules\Address\Services\AddressService;

class AddressController extends ApiController
{
    public function __construct(
        private readonly AddressService $addressService,
    ) {}

    public function index()
    {
        return $this->success($this->addressService->list(request()->user()));
    }

    public function store(StoreAddressRequest $request)
    {
        return $this->success($this->addressService->create($request->user(), $request->validated()), status: 201);
    }

    public function update(UpdateAddressRequest $request, int $id)
    {
        return $this->success($this->addressService->update($request->user(), $id, $request->validated()));
    }

    public function destroy(int $id)
    {
        return $this->success($this->addressService->delete(request()->user(), $id));
    }
}
