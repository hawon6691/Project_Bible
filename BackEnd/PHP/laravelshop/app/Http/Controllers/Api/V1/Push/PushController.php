<?php

namespace App\Http\Controllers\Api\V1\Push;

use App\Http\Controllers\Api\V1\ApiController;
use App\Modules\Push\Requests\RegisterPushSubscriptionRequest;
use App\Modules\Push\Requests\UnregisterPushSubscriptionRequest;
use App\Modules\Push\Requests\UpdatePushPreferenceRequest;
use App\Modules\Push\Services\PushService;

class PushController extends ApiController
{
    public function __construct(
        private readonly PushService $pushService,
    ) {}

    public function subscriptions()
    {
        return $this->success($this->pushService->getSubscriptions(request()->user()));
    }

    public function storeSubscription(RegisterPushSubscriptionRequest $request)
    {
        return $this->success($this->pushService->registerSubscription($request->user(), $request->validated()), status: 201);
    }

    public function unsubscribe(UnregisterPushSubscriptionRequest $request)
    {
        return $this->success($this->pushService->unregisterSubscription($request->user(), $request->validated()));
    }

    public function preference()
    {
        return $this->success($this->pushService->getPreference(request()->user()));
    }

    public function updatePreference(UpdatePushPreferenceRequest $request)
    {
        $payload = [];
        foreach (['priceAlertEnabled', 'orderStatusEnabled', 'chatMessageEnabled', 'dealEnabled'] as $key) {
            if (array_key_exists($key, $request->all())) {
                $payload[$key] = (bool) $request->input($key);
            }
        }

        return $this->success($this->pushService->updatePreference($request->user(), $payload));
    }
}
