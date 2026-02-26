import { ApiProperty, ApiPropertyOptional } from '@nestjs/swagger';
import { IsBoolean, IsOptional, IsString, Length, Matches } from 'class-validator';
import { IsKoreanPhone } from '../../common/validators';

export class CreateAddressDto {
  @ApiProperty({ description: '수령인 이름', example: '홍길동' })
  @IsString()
  @Length(1, 50)
  recipientName: string;

  @ApiProperty({ description: '수령인 전화번호', example: '010-1234-5678' })
  @IsString()
  @IsKoreanPhone()
  phone: string;

  @ApiProperty({ description: '우편번호(5자리)', example: '06236' })
  @IsString()
  @Matches(/^\d{5}$/, { message: '우편번호는 5자리 숫자여야 합니다.' })
  zipCode: string;

  @ApiProperty({ description: '기본 주소', example: '서울특별시 강남구 테헤란로 123' })
  @IsString()
  @Length(1, 255)
  address: string;

  @ApiPropertyOptional({ description: '상세 주소', example: '101동 202호' })
  @IsOptional()
  @IsString()
  @Length(1, 255)
  addressDetail?: string;

  @ApiPropertyOptional({ description: '기본 배송지 여부', default: false })
  @IsOptional()
  @IsBoolean()
  isDefault?: boolean;
}
