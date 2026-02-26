import { Global, Module } from '@nestjs/common';
import { APP_FILTER, APP_INTERCEPTOR, APP_GUARD } from '@nestjs/core';
import { JwtModule } from '@nestjs/jwt';
import { ConfigModule } from '@nestjs/config';
import { HttpExceptionFilter } from './filters/http-exception.filter';
import { ResponseInterceptor } from './interceptors/response.interceptor';
import { LoggingInterceptor } from './interceptors/logging.interceptor';
import { LocaleInterceptor } from './interceptors/locale.interceptor';
import { JwtAuthGuard } from './guards/jwt-auth.guard';
import { RolesGuard } from './guards/roles.guard';
import { RateLimitGuard } from './guards/rate-limit.guard';
import { ErrorCodeController } from './controllers/error-code.controller';
import { CacheService } from './cache/cache.service';

@Global()
@Module({
  imports: [ConfigModule, JwtModule],
  controllers: [ErrorCodeController],
  providers: [
    CacheService,
    // 전역 예외 필터
    {
      provide: APP_FILTER,
      useClass: HttpExceptionFilter,
    },
    // 전역 인터셉터 (순서: Locale → Logging → Response)
    {
      provide: APP_INTERCEPTOR,
      useClass: LocaleInterceptor,
    },
    {
      provide: APP_INTERCEPTOR,
      useClass: LoggingInterceptor,
    },
    {
      provide: APP_INTERCEPTOR,
      useClass: ResponseInterceptor,
    },
    // 전역 가드 (순서: RateLimit → JWT → Roles)
    {
      provide: APP_GUARD,
      useClass: RateLimitGuard,
    },
    {
      provide: APP_GUARD,
      useClass: JwtAuthGuard,
    },
    {
      provide: APP_GUARD,
      useClass: RolesGuard,
    },
  ],
  exports: [JwtModule, CacheService],
})
export class CommonModule {}
