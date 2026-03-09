<?php

namespace App\Modules\AdminSettings\Requests;

use Illuminate\Foundation\Http\FormRequest;

class UpdateReviewPolicyRequest extends FormRequest
{
    public function authorize(): bool { return true; }
    public function rules(): array { return ['maxImageCount' => ['required', 'integer', 'min:0'], 'pointAmount' => ['required', 'integer', 'min:0']]; }
}
