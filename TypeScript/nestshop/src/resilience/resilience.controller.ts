import { Controller, Get, Param, Post } from '@nestjs/common';
import { ApiBearerAuth, ApiOperation, ApiTags } from '@nestjs/swagger';
import { Roles, UserRole } from '../common/decorators/roles.decorator';
import { API_ROUTES } from '../routes/api-routes';
import { ResilienceService } from './resilience.service';

@ApiTags('Resilience')
@ApiBearerAuth()
@Roles(UserRole.ADMIN)
@Controller(API_ROUTES.RESILIENCE.BASE)
export class ResilienceController {
  constructor(private readonly resilienceService: ResilienceService) {}

  @Get()
  @ApiOperation({ summary: 'Circuit Breaker 상태 목록 조회 (Admin)' })
  getSnapshots() {
    return { items: this.resilienceService.getSnapshots() };
  }

  @Get(API_ROUTES.RESILIENCE.DETAIL)
  @ApiOperation({ summary: 'Circuit Breaker 상태 조회 (Admin)' })
  getSnapshot(@Param('name') name: string) {
    return this.resilienceService.getSnapshot(name);
  }

  @Post(API_ROUTES.RESILIENCE.RESET)
  @ApiOperation({ summary: 'Circuit Breaker 수동 초기화 (Admin)' })
  reset(@Param('name') name: string) {
    return this.resilienceService.reset(name);
  }
}
