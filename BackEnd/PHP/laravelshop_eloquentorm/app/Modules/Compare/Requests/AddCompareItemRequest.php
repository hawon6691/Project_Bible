<?php

namespace App\Modules\Compare\Requests;

use Illuminate\Foundation\Http\FormRequest;

class AddCompareItemRequest extends FormRequest
{
    public function authorize(): bool
    {
        return true;
    }

    public function rules(): array
    {
        return [
            'productId' => ['required', 'integer', 'min:1'],
        ];
    }
}
