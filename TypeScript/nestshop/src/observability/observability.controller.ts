import { Controller, Get, Query } from '@nestjs/common';
import { ApiBearerAuth, ApiOperation, ApiTags } from '@nestjs/swagger';
import { Roles, UserRole } from '../common/decorators/roles.decorator';
import { API_ROUTES } from '../routes/api-routes';
import { TraceQueryDto } from './dto/trace-query.dto';
import { ObservabilityService } from './observability.service';

@ApiTags('Observability')
@ApiBearerAuth()
@Roles(UserRole.ADMIN)
@Controller(API_ROUTES.OBSERVABILITY.BASE)
export class ObservabilityController {
  constructor(private readonly observabilityService: ObservabilityService) {}

  @Get(API_ROUTES.OBSERVABILITY.METRICS)
  @ApiOperation({ summary: '관측성 메트릭 요약 조회 (Admin)' })
  getMetrics() {
    return this.observabilityService.getMetricsSummary();
  }

  @Get(API_ROUTES.OBSERVABILITY.TRACES)
  @ApiOperation({ summary: '최근 요청 트레이스 조회 (Admin)' })
  getTraces(@Query() query: TraceQueryDto) {
    return {
      items: this.observabilityService.getTraces(query.limit, query.pathContains),
    };
  }

  @Get(API_ROUTES.OBSERVABILITY.DASHBOARD)
  @ApiOperation({ summary: '관측성 통합 대시보드 조회 (Admin)' })
  getDashboard() {
    return this.observabilityService.getDashboard();
  }
}
