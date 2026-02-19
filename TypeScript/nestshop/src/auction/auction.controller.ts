import { Body, Controller, Delete, Get, Param, ParseIntPipe, Patch, Post, Query } from '@nestjs/common';
import { ApiBearerAuth, ApiOperation, ApiTags } from '@nestjs/swagger';
import { CurrentUser, JwtPayload } from '../common/decorators/current-user.decorator';
import { Public } from '../common/decorators/public.decorator';
import { Roles, UserRole } from '../common/decorators/roles.decorator';
import { AuctionListQueryDto } from './dto/auction-list-query.dto';
import { CreateAuctionBidDto } from './dto/create-auction-bid.dto';
import { CreateAuctionDto } from './dto/create-auction.dto';
import { UpdateAuctionBidDto } from './dto/update-auction-bid.dto';
import { AuctionService } from './auction.service';

@ApiTags('Auction')
@Controller('auctions')
export class AuctionController {
  constructor(private readonly auctionService: AuctionService) {}

  @ApiBearerAuth()
  @Post()
  @ApiOperation({ summary: '역경매 등록' })
  createAuction(@CurrentUser() user: JwtPayload, @Body() dto: CreateAuctionDto) {
    return this.auctionService.createAuction(user.sub, dto);
  }

  @Public()
  @Get()
  @ApiOperation({ summary: '역경매 목록 조회' })
  getAuctions(@Query() query: AuctionListQueryDto) {
    return this.auctionService.getAuctions(query);
  }

  @Public()
  @Get(':id')
  @ApiOperation({ summary: '역경매 상세 조회' })
  getAuctionDetail(@Param('id', ParseIntPipe) id: number) {
    return this.auctionService.getAuctionDetail(id);
  }

  @ApiBearerAuth()
  @Roles(UserRole.SELLER)
  @Post(':id/bids')
  @ApiOperation({ summary: '입찰 등록 (Seller)' })
  createBid(
    @CurrentUser() user: JwtPayload,
    @Param('id', ParseIntPipe) auctionId: number,
    @Body() dto: CreateAuctionBidDto,
  ) {
    return this.auctionService.createBid(user.sub, auctionId, dto);
  }

  @ApiBearerAuth()
  @Patch(':id/bids/:bidId/select')
  @ApiOperation({ summary: '낙찰 선택 (경매 등록자)' })
  selectBid(
    @CurrentUser() user: JwtPayload,
    @Param('id', ParseIntPipe) auctionId: number,
    @Param('bidId', ParseIntPipe) bidId: number,
  ) {
    return this.auctionService.selectBid(user.sub, auctionId, bidId);
  }

  @ApiBearerAuth()
  @Delete(':id')
  @ApiOperation({ summary: '역경매 취소 (경매 등록자)' })
  cancelAuction(@CurrentUser() user: JwtPayload, @Param('id', ParseIntPipe) auctionId: number) {
    return this.auctionService.cancelAuction(user.sub, auctionId);
  }

  @ApiBearerAuth()
  @Roles(UserRole.SELLER)
  @Patch(':id/bids/:bidId')
  @ApiOperation({ summary: '입찰 수정 (입찰자)' })
  updateBid(
    @CurrentUser() user: JwtPayload,
    @Param('id', ParseIntPipe) auctionId: number,
    @Param('bidId', ParseIntPipe) bidId: number,
    @Body() dto: UpdateAuctionBidDto,
  ) {
    return this.auctionService.updateBid(user.sub, auctionId, bidId, dto);
  }

  @ApiBearerAuth()
  @Roles(UserRole.SELLER)
  @Delete(':id/bids/:bidId')
  @ApiOperation({ summary: '입찰 취소 (입찰자)' })
  removeBid(
    @CurrentUser() user: JwtPayload,
    @Param('id', ParseIntPipe) auctionId: number,
    @Param('bidId', ParseIntPipe) bidId: number,
  ) {
    return this.auctionService.removeBid(user.sub, auctionId, bidId);
  }
}
