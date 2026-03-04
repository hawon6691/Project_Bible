import { FormEvent, useState } from 'react';
import {
  createFaqAdmin,
  createNoticeAdmin,
  fetchFaqs,
  fetchNotices,
  removeFaqAdmin,
  removeNoticeAdmin,
  updateFaqAdmin,
  updateNoticeAdmin,
} from '@/lib/endpoints';

export default function HelpApiPage() {
  const [faqs, setFaqs] = useState<any[]>([]);
  const [notices, setNotices] = useState<any[]>([]);

  const [faqCategory, setFaqCategory] = useState('');
  const [faqKeyword, setFaqKeyword] = useState('');
  const [noticePage, setNoticePage] = useState('1');
  const [noticeLimit, setNoticeLimit] = useState('20');

  const [createFaqCategory, setCreateFaqCategory] = useState('ORDER');
  const [createFaqQuestion, setCreateFaqQuestion] = useState('');
  const [createFaqAnswer, setCreateFaqAnswer] = useState('');
  const [createFaqActive, setCreateFaqActive] = useState(true);

  const [updateFaqId, setUpdateFaqId] = useState('');
  const [updateFaqQuestion, setUpdateFaqQuestion] = useState('');
  const [deleteFaqId, setDeleteFaqId] = useState('');

  const [createNoticeTitle, setCreateNoticeTitle] = useState('');
  const [createNoticeContent, setCreateNoticeContent] = useState('');
  const [createNoticePublished, setCreateNoticePublished] = useState(true);

  const [updateNoticeId, setUpdateNoticeId] = useState('');
  const [updateNoticeTitle, setUpdateNoticeTitle] = useState('');
  const [deleteNoticeId, setDeleteNoticeId] = useState('');

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
      <h1>Help API Step</h1>
      <p className="sub">spec step 18 - faq/notice API integration</p>
      {message ? <p className="sub">{message}</p> : null}
      {error ? <p className="error">{error}</p> : null}

      <div className="panel-grid">
        <div className="panel">
          <h2>FAQ Public API</h2>
          <form className="form-box" onSubmit={(e: FormEvent) => {
            e.preventDefault();
            run(async () => {
              const res = await fetchFaqs({
                ...(faqCategory ? { category: faqCategory } : {}),
                ...(faqKeyword ? { keyword: faqKeyword } : {}),
                page: 1,
                limit: 30,
              });
              setFaqs(res.data as any[]);
              setMessage('GET /faq success');
            });
          }}>
            <label htmlFor="faqCategory">category(optional)</label>
            <input id="faqCategory" value={faqCategory} onChange={(e) => setFaqCategory(e.target.value)} />
            <label htmlFor="faqKeyword">keyword(optional)</label>
            <input id="faqKeyword" value={faqKeyword} onChange={(e) => setFaqKeyword(e.target.value)} />
            <button type="submit">GET /faq</button>
          </form>

          <ul className="list mt-12">
            {faqs.map((faq) => (
              <li key={faq.id}>{faq.id} / {faq.category} / {faq.question}</li>
            ))}
          </ul>
        </div>

        <div className="panel">
          <h2>Notice Public API</h2>
          <form className="form-box" onSubmit={(e: FormEvent) => {
            e.preventDefault();
            run(async () => {
              const res = await fetchNotices({ page: Number(noticePage), limit: Number(noticeLimit) });
              setNotices(res.data as any[]);
              setMessage('GET /notices success');
            });
          }}>
            <label htmlFor="noticePage">page</label>
            <input id="noticePage" value={noticePage} onChange={(e) => setNoticePage(e.target.value)} required />
            <label htmlFor="noticeLimit">limit</label>
            <input id="noticeLimit" value={noticeLimit} onChange={(e) => setNoticeLimit(e.target.value)} required />
            <button type="submit">GET /notices</button>
          </form>

          <ul className="list mt-12">
            {notices.map((notice) => (
              <li key={notice.id}>{notice.id} / {notice.title} / published {String(notice.isPublished)}</li>
            ))}
          </ul>
        </div>
      </div>

      <div className="panel-grid mt-12">
        <div className="panel">
          <h2>FAQ Admin API</h2>
          <form className="form-box" onSubmit={(e: FormEvent) => {
            e.preventDefault();
            run(async () => {
              await createFaqAdmin({
                category: createFaqCategory,
                question: createFaqQuestion,
                answer: createFaqAnswer,
                isActive: createFaqActive,
              });
              setMessage('POST /admin/faq success');
            });
          }}>
            <label htmlFor="createFaqCategory">category</label>
            <input id="createFaqCategory" value={createFaqCategory} onChange={(e) => setCreateFaqCategory(e.target.value)} required />
            <label htmlFor="createFaqQuestion">question</label>
            <input id="createFaqQuestion" value={createFaqQuestion} onChange={(e) => setCreateFaqQuestion(e.target.value)} required />
            <label htmlFor="createFaqAnswer">answer</label>
            <input id="createFaqAnswer" value={createFaqAnswer} onChange={(e) => setCreateFaqAnswer(e.target.value)} required />
            <label htmlFor="createFaqActive">isActive</label>
            <input id="createFaqActive" type="checkbox" checked={createFaqActive} onChange={(e) => setCreateFaqActive(e.target.checked)} />
            <button type="submit">POST /admin/faq</button>
          </form>

          <form className="form-box mt-12" onSubmit={(e: FormEvent) => {
            e.preventDefault();
            run(async () => {
              await updateFaqAdmin(Number(updateFaqId), {
                ...(updateFaqQuestion ? { question: updateFaqQuestion } : {}),
              });
              setMessage('PATCH /admin/faq/:id success');
            });
          }}>
            <label htmlFor="updateFaqId">faqId</label>
            <input id="updateFaqId" value={updateFaqId} onChange={(e) => setUpdateFaqId(e.target.value)} required />
            <label htmlFor="updateFaqQuestion">question(optional)</label>
            <input id="updateFaqQuestion" value={updateFaqQuestion} onChange={(e) => setUpdateFaqQuestion(e.target.value)} />
            <button type="submit">PATCH /admin/faq/:id</button>
          </form>

          <form className="form-box mt-12" onSubmit={(e: FormEvent) => {
            e.preventDefault();
            run(async () => {
              await removeFaqAdmin(Number(deleteFaqId));
              setMessage('DELETE /admin/faq/:id success');
            });
          }}>
            <label htmlFor="deleteFaqId">faqId</label>
            <input id="deleteFaqId" value={deleteFaqId} onChange={(e) => setDeleteFaqId(e.target.value)} required />
            <button type="submit">DELETE /admin/faq/:id</button>
          </form>
        </div>

        <div className="panel">
          <h2>Notice Admin API</h2>
          <form className="form-box" onSubmit={(e: FormEvent) => {
            e.preventDefault();
            run(async () => {
              await createNoticeAdmin({
                title: createNoticeTitle,
                content: createNoticeContent,
                isPublished: createNoticePublished,
              });
              setMessage('POST /admin/notices success');
            });
          }}>
            <label htmlFor="createNoticeTitle">title</label>
            <input id="createNoticeTitle" value={createNoticeTitle} onChange={(e) => setCreateNoticeTitle(e.target.value)} required />
            <label htmlFor="createNoticeContent">content</label>
            <input id="createNoticeContent" value={createNoticeContent} onChange={(e) => setCreateNoticeContent(e.target.value)} required />
            <label htmlFor="createNoticePublished">isPublished</label>
            <input id="createNoticePublished" type="checkbox" checked={createNoticePublished} onChange={(e) => setCreateNoticePublished(e.target.checked)} />
            <button type="submit">POST /admin/notices</button>
          </form>

          <form className="form-box mt-12" onSubmit={(e: FormEvent) => {
            e.preventDefault();
            run(async () => {
              await updateNoticeAdmin(Number(updateNoticeId), {
                ...(updateNoticeTitle ? { title: updateNoticeTitle } : {}),
              });
              setMessage('PATCH /admin/notices/:id success');
            });
          }}>
            <label htmlFor="updateNoticeId">noticeId</label>
            <input id="updateNoticeId" value={updateNoticeId} onChange={(e) => setUpdateNoticeId(e.target.value)} required />
            <label htmlFor="updateNoticeTitle">title(optional)</label>
            <input id="updateNoticeTitle" value={updateNoticeTitle} onChange={(e) => setUpdateNoticeTitle(e.target.value)} />
            <button type="submit">PATCH /admin/notices/:id</button>
          </form>

          <form className="form-box mt-12" onSubmit={(e: FormEvent) => {
            e.preventDefault();
            run(async () => {
              await removeNoticeAdmin(Number(deleteNoticeId));
              setMessage('DELETE /admin/notices/:id success');
            });
          }}>
            <label htmlFor="deleteNoticeId">noticeId</label>
            <input id="deleteNoticeId" value={deleteNoticeId} onChange={(e) => setDeleteNoticeId(e.target.value)} required />
            <button type="submit">DELETE /admin/notices/:id</button>
          </form>
        </div>
      </div>
    </section>
  );
}
