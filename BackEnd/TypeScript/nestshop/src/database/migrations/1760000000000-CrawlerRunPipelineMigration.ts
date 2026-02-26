import { MigrationInterface, QueryRunner } from 'typeorm';

export class CrawlerRunPipelineMigration1760000000000 implements MigrationInterface {
  name = 'CrawlerRunPipelineMigration1760000000000';

  public async up(queryRunner: QueryRunner): Promise<void> {
    // 상태 enum에 큐 기반 실행 상태를 추가한다.
    await queryRunner.query(`
      DO $$
      BEGIN
        IF EXISTS (SELECT 1 FROM pg_type WHERE typname = 'crawler_runs_status_enum') THEN
          ALTER TYPE "public"."crawler_runs_status_enum" ADD VALUE IF NOT EXISTS 'QUEUED';
          ALTER TYPE "public"."crawler_runs_status_enum" ADD VALUE IF NOT EXISTS 'PROCESSING';
        END IF;
      END $$;
    `);

    // 큐 실행 시점의 수집 옵션을 이력에 보존한다.
    await queryRunner.query(`
      DO $$
      BEGIN
        IF EXISTS (
          SELECT 1
          FROM information_schema.tables
          WHERE table_schema = 'public' AND table_name = 'crawler_runs'
        ) THEN
          ALTER TABLE "crawler_runs"
          ADD COLUMN IF NOT EXISTS "collect_price" boolean NOT NULL DEFAULT true;
          ALTER TABLE "crawler_runs"
          ADD COLUMN IF NOT EXISTS "collect_spec" boolean NOT NULL DEFAULT true;
          ALTER TABLE "crawler_runs"
          ADD COLUMN IF NOT EXISTS "detect_anomaly" boolean NOT NULL DEFAULT true;
        END IF;
      END $$;
    `);

    await queryRunner.query(`
      DO $$
      BEGIN
        IF EXISTS (
          SELECT 1
          FROM information_schema.tables
          WHERE table_schema = 'public' AND table_name = 'crawler_runs'
        ) THEN
          CREATE INDEX IF NOT EXISTS "idx_crawler_runs_status"
          ON "crawler_runs" ("status");
        END IF;
      END $$;
    `);
  }

  public async down(queryRunner: QueryRunner): Promise<void> {
    await queryRunner.query(`DROP INDEX IF EXISTS "idx_crawler_runs_status"`);

    await queryRunner.query(`
      DO $$
      BEGIN
        IF EXISTS (
          SELECT 1
          FROM information_schema.tables
          WHERE table_schema = 'public' AND table_name = 'crawler_runs'
        ) THEN
          ALTER TABLE "crawler_runs"
          DROP COLUMN IF EXISTS "detect_anomaly";
          ALTER TABLE "crawler_runs"
          DROP COLUMN IF EXISTS "collect_spec";
          ALTER TABLE "crawler_runs"
          DROP COLUMN IF EXISTS "collect_price";
        END IF;
      END $$;
    `);

    // Postgres enum value 삭제는 안전하지 않아 down에서 제거하지 않는다.
  }
}
