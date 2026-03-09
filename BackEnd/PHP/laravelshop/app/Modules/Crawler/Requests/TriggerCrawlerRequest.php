<?php

namespace App\Modules\Crawler\Requests;

use Illuminate\Foundation\Http\FormRequest;

class TriggerCrawlerRequest extends FormRequest
{
    public function authorize(): bool { return true; }
    public function rules(): array { return ['jobId' => ['nullable', 'integer', 'min:1'], 'targetType' => ['nullable', 'string', 'max:40'], 'targetId' => ['nullable', 'integer', 'min:1']]; }
}
