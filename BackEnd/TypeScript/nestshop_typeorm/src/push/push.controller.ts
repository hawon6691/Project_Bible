import { Body, Controller, Get, Post } from '@nestjs/common';
import { ApiBearerAuth, ApiOperation, ApiTags } from '@nestjs/swagger';
import { CurrentUser, JwtPayload } from '../common/decorators/current-user.decorator';
import { RegisterPushSubscriptionDto } from './dto/register-push-subscription.dto';
import { UnregisterPushSubscriptionDto } from './dto/unregister-push-subscription.dto';
import { UpdatePushPreferenceDto } from './dto/update-push-preference.dto';
import { PushService } from './push.service';

@ApiTags('Push')
@Controller('push')
@ApiBearerAuth()
export class PushController {
  constructor(private readonly pushService: PushService) {}

  // PUSH-01: 푸시 구독 등록
  @Post('subscriptions')
  @ApiOperation({ summary: '푸시 구독 등록' })
  registerSubscription(@CurrentUser() user: JwtPayload, @Body() dto: RegisterPushSubscriptionDto) {
    return this.pushService.registerSubscription(user.sub, dto);
  }

  // PUSH-02: 푸시 구독 해제
  @Post('subscriptions/unsubscribe')
  @ApiOperation({ summary: '푸시 구독 해제' })
  unregisterSubscription(@CurrentUser() user: JwtPayload, @Body() dto: UnregisterPushSubscriptionDto) {
    return this.pushService.unregisterSubscription(user.sub, dto);
  }

  // 내 활성 구독 목록
  @Get('subscriptions')
  @ApiOperation({ summary: '내 활성 푸시 구독 목록' })
  getMySubscriptions(@CurrentUser() user: JwtPayload) {
    return this.pushService.getMySubscriptions(user.sub);
  }

  // PUSH-07: 알림 설정 조회
  @Get('preferences')
  @ApiOperation({ summary: '푸시 알림 설정 조회' })
  getPreference(@CurrentUser() user: JwtPayload) {
    return this.pushService.getPreference(user.sub);
  }

  // PUSH-07: 알림 설정 변경
  @Post('preferences')
  @ApiOperation({ summary: '푸시 알림 설정 변경' })
  updatePreference(@CurrentUser() user: JwtPayload, @Body() dto: UpdatePushPreferenceDto) {
    return this.pushService.updatePreference(user.sub, dto);
  }
}
