import { FormEvent, useState } from 'react';
import {
  createNewsAdmin,
  createNewsCategoryAdmin,
  fetchNews,
  fetchNewsCategories,
  fetchNewsDetail,
  removeNewsAdmin,
  removeNewsCategoryAdmin,
  updateNewsAdmin,
} from '@/lib/endpoints';
import type { NewsCategoryItem, NewsDetailItem, NewsItem } from '@/lib/types';

export default function NewsApiPage() {
  const [newsList, setNewsList] = useState<NewsItem[]>([]);
  const [categories, setCategories] = useState<NewsCategoryItem[]>([]);
  const [detail, setDetail] = useState<NewsDetailItem | null>(null);
  const [result, setResult] = useState<unknown>(null);
  const [newsId, setNewsId] = useState('');
  const [categoryId, setCategoryId] = useState('');
  const [title, setTitle] = useState('테스트 뉴스 제목');
  const [content, setContent] = useState('테스트 뉴스 내용');
  const [thumbnailUrl, setThumbnailUrl] = useState('');
  const [productIdsText, setProductIdsText] = useState('');
  const [categoryName, setCategoryName] = useState('테스트 카테고리');
  const [categorySlug, setCategorySlug] = useState('test-category');
  const [removeCategoryId, setRemoveCategoryId] = useState('');
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  const run = async (fn: () => Promise<void>) => {
    setMessage('');
    setError('');
    try {
      await fn();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'request failed');
    }
  };

  const parseProductIds = () =>
    productIdsText
      .split(',')
      .map((v) => Number(v.trim()))
      .filter((v) => Number.isFinite(v) && v > 0);

  return (
    <section>
      <h1>News API Step</h1>
      <p className="sub">step 37 - news API integration</p>
      {message ? <p className="sub">{message}</p> : null}
      {error ? <p className="error">{error}</p> : null}

      <div className="panel-grid">
        <div className="panel">
          <h2>Public News APIs</h2>
          <div className="button-row">
            <button type="button" onClick={() => run(async () => {
              const res = await fetchNews(10);
              setNewsList(res.data);
              setMessage('GET /news success');
            })}>load news list</button>
            <button type="button" onClick={() => run(async () => {
              const res = await fetchNewsCategories();
              setCategories(res.data);
              setMessage('GET /news/categories success');
            })}>load categories</button>
          </div>

          <form className="form-box mt-12" onSubmit={(e: FormEvent) => {
            e.preventDefault();
            run(async () => {
              const res = await fetchNewsDetail(Number(newsId));
              setDetail(res.data);
              setMessage('GET /news/:id success');
            });
          }}>
            <label htmlFor="news-id">newsId</label>
            <input id="news-id" value={newsId} onChange={(e) => setNewsId(e.target.value)} required />
            <button type="submit">load news detail</button>
          </form>
        </div>

        <div className="panel">
          <h2>Admin News APIs</h2>
          <form className="form-box" onSubmit={(e: FormEvent) => {
            e.preventDefault();
            run(async () => {
              const res = await createNewsAdmin({
                title,
                content,
                categoryId: Number(categoryId),
                thumbnailUrl: thumbnailUrl || undefined,
                productIds: parseProductIds(),
              });
              setResult(res.data);
              setMessage('POST /news success');
            });
          }}>
            <label htmlFor="news-title">title</label>
            <input id="news-title" value={title} onChange={(e) => setTitle(e.target.value)} required />
            <label htmlFor="news-content">content</label>
            <input id="news-content" value={content} onChange={(e) => setContent(e.target.value)} required />
            <label htmlFor="news-category-id">categoryId</label>
            <input id="news-category-id" value={categoryId} onChange={(e) => setCategoryId(e.target.value)} required />
            <label htmlFor="news-thumbnail-url">thumbnailUrl(optional)</label>
            <input id="news-thumbnail-url" value={thumbnailUrl} onChange={(e) => setThumbnailUrl(e.target.value)} />
            <label htmlFor="news-product-ids">productIds(csv optional)</label>
            <input id="news-product-ids" value={productIdsText} onChange={(e) => setProductIdsText(e.target.value)} />
            <button type="submit">create news</button>
          </form>

          <form className="form-box mt-12" onSubmit={(e: FormEvent) => {
            e.preventDefault();
            run(async () => {
              const res = await updateNewsAdmin(Number(newsId), {
                title,
                content,
                categoryId: categoryId ? Number(categoryId) : undefined,
                thumbnailUrl: thumbnailUrl || undefined,
                productIds: parseProductIds(),
              });
              setResult(res.data);
              setMessage('PATCH /news/:id success');
            });
          }}>
            <div className="button-row">
              <button type="submit">update news</button>
              <button type="button" onClick={() => run(async () => {
                const res = await removeNewsAdmin(Number(newsId));
                setResult(res.data);
                setMessage('DELETE /news/:id success');
              })}>delete news</button>
            </div>
          </form>

          <form className="form-box mt-12" onSubmit={(e: FormEvent) => {
            e.preventDefault();
            run(async () => {
              const res = await createNewsCategoryAdmin({ name: categoryName, slug: categorySlug });
              setResult(res.data);
              setMessage('POST /news/categories success');
            });
          }}>
            <label htmlFor="news-category-name">category name</label>
            <input id="news-category-name" value={categoryName} onChange={(e) => setCategoryName(e.target.value)} required />
            <label htmlFor="news-category-slug">category slug</label>
            <input id="news-category-slug" value={categorySlug} onChange={(e) => setCategorySlug(e.target.value)} required />
            <button type="submit">create category</button>
          </form>

          <form className="form-box mt-12" onSubmit={(e: FormEvent) => {
            e.preventDefault();
            run(async () => {
              const res = await removeNewsCategoryAdmin(Number(removeCategoryId));
              setResult(res.data);
              setMessage('DELETE /news/categories/:id success');
            });
          }}>
            <label htmlFor="remove-category-id">remove categoryId</label>
            <input id="remove-category-id" value={removeCategoryId} onChange={(e) => setRemoveCategoryId(e.target.value)} required />
            <button type="submit">delete category</button>
          </form>
        </div>
      </div>

      <div className="panel-grid mt-12">
        <div className="panel">
          <h2>News List</h2>
          <ul className="list">
            {newsList.map((item) => (
              <li key={item.id}>#{item.id} / {item.title}</li>
            ))}
          </ul>
        </div>
        <div className="panel">
          <h2>Categories</h2>
          <ul className="list">
            {categories.map((item) => (
              <li key={item.id}>#{item.id} / {item.name} ({item.slug})</li>
            ))}
          </ul>
        </div>
      </div>

      {detail ? <pre className="code-view mt-12">{JSON.stringify(detail, null, 2)}</pre> : null}
      {result ? <pre className="code-view mt-12">{JSON.stringify(result, null, 2)}</pre> : null}
    </section>
  );
}
