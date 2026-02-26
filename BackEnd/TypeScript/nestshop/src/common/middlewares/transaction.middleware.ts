import { Injectable, NestMiddleware } from '@nestjs/common';
import { Request, Response, NextFunction } from 'express';
import { DataSource } from 'typeorm';

@Injectable()
export class TransactionMiddleware implements NestMiddleware {
  constructor(private dataSource: DataSource) {}

  async use(req: Request, _res: Response, next: NextFunction) {
    // GET 요청은 트랜잭션 불필요
    if (req.method === 'GET') {
      return next();
    }

    const queryRunner = this.dataSource.createQueryRunner();
    await queryRunner.connect();
    await queryRunner.startTransaction();

    // request 객체에 queryRunner 주입
    (req as Request & { queryRunner: typeof queryRunner }).queryRunner = queryRunner;

    // 응답 완료 시 commit/rollback
    const originalEnd = _res.end.bind(_res);
    _res.end = ((...args: Parameters<Response['end']>) => {
      if (_res.statusCode >= 400) {
        queryRunner.rollbackTransaction().finally(() => queryRunner.release());
      } else {
        queryRunner.commitTransaction().finally(() => queryRunner.release());
      }
      return originalEnd(...args);
    }) as Response['end'];

    next();
  }
}
