import { MigrationInterface, QueryRunner } from 'typeorm';

export class SearchIndexOutboxMigration1760000002000 implements MigrationInterface {
  name = 'SearchIndexOutboxMigration1760000002000';

  public async up(queryRunner: QueryRunner): Promise<void> {
    await queryRunner.query(`
      DO $$
      BEGIN
        IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'search_index_outbox_event_type_enum') THEN
          CREATE TYPE "public"."search_index_outbox_event_type_enum" AS ENUM (
            'PRODUCT_UPSERT',
            'PRODUCT_DELETE',
            'PRICE_CHANGED'
          );
        END IF;
      END $$;
    `);

    await queryRunner.query(`
      DO $$
      BEGIN
        IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'search_index_outbox_status_enum') THEN
          CREATE TYPE "public"."search_index_outbox_status_enum" AS ENUM (
            'PENDING',
            'PROCESSING',
            'COMPLETED',
            'FAILED'
          );
        END IF;
      END $$;
    `);

    await queryRunner.query(`
      CREATE TABLE IF NOT EXISTS "search_index_outbox" (
        "id" SERIAL NOT NULL,
        "created_at" TIMESTAMP NOT NULL DEFAULT now(),
        "updated_at" TIMESTAMP NOT NULL DEFAULT now(),
        "deleted_at" TIMESTAMP,
        "event_type" "public"."search_index_outbox_event_type_enum" NOT NULL,
        "status" "public"."search_index_outbox_status_enum" NOT NULL DEFAULT 'PENDING',
        "aggregate_id" integer NOT NULL,
        "payload" json,
        "attempt_count" integer NOT NULL DEFAULT 0,
        "last_error" character varying(500),
        "processed_at" TIMESTAMP,
        CONSTRAINT "PK_search_index_outbox_id" PRIMARY KEY ("id")
      )
    `);

    await queryRunner.query(`
      CREATE INDEX IF NOT EXISTS "idx_search_index_outbox_event_type"
      ON "search_index_outbox" ("event_type")
    `);
    await queryRunner.query(`
      CREATE INDEX IF NOT EXISTS "idx_search_index_outbox_status"
      ON "search_index_outbox" ("status")
    `);
  }

  public async down(queryRunner: QueryRunner): Promise<void> {
    await queryRunner.query(`DROP INDEX IF EXISTS "idx_search_index_outbox_status"`);
    await queryRunner.query(`DROP INDEX IF EXISTS "idx_search_index_outbox_event_type"`);
    await queryRunner.query(`DROP TABLE IF EXISTS "search_index_outbox"`);
    await queryRunner.query(`DROP TYPE IF EXISTS "public"."search_index_outbox_status_enum"`);
    await queryRunner.query(`DROP TYPE IF EXISTS "public"."search_index_outbox_event_type_enum"`);
  }
}
