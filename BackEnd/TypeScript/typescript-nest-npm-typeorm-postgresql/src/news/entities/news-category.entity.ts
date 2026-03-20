import { Column, Entity, Index, OneToMany } from 'typeorm';
import { BaseEntity } from '../../common/entities/base.entity';
import { News } from './news.entity';

@Entity('news_categories')
export class NewsCategory extends BaseEntity {
  @Index('idx_news_categories_name', { unique: true })
  @Column({ type: 'varchar', length: 80, unique: true })
  name: string;

  @Index('idx_news_categories_slug', { unique: true })
  @Column({ type: 'varchar', length: 80, unique: true })
  slug: string;

  @OneToMany(() => News, (news) => news.category)
  newsList: News[];
}
