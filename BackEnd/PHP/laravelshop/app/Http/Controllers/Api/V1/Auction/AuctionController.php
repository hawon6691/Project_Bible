<?php

namespace App\Http\Controllers\Api\V1\Auction;

use App\Http\Controllers\Api\V1\ApiController;
use App\Modules\Auction\Requests\StoreAuctionBidRequest;
use App\Modules\Auction\Requests\StoreAuctionRequest;
use App\Modules\Auction\Requests\UpdateAuctionBidRequest;
use App\Modules\Auction\Services\AuctionService;
use Illuminate\Http\Request;

class AuctionController extends ApiController
{
    public function __construct(private readonly AuctionService $service)
    {
    }

    public function store(StoreAuctionRequest $request)
    {
        return $this->success($this->service->create($request->user(), $request->validated()), [], 201);
    }

    public function index(Request $request)
    {
        return $this->success($this->service->list($request->query('status'), $request->query('categoryId') ? (int) $request->query('categoryId') : null, (int) $request->query('page', 1), (int) $request->query('limit', 20)));
    }

    public function show(int $id)
    {
        return $this->success($this->service->detail($id));
    }

    public function storeBid(StoreAuctionBidRequest $request, int $id)
    {
        return $this->success($this->service->createBid($request->user(), $id, $request->validated()), [], 201);
    }

    public function selectBid(Request $request, int $id, int $bidId)
    {
        return $this->success($this->service->selectBid($request->user(), $id, $bidId));
    }

    public function destroy(Request $request, int $id)
    {
        return $this->success($this->service->cancel($request->user(), $id));
    }

    public function updateBid(UpdateAuctionBidRequest $request, int $id, int $bidId)
    {
        return $this->success($this->service->updateBid($request->user(), $id, $bidId, $request->validated()));
    }

    public function destroyBid(Request $request, int $id, int $bidId)
    {
        return $this->success($this->service->deleteBid($request->user(), $id, $bidId));
    }
}
