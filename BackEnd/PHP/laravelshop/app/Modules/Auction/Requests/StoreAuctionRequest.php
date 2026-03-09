<?php

namespace App\Modules\Auction\Requests;

use Illuminate\Foundation\Http\FormRequest;

class StoreAuctionRequest extends FormRequest
{
    public function authorize(): bool
    {
        return true;
    }

    public function rules(): array
    {
        return [
            'title' => ['required', 'string', 'max:255'],
            'description' => ['nullable', 'string'],
            'categoryId' => ['nullable', 'integer', 'min:1'],
            'specs' => ['nullable', 'array'],
            'budget' => ['nullable', 'numeric', 'min:0'],
        ];
    }
}
