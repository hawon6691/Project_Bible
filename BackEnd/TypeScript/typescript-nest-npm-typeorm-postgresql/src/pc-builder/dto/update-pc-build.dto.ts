import { PartialType } from '@nestjs/swagger';
import { CreatePcBuildDto } from './create-pc-build.dto';

export class UpdatePcBuildDto extends PartialType(CreatePcBuildDto) {}
