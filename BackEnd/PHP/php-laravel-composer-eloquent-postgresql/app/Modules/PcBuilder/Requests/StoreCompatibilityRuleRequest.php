<?php

namespace App\Modules\PcBuilder\Requests;

use Illuminate\Foundation\Http\FormRequest;

class StoreCompatibilityRuleRequest extends FormRequest
{
    public function authorize(): bool
    {
        return true;
    }

    public function rules(): array
    {
        return [
            'name' => ['required', 'string', 'max:120'],
            'sourcePartType' => ['required', 'string', 'max:40'],
            'targetPartType' => ['required', 'string', 'max:40'],
            'ruleType' => ['required', 'string', 'max:40'],
            'ruleValue' => ['nullable', 'array'],
        ];
    }
}
