<?php

namespace App\Modules\Crawler\Requests;

use Illuminate\Foundation\Http\FormRequest;

class UpdateCrawlerJobRequest extends FormRequest
{
    public function authorize(): bool { return true; }
    public function rules(): array { return ['name' => ['sometimes', 'string', 'max:120'], 'jobType' => ['sometimes', 'string', 'max:40'], 'status' => ['sometimes', 'string', 'max:20'], 'payload' => ['nullable', 'array']]; }
}
