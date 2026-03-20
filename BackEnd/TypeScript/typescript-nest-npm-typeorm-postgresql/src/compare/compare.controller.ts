import { Body, Controller, Delete, Get, Headers, Param, ParseIntPipe, Post } from '@nestjs/common';
import { ApiOperation, ApiTags } from '@nestjs/swagger';
import { Public } from '../common/decorators/public.decorator';
import { AddCompareItemDto } from './dto/add-compare-item.dto';
import { CompareService } from './compare.service';

@ApiTags('Compare')
@Controller('compare')
export class CompareController {
  constructor(private readonly compareService: CompareService) {}

  @Public()
  @Post('add')
  @ApiOperation({ summary: '비교함에 상품 추가 (최대 4개)' })
  add(@Headers('x-compare-key') compareKey: string | undefined, @Body() dto: AddCompareItemDto) {
    return this.compareService.add(compareKey ?? 'guest', dto);
  }

  @Public()
  @Delete(':productId')
  @ApiOperation({ summary: '비교함에서 상품 제거' })
  remove(
    @Headers('x-compare-key') compareKey: string | undefined,
    @Param('productId', ParseIntPipe) productId: number,
  ) {
    return this.compareService.remove(compareKey ?? 'guest', productId);
  }

  @Public()
  @Get()
  @ApiOperation({ summary: '비교함 현재 목록 조회' })
  getList(@Headers('x-compare-key') compareKey: string | undefined) {
    return this.compareService.getList(compareKey ?? 'guest');
  }

  @Public()
  @Get('detail')
  @ApiOperation({ summary: '비교 상세 조회' })
  getDetail(@Headers('x-compare-key') compareKey: string | undefined) {
    return this.compareService.getDetail(compareKey ?? 'guest');
  }
}
