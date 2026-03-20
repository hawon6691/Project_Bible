<?php

namespace App\Modules\PcBuilder\Requests;

use Illuminate\Foundation\Http\FormRequest;

class StorePcBuildPartRequest extends FormRequest
{
    public function authorize(): bool
    {
        return true;
    }

    public function rules(): array
    {
        return [
            'partType' => ['required', 'string', 'max:40'],
            'productId' => ['required', 'integer', 'min:1'],
            'quantity' => ['nullable', 'integer', 'min:1', 'max:20'],
        ];
    }
}
