<?php

namespace App\Http\Controllers\Api\V1\OpsDashboard;

use App\Http\Controllers\Api\V1\ApiController;
use App\Modules\OpsDashboard\Services\OpsDashboardService;
use Illuminate\Http\Request;

class OpsDashboardController extends ApiController
{
    public function __construct(private readonly OpsDashboardService $service) {}
    public function summary(Request $request) { return $this->success($this->service->summary($request->user())); }
}
