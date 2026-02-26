import { PartialType } from '@nestjs/swagger';
import { CreateAuctionBidDto } from './create-auction-bid.dto';

export class UpdateAuctionBidDto extends PartialType(CreateAuctionBidDto) {}
