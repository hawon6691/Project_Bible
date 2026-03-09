<?php

namespace App\Modules\Image\Requests;

use App\Http\Requests\ApiRequest;

class UploadImageRequest extends ApiRequest
{
    public function rules(): array
    {
        return [
            'file' => ['required', 'file', 'image', 'max:10240'],
            'category' => ['required', 'string', 'max:40'],
        ];
    }
}
