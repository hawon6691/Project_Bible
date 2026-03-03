import { FormEvent, useState } from 'react';
import {
  createReview,
  fetchReviews,
  removeReview,
  updateReview,
} from '@/lib/endpoints';

export default function ReviewApiPage() {
  const [productId, setProductId] = useState('');
  const [reviews, setReviews] = useState<any[]>([]);

  const [createProductId, setCreateProductId] = useState('');
  const [createOrderId, setCreateOrderId] = useState('');
  const [createRating, setCreateRating] = useState('5');
  const [createContent, setCreateContent] = useState('');

  const [updateReviewId, setUpdateReviewId] = useState('');
  const [updateRating, setUpdateRating] = useState('');
  const [updateContent, setUpdateContent] = useState('');

  const [deleteReviewId, setDeleteReviewId] = useState('');

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

  return (
    <section>
      <h1>Review API Step</h1>
      <p className="sub">spec step 12 - review API integration</p>
      {message ? <p className="sub">{message}</p> : null}
      {error ? <p className="error">{error}</p> : null}

      <div className="panel-grid">
        <div className="panel">
          <h2>GET /products/:productId/reviews</h2>
          <form className="form-box" onSubmit={(e: FormEvent) => {
            e.preventDefault();
            run(async () => {
              const res = await fetchReviews(Number(productId), 1, 20);
              setReviews(res.data as any[]);
              setMessage('GET /products/:productId/reviews success');
            });
          }}>
            <label htmlFor="productId">productId</label>
            <input id="productId" value={productId} onChange={(e) => setProductId(e.target.value)} required />
            <button type="submit">load reviews</button>
          </form>

          <ul className="list mt-12">
            {reviews.map((review) => (
              <li key={review.id}>
                {review.id} / product {review.productId} / order {review.orderId} / {review.rating}점 / {review.content}
              </li>
            ))}
          </ul>
        </div>

        <div className="panel">
          <h2>POST /products/:productId/reviews</h2>
          <form className="form-box" onSubmit={(e: FormEvent) => {
            e.preventDefault();
            run(async () => {
              await createReview(Number(createProductId), {
                orderId: Number(createOrderId),
                rating: Number(createRating),
                content: createContent,
              });
              setMessage('POST /products/:productId/reviews success');
              if (productId && Number(productId) === Number(createProductId)) {
                const res = await fetchReviews(Number(productId), 1, 20);
                setReviews(res.data as any[]);
              }
            });
          }}>
            <label htmlFor="createProductId">productId</label>
            <input id="createProductId" value={createProductId} onChange={(e) => setCreateProductId(e.target.value)} required />
            <label htmlFor="createOrderId">orderId</label>
            <input id="createOrderId" value={createOrderId} onChange={(e) => setCreateOrderId(e.target.value)} required />
            <label htmlFor="createRating">rating</label>
            <input id="createRating" value={createRating} onChange={(e) => setCreateRating(e.target.value)} required />
            <label htmlFor="createContent">content</label>
            <input id="createContent" value={createContent} onChange={(e) => setCreateContent(e.target.value)} required />
            <button type="submit">create review</button>
          </form>
        </div>
      </div>

      <div className="panel-grid mt-12">
        <div className="panel">
          <h2>PATCH /reviews/:id</h2>
          <form className="form-box" onSubmit={(e: FormEvent) => {
            e.preventDefault();
            run(async () => {
              await updateReview(Number(updateReviewId), {
                ...(updateRating ? { rating: Number(updateRating) } : {}),
                ...(updateContent ? { content: updateContent } : {}),
              });
              setMessage('PATCH /reviews/:id success');
            });
          }}>
            <label htmlFor="updateReviewId">reviewId</label>
            <input id="updateReviewId" value={updateReviewId} onChange={(e) => setUpdateReviewId(e.target.value)} required />
            <label htmlFor="updateRating">rating(optional)</label>
            <input id="updateRating" value={updateRating} onChange={(e) => setUpdateRating(e.target.value)} />
            <label htmlFor="updateContent">content(optional)</label>
            <input id="updateContent" value={updateContent} onChange={(e) => setUpdateContent(e.target.value)} />
            <button type="submit">update review</button>
          </form>
        </div>

        <div className="panel">
          <h2>DELETE /reviews/:id</h2>
          <form className="form-box" onSubmit={(e: FormEvent) => {
            e.preventDefault();
            run(async () => {
              await removeReview(Number(deleteReviewId));
              setMessage('DELETE /reviews/:id success');
            });
          }}>
            <label htmlFor="deleteReviewId">reviewId</label>
            <input id="deleteReviewId" value={deleteReviewId} onChange={(e) => setDeleteReviewId(e.target.value)} required />
            <button type="submit">delete review</button>
          </form>
        </div>
      </div>
    </section>
  );
}
