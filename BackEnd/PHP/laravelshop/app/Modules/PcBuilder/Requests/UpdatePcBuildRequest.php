<?php

namespace App\Modules\PcBuilder\Requests;

use Illuminate\Foundation\Http\FormRequest;

class UpdatePcBuildRequest extends FormRequest
{
    public function authorize(): bool
    {
        return true;
    }

    public function rules(): array
    {
        return [
            'name' => ['sometimes', 'string', 'max:120'],
            'description' => ['nullable', 'string', 'max:255'],
        ];
    }
}
