<?php

namespace App\Modules\Media\Requests;

use Illuminate\Foundation\Http\FormRequest;

class UploadMediaRequest extends FormRequest
{
    public function authorize(): bool
    {
        return true;
    }

    public function rules(): array
    {
        return [
            'files' => ['required', 'array', 'min:1'],
            'files.*.fileName' => ['required', 'string', 'max:255'],
            'files.*.fileUrl' => ['required', 'string', 'max:500'],
            'files.*.mimeType' => ['required', 'string', 'max:120'],
            'files.*.size' => ['required', 'integer', 'min:1'],
            'ownerType' => ['nullable', 'string', 'max:40'],
            'ownerId' => ['nullable', 'integer', 'min:1'],
        ];
    }
}
