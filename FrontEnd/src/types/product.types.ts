export enum ProductStatus {
  ON_SALE = 'ON_SALE',
  SOLD_OUT = 'SOLD_OUT',
  HIDDEN = 'HIDDEN',
}

export enum ProductSort {
  NEWEST = 'newest',
  POPULARITY = 'popularity',
  PRICE_ASC = 'price_asc',
  PRICE_DESC = 'price_desc',
  RATING_DESC = 'rating_desc',
  RATING_ASC = 'rating_asc',
}

export interface ProductSummary {
  id: number;
  name: string;
  lowestPrice: number;
  sellerCount: number;
  thumbnailUrl: string;
  reviewCount: number;
  averageRating: number;
  priceDiff?: number;
  priceDiffPercent?: number;
  createdAt: string;
}

export interface ProductDetail {
  id: number;
  name: string;
  description: string;
  lowestPrice: number;
  highestPrice: number;
  averagePrice: number;
  stock: number;
  status: ProductStatus;
  category: { id: number; name: string } | null;
  options: ProductOption[];
  images: ProductImage[];
  specs: ProductSpec[];
  priceEntries: PriceEntry[];
  reviewCount: number;
  averageRating: number;
  createdAt: string;
}

export interface ProductOption {
  id: number;
  name: string;
  values: string[];
}

export interface ProductImage {
  id: number;
  url: string;
  isMain: boolean;
  sortOrder: number;
}

export interface ProductSpec {
  name: string;
  value: string;
}

export interface PriceEntry {
  seller: SellerInfo;
  price: number;
  url: string;
  shipping: string;
}

export interface SellerInfo {
  id: number;
  name: string;
  logoUrl: string;
  trustScore: number;
}

export interface ProductQueryParams {
  page?: number;
  limit?: number;
  categoryId?: number;
  search?: string;
  minPrice?: number;
  maxPrice?: number;
  sort?: ProductSort | string;
  specs?: string;
}

export interface Category {
  id: number;
  name: string;
  sortOrder: number;
  children: Category[];
}

export interface SpecDefinition {
  id: number;
  name: string;
  type: 'TEXT' | 'NUMBER' | 'SELECT';
  options: string[] | null;
  unit: string | null;
}

export interface CompareResult {
  products: { id: number; name: string }[];
  specs: {
    name: string;
    values: Record<number, string>;
  }[];
}

export interface ScoredCompareResult extends CompareResult {
  scores: Record<number, number>;
}
