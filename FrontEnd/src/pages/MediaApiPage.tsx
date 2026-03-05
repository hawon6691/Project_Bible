import { FormEvent, useState } from 'react';
import {
  createMediaPresignedUrl,
  fetchMediaMetadata,
  fetchMediaStreamInfo,
  removeMedia,
  uploadMedia,
} from '@/lib/endpoints';
import type { MediaAssetItem } from '@/lib/types';

export default function MediaApiPage() {
  const [files, setFiles] = useState<File[]>([]);
  const [ownerType, setOwnerType] = useState<'PRODUCT' | 'COMMUNITY' | 'SUPPORT' | 'SELLER' | 'SHORTFORM' | 'USER'>('PRODUCT');
  const [ownerId, setOwnerId] = useState('');
  const [mediaId, setMediaId] = useState('');
  const [fileName, setFileName] = useState('demo.mp4');
  const [fileType, setFileType] = useState('video/mp4');
  const [fileSize, setFileSize] = useState('1048576');
  const [uploaded, setUploaded] = useState<MediaAssetItem[]>([]);
  const [result, setResult] = useState<unknown>(null);
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
      <h1>Media API Step</h1>
      <p className="sub">step 36 - media API integration</p>
      {message ? <p className="sub">{message}</p> : null}
      {error ? <p className="error">{error}</p> : null}

      <div className="panel-grid">
        <div className="panel">
          <h2>Upload / Presigned URL</h2>
          <form className="form-box" onSubmit={(e: FormEvent) => {
            e.preventDefault();
            if (!files.length) {
              setError('파일을 선택해주세요.');
              return;
            }
            run(async () => {
              const res = await uploadMedia(files, {
                ownerType,
                ownerId: Number(ownerId),
              });
              setUploaded(res.data);
              setMessage('POST /media/upload success');
            });
          }}>
            <label htmlFor="media-files">files</label>
            <input
              id="media-files"
              type="file"
              multiple
              onChange={(e) => setFiles(Array.from(e.target.files || []))}
              required
            />
            <label htmlFor="media-owner-type">ownerType</label>
            <input id="media-owner-type" value={ownerType} onChange={(e) => setOwnerType(e.target.value as any)} required />
            <label htmlFor="media-owner-id">ownerId</label>
            <input id="media-owner-id" value={ownerId} onChange={(e) => setOwnerId(e.target.value)} required />
            <button type="submit">upload media</button>
          </form>

          <form className="form-box mt-12" onSubmit={(e: FormEvent) => {
            e.preventDefault();
            run(async () => {
              const res = await createMediaPresignedUrl({
                fileName,
                fileType,
                fileSize: Number(fileSize),
              });
              setResult(res.data);
              setMessage('POST /media/presigned-url success');
            });
          }}>
            <label htmlFor="media-file-name">fileName</label>
            <input id="media-file-name" value={fileName} onChange={(e) => setFileName(e.target.value)} required />
            <label htmlFor="media-file-type">fileType</label>
            <input id="media-file-type" value={fileType} onChange={(e) => setFileType(e.target.value)} required />
            <label htmlFor="media-file-size">fileSize</label>
            <input id="media-file-size" value={fileSize} onChange={(e) => setFileSize(e.target.value)} required />
            <button type="submit">create presigned url</button>
          </form>
        </div>

        <div className="panel">
          <h2>Stream / Metadata / Delete</h2>
          <form className="form-box" onSubmit={(e: FormEvent) => {
            e.preventDefault();
            run(async () => {
              const res = await fetchMediaStreamInfo(Number(mediaId));
              setResult(res.data);
              setMessage('GET /media/stream/:id success');
            });
          }}>
            <label htmlFor="media-id">mediaId</label>
            <input id="media-id" value={mediaId} onChange={(e) => setMediaId(e.target.value)} required />
            <div className="button-row">
              <button type="submit">load stream info</button>
              <button type="button" onClick={() => run(async () => {
                const res = await fetchMediaMetadata(Number(mediaId));
                setResult(res.data);
                setMessage('GET /media/:id/metadata success');
              })}>load metadata</button>
              <button type="button" onClick={() => run(async () => {
                const res = await removeMedia(Number(mediaId));
                setResult(res.data);
                setMessage('DELETE /media/:id success');
              })}>delete media</button>
            </div>
          </form>
        </div>
      </div>

      <div className="panel mt-12">
        <h2>Uploaded Media</h2>
        <ul className="list">
          {uploaded.map((item) => (
            <li key={item.id}>
              #{item.id} / {item.ownerType}:{item.ownerId} / {item.type} / {item.mime} / {item.size}
            </li>
          ))}
        </ul>
      </div>

      {result ? <pre className="code-view mt-12">{JSON.stringify(result, null, 2)}</pre> : null}
    </section>
  );
}
