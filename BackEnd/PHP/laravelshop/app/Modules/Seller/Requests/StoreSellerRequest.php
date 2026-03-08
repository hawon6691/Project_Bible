<?php

namespace App\Modules\Seller\Requests;

use App\Http\Requests\ApiRequest;

class StoreSellerRequest extends ApiRequest
{
    public function rules(): array
    {
        return [
            'name' => ['required', 'string', 'max:150'],
            'code' => ['required', 'string', 'max:80'],
            'status' => ['sometimes', 'string', 'max:30'],
            'rating' => ['sometimes', 'numeric', 'min:0', 'max:5'],
            'contactEmail' => ['sometimes', 'nullable', 'email', 'max:255'],
            'homepageUrl' => ['sometimes', 'nullable', 'url', 'max:500'],
        ];
    }
}
