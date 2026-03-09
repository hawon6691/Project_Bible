<?php

namespace App\Modules\Matching\Requests;

use Illuminate\Foundation\Http\FormRequest;

class ApproveMappingRequest extends FormRequest
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
