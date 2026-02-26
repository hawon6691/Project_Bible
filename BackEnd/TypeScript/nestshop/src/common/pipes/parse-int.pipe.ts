import { PipeTransform, Injectable, BadRequestException } from '@nestjs/common';

@Injectable()
export class ParseIntPipe implements PipeTransform<string, number> {
  transform(value: string): number {
    const parsed = parseInt(value, 10);
    if (isNaN(parsed)) {
      throw new BadRequestException(`"${value}"은(는) 유효한 정수가 아닙니다.`);
    }
    return parsed;
  }
}
