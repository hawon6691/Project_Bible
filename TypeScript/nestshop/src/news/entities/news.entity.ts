import { Column, Entity, Index, JoinColumn, ManyToOne, OneToMany } from 'typeorm';
import { BaseEntity } from '../../common/entities/base.entity';
import { NewsCategory } from './news-category.entity';
import { NewsProduct } from './news-product.entity';

@Entity('news')
export class News extends BaseEntity {
  @Column({ type: 'varchar', length: 200 })
  title: string;

  @Column({ type: 'text' })
  content: string;

  @Index('idx_news_category_id')
  @Column({ name: 'category_id', type: 'int' })
  categoryId: number;

  @Column({ name: 'thumbnail_url', type: 'varchar', length: 500, nullable: true })
  thumbnailUrl: string | null;

  @Column({ name: 'view_count', type: 'int', default: 0 })
  viewCount: number;

  @ManyToOne(() => NewsCategory, (category) => category.newsList, { onDelete: 'RESTRICT' })
  @JoinColumn({ name: 'category_id' })
  category: NewsCategory;

  @OneToMany(() => NewsProduct, (newsProduct) => newsProduct.news)
  products: NewsProduct[];
}
