<?php

namespace App\Modules\Badge\Requests;

use App\Http\Requests\ApiRequest;

class UpdateBadgeRequest extends ApiRequest
{
    public function rules(): array
    {
        return [
            'name' => ['sometimes', 'string', 'max:100'],
            'description' => ['nullable', 'string', 'max:255'],
            'iconUrl' => ['nullable', 'string', 'max:500'],
            'type' => ['sometimes', 'string', 'max:20'],
            'condition' => ['nullable', 'array'],
            'rarity' => ['sometimes', 'string', 'max:20'],
        ];
    }
}
