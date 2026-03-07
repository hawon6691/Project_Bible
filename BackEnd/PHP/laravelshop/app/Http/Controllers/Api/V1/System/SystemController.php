<?php

namespace App\Http\Controllers\Api\V1\System;

use App\Http\Controllers\Api\V1\ApiController;

class SystemController extends ApiController
{
    public function health()
    {
        return $this->success([
            'service' => config('app.name'),
            'language' => 'php',
            'framework' => 'laravel',
            'status' => 'ok',
        ]);
    }
}
