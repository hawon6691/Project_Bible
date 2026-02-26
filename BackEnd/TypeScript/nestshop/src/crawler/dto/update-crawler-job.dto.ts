import { PartialType } from '@nestjs/swagger';
import { CreateCrawlerJobDto } from './create-crawler-job.dto';

export class UpdateCrawlerJobDto extends PartialType(CreateCrawlerJobDto) {}
