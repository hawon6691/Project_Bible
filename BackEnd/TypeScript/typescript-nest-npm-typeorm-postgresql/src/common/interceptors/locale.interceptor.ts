import {
  Injectable,
  NestInterceptor,
  ExecutionContext,
  CallHandler,
} from '@nestjs/common';
import { Observable } from 'rxjs';
import { Request } from 'express';
import { Language } from '../constants/locale.enum';

@Injectable()
export class LocaleInterceptor implements NestInterceptor {
  private readonly supportedLanguages = Object.values(Language);

  intercept(context: ExecutionContext, next: CallHandler): Observable<unknown> {
    const request = context.switchToHttp().getRequest<Request>();
    const acceptLanguage = request.get('Accept-Language') ?? '';

    // Accept-Language 헤더에서 지원 언어 추출 (예: ko-KR,ko;q=0.9 → ko)
    const lang = acceptLanguage
      .split(',')
      .map((part) => part.split(';')[0].trim().substring(0, 2).toLowerCase())
      .find((code) => this.supportedLanguages.includes(code as Language));

    (request as Request & { locale: Language }).locale =
      (lang as Language) ?? Language.KO;

    return next.handle();
  }
}
