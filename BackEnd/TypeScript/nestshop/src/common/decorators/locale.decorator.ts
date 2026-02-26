import { createParamDecorator, ExecutionContext } from '@nestjs/common';
import { Language } from '../constants/locale.enum';

export const Locale = createParamDecorator(
  (_data: unknown, ctx: ExecutionContext): Language => {
    const request = ctx.switchToHttp().getRequest();
    return request.locale ?? Language.KO;
  },
);
