<?php

namespace App\Modules\Shortform\Requests;

use Illuminate\Foundation\Http\FormRequest;

class StoreShortformCommentRequest extends FormRequest
{
    public function authorize(): bool
    {
        return true;
    }

    public function rules(): array
    {
        return [
            'content' => ['required', 'string', 'max:1000'],
        ];
    }
}
