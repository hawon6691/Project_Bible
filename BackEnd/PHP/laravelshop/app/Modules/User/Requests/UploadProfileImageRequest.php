<?php

namespace App\Modules\User\Requests;

use App\Http\Requests\ApiRequest;

class UploadProfileImageRequest extends ApiRequest
{
    public function rules(): array
    {
        return [
            'image' => ['required', 'file', 'image', 'max:5120'],
        ];
    }
}
