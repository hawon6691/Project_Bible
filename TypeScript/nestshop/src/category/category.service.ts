import { Injectable, HttpStatus } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { Category } from './entities/category.entity';
import { CreateCategoryDto } from './dto/create-category.dto';
import { UpdateCategoryDto } from './dto/update-category.dto';
import { BusinessException } from '../common/exceptions/business.exception';

@Injectable()
export class CategoryService {
  constructor(
    @InjectRepository(Category)
    private categoryRepository: Repository<Category>,
  ) {}

  // ─── CAT-01: 카테고리 목록 조회 (트리) ───
  async findAllTree() {
    const categories = await this.categoryRepository.find({
      order: { sortOrder: 'ASC', id: 'ASC' },
    });

    return this.buildTree(categories);
  }

  // ─── CAT-01: 단일 카테고리 조회 ───
  async findOne(id: number) {
    const category = await this.categoryRepository.findOne({
      where: { id },
      relations: ['children'],
    });
    if (!category) {
      throw new BusinessException('CATEGORY_NOT_FOUND', HttpStatus.NOT_FOUND);
    }

    return {
      id: category.id,
      name: category.name,
      parentId: category.parentId,
      sortOrder: category.sortOrder,
      children: category.children
        .sort((a, b) => a.sortOrder - b.sortOrder || a.id - b.id)
        .map((c) => ({
          id: c.id,
          name: c.name,
          sortOrder: c.sortOrder,
        })),
      createdAt: category.createdAt,
    };
  }

  // ─── CAT-02: 카테고리 생성 ───
  async create(dto: CreateCategoryDto) {
    if (dto.parentId) {
      const parent = await this.categoryRepository.findOne({
        where: { id: dto.parentId },
      });
      if (!parent) {
        throw new BusinessException('CATEGORY_NOT_FOUND', HttpStatus.NOT_FOUND);
      }
    }

    const category = this.categoryRepository.create({
      name: dto.name,
      parentId: dto.parentId || null,
    });

    const saved = await this.categoryRepository.save(category);
    return this.toCategoryResponse(saved);
  }

  // ─── CAT-03: 카테고리 수정 ───
  async update(id: number, dto: UpdateCategoryDto) {
    const category = await this.categoryRepository.findOne({ where: { id } });
    if (!category) {
      throw new BusinessException('CATEGORY_NOT_FOUND', HttpStatus.NOT_FOUND);
    }

    if (dto.name !== undefined) category.name = dto.name;
    if (dto.sortOrder !== undefined) category.sortOrder = dto.sortOrder;

    const saved = await this.categoryRepository.save(category);
    return this.toCategoryResponse(saved);
  }

  // ─── CAT-04: 카테고리 삭제 ───
  async remove(id: number) {
    const category = await this.categoryRepository.findOne({
      where: { id },
      relations: ['children'],
    });
    if (!category) {
      throw new BusinessException('CATEGORY_NOT_FOUND', HttpStatus.NOT_FOUND);
    }

    if (category.children && category.children.length > 0) {
      throw new BusinessException(
        'CATEGORY_HAS_CHILDREN',
        HttpStatus.BAD_REQUEST,
      );
    }

    await this.categoryRepository.remove(category);
    return { message: '카테고리가 삭제되었습니다.' };
  }

  // ─── 헬퍼: 트리 구조 빌드 ───
  private buildTree(categories: Category[]) {
    const map = new Map<number, any>();
    const roots: any[] = [];

    for (const cat of categories) {
      map.set(cat.id, {
        id: cat.id,
        name: cat.name,
        sortOrder: cat.sortOrder,
        children: [],
      });
    }

    for (const cat of categories) {
      const node = map.get(cat.id);
      if (cat.parentId && map.has(cat.parentId)) {
        map.get(cat.parentId).children.push(node);
      } else {
        roots.push(node);
      }
    }

    return roots;
  }

  // ─── 헬퍼: 응답 변환 ───
  private toCategoryResponse(category: Category) {
    return {
      id: category.id,
      name: category.name,
      parentId: category.parentId,
      sortOrder: category.sortOrder,
      createdAt: category.createdAt,
    };
  }
}
