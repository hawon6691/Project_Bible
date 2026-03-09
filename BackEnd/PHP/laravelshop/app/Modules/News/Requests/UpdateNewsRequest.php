<?php

namespace App\Modules\News\Requests;

use Illuminate\Foundation\Http\FormRequest;

class UpdateNewsRequest extends FormRequest
{
    public function authorize(): bool
    {
        return true;
    }

    public function rules(): array
    {
        return [
            'title' => ['sometimes', 'string', 'max:255'],
            'content' => ['sometimes', 'string'],
            'categoryId' => ['nullable', 'integer', 'min:1'],
            'thumbnailUrl' => ['nullable', 'string', 'max:500'],
            'productIds' => ['nullable', 'array'],
            'productIds.*' => ['integer', 'min:1'],
        ];
    }
}
