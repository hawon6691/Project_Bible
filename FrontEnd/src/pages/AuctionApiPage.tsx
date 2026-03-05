import { FormEvent, useState } from 'react';
import {
  cancelAuction,
  createAuction,
  createAuctionBid,
  fetchAuctionDetail,
  fetchAuctions,
  removeAuctionBid,
  selectAuctionBid,
  updateAuctionBid,
} from '@/lib/endpoints';
import type { AuctionSummaryItem } from '@/lib/types';

export default function AuctionApiPage() {
  const [auctions, setAuctions] = useState<AuctionSummaryItem[]>([]);
  const [result, setResult] = useState<unknown>(null);
  const [auctionId, setAuctionId] = useState('');
  const [bidId, setBidId] = useState('');
  const [title, setTitle] = useState('역경매 테스트');
  const [description, setDescription] = useState('상세 스펙 기반 역경매');
  const [categoryId, setCategoryId] = useState('');
  const [specsText, setSpecsText] = useState('{"cpu":"i7","ram":"16GB"}');
  const [budget, setBudget] = useState('1500000');
  const [bidPrice, setBidPrice] = useState('1200000');
  const [bidDescription, setBidDescription] = useState('빠른 배송 가능');
  const [deliveryDays, setDeliveryDays] = useState('3');
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
      <h1>Auction API Step</h1>
      <p className="sub">step 43 - reverse auction API integration</p>
      {message ? <p className="sub">{message}</p> : null}
      {error ? <p className="error">{error}</p> : null}

      <div className="panel-grid">
        <div className="panel">
          <h2>Auction APIs</h2>
          <div className="button-row">
            <button
              type="button"
              onClick={() =>
                run(async () => {
                  const res = await fetchAuctions();
                  setAuctions(res.data);
                  setMessage('GET /auctions success');
                })
              }
            >
              load auctions
            </button>
          </div>

          <form
            className="form-box mt-12"
            onSubmit={(e: FormEvent) => {
              e.preventDefault();
              run(async () => {
                let specs: Record<string, unknown> | undefined;
                if (specsText.trim()) {
                  specs = JSON.parse(specsText);
                }
                const res = await createAuction({
                  title,
                  description,
                  categoryId: Number(categoryId),
                  specs,
                  budget: Number(budget),
                });
                setResult(res.data);
                setMessage('POST /auctions success');
              });
            }}
          >
            <label htmlFor="auction-title">title</label>
            <input id="auction-title" value={title} onChange={(e) => setTitle(e.target.value)} required />
            <label htmlFor="auction-description">description</label>
            <input id="auction-description" value={description} onChange={(e) => setDescription(e.target.value)} required />
            <label htmlFor="auction-category-id">categoryId</label>
            <input id="auction-category-id" value={categoryId} onChange={(e) => setCategoryId(e.target.value)} required />
            <label htmlFor="auction-specs">specs(json optional)</label>
            <input id="auction-specs" value={specsText} onChange={(e) => setSpecsText(e.target.value)} />
            <label htmlFor="auction-budget">budget</label>
            <input id="auction-budget" value={budget} onChange={(e) => setBudget(e.target.value)} required />
            <button type="submit">create auction</button>
          </form>

          <form
            className="form-box mt-12"
            onSubmit={(e: FormEvent) => {
              e.preventDefault();
              run(async () => {
                const res = await fetchAuctionDetail(Number(auctionId));
                setResult(res.data);
                setMessage('GET /auctions/:id success');
              });
            }}
          >
            <label htmlFor="auction-id">auctionId</label>
            <input id="auction-id" value={auctionId} onChange={(e) => setAuctionId(e.target.value)} required />
            <div className="button-row">
              <button type="submit">load auction detail</button>
              <button
                type="button"
                onClick={() =>
                  run(async () => {
                    const res = await cancelAuction(Number(auctionId));
                    setResult(res.data);
                    setMessage('DELETE /auctions/:id success');
                  })
                }
              >
                cancel auction
              </button>
            </div>
          </form>
        </div>

        <div className="panel">
          <h2>Bid APIs</h2>
          <form
            className="form-box"
            onSubmit={(e: FormEvent) => {
              e.preventDefault();
              run(async () => {
                const res = await createAuctionBid(Number(auctionId), {
                  price: Number(bidPrice),
                  description: bidDescription || undefined,
                  deliveryDays: Number(deliveryDays),
                });
                setResult(res.data);
                setMessage('POST /auctions/:id/bids success');
              });
            }}
          >
            <label htmlFor="bid-price">price</label>
            <input id="bid-price" value={bidPrice} onChange={(e) => setBidPrice(e.target.value)} required />
            <label htmlFor="bid-description">description(optional)</label>
            <input id="bid-description" value={bidDescription} onChange={(e) => setBidDescription(e.target.value)} />
            <label htmlFor="delivery-days">deliveryDays</label>
            <input id="delivery-days" value={deliveryDays} onChange={(e) => setDeliveryDays(e.target.value)} required />
            <button type="submit">create bid</button>
          </form>

          <form
            className="form-box mt-12"
            onSubmit={(e: FormEvent) => {
              e.preventDefault();
              run(async () => {
                const res = await updateAuctionBid(Number(auctionId), Number(bidId), {
                  price: Number(bidPrice),
                  description: bidDescription || undefined,
                  deliveryDays: Number(deliveryDays),
                });
                setResult(res.data);
                setMessage('PATCH /auctions/:id/bids/:bidId success');
              });
            }}
          >
            <label htmlFor="bid-id">bidId</label>
            <input id="bid-id" value={bidId} onChange={(e) => setBidId(e.target.value)} required />
            <div className="button-row">
              <button type="submit">update bid</button>
              <button
                type="button"
                onClick={() =>
                  run(async () => {
                    const res = await selectAuctionBid(Number(auctionId), Number(bidId));
                    setResult(res.data);
                    setMessage('PATCH /auctions/:id/bids/:bidId/select success');
                  })
                }
              >
                select bid
              </button>
              <button
                type="button"
                onClick={() =>
                  run(async () => {
                    const res = await removeAuctionBid(Number(auctionId), Number(bidId));
                    setResult(res.data);
                    setMessage('DELETE /auctions/:id/bids/:bidId success');
                  })
                }
              >
                remove bid
              </button>
            </div>
          </form>
        </div>
      </div>

      <div className="panel mt-12">
        <h2>Auction List</h2>
        <ul className="list">
          {auctions.map((item) => (
            <li key={item.id}>#{item.id} / {item.title} / {item.status} / budget {item.budget}</li>
          ))}
        </ul>
      </div>

      {result ? <pre className="code-view mt-12">{JSON.stringify(result, null, 2)}</pre> : null}
    </section>
  );
}
