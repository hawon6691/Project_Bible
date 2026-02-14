import { Controller, Get, Query } from '@nestjs/common';
import { ApiBearerAuth, ApiOperation, ApiTags } from '@nestjs/swagger';
import { CurrentUser, JwtPayload } from '../common/decorators/current-user.decorator';
import { Public } from '../common/decorators/public.decorator';
import { RecommendationQueryDto } from './dto/recommendation-query.dto';
import { RecommendationService } from './recommendation.service';

@ApiTags('Recommendation')
@Controller('recommendations')
export class RecommendationController {
  constructor(private readonly recommendationService: RecommendationService) {}

  // RECO-01: 개인화 추천
  @ApiBearerAuth()
  @Get('personal')
  @ApiOperation({ summary: '개인화 추천 상품 조회' })
  getPersonalRecommendations(@CurrentUser() user: JwtPayload, @Query() query: RecommendationQueryDto) {
    return this.recommendationService.getPersonalRecommendations(user.sub, query);
  }

  // RECO-02: 트렌딩 추천
  @Public()
  @Get('trending')
  @ApiOperation({ summary: '트렌딩 추천 상품 조회' })
  getTrendingRecommendations(@Query() query: RecommendationQueryDto) {
    return this.recommendationService.getTrendingRecommendations(query);
  }
}
