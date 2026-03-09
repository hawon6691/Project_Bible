<?php

namespace App\Modules\ErrorCode\Services;

class ErrorCodeService
{
    private array $items = [
        ['key' => 'FORBIDDEN', 'status' => 403, 'message' => '권한이 없습니다.'],
        ['key' => 'PRODUCT_NOT_FOUND', 'status' => 404, 'message' => '상품을 찾을 수 없습니다.'],
        ['key' => 'AUCTION_CLOSED', 'status' => 400, 'message' => '종료된 역경매입니다.'],
    ];

    public function list(): array
    {
        return ['total' => count($this->items), 'items' => $this->items];
    }

    public function show(string $key): ?array
    {
        return collect($this->items)->firstWhere('key', $key);
    }
}
