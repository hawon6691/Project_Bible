<?php

namespace App\Modules\Payment\Services;

use App\Common\Exceptions\BusinessException;
use App\Models\Order;
use App\Models\Payment;
use App\Models\User;
use Illuminate\Support\Str;
use Symfony\Component\HttpFoundation\Response;

class PaymentService
{
    public function create(User $user, array $payload): array
    {
        $order = Order::query()->where('user_id', $user->id)->find($payload['orderId']);
        if (! $order) {
            throw new BusinessException('주문을 찾을 수 없습니다.', 'ORDER_NOT_FOUND', Response::HTTP_NOT_FOUND);
        }

        $payment = Payment::query()->create([
            'order_id' => $order->id,
            'user_id' => $user->id,
            'payment_number' => $this->generatePaymentNumber(),
            'method' => $payload['method'] ?? 'CARD',
            'status' => 'PAID',
            'amount' => (float) $order->final_amount,
            'provider' => $payload['provider'] ?? 'MOCK_PG',
            'provider_reference' => Str::upper(Str::random(12)),
            'paid_at' => now(),
        ]);

        return $this->serialize($payment->fresh('order'));
    }

    public function detail(User $user, int $paymentId): array
    {
        $payment = Payment::query()->with('order')->where('user_id', $user->id)->find($paymentId);
        if (! $payment) {
            throw new BusinessException('결제 정보를 찾을 수 없습니다.', 'PAYMENT_NOT_FOUND', Response::HTTP_NOT_FOUND);
        }

        return $this->serialize($payment);
    }

    public function refund(User $user, int $paymentId): array
    {
        $payment = Payment::query()->with('order')->where('user_id', $user->id)->find($paymentId);
        if (! $payment) {
            throw new BusinessException('결제 정보를 찾을 수 없습니다.', 'PAYMENT_NOT_FOUND', Response::HTTP_NOT_FOUND);
        }

        $payment->forceFill([
            'status' => 'REFUNDED',
            'refunded_at' => now(),
        ])->save();

        if ($payment->order) {
            $payment->order->forceFill([
                'status' => 'REFUNDED',
            ])->save();
        }

        return $this->serialize($payment->fresh('order'));
    }

    private function serialize(Payment $payment): array
    {
        return [
            'id' => $payment->id,
            'paymentNumber' => $payment->payment_number,
            'orderId' => $payment->order_id,
            'orderNumber' => $payment->order?->order_number,
            'method' => $payment->method,
            'status' => $payment->status,
            'amount' => (float) $payment->amount,
            'provider' => $payment->provider,
            'providerReference' => $payment->provider_reference,
            'paidAt' => optional($payment->paid_at)?->toISOString(),
            'refundedAt' => optional($payment->refunded_at)?->toISOString(),
            'createdAt' => optional($payment->created_at)?->toISOString(),
        ];
    }

    private function generatePaymentNumber(): string
    {
        return 'PAY-' . now()->format('Ymd') . '-' . Str::upper(Str::random(6));
    }
}
