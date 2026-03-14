<?php

namespace App\Modules\Auction\Requests;

use Illuminate\Foundation\Http\FormRequest;

class UpdateAuctionBidRequest extends FormRequest
{
    public function authorize(): bool
    {
        return true;
    }

    public function rules(): array
    {
        return [
            'price' => ['nullable', 'numeric', 'min:0'],
            'description' => ['nullable', 'string'],
            'deliveryDays' => ['nullable', 'integer', 'min:1'],
        ];
    }
}
