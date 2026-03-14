<?php

namespace App\Modules\AdminSettings\Requests;

use Illuminate\Foundation\Http\FormRequest;

class UpdateUploadLimitsRequest extends FormRequest
{
    public function authorize(): bool
    {
        return true;
    }

    public function rules(): array
    {
        return ['image' => ['required', 'integer', 'min:1'], 'video' => ['required', 'integer', 'min:1'], 'audio' => ['required', 'integer', 'min:1']];
    }
}
