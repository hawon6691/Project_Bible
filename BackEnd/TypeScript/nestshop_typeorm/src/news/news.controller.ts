import {
  Body,
  Controller,
  Delete,
  Get,
  Param,
  ParseIntPipe,
  Patch,
  Post,
  Query,
} from '@nestjs/common';
import { ApiBearerAuth, ApiOperation, ApiTags } from '@nestjs/swagger';
import { Roles, UserRole } from '../common/decorators/roles.decorator';
import { Public } from '../common/decorators/public.decorator';
import { CreateNewsCategoryDto } from './dto/create-news-category.dto';
import { CreateNewsDto } from './dto/create-news.dto';
import { NewsListQueryDto } from './dto/news-list-query.dto';
import { UpdateNewsDto } from './dto/update-news.dto';
import { NewsService } from './news.service';

@ApiTags('News')
@Controller('news')
export class NewsController {
  constructor(private readonly newsService: NewsService) {}

  @Public()
  @Get()
  @ApiOperation({ summary: '뉴스 목록 조회' })
  getNewsList(@Query() query: NewsListQueryDto) {
    return this.newsService.getNewsList(query);
  }

  @Public()
  @Get('categories')
  @ApiOperation({ summary: '뉴스 카테고리 목록 조회' })
  getCategories() {
    return this.newsService.getCategories();
  }

  @Public()
  @Get(':id')
  @ApiOperation({ summary: '뉴스 상세 조회' })
  getNewsDetail(@Param('id', ParseIntPipe) id: number) {
    return this.newsService.getNewsDetail(id);
  }

  @ApiBearerAuth()
  @Roles(UserRole.ADMIN)
  @Post()
  @ApiOperation({ summary: '뉴스 작성 (Admin)' })
  createNews(@Body() dto: CreateNewsDto) {
    return this.newsService.createNews(dto);
  }

  @ApiBearerAuth()
  @Roles(UserRole.ADMIN)
  @Patch(':id')
  @ApiOperation({ summary: '뉴스 수정 (Admin)' })
  updateNews(@Param('id', ParseIntPipe) id: number, @Body() dto: UpdateNewsDto) {
    return this.newsService.updateNews(id, dto);
  }

  @ApiBearerAuth()
  @Roles(UserRole.ADMIN)
  @Delete(':id')
  @ApiOperation({ summary: '뉴스 삭제 (Admin)' })
  removeNews(@Param('id', ParseIntPipe) id: number) {
    return this.newsService.removeNews(id);
  }

  @ApiBearerAuth()
  @Roles(UserRole.ADMIN)
  @Post('categories')
  @ApiOperation({ summary: '뉴스 카테고리 추가 (Admin)' })
  createCategory(@Body() dto: CreateNewsCategoryDto) {
    return this.newsService.createCategory(dto);
  }

  @ApiBearerAuth()
  @Roles(UserRole.ADMIN)
  @Delete('categories/:id')
  @ApiOperation({ summary: '뉴스 카테고리 삭제 (Admin)' })
  removeCategory(@Param('id', ParseIntPipe) id: number) {
    return this.newsService.removeCategory(id);
  }
}
