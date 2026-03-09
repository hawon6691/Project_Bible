<?php

namespace App\Modules\Auto\Requests;

use Illuminate\Foundation\Http\FormRequest;

class EstimateAutoRequest extends FormRequest
{
    public function authorize(): bool
    {
        return true;
    }

    public function rules(): array
    {
        return [
            'modelId' => ['required', 'integer', 'min:1'],
            'trimId' => ['required', 'integer', 'min:1'],
            'optionIds' => ['nullable', 'array'],
            'optionIds.*' => ['integer', 'min:1'],
        ];
    }
}
