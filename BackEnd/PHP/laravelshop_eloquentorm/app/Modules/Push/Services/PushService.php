<?php

namespace App\Modules\Push\Services;

use App\Models\PushPreference;
use App\Models\PushSubscription;
use App\Models\User;

class PushService
{
    public function registerSubscription(User $user, array $payload): array
    {
        $endpointHash = hash('sha256', $payload['endpoint']);

        $subscription = PushSubscription::query()->updateOrCreate(
            ['user_id' => $user->id, 'endpoint_hash' => $endpointHash],
            [
                'endpoint' => $payload['endpoint'],
                'p256dh_key' => $payload['p256dhKey'],
                'auth_key' => $payload['authKey'],
                'expiration_time' => $payload['expirationTime'] ?? null,
                'is_active' => true,
            ],
        );

        return $this->serializeSubscription($subscription);
    }

    public function unregisterSubscription(User $user, array $payload): array
    {
        $endpointHash = hash('sha256', $payload['endpoint']);

        PushSubscription::query()
            ->where('user_id', $user->id)
            ->where('endpoint_hash', $endpointHash)
            ->update(['is_active' => false]);

        return [
            'success' => true,
            'message' => '푸시 구독이 해제되었습니다.',
        ];
    }

    public function getSubscriptions(User $user): array
    {
        return PushSubscription::query()
            ->where('user_id', $user->id)
            ->where('is_active', true)
            ->orderByDesc('id')
            ->get()
            ->map(fn (PushSubscription $subscription): array => $this->serializeSubscription($subscription))
            ->values()
            ->all();
    }

    public function getPreference(User $user): array
    {
        return $this->serializePreference($this->getOrCreatePreference($user));
    }

    public function updatePreference(User $user, array $payload): array
    {
        $preference = $this->getOrCreatePreference($user);
        $updates = [];

        if (array_key_exists('priceAlertEnabled', $payload)) {
            $updates['price_alert_enabled'] = $payload['priceAlertEnabled'];
        }
        if (array_key_exists('orderStatusEnabled', $payload)) {
            $updates['order_status_enabled'] = $payload['orderStatusEnabled'];
        }
        if (array_key_exists('chatMessageEnabled', $payload)) {
            $updates['chat_message_enabled'] = $payload['chatMessageEnabled'];
        }
        if (array_key_exists('dealEnabled', $payload)) {
            $updates['deal_enabled'] = $payload['dealEnabled'];
        }

        if ($updates !== []) {
            $preference->forceFill($updates)->save();
        }

        return $this->serializePreference($preference->fresh());
    }

    private function getOrCreatePreference(User $user): PushPreference
    {
        return PushPreference::query()->firstOrCreate(
            ['user_id' => $user->id],
            [
                'price_alert_enabled' => true,
                'order_status_enabled' => true,
                'chat_message_enabled' => true,
                'deal_enabled' => true,
            ],
        );
    }

    private function serializeSubscription(PushSubscription $subscription): array
    {
        return [
            'id' => $subscription->id,
            'endpoint' => $subscription->endpoint,
            'expirationTime' => $subscription->expiration_time,
            'isActive' => $subscription->is_active,
            'createdAt' => optional($subscription->created_at)?->toISOString(),
            'updatedAt' => optional($subscription->updated_at)?->toISOString(),
        ];
    }

    private function serializePreference(PushPreference $preference): array
    {
        return [
            'id' => $preference->id,
            'priceAlertEnabled' => $preference->price_alert_enabled,
            'orderStatusEnabled' => $preference->order_status_enabled,
            'chatMessageEnabled' => $preference->chat_message_enabled,
            'dealEnabled' => $preference->deal_enabled,
            'createdAt' => optional($preference->created_at)?->toISOString(),
            'updatedAt' => optional($preference->updated_at)?->toISOString(),
        ];
    }
}
