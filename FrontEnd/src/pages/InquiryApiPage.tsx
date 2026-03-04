import { FormEvent, useState } from 'react';
import {
  answerInquiry,
  createProductInquiry,
  fetchMyInquiries,
  fetchProductInquiries,
  removeInquiry,
} from '@/lib/endpoints';

export default function InquiryApiPage() {
  const [productId, setProductId] = useState('');
  const [page, setPage] = useState('1');
  const [limit, setLimit] = useState('20');
  const [productInquiries, setProductInquiries] = useState<any[]>([]);
  const [myInquiries, setMyInquiries] = useState<any[]>([]);

  const [createProductId, setCreateProductId] = useState('');
  const [createTitle, setCreateTitle] = useState('');
  const [createContent, setCreateContent] = useState('');
  const [createSecret, setCreateSecret] = useState(false);

  const [answerInquiryId, setAnswerInquiryId] = useState('');
  const [answerContent, setAnswerContent] = useState('');

  const [deleteInquiryId, setDeleteInquiryId] = useState('');

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
      <h1>Inquiry API Step</h1>
      <p className="sub">spec step 16 - inquiry API integration</p>
      {message ? <p className="sub">{message}</p> : null}
      {error ? <p className="error">{error}</p> : null}

      <div className="panel-grid">
        <div className="panel">
          <h2>GET /products/:productId/inquiries</h2>
          <form className="form-box" onSubmit={(e: FormEvent) => {
            e.preventDefault();
            run(async () => {
              const res = await fetchProductInquiries(Number(productId), {
                page: Number(page),
                limit: Number(limit),
              });
              setProductInquiries(res.data as any[]);
              setMessage('GET /products/:productId/inquiries success');
            });
          }}>
            <label htmlFor="productId">productId</label>
            <input id="productId" value={productId} onChange={(e) => setProductId(e.target.value)} required />
            <label htmlFor="page">page</label>
            <input id="page" value={page} onChange={(e) => setPage(e.target.value)} required />
            <label htmlFor="limit">limit</label>
            <input id="limit" value={limit} onChange={(e) => setLimit(e.target.value)} required />
            <button type="submit">load product inquiries</button>
          </form>

          <ul className="list mt-12">
            {productInquiries.map((item) => (
              <li key={item.id}>
                {item.id} / product {item.productId} / {item.title} / secret={String(item.isSecret)} / status {item.status || '-'}
              </li>
            ))}
          </ul>
        </div>

        <div className="panel">
          <h2>POST /products/:productId/inquiries</h2>
          <form className="form-box" onSubmit={(e: FormEvent) => {
            e.preventDefault();
            run(async () => {
              await createProductInquiry(Number(createProductId), {
                title: createTitle,
                content: createContent,
                isSecret: createSecret,
              });
              setMessage('POST /products/:productId/inquiries success');
            });
          }}>
            <label htmlFor="createProductId">productId</label>
            <input id="createProductId" value={createProductId} onChange={(e) => setCreateProductId(e.target.value)} required />
            <label htmlFor="createTitle">title</label>
            <input id="createTitle" value={createTitle} onChange={(e) => setCreateTitle(e.target.value)} required />
            <label htmlFor="createContent">content</label>
            <input id="createContent" value={createContent} onChange={(e) => setCreateContent(e.target.value)} required />
            <label htmlFor="createSecret">isSecret</label>
            <select id="createSecret" value={String(createSecret)} onChange={(e) => setCreateSecret(e.target.value === 'true')}>
              <option value="false">false</option>
              <option value="true">true</option>
            </select>
            <button type="submit">create inquiry</button>
          </form>
        </div>
      </div>

      <div className="panel-grid mt-12">
        <div className="panel">
          <h2>POST /inquiries/:id/answer</h2>
          <form className="form-box" onSubmit={(e: FormEvent) => {
            e.preventDefault();
            run(async () => {
              await answerInquiry(Number(answerInquiryId), { content: answerContent });
              setMessage('POST /inquiries/:id/answer success');
            });
          }}>
            <label htmlFor="answerInquiryId">inquiryId</label>
            <input id="answerInquiryId" value={answerInquiryId} onChange={(e) => setAnswerInquiryId(e.target.value)} required />
            <label htmlFor="answerContent">content</label>
            <input id="answerContent" value={answerContent} onChange={(e) => setAnswerContent(e.target.value)} required />
            <button type="submit">answer inquiry</button>
          </form>
        </div>

        <div className="panel">
          <h2>GET /inquiries/me</h2>
          <div className="button-row">
            <button
              type="button"
              onClick={() => run(async () => {
                const res = await fetchMyInquiries({
                  page: Number(page),
                  limit: Number(limit),
                });
                setMyInquiries(res.data as any[]);
                setMessage('GET /inquiries/me success');
              })}
            >
              load my inquiries
            </button>
          </div>

          <ul className="list mt-12">
            {myInquiries.map((item) => (
              <li key={item.id}>
                {item.id} / {item.title} / status {item.status || '-'} / answer {item.answer ? 'Y' : 'N'}
              </li>
            ))}
          </ul>
        </div>
      </div>

      <div className="panel-grid mt-12">
        <div className="panel">
          <h2>DELETE /inquiries/:id</h2>
          <form className="form-box" onSubmit={(e: FormEvent) => {
            e.preventDefault();
            run(async () => {
              await removeInquiry(Number(deleteInquiryId));
              setMessage('DELETE /inquiries/:id success');
            });
          }}>
            <label htmlFor="deleteInquiryId">inquiryId</label>
            <input id="deleteInquiryId" value={deleteInquiryId} onChange={(e) => setDeleteInquiryId(e.target.value)} required />
            <button type="submit">delete inquiry</button>
          </form>
        </div>
      </div>
    </section>
  );
}
