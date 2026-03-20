import { Injectable } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { DataSource } from 'typeorm';

type CheckStatus = 'up' | 'down' | 'unknown';

export interface CheckResult {
  status: CheckStatus;
  message?: string;
  latencyMs?: number;
}


export interface HealthCheckResponse {
  status: 'up' | 'degraded' | 'down';
  checks: {
    database: CheckResult;
    redis: CheckResult;
    elasticsearch: CheckResult;
  };
  checkedAt: string;
}
@Injectable()
export class HealthService {
  constructor(
    private readonly dataSource: DataSource,
    private readonly configService: ConfigService,
  ) {}

  async getHealth(): Promise<HealthCheckResponse> {
    const [database, redis, elasticsearch] = await Promise.all([
      this.checkDatabase(),
      this.checkRedis(),
      this.checkElasticsearch(),
    ]);

    const checks = { database, redis, elasticsearch };
    return {
      status: this.getOverallStatus(checks),
      checks,
      checkedAt: new Date().toISOString(),
    };
  }

  private async checkDatabase(): Promise<CheckResult> {
    const startedAt = Date.now();

    try {
      await this.dataSource.query('SELECT 1');
      return { status: 'up', latencyMs: Date.now() - startedAt };
    } catch (error) {
      return {
        status: 'down',
        latencyMs: Date.now() - startedAt,
        message: this.toErrorMessage(error),
      };
    }
  }

  private async checkRedis(): Promise<CheckResult> {
    const host = this.configService.get<string>('REDIS_HOST');
    const port = Number(this.configService.get<string>('REDIS_PORT') ?? 6379);

    if (!host) {
      return {
        status: 'unknown',
        message: 'REDIS_HOST is not configured',
      };
    }

    const startedAt = Date.now();
    const net = await import('node:net');

    return new Promise<CheckResult>((resolve) => {
      const socket = net.createConnection({ host, port });

      const finish = (result: CheckResult) => {
        socket.removeAllListeners();
        socket.destroy();
        resolve(result);
      };

      socket.setTimeout(1500);

      socket.on('connect', () => {
        finish({ status: 'up', latencyMs: Date.now() - startedAt });
      });

      socket.on('timeout', () => {
        finish({
          status: 'down',
          latencyMs: Date.now() - startedAt,
          message: 'Redis timeout',
        });
      });

      socket.on('error', (error: Error) => {
        finish({
          status: 'down',
          latencyMs: Date.now() - startedAt,
          message: this.toErrorMessage(error),
        });
      });
    });
  }

  private async checkElasticsearch(): Promise<CheckResult> {
    const node = this.configService.get<string>('ELASTICSEARCH_NODE');

    if (!node) {
      return {
        status: 'unknown',
        message: 'ELASTICSEARCH_NODE is not configured',
      };
    }

    const startedAt = Date.now();

    try {
      const response = await fetch(`${node.replace(/\/$/, '')}/_cluster/health`, {
        method: 'GET',
      });

      if (!response.ok) {
        return {
          status: 'down',
          latencyMs: Date.now() - startedAt,
          message: `Elasticsearch responded with ${response.status}`,
        };
      }

      return { status: 'up', latencyMs: Date.now() - startedAt };
    } catch (error) {
      return {
        status: 'down',
        latencyMs: Date.now() - startedAt,
        message: this.toErrorMessage(error),
      };
    }
  }

  // 하나라도 down이면 down, down은 없고 unknown이 있으면 degraded로 응답한다.
  private getOverallStatus(checks: Record<string, CheckResult>): 'up' | 'degraded' | 'down' {
    const statuses = Object.values(checks).map((check) => check.status);

    if (statuses.includes('down')) {
      return 'down';
    }

    if (statuses.includes('unknown')) {
      return 'degraded';
    }

    return 'up';
  }

  private toErrorMessage(error: unknown): string {
    if (error instanceof Error) {
      return error.message;
    }

    return 'Unknown error';
  }
}

