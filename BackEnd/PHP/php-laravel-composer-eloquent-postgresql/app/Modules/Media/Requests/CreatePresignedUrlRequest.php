<?php

namespace App\Modules\Media\Requests;

use Illuminate\Foundation\Http\FormRequest;

class CreatePresignedUrlRequest extends FormRequest
{
    public function authorize(): bool
    {
        return true;
    }

    public function rules(): array
    {
        return [
            'fileName' => ['required', 'string', 'max:255'],
            'fileType' => ['required', 'string', 'max:120'],
            'fileSize' => ['required', 'integer', 'min:1'],
        ];
    }
}
