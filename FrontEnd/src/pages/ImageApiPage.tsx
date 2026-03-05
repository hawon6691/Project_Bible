import { FormEvent, useState } from 'react';
import { fetchImageVariants, removeImageAdmin, uploadImage } from '@/lib/endpoints';
import type { ImageUploadResult, ImageVariantItem } from '@/lib/types';

export default function ImageApiPage() {
  const [file, setFile] = useState<File | null>(null);
  const [category, setCategory] = useState<'product' | 'community' | 'support' | 'seller'>('product');
  const [imageId, setImageId] = useState('');
  const [uploadResult, setUploadResult] = useState<ImageUploadResult | null>(null);
  const [variants, setVariants] = useState<ImageVariantItem[]>([]);
  const [lastResult, setLastResult] = useState<unknown>(null);
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
      <h1>Image API Step</h1>
      <p className="sub">step 31 - image API integration</p>
      {message ? <p className="sub">{message}</p> : null}
      {error ? <p className="error">{error}</p> : null}

      <div className="panel-grid">
        <div className="panel">
          <h2>POST /images/upload</h2>
          <form
            className="form-box"
            onSubmit={(e: FormEvent) => {
              e.preventDefault();
              if (!file) {
                setError('파일을 선택해주세요.');
                return;
              }
              run(async () => {
                const res = await uploadImage(file, category);
                setUploadResult(res.data);
                setMessage('POST /images/upload success');
              });
            }}
          >
            <label htmlFor="image-file">file</label>
            <input
              id="image-file"
              type="file"
              onChange={(e) => setFile(e.target.files?.[0] || null)}
              required
            />

            <label htmlFor="image-category">category</label>
            <select
              id="image-category"
              value={category}
              onChange={(e) => setCategory(e.target.value as 'product' | 'community' | 'support' | 'seller')}
            >
              <option value="product">product</option>
              <option value="community">community</option>
              <option value="support">support</option>
              <option value="seller">seller</option>
            </select>

            <button type="submit">upload image</button>
          </form>

          {uploadResult ? (
            <pre className="code-view mt-12">{JSON.stringify(uploadResult, null, 2)}</pre>
          ) : null}
        </div>

        <div className="panel">
          <h2>GET /images/:id/variants + DELETE /images/:id</h2>
          <form
            className="form-box"
            onSubmit={(e: FormEvent) => {
              e.preventDefault();
              run(async () => {
                const res = await fetchImageVariants(Number(imageId));
                setVariants(res.data);
                setMessage('GET /images/:id/variants success');
              });
            }}
          >
            <label htmlFor="image-id">imageId</label>
            <input
              id="image-id"
              value={imageId}
              onChange={(e) => setImageId(e.target.value)}
              required
            />
            <div className="button-row">
              <button type="submit">load variants</button>
              <button
                type="button"
                onClick={() =>
                  run(async () => {
                    const res = await removeImageAdmin(Number(imageId));
                    setLastResult(res.data);
                    setMessage('DELETE /images/:id success');
                  })
                }
              >
                delete image (admin)
              </button>
            </div>
          </form>

          <ul className="list mt-12">
            {variants.map((item) => (
              <li key={item.id}>
                #{item.id} / {item.type} / {item.width}x{item.height} / {item.format}
              </li>
            ))}
          </ul>

          {lastResult ? <pre className="code-view mt-12">{JSON.stringify(lastResult, null, 2)}</pre> : null}
        </div>
      </div>
    </section>
  );
}
