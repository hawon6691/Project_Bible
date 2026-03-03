import { useEffect, useMemo, useState } from 'react';
import { useParams } from 'react-router-dom';
import { addToCart, createReview, fetchProduct, fetchReviews, toggleWishlist } from '@/lib/endpoints';
import { getAccessToken } from '@/lib/auth';
import type { ProductDetail, ReviewItem } from '@/lib/types';

export default function ProductDetailPage() {
  const { id } = useParams();
  const productId = Number(id);

  const [product, setProduct] = useState<ProductDetail | null>(null);
  const [reviews, setReviews] = useState<ReviewItem[]>([]);
  const [qty, setQty] = useState(1);
  const [sellerId, setSellerId] = useState(1);
  const [orderIdForReview, setOrderIdForReview] = useState('');
  const [rating, setRating] = useState(5);
  const [reviewContent, setReviewContent] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');

  const loadData = async () => {
    if (!productId) return;
    setLoading(true);
    try {
      const [productRes, reviewRes] = await Promise.all([
        fetchProduct(productId),
        fetchReviews(productId, 1, 10),
      ]);
      setProduct(productRes.data);
      setReviews(reviewRes.data);
    } catch (err) {
      const msg = err instanceof Error ? err.message : 'failed';
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, [productId]);

  const primaryImage = useMemo(() => {
    if (!product?.images?.length) return '';
    return product.images.find((img) => img.isMain)?.url || product.images[0].url;
  }, [product]);

  if (!productId) return <p className="error">invalid product id</p>;
  if (loading) return <p>loading...</p>;
  if (error) return <p className="error">{error}</p>;
  if (!product) return <p className="error">no product</p>;

  return (
    <section>
      <h1>{product.name}</h1>
      <div className="detail-grid">
        <div className="thumb detail">
          {primaryImage ? <img src={primaryImage} alt={product.name} /> : <span>NO IMAGE</span>}
        </div>
        <div>
          <p>{product.description || 'no description'}</p>
          <p>lowest: <strong>{Number(product.lowestPrice || product.price || 0).toLocaleString()} KRW</strong></p>
          <p>stock: {product.stock}</p>

          <div className="form-row">
            <label htmlFor="sellerId">sellerId</label>
            <input id="sellerId" type="number" value={sellerId} onChange={(e) => setSellerId(Number(e.target.value || 1))} min={1} />
          </div>

          <div className="form-row">
            <label htmlFor="qty">qty</label>
            <input id="qty" type="number" value={qty} onChange={(e) => setQty(Number(e.target.value || 1))} min={1} />
          </div>

          <div className="button-row">
            <button
              type="button"
              onClick={() => {
                setMessage('');
                addToCart({ productId, sellerId, quantity: qty })
                  .then(() => setMessage('added to cart'))
                  .catch((err: Error) => setMessage(err.message || 'add failed'));
              }}
            >
              Add to Cart API
            </button>
            <button
              type="button"
              onClick={() => {
                setMessage('');
                toggleWishlist(productId)
                  .then((res) => setMessage(res.data.wishlisted ? 'wishlisted' : 'wishlist removed'))
                  .catch((err: Error) => setMessage(err.message || 'wishlist failed'));
              }}
              disabled={!getAccessToken()}
            >
              Wishlist Toggle API
            </button>
          </div>

          {message ? <p className="sub">{message}</p> : null}
        </div>
      </div>

      <div className="panel mt-12">
        <h2>Reviews API</h2>
        <ul className="list">
          {reviews.map((r) => (
            <li key={r.id}>[{r.rating}/5] {r.content} - {r.user.nickname}</li>
          ))}
        </ul>

        <form
          className="form-box mt-12"
          onSubmit={async (e) => {
            e.preventDefault();
            if (!getAccessToken()) {
              setMessage('login required for create review');
              return;
            }
            try {
              await createReview(productId, {
                orderId: Number(orderIdForReview),
                rating,
                content: reviewContent,
              });
              setOrderIdForReview('');
              setReviewContent('');
              await loadData();
              setMessage('review created');
            } catch (err) {
              const msg = err instanceof Error ? err.message : 'review failed';
              setMessage(msg);
            }
          }}
        >
          <label htmlFor="orderId">orderId</label>
          <input id="orderId" value={orderIdForReview} onChange={(e) => setOrderIdForReview(e.target.value)} required />

          <label htmlFor="rating">rating (1~5)</label>
          <input id="rating" type="number" min={1} max={5} value={rating} onChange={(e) => setRating(Number(e.target.value || 1))} required />

          <label htmlFor="content">content</label>
          <input id="content" value={reviewContent} onChange={(e) => setReviewContent(e.target.value)} required />

          <button type="submit">Create Review API</button>
        </form>
      </div>
    </section>
  );
}
