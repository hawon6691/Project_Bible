<?php

namespace App\Modules\Address\Services;

use App\Common\Exceptions\BusinessException;
use App\Models\Address;
use App\Models\User;
use Symfony\Component\HttpFoundation\Response;

class AddressService
{
    public function list(User $user): array
    {
        return Address::query()
            ->where('user_id', $user->id)
            ->orderByDesc('is_default')
            ->orderByDesc('id')
            ->get()
            ->map(fn (Address $address): array => $this->serialize($address))
            ->values()
            ->all();
    }

    public function create(User $user, array $payload): array
    {
        if (($payload['isDefault'] ?? false) === true) {
            $this->clearDefault($user);
        }

        $address = Address::query()->create([
            'user_id' => $user->id,
            'recipient_name' => $payload['recipientName'],
            'label' => $payload['label'] ?? null,
            'phone' => $payload['phone'],
            'zip_code' => $payload['zipCode'],
            'address_line1' => $payload['addressLine1'],
            'address_line2' => $payload['addressLine2'] ?? null,
            'memo' => $payload['memo'] ?? null,
            'is_default' => (bool) ($payload['isDefault'] ?? false),
        ]);

        return $this->serialize($address);
    }

    public function update(User $user, int $id, array $payload): array
    {
        $address = Address::query()->where('user_id', $user->id)->find($id);
        if (! $address) {
            throw new BusinessException('배송지를 찾을 수 없습니다.', 'ADDRESS_NOT_FOUND', Response::HTTP_NOT_FOUND);
        }

        if (($payload['isDefault'] ?? false) === true) {
            $this->clearDefault($user);
        }

        $updates = [];
        if (array_key_exists('recipientName', $payload)) {
            $updates['recipient_name'] = $payload['recipientName'];
        }
        if (array_key_exists('label', $payload)) {
            $updates['label'] = $payload['label'];
        }
        if (array_key_exists('phone', $payload)) {
            $updates['phone'] = $payload['phone'];
        }
        if (array_key_exists('zipCode', $payload)) {
            $updates['zip_code'] = $payload['zipCode'];
        }
        if (array_key_exists('addressLine1', $payload)) {
            $updates['address_line1'] = $payload['addressLine1'];
        }
        if (array_key_exists('addressLine2', $payload)) {
            $updates['address_line2'] = $payload['addressLine2'];
        }
        if (array_key_exists('memo', $payload)) {
            $updates['memo'] = $payload['memo'];
        }
        if (array_key_exists('isDefault', $payload)) {
            $updates['is_default'] = (bool) $payload['isDefault'];
        }

        if ($updates !== []) {
            $address->forceFill($updates)->save();
        }

        return $this->serialize($address->fresh());
    }

    public function delete(User $user, int $id): array
    {
        $address = Address::query()->where('user_id', $user->id)->find($id);
        if (! $address) {
            throw new BusinessException('배송지를 찾을 수 없습니다.', 'ADDRESS_NOT_FOUND', Response::HTTP_NOT_FOUND);
        }

        $address->delete();

        return ['message' => '배송지가 삭제되었습니다.'];
    }

    private function serialize(Address $address): array
    {
        return [
            'id' => $address->id,
            'recipientName' => $address->recipient_name,
            'label' => $address->label,
            'phone' => $address->phone,
            'zipCode' => $address->zip_code,
            'addressLine1' => $address->address_line1,
            'addressLine2' => $address->address_line2,
            'memo' => $address->memo,
            'isDefault' => (bool) $address->is_default,
            'createdAt' => optional($address->created_at)?->toISOString(),
            'updatedAt' => optional($address->updated_at)?->toISOString(),
        ];
    }

    private function clearDefault(User $user): void
    {
        Address::query()->where('user_id', $user->id)->update(['is_default' => false]);
    }
}
