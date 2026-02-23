import { DataSource } from 'typeorm';
import { join } from 'path';

const isTsRuntime = __filename.endsWith('.ts');
const rootDir = isTsRuntime ? 'src' : 'dist';

export const AppDataSource = new DataSource({
  type: 'postgres',
  host: process.env.DB_HOST ?? 'localhost',
  port: Number(process.env.DB_PORT ?? 5432),
  username: process.env.DB_USERNAME ?? 'postgres',
  password: process.env.DB_PASSWORD ?? 'postgres',
  database: process.env.DB_DATABASE ?? 'nestshop',
  synchronize: false,
  logging: process.env.DB_LOGGING === 'true',
  entities: [join(process.cwd(), rootDir, '**', '*.entity.{ts,js}')],
  migrations: [join(process.cwd(), rootDir, 'database', 'migrations', '*.{ts,js}')],
});

export default AppDataSource;
