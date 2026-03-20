<?php

namespace App\Modules\I18n\Requests;

use App\Http\Requests\ApiRequest;

class UpsertTranslationRequest extends ApiRequest
{
    public function rules(): array
    {
        return [
            'locale' => ['required', 'string', 'max:10'],
            'namespace' => ['required', 'string', 'max:100'],
            'key' => ['required', 'string', 'max:191'],
            'value' => ['required', 'string'],
        ];
    }
}
