import { PartialType } from '@nestjs/swagger';
import { PushPreferenceDto } from './upsert-push-preference.dto';

export class UpdatePushPreferenceDto extends PartialType(PushPreferenceDto) {}
