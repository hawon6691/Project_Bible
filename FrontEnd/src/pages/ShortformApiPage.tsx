import { FormEvent, useState } from 'react';
import {
  createShortform,
  createShortformComment,
  fetchShortformComments,
  fetchShortformDetail,
  fetchShortformFeed,
  fetchShortformRanking,
  fetchShortformTranscodeStatus,
  fetchUserShortforms,
  removeShortform,
  retryShortformTranscode,
  toggleShortformLike,
} from '@/lib/endpoints';
import type { ShortformCommentItem, ShortformItem } from '@/lib/types';

export default function ShortformApiPage() {
  const [shortforms, setShortforms] = useState<ShortformItem[]>([]);
  const [comments, setComments] = useState<ShortformCommentItem[]>([]);
  const [result, setResult] = useState<unknown>(null);
  const [file, setFile] = useState<File | null>(null);
  const [title, setTitle] = useState('테스트 숏폼');
  const [durationSec, setDurationSec] = useState('15');
  const [productIdsText, setProductIdsText] = useState('');
  const [shortformId, setShortformId] = useState('');
  const [userId, setUserId] = useState('');
  const [commentContent, setCommentContent] = useState('테스트 댓글');
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
      <h1>Shortform API Step</h1>
      <p className="sub">step 35 - shortform API integration</p>
      {message ? <p className="sub">{message}</p> : null}
      {error ? <p className="error">{error}</p> : null}

      <div className="panel-grid">
        <div className="panel">
          <h2>Create / List / Detail</h2>
          <form className="form-box" onSubmit={(e: FormEvent) => {
            e.preventDefault();
            if (!file) {
              setError('영상 파일을 선택해주세요.');
              return;
            }
            run(async () => {
              const productIds = productIdsText
                .split(',')
                .map((v) => Number(v.trim()))
                .filter((v) => Number.isFinite(v) && v > 0);
              const res = await createShortform({
                videoFile: file,
                title,
                durationSec: Number(durationSec),
                productIds: productIds.length ? productIds : undefined,
              });
              setResult(res.data);
              setMessage('POST /shortforms success');
            });
          }}>
            <label htmlFor="sf-file">video file</label>
            <input id="sf-file" type="file" onChange={(e) => setFile(e.target.files?.[0] || null)} required />
            <label htmlFor="sf-title">title</label>
            <input id="sf-title" value={title} onChange={(e) => setTitle(e.target.value)} required />
            <label htmlFor="sf-duration">durationSec</label>
            <input id="sf-duration" value={durationSec} onChange={(e) => setDurationSec(e.target.value)} />
            <label htmlFor="sf-product-ids">productIds(csv optional)</label>
            <input id="sf-product-ids" value={productIdsText} onChange={(e) => setProductIdsText(e.target.value)} />
            <button type="submit">create shortform</button>
          </form>

          <div className="button-row mt-12">
            <button type="button" onClick={() => run(async () => {
              const res = await fetchShortformFeed();
              setShortforms(res.data);
              setMessage('GET /shortforms success');
            })}>load feed</button>
            <button type="button" onClick={() => run(async () => {
              const res = await fetchShortformRanking('day');
              setShortforms(res.data);
              setMessage('GET /shortforms/ranking/list success');
            })}>load ranking(day)</button>
          </div>

          <form className="form-box mt-12" onSubmit={(e: FormEvent) => {
            e.preventDefault();
            run(async () => {
              const res = await fetchShortformDetail(Number(shortformId));
              setResult(res.data);
              setMessage('GET /shortforms/:id success');
            });
          }}>
            <label htmlFor="sf-id">shortformId</label>
            <input id="sf-id" value={shortformId} onChange={(e) => setShortformId(e.target.value)} required />
            <div className="button-row">
              <button type="submit">load detail</button>
              <button type="button" onClick={() => run(async () => {
                const res = await toggleShortformLike(Number(shortformId));
                setResult(res.data);
                setMessage('POST /shortforms/:id/like success');
              })}>toggle like</button>
              <button type="button" onClick={() => run(async () => {
                const res = await removeShortform(Number(shortformId));
                setResult(res.data);
                setMessage('DELETE /shortforms/:id success');
              })}>delete shortform</button>
            </div>
          </form>
        </div>

        <div className="panel">
          <h2>Comments / Transcode / User list</h2>
          <form className="form-box" onSubmit={(e: FormEvent) => {
            e.preventDefault();
            run(async () => {
              const res = await createShortformComment(Number(shortformId), { content: commentContent });
              setResult(res.data);
              setMessage('POST /shortforms/:id/comments success');
            });
          }}>
            <label htmlFor="sf-comment-content">comment content</label>
            <input id="sf-comment-content" value={commentContent} onChange={(e) => setCommentContent(e.target.value)} required />
            <div className="button-row">
              <button type="submit">create comment</button>
              <button type="button" onClick={() => run(async () => {
                const res = await fetchShortformComments(Number(shortformId));
                setComments(res.data);
                setMessage('GET /shortforms/:id/comments success');
              })}>load comments</button>
            </div>
          </form>

          <div className="button-row mt-12">
            <button type="button" onClick={() => run(async () => {
              const res = await fetchShortformTranscodeStatus(Number(shortformId));
              setResult(res.data);
              setMessage('GET /shortforms/:id/transcode-status success');
            })}>transcode status</button>
            <button type="button" onClick={() => run(async () => {
              const res = await retryShortformTranscode(Number(shortformId));
              setResult(res.data);
              setMessage('POST /shortforms/:id/transcode/retry success');
            })}>retry transcode</button>
          </div>

          <form className="form-box mt-12" onSubmit={(e: FormEvent) => {
            e.preventDefault();
            run(async () => {
              const res = await fetchUserShortforms(Number(userId));
              setShortforms(res.data);
              setMessage('GET /shortforms/user/:userId success');
            });
          }}>
            <label htmlFor="sf-user-id">userId</label>
            <input id="sf-user-id" value={userId} onChange={(e) => setUserId(e.target.value)} required />
            <button type="submit">load user shortforms</button>
          </form>
        </div>
      </div>

      <div className="panel-grid mt-12">
        <div className="panel">
          <h2>Shortforms</h2>
          <ul className="list">
            {shortforms.map((item) => (
              <li key={item.id}>#{item.id} / {item.title} / likes {item.likeCount} / status {item.transcodeStatus}</li>
            ))}
          </ul>
        </div>
        <div className="panel">
          <h2>Comments</h2>
          <ul className="list">
            {comments.map((item) => (
              <li key={item.id}>#{item.id} / user {item.userId} / {item.content}</li>
            ))}
          </ul>
        </div>
      </div>

      {result ? <pre className="code-view mt-12">{JSON.stringify(result, null, 2)}</pre> : null}
    </section>
  );
}
