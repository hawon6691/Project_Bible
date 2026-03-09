<?php

namespace App\Modules\AdminSettings\Requests;

use Illuminate\Foundation\Http\FormRequest;

class UpdateExtensionsRequest extends FormRequest
{
    public function authorize(): bool { return true; }
    public function rules(): array { return ['extensions' => ['required', 'array'], 'extensions.*' => ['string', 'max:20']]; }
}
