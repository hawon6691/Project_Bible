import { Column, Entity, Index, JoinColumn, ManyToOne, Unique } from 'typeorm';
import { BaseEntity } from '../../common/entities/base.entity';
import { News } from './news.entity';

@Entity('news_products')
@Unique('uq_news_products_news_product', ['newsId', 'productId'])
export class NewsProduct extends BaseEntity {
  @Index('idx_news_products_news_id')
  @Column({ name: 'news_id', type: 'int' })
  newsId: number;

  @Index('idx_news_products_product_id')
  @Column({ name: 'product_id', type: 'int' })
  productId: number;

  @ManyToOne(() => News, (news) => news.products, { onDelete: 'CASCADE' })
  @JoinColumn({ name: 'news_id' })
  news: News;
}
