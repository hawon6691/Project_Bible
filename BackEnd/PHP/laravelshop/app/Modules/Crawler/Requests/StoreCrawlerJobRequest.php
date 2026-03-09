<?php

namespace App\Modules\Crawler\Requests;

use Illuminate\Foundation\Http\FormRequest;

class StoreCrawlerJobRequest extends FormRequest
{
    public function authorize(): bool { return true; }
    public function rules(): array { return ['name' => ['required', 'string', 'max:120'], 'jobType' => ['required', 'string', 'max:40'], 'status' => ['nullable', 'string', 'max:20'], 'payload' => ['nullable', 'array']]; }
}
