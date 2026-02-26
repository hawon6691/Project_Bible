import { HttpStatus, Injectable } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { DataSource, Not, Repository } from 'typeorm';
import { BusinessException } from '../common/exceptions/business.exception';
import { Address } from './entities/address.entity';
import { CreateAddressDto } from './dto/create-address.dto';
import { UpdateAddressDto } from './dto/update-address.dto';

@Injectable()
export class AddressService {
  constructor(
    @InjectRepository(Address)
    private addressRepository: Repository<Address>,
    private dataSource: DataSource,
  ) {}

  // ADDR-01: 내 배송지 목록 조회
  async findMyAddresses(userId: number) {
    const addresses = await this.addressRepository.find({
      where: { userId },
      order: { isDefault: 'DESC', createdAt: 'DESC' },
    });

    return addresses.map((address) => this.toDetail(address));
  }

  // ADDR-02: 배송지 추가
  async create(userId: number, dto: CreateAddressDto) {
    return this.dataSource.transaction(async (manager) => {
      const addressRepo = manager.getRepository(Address);
      const currentDefault = await addressRepo.findOne({ where: { userId, isDefault: true } });

      const shouldBeDefault = dto.isDefault === true || !currentDefault;
      if (shouldBeDefault) {
        await addressRepo.update({ userId, isDefault: true }, { isDefault: false });
      }

      const address = addressRepo.create({
        userId,
        recipientName: dto.recipientName,
        phone: dto.phone,
        zipCode: dto.zipCode,
        address: dto.address,
        addressDetail: dto.addressDetail ?? null,
        isDefault: shouldBeDefault,
      });
      const saved = await addressRepo.save(address);

      return this.toDetail(saved);
    });
  }

  // ADDR-03: 배송지 수정
  async update(userId: number, id: number, dto: UpdateAddressDto) {
    return this.dataSource.transaction(async (manager) => {
      const addressRepo = manager.getRepository(Address);
      const address = await addressRepo.findOne({ where: { id, userId } });
      if (!address) {
        throw new BusinessException('RESOURCE_NOT_FOUND', HttpStatus.NOT_FOUND);
      }

      if (dto.isDefault === true) {
        await addressRepo.update({ userId, isDefault: true }, { isDefault: false });
      }

      // 기본 배송지를 해제할 때는 다른 기본 배송지가 있어야 한다.
      if (address.isDefault && dto.isDefault === false) {
        const anotherDefault = await addressRepo.findOne({
          where: { userId, isDefault: true, id: Not(id) },
        });
        if (!anotherDefault) {
          throw new BusinessException(
            'VALIDATION_FAILED',
            HttpStatus.BAD_REQUEST,
            '기본 배송지는 최소 1개 이상 있어야 합니다.',
          );
        }
      }

      address.recipientName = dto.recipientName ?? address.recipientName;
      address.phone = dto.phone ?? address.phone;
      address.zipCode = dto.zipCode ?? address.zipCode;
      address.address = dto.address ?? address.address;
      if (dto.addressDetail !== undefined) {
        address.addressDetail = dto.addressDetail ?? null;
      }
      if (dto.isDefault !== undefined) {
        address.isDefault = dto.isDefault;
      }

      const saved = await addressRepo.save(address);
      return this.toDetail(saved);
    });
  }

  // ADDR-04: 배송지 삭제
  async remove(userId: number, id: number) {
    return this.dataSource.transaction(async (manager) => {
      const addressRepo = manager.getRepository(Address);
      const address = await addressRepo.findOne({ where: { id, userId } });
      if (!address) {
        throw new BusinessException('RESOURCE_NOT_FOUND', HttpStatus.NOT_FOUND);
      }

      await addressRepo.softDelete({ id, userId });

      // 기본 배송지를 삭제하면 가장 최근 배송지를 기본 배송지로 승격한다.
      if (address.isDefault) {
        const nextDefault = await addressRepo.findOne({
          where: { userId },
          order: { createdAt: 'DESC' },
        });
        if (nextDefault) {
          nextDefault.isDefault = true;
          await addressRepo.save(nextDefault);
        }
      }

      return {
        id,
        deleted: true,
      };
    });
  }

  private toDetail(address: Address) {
    return {
      id: address.id,
      recipientName: address.recipientName,
      phone: address.phone,
      zipCode: address.zipCode,
      address: address.address,
      addressDetail: address.addressDetail,
      isDefault: address.isDefault,
      createdAt: address.createdAt,
      updatedAt: address.updatedAt,
    };
  }
}
