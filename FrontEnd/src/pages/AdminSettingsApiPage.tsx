import { FormEvent, useState } from 'react';
import {
  fetchAllowedExtensionsAdmin,
  fetchReviewPolicyAdmin,
  fetchUploadLimitsAdmin,
  setAllowedExtensionsAdmin,
  updateReviewPolicyAdmin,
  updateUploadLimitsAdmin,
} from '@/lib/endpoints';

export default function AdminSettingsApiPage() {
  const [extensionsText, setExtensionsText] = useState('jpg,jpeg,png,webp,gif,mp4,mp3,pdf');
  const [imageLimit, setImageLimit] = useState('5');
  const [videoLimit, setVideoLimit] = useState('100');
  const [audioLimit, setAudioLimit] = useState('20');
  const [maxImageCount, setMaxImageCount] = useState('10');
  const [pointAmount, setPointAmount] = useState('500');
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

  const parseExtensions = () =>
    extensionsText
      .split(',')
      .map((v) => v.trim().toLowerCase())
      .filter(Boolean);

  return (
    <section>
      <h1>Admin Settings API Step</h1>
      <p className="sub">step 45 - admin settings API integration</p>
      {message ? <p className="sub">{message}</p> : null}
      {error ? <p className="error">{error}</p> : null}

      <div className="panel-grid">
        <div className="panel">
          <h2>Extensions</h2>
          <div className="button-row">
            <button
              type="button"
              onClick={() =>
                run(async () => {
                  const res = await fetchAllowedExtensionsAdmin();
                  setResult(res.data);
                  setMessage('GET /admin/settings/extensions success');
                })
              }
            >
              load extensions
            </button>
          </div>
          <form
            className="form-box mt-12"
            onSubmit={(e: FormEvent) => {
              e.preventDefault();
              run(async () => {
                const res = await setAllowedExtensionsAdmin({ extensions: parseExtensions() });
                setResult(res.data);
                setMessage('POST /admin/settings/extensions success');
              });
            }}
          >
            <label htmlFor="settings-extensions">extensions(csv)</label>
            <input
              id="settings-extensions"
              value={extensionsText}
              onChange={(e) => setExtensionsText(e.target.value)}
              required
            />
            <button type="submit">set extensions</button>
          </form>
        </div>

        <div className="panel">
          <h2>Upload Limits / Review Policy</h2>
          <div className="button-row">
            <button
              type="button"
              onClick={() =>
                run(async () => {
                  const res = await fetchUploadLimitsAdmin();
                  setResult(res.data);
                  setMessage('GET /admin/settings/upload-limits success');
                })
              }
            >
              load upload limits
            </button>
            <button
              type="button"
              onClick={() =>
                run(async () => {
                  const res = await fetchReviewPolicyAdmin();
                  setResult(res.data);
                  setMessage('GET /admin/settings/review-policy success');
                })
              }
            >
              load review policy
            </button>
          </div>

          <form
            className="form-box mt-12"
            onSubmit={(e: FormEvent) => {
              e.preventDefault();
              run(async () => {
                const res = await updateUploadLimitsAdmin({
                  image: Number(imageLimit),
                  video: Number(videoLimit),
                  audio: Number(audioLimit),
                });
                setResult(res.data);
                setMessage('PATCH /admin/settings/upload-limits success');
              });
            }}
          >
            <label htmlFor="settings-image-limit">image(MB)</label>
            <input
              id="settings-image-limit"
              value={imageLimit}
              onChange={(e) => setImageLimit(e.target.value)}
              required
            />
            <label htmlFor="settings-video-limit">video(MB)</label>
            <input
              id="settings-video-limit"
              value={videoLimit}
              onChange={(e) => setVideoLimit(e.target.value)}
              required
            />
            <label htmlFor="settings-audio-limit">audio(MB)</label>
            <input
              id="settings-audio-limit"
              value={audioLimit}
              onChange={(e) => setAudioLimit(e.target.value)}
              required
            />
            <button type="submit">update upload limits</button>
          </form>

          <form
            className="form-box mt-12"
            onSubmit={(e: FormEvent) => {
              e.preventDefault();
              run(async () => {
                const res = await updateReviewPolicyAdmin({
                  maxImageCount: Number(maxImageCount),
                  pointAmount: Number(pointAmount),
                });
                setResult(res.data);
                setMessage('PATCH /admin/settings/review-policy success');
              });
            }}
          >
            <label htmlFor="settings-max-image-count">maxImageCount</label>
            <input
              id="settings-max-image-count"
              value={maxImageCount}
              onChange={(e) => setMaxImageCount(e.target.value)}
              required
            />
            <label htmlFor="settings-point-amount">pointAmount</label>
            <input
              id="settings-point-amount"
              value={pointAmount}
              onChange={(e) => setPointAmount(e.target.value)}
              required
            />
            <button type="submit">update review policy</button>
          </form>
        </div>
      </div>

      {result ? <pre className="code-view mt-12">{JSON.stringify(result, null, 2)}</pre> : null}
    </section>
  );
}
