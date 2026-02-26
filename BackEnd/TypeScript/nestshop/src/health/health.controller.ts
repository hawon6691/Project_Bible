import { Controller, Get } from '@nestjs/common';
import { ApiOperation, ApiTags } from '@nestjs/swagger';
import { Public } from '../common/decorators/public.decorator';
import { API_ROUTES } from '../routes/api-routes';
import { HealthCheckResponse, HealthService } from './health.service';

@ApiTags('Health')
@Controller(API_ROUTES.HEALTH.BASE)
export class HealthController {
  constructor(private readonly healthService: HealthService) {}

  // 인프라 상태는 인증 없이 확인할 수 있도록 공개 엔드포인트로 제공한다.
  @Public()
  @Get()
  @ApiOperation({ summary: '서비스 헬스 체크 (DB/Redis/Elasticsearch)' })
  getHealth(): Promise<HealthCheckResponse> {
    return this.healthService.getHealth();
  }
}


