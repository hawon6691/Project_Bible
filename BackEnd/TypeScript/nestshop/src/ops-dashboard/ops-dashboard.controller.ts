import { Controller, Get } from '@nestjs/common';
import { ApiBearerAuth, ApiOperation, ApiTags } from '@nestjs/swagger';
import { Roles, UserRole } from '../common/decorators/roles.decorator';
import { OpsDashboardService } from './ops-dashboard.service';
import { API_ROUTES } from '../routes/api-routes';

@ApiTags('OpsDashboard')
@ApiBearerAuth()
@Roles(UserRole.ADMIN)
@Controller(API_ROUTES.OPS_DASHBOARD.BASE)
export class OpsDashboardController {
  constructor(private readonly opsDashboardService: OpsDashboardService) {}

  @Get(API_ROUTES.OPS_DASHBOARD.SUMMARY)
  @ApiOperation({ summary: '운영 대시보드 통합 요약 조회 (Admin)' })
  getSummary() {
    return this.opsDashboardService.getSummary();
  }
}
