<?php

namespace App\Modules\Product\Requests;

use App\Http\Requests\ApiRequest;
use Illuminate\Validation\Rule;

class ListProductsRequest extends ApiRequest
{
    public function rules(): array
    {
        return [
            'page' => ['sometimes', 'integer', 'min:1'],
            'limit' => ['sometimes', 'integer', 'min:1', 'max:100'],
            'categoryId' => ['sometimes', 'integer', 'exists:categories,id'],
            'search' => ['sometimes', 'string', 'max:255'],
            'minPrice' => ['sometimes', 'numeric', 'min:0'],
            'maxPrice' => ['sometimes', 'numeric', 'min:0'],
            'sort' => ['sometimes', 'string', Rule::in([
                'popularity',
                'price_asc',
                'price_desc',
                'newest',
                'rating',
                'rating_desc',
                'rating_asc',
            ])],
        ];
    }
}
