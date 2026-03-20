<?php

namespace App\Modules\Auction\Services;

use App\Common\Exceptions\BusinessException;
use App\Models\Auction;
use App\Models\AuctionBid;
use App\Models\Seller;
use App\Models\User;
use Symfony\Component\HttpFoundation\Response;

class AuctionService
{
    public function create(User $user, array $payload): array
    {
        $auction = Auction::query()->create([
            'user_id' => $user->id,
            'category_id' => $payload['categoryId'] ?? null,
            'title' => $payload['title'],
            'description' => $payload['description'] ?? null,
            'specs' => $payload['specs'] ?? null,
            'budget' => $payload['budget'] ?? null,
            'status' => 'OPEN',
        ]);

        return $this->detail($auction->id);
    }

    public function list(?string $status, ?int $categoryId, int $page, int $limit): array
    {
        $result = Auction::query()
            ->when($status, fn ($q) => $q->where('status', $status))
            ->when($categoryId, fn ($q) => $q->where('category_id', $categoryId))
            ->orderByDesc('id')
            ->paginate($limit, ['*'], 'page', $page);

        return [
            'items' => $result->getCollection()->map(fn (Auction $auction): array => $this->serializeAuction($auction))->all(),
            'pagination' => [
                'page' => $result->currentPage(),
                'limit' => $result->perPage(),
                'total' => $result->total(),
                'totalPages' => $result->lastPage(),
            ],
        ];
    }

    public function detail(int $id): array
    {
        $auction = $this->findAuction($id);
        $bids = AuctionBid::query()->where('auction_id', $id)->orderBy('price')->get()->map(fn (AuctionBid $bid): array => $this->serializeBid($bid))->all();

        return array_merge($this->serializeAuction($auction), ['bids' => $bids]);
    }

    public function createBid(User $sellerUser, int $auctionId, array $payload): array
    {
        $auction = $this->findAuction($auctionId);
        if ($auction->status !== 'OPEN') {
            throw new BusinessException('종료된 역경매입니다.', 'AUCTION_CLOSED', Response::HTTP_BAD_REQUEST);
        }

        $seller = Seller::query()->firstOrCreate([
            'code' => 'seller-user-'.$sellerUser->id,
        ], [
            'name' => $sellerUser->name.' Seller',
            'status' => 'ACTIVE',
            'rating' => 4.5,
        ]);

        $bid = AuctionBid::query()->create([
            'auction_id' => $auctionId,
            'seller_id' => $seller->id,
            'price' => $payload['price'],
            'description' => $payload['description'] ?? null,
            'delivery_days' => $payload['deliveryDays'] ?? null,
        ]);

        return $this->serializeBid($bid);
    }

    public function selectBid(User $owner, int $auctionId, int $bidId): array
    {
        $auction = $this->findOwnedAuction($owner, $auctionId);
        $bid = AuctionBid::query()->where('auction_id', $auction->id)->find($bidId);
        if (! $bid) {
            throw new BusinessException('입찰을 찾을 수 없습니다.', 'AUCTION_BID_NOT_FOUND', Response::HTTP_NOT_FOUND);
        }

        $auction->forceFill(['selected_bid_id' => $bid->id, 'status' => 'CLOSED'])->save();

        return ['message' => '낙찰을 선택했습니다.'];
    }

    public function cancel(User $owner, int $auctionId): array
    {
        $auction = $this->findOwnedAuction($owner, $auctionId);
        $auction->forceFill(['status' => 'CANCELLED'])->save();

        return ['message' => '역경매가 취소되었습니다.'];
    }

    public function updateBid(User $sellerUser, int $auctionId, int $bidId, array $payload): array
    {
        $bid = $this->findSellerBid($sellerUser, $auctionId, $bidId);
        $bid->forceFill([
            'price' => $payload['price'] ?? $bid->price,
            'description' => array_key_exists('description', $payload) ? $payload['description'] : $bid->description,
            'delivery_days' => array_key_exists('deliveryDays', $payload) ? $payload['deliveryDays'] : $bid->delivery_days,
        ])->save();

        return $this->serializeBid($bid);
    }

    public function deleteBid(User $sellerUser, int $auctionId, int $bidId): array
    {
        $bid = $this->findSellerBid($sellerUser, $auctionId, $bidId);
        $bid->delete();

        return ['message' => '입찰이 취소되었습니다.'];
    }

    private function findAuction(int $id): Auction
    {
        $auction = Auction::query()->find($id);
        if (! $auction) {
            throw new BusinessException('역경매를 찾을 수 없습니다.', 'AUCTION_NOT_FOUND', Response::HTTP_NOT_FOUND);
        }

        return $auction;
    }

    private function findOwnedAuction(User $user, int $id): Auction
    {
        $auction = Auction::query()->where('user_id', $user->id)->find($id);
        if (! $auction) {
            throw new BusinessException('역경매를 찾을 수 없습니다.', 'AUCTION_NOT_FOUND', Response::HTTP_NOT_FOUND);
        }

        return $auction;
    }

    private function findSellerBid(User $sellerUser, int $auctionId, int $bidId): AuctionBid
    {
        $seller = Seller::query()->where('code', 'seller-user-'.$sellerUser->id)->first();
        if (! $seller) {
            throw new BusinessException('판매자 정보를 찾을 수 없습니다.', 'SELLER_NOT_FOUND', Response::HTTP_NOT_FOUND);
        }

        $bid = AuctionBid::query()->where('auction_id', $auctionId)->where('seller_id', $seller->id)->find($bidId);
        if (! $bid) {
            throw new BusinessException('입찰을 찾을 수 없습니다.', 'AUCTION_BID_NOT_FOUND', Response::HTTP_NOT_FOUND);
        }

        return $bid;
    }

    private function serializeAuction(Auction $auction): array
    {
        return [
            'id' => $auction->id,
            'userId' => $auction->user_id,
            'categoryId' => $auction->category_id,
            'title' => $auction->title,
            'description' => $auction->description,
            'specs' => $auction->specs,
            'budget' => $auction->budget,
            'status' => $auction->status,
            'selectedBidId' => $auction->selected_bid_id,
            'createdAt' => optional($auction->created_at)?->toISOString(),
        ];
    }

    private function serializeBid(AuctionBid $bid): array
    {
        return [
            'id' => $bid->id,
            'auctionId' => $bid->auction_id,
            'sellerId' => $bid->seller_id,
            'price' => (float) $bid->price,
            'description' => $bid->description,
            'deliveryDays' => $bid->delivery_days,
            'createdAt' => optional($bid->created_at)?->toISOString(),
        ];
    }
}
