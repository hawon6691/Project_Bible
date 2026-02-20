import { Body, Controller, Delete, Get, Param, ParseIntPipe, Patch, Post, Query } from '@nestjs/common';
import { ApiBearerAuth, ApiOperation, ApiTags } from '@nestjs/swagger';
import { CurrentUser, JwtPayload } from '../common/decorators/current-user.decorator';
import { Public } from '../common/decorators/public.decorator';
import { Roles, UserRole } from '../common/decorators/roles.decorator';
import { API_ROUTES } from '../routes/api-routes';
import { PopularSearchQueryDto } from './dto/popular-search-query.dto';
import { SaveRecentSearchDto } from './dto/save-recent-search.dto';
import { SearchAutocompleteQueryDto } from './dto/search-autocomplete-query.dto';
import { SearchQueryDto } from './dto/search-query.dto';
import { UpdateSearchPreferenceDto } from './dto/update-search-preference.dto';
import { UpdateSearchWeightDto } from './dto/update-search-weight.dto';
import { SearchService } from './search.service';

@ApiTags('Search')
@Controller(API_ROUTES.SEARCH.BASE)
export class SearchController {
  constructor(private readonly searchService: SearchService) {}

  @Public()
  @Get()
  @ApiOperation({ summary: '상품 통합 검색' })
  search(@Query() query: SearchQueryDto) {
    return this.searchService.search(query);
  }

  @Public()
  @Get(API_ROUTES.SEARCH.AUTOCOMPLETE)
  @ApiOperation({ summary: '자동완성 검색어 조회' })
  autocomplete(@Query() query: SearchAutocompleteQueryDto) {
    return this.searchService.autocomplete(query);
  }

  @Public()
  @Get(API_ROUTES.SEARCH.POPULAR)
  @ApiOperation({ summary: '인기 검색어 조회' })
  getPopularKeywords(@Query() query: PopularSearchQueryDto) {
    return this.searchService.getPopularKeywords(query);
  }

  @ApiBearerAuth()
  @Post(API_ROUTES.SEARCH.RECENT)
  @ApiOperation({ summary: '최근 검색어 저장 (User)' })
  saveRecentKeyword(@CurrentUser() user: JwtPayload, @Body() dto: SaveRecentSearchDto) {
    return this.searchService.saveRecentKeyword(user.sub, dto);
  }

  @ApiBearerAuth()
  @Get(API_ROUTES.SEARCH.RECENT)
  @ApiOperation({ summary: '최근 검색어 조회 (User)' })
  getRecentKeywords(@CurrentUser() user: JwtPayload) {
    return this.searchService.getRecentKeywords(user.sub);
  }

  @ApiBearerAuth()
  @Delete(API_ROUTES.SEARCH.RECENT_DETAIL)
  @ApiOperation({ summary: '최근 검색어 개별 삭제 (User)' })
  removeRecentKeyword(@CurrentUser() user: JwtPayload, @Param('id', ParseIntPipe) id: number) {
    return this.searchService.removeRecentKeyword(user.sub, id);
  }

  @ApiBearerAuth()
  @Delete(API_ROUTES.SEARCH.RECENT)
  @ApiOperation({ summary: '최근 검색어 전체 삭제 (User)' })
  clearRecentKeywords(@CurrentUser() user: JwtPayload) {
    return this.searchService.clearRecentKeywords(user.sub);
  }

  @ApiBearerAuth()
  @Patch(API_ROUTES.SEARCH.PREFERENCES)
  @ApiOperation({ summary: '검색어 자동 저장 설정 변경 (User)' })
  updatePreference(@CurrentUser() user: JwtPayload, @Body() dto: UpdateSearchPreferenceDto) {
    return this.searchService.updatePreference(user.sub, dto);
  }

  @ApiBearerAuth()
  @Roles(UserRole.ADMIN)
  @Get(API_ROUTES.SEARCH.ADMIN_WEIGHTS)
  @ApiOperation({ summary: '검색 가중치 조회 (Admin)' })
  getWeights() {
    return this.searchService.getWeights();
  }

  @ApiBearerAuth()
  @Roles(UserRole.ADMIN)
  @Patch(API_ROUTES.SEARCH.ADMIN_WEIGHTS)
  @ApiOperation({ summary: '검색 가중치 수정 (Admin)' })
  updateWeights(@Body() dto: UpdateSearchWeightDto) {
    return this.searchService.updateWeights(dto);
  }
}
