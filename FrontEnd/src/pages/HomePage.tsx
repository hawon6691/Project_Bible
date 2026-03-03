import { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import {
  fetchCategories,
  fetchDeals,
  fetchNews,
  fetchPopularKeywords,
  fetchProducts,
  fetchRankingProducts,
} from '@/lib/endpoints';
import type { Category, DealItem, NewsItem, ProductSummary, RankingProductItem } from '@/lib/types';

function flattenTree(items: Category[]): Array<{ id: number; name: string }> {
  const result: Array<{ id: number; name: string }> = [];

  const walk = (nodes: Category[]) => {
    nodes.forEach((node) => {
      result.push({ id: node.id, name: node.name });
      if (node.children?.length) walk(node.children);
    });
  };

  walk(items);
  return result;
}

export default function HomePage() {
  const [categories, setCategories] = useState<Category[]>([]);
  const [products, setProducts] = useState<ProductSummary[]>([]);
  const [ranking, setRanking] = useState<RankingProductItem[]>([]);
  const [popularKeywords, setPopularKeywords] = useState<string[]>([]);
  const [news, setNews] = useState<NewsItem[]>([]);
  const [deals, setDeals] = useState<DealItem[]>([]);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([
      fetchCategories(),
      fetchProducts({ page: 1, limit: 8, sort: 'popularity' }),
      fetchRankingProducts(6),
      fetchPopularKeywords(6),
      fetchNews(4),
      fetchDeals(4),
    ])
      .then(([categoryRes, productRes, rankRes, keywordRes, newsRes, dealRes]) => {
        setCategories(categoryRes.data);
        setProducts(productRes.data);
        setRanking(rankRes.data);
        setPopularKeywords(keywordRes.data.items.map((item) => item.keyword));
        setNews(newsRes.data);
        setDeals(dealRes.data);
      })
      .catch((err: Error) => {
        setError(err.message || 'failed to load');
      })
      .finally(() => setLoading(false));
  }, []);

  const flatCategories = useMemo(() => flattenTree(categories), [categories]);

  return (
    <section>
      <h1>API Applied FrontEnd</h1>
      <p className="sub">categories, products, rankings, keywords, news, deals API connected</p>
      {error ? <p className="error">{error}</p> : null}

      <div className="panel-grid">
        <div className="panel">
          <h2>Categories</h2>
          {loading ? <p>loading...</p> : null}
          <ul className="list">
            {flatCategories.map((cat) => (
              <li key={cat.id}>
                <Link to={`/products?categoryId=${cat.id}`}>{cat.name}</Link>
              </li>
            ))}
          </ul>
        </div>

        <div className="panel">
          <h2>Popular Products</h2>
          <ul className="cards">
            {products.map((product) => (
              <li key={product.id} className="card">
                <Link to={`/products/${product.id}`}>
                  <div className="thumb">
                    {product.thumbnailUrl ? <img src={product.thumbnailUrl} alt={product.name} /> : <span>NO IMAGE</span>}
                  </div>
                  <strong>{product.name}</strong>
                  <div>{Number(product.lowestPrice || 0).toLocaleString()} KRW</div>
                </Link>
              </li>
            ))}
          </ul>
        </div>
      </div>

      <div className="panel-grid mt-12">
        <div className="panel">
          <h2>Ranking API</h2>
          <ul className="list">
            {ranking.map((item, idx) => (
              <li key={`${item.productId}-${idx}`}>
                {idx + 1}. {item.name}
              </li>
            ))}
          </ul>
        </div>

        <div className="panel">
          <h2>Search Popular API</h2>
          <ul className="list">
            {popularKeywords.map((keyword) => (
              <li key={keyword}>
                <Link to={`/products?search=${encodeURIComponent(keyword)}`}>{keyword}</Link>
              </li>
            ))}
          </ul>
        </div>

        <div className="panel">
          <h2>News API</h2>
          <ul className="list">
            {news.map((item) => (
              <li key={item.id}>{item.title}</li>
            ))}
          </ul>
        </div>

        <div className="panel">
          <h2>Deals API</h2>
          <ul className="list">
            {deals.map((item) => (
              <li key={item.id}>{item.title}</li>
            ))}
          </ul>
        </div>
      </div>
    </section>
  );
}
