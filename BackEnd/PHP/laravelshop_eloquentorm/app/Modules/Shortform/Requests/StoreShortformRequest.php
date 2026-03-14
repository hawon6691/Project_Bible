<?php

namespace App\Modules\Shortform\Requests;

use Illuminate\Foundation\Http\FormRequest;

class StoreShortformRequest extends FormRequest
{
    public function authorize(): bool
    {
        return true;
    }

    public function rules(): array
    {
        return [
            'title' => ['required', 'string', 'max:255'],
            'videoUrl' => ['required', 'string', 'max:500'],
            'thumbnailUrl' => ['nullable', 'string', 'max:500'],
            'productIds' => ['nullable', 'array'],
            'productIds.*' => ['integer', 'min:1'],
        ];
    }
}
