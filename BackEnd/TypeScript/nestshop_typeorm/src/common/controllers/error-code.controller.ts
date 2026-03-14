import { Controller, Get, Param } from '@nestjs/common';
import { ApiOperation, ApiTags } from '@nestjs/swagger';
import { Public } from '../decorators/public.decorator';
import { ERROR_CODE_CATALOG } from '../constants/error-codes';
import { API_ROUTES } from '../../routes/api-routes';

@ApiTags('ErrorCode')
@Controller(API_ROUTES.ERRORS.BASE)
export class ErrorCodeController {
  @Public()
  @Get(API_ROUTES.ERRORS.CODES)
  @ApiOperation({ summary: '시스템 에러코드 전체 목록 조회' })
  getCodes() {
    const items = [...ERROR_CODE_CATALOG].sort((a, b) => a.code.localeCompare(b.code));

    return {
      total: items.length,
      items,
    };
  }

  @Public()
  @Get(API_ROUTES.ERRORS.CODE_DETAIL)
  @ApiOperation({ summary: '시스템 에러코드 단건 조회' })
  getCode(@Param('key') key: string) {
    const target = ERROR_CODE_CATALOG.find((item) => item.key === key);
    if (!target) {
      return null;
    }
    return target;
  }
}
