import { Body, Controller, Delete, Get, Param, ParseIntPipe, Patch, Post } from '@nestjs/common';
import { ApiBearerAuth, ApiOperation, ApiTags } from '@nestjs/swagger';
import { AddressService } from './address.service';
import { CurrentUser, JwtPayload } from '../common/decorators/current-user.decorator';
import { CreateAddressDto } from './dto/create-address.dto';
import { UpdateAddressDto } from './dto/update-address.dto';

@ApiTags('Addresses')
@Controller('addresses')
@ApiBearerAuth()
export class AddressController {
  constructor(private readonly addressService: AddressService) {}

  // ADDR-01: 배송지 목록 조회
  @Get()
  @ApiOperation({ summary: '내 배송지 목록 조회' })
  findMyAddresses(@CurrentUser() user: JwtPayload) {
    return this.addressService.findMyAddresses(user.sub);
  }

  // ADDR-02: 배송지 추가
  @Post()
  @ApiOperation({ summary: '배송지 추가' })
  create(@CurrentUser() user: JwtPayload, @Body() dto: CreateAddressDto) {
    return this.addressService.create(user.sub, dto);
  }

  // ADDR-03: 배송지 수정
  @Patch(':id')
  @ApiOperation({ summary: '배송지 수정' })
  update(
    @CurrentUser() user: JwtPayload,
    @Param('id', ParseIntPipe) id: number,
    @Body() dto: UpdateAddressDto,
  ) {
    return this.addressService.update(user.sub, id, dto);
  }

  // ADDR-04: 배송지 삭제
  @Delete(':id')
  @ApiOperation({ summary: '배송지 삭제' })
  remove(@CurrentUser() user: JwtPayload, @Param('id', ParseIntPipe) id: number) {
    return this.addressService.remove(user.sub, id);
  }
}
