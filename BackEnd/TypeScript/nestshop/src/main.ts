import { NestFactory } from '@nestjs/core';
import { ValidationPipe } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { DocumentBuilder, SwaggerModule } from '@nestjs/swagger';
import { AppModule } from './app.module';
import { securityHeadersMiddleware } from './common/middlewares/security-headers.middleware';

function resolveLoggerLevels() {
  const env = process.env.NODE_ENV ?? 'development';
  if (env === 'production') {
    return ['warn', 'error'] as const;
  }
  if (env === 'test') {
    return ['error'] as const;
  }
  return ['debug', 'log', 'warn', 'error'] as const;
}

async function bootstrap() {
  const app = await NestFactory.create(AppModule, {
    // 환경별 로그 레벨 분리: dev(debug), prod(warn)
    logger: [...resolveLoggerLevels()],
  });
  const configService = app.get(ConfigService);

  // Helmet 대체 보안 헤더 적용
  app.use(securityHeadersMiddleware);

  // Global prefix
  const apiPrefix = configService.get<string>('API_PREFIX', 'api/v1');
  app.setGlobalPrefix(apiPrefix);

  // CORS
  app.enableCors({
    origin: true,
    credentials: true,
  });

  // Global Validation Pipe
  app.useGlobalPipes(
    new ValidationPipe({
      whitelist: true,
      forbidNonWhitelisted: true,
      transform: true,
      transformOptions: {
        enableImplicitConversion: true,
      },
    }),
  );

  // Swagger
  const swaggerConfig = new DocumentBuilder()
    .setTitle('NestShop API')
    .setDescription('Danawa-style price comparison shopping mall API')
    .setVersion('1.0')
    .addBearerAuth()
    .build();
  const document = SwaggerModule.createDocument(app, swaggerConfig);
  SwaggerModule.setup('docs', app, document);

  // Start
  const port = configService.get<number>('APP_PORT', 3000);
  await app.listen(port);
  console.log(`Application is running on: http://localhost:${port}`);
  console.log(`Swagger docs: http://localhost:${port}/docs`);
}
bootstrap();
