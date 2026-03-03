import { FormEvent, useState } from 'react';
import {
  compareSpecs,
  compareSpecsNumeric,
  compareSpecsScored,
  createSpecDefinitionAdmin,
  fetchProductGroupedSpecs,
  fetchProductSpecs,
  fetchResolvedSpecDefinitions,
  fetchSimilarProductsBySpec,
  fetchSpecDefinitions,
  removeSpecDefinitionAdmin,
  scoreByCategory,
  setProductSpecsAdmin,
  setSpecScoresAdmin,
  updateSpecDefinitionAdmin,
} from '@/lib/endpoints';

function parseNumberList(csv: string) {
  return csv
    .split(',')
    .map((v) => Number(v.trim()))
    .filter((v) => Number.isFinite(v) && v > 0);
}

export default function SpecApiPage() {
  const [categoryId, setCategoryId] = useState('');
  const [definitions, setDefinitions] = useState<any[]>([]);
  const [resolvedDefinitions, setResolvedDefinitions] = useState<any[]>([]);

  const [createCategoryId, setCreateCategoryId] = useState('');
  const [createName, setCreateName] = useState('');
  const [createType, setCreateType] = useState('SELECT');
  const [createOptionsCsv, setCreateOptionsCsv] = useState('');

  const [updateDefId, setUpdateDefId] = useState('');
  const [updateName, setUpdateName] = useState('');

  const [deleteDefId, setDeleteDefId] = useState('');

  const [productIdForSpecs, setProductIdForSpecs] = useState('');
  const [productSpecs, setProductSpecs] = useState<any[]>([]);
  const [groupedSpecs, setGroupedSpecs] = useState<any[]>([]);

  const [setSpecsProductId, setSetSpecsProductId] = useState('');
  const [setSpecsSpecDefId, setSetSpecsSpecDefId] = useState('');
  const [setSpecsValue, setSetSpecsValue] = useState('');

  const [compareIdsCsv, setCompareIdsCsv] = useState('');
  const [scoreCategoryId, setScoreCategoryId] = useState('');
  const [compareResult, setCompareResult] = useState<unknown>(null);

  const [setScoresDefId, setSetScoresDefId] = useState('');
  const [setScoresValue, setSetScoresValue] = useState('');
  const [setScoresScore, setSetScoresScore] = useState('');

  const [similarProductId, setSimilarProductId] = useState('');
  const [similarResult, setSimilarResult] = useState<any[]>([]);

  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  const safeRun = async (fn: () => Promise<void>) => {
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
      <h1>Spec API Step</h1>
      <p className="sub">spec step 5 - specs API integration</p>
      {message ? <p className="sub">{message}</p> : null}
      {error ? <p className="error">{error}</p> : null}

      <div className="panel-grid">
        <div className="panel">
          <h2>Definitions API</h2>
          <form className="form-box" onSubmit={(e: FormEvent) => {
            e.preventDefault();
            safeRun(async () => {
              const res = await fetchSpecDefinitions(categoryId ? Number(categoryId) : undefined);
              setDefinitions(res.data);
              setMessage('GET /specs/definitions success');
            });
          }}>
            <label htmlFor="specCategoryId">categoryId(optional)</label>
            <input id="specCategoryId" value={categoryId} onChange={(e) => setCategoryId(e.target.value)} />
            <button type="submit">GET /specs/definitions</button>
          </form>

          <form className="form-box mt-12" onSubmit={(e: FormEvent) => {
            e.preventDefault();
            safeRun(async () => {
              const res = await fetchResolvedSpecDefinitions(Number(categoryId));
              setResolvedDefinitions(res.data);
              setMessage('GET /specs/definitions/resolved/:categoryId success');
            });
          }}>
            <button type="submit">GET resolved definitions</button>
          </form>

          <ul className="list mt-12">
            {definitions.map((d) => <li key={d.id}>{d.id} / {d.name} / {d.type}</li>)}
          </ul>

          <ul className="list mt-12">
            {resolvedDefinitions.map((d) => <li key={d.id}>resolved: {d.id} / {d.name}</li>)}
          </ul>
        </div>

        <div className="panel">
          <h2>Definition Admin API</h2>
          <form className="form-box" onSubmit={(e: FormEvent) => {
            e.preventDefault();
            safeRun(async () => {
              await createSpecDefinitionAdmin({
                categoryId: Number(createCategoryId),
                name: createName,
                type: createType,
                ...(createOptionsCsv ? { options: createOptionsCsv.split(',').map((v) => v.trim()).filter(Boolean) } : {}),
              });
              setMessage('POST /specs/definitions success');
            });
          }}>
            <label htmlFor="createCategoryId">categoryId</label>
            <input id="createCategoryId" value={createCategoryId} onChange={(e) => setCreateCategoryId(e.target.value)} required />
            <label htmlFor="createName">name</label>
            <input id="createName" value={createName} onChange={(e) => setCreateName(e.target.value)} required />
            <label htmlFor="createType">type</label>
            <input id="createType" value={createType} onChange={(e) => setCreateType(e.target.value)} required />
            <label htmlFor="createOptionsCsv">options csv(optional)</label>
            <input id="createOptionsCsv" value={createOptionsCsv} onChange={(e) => setCreateOptionsCsv(e.target.value)} />
            <button type="submit">POST /specs/definitions</button>
          </form>

          <form className="form-box mt-12" onSubmit={(e: FormEvent) => {
            e.preventDefault();
            safeRun(async () => {
              await updateSpecDefinitionAdmin(Number(updateDefId), { name: updateName || undefined });
              setMessage('PATCH /specs/definitions/:id success');
            });
          }}>
            <label htmlFor="updateDefId">definitionId</label>
            <input id="updateDefId" value={updateDefId} onChange={(e) => setUpdateDefId(e.target.value)} required />
            <label htmlFor="updateName">name(optional)</label>
            <input id="updateName" value={updateName} onChange={(e) => setUpdateName(e.target.value)} />
            <button type="submit">PATCH /specs/definitions/:id</button>
          </form>

          <form className="form-box mt-12" onSubmit={(e: FormEvent) => {
            e.preventDefault();
            safeRun(async () => {
              await removeSpecDefinitionAdmin(Number(deleteDefId));
              setMessage('DELETE /specs/definitions/:id success');
            });
          }}>
            <label htmlFor="deleteDefId">definitionId</label>
            <input id="deleteDefId" value={deleteDefId} onChange={(e) => setDeleteDefId(e.target.value)} required />
            <button type="submit">DELETE /specs/definitions/:id</button>
          </form>
        </div>
      </div>

      <div className="panel-grid mt-12">
        <div className="panel">
          <h2>Product Spec API</h2>
          <form className="form-box" onSubmit={(e: FormEvent) => {
            e.preventDefault();
            safeRun(async () => {
              const res = await fetchProductSpecs(Number(productIdForSpecs));
              setProductSpecs(res.data as any[]);
              setMessage('GET /products/:id/specs success');
            });
          }}>
            <label htmlFor="productIdForSpecs">productId</label>
            <input id="productIdForSpecs" value={productIdForSpecs} onChange={(e) => setProductIdForSpecs(e.target.value)} required />
            <button type="submit">GET /products/:id/specs</button>
          </form>

          <form className="form-box mt-12" onSubmit={(e: FormEvent) => {
            e.preventDefault();
            safeRun(async () => {
              const res = await fetchProductGroupedSpecs(Number(productIdForSpecs));
              setGroupedSpecs(res.data as any[]);
              setMessage('GET /products/:id/specs/grouped success');
            });
          }}>
            <button type="submit">GET /products/:id/specs/grouped</button>
          </form>

          <ul className="list mt-12">
            {productSpecs.map((s, idx) => <li key={`ps-${idx}`}>{s.name}: {s.value}</li>)}
          </ul>
          <ul className="list mt-12">
            {groupedSpecs.map((g, idx) => <li key={`gs-${idx}`}>{g.groupName}: {g.specs?.length || 0}</li>)}
          </ul>

          <form className="form-box mt-12" onSubmit={(e: FormEvent) => {
            e.preventDefault();
            safeRun(async () => {
              await setProductSpecsAdmin(Number(setSpecsProductId), {
                specs: [{
                  specDefinitionId: Number(setSpecsSpecDefId),
                  value: setSpecsValue,
                }],
              });
              setMessage('PUT /products/:id/specs success');
            });
          }}>
            <label htmlFor="setSpecsProductId">productId</label>
            <input id="setSpecsProductId" value={setSpecsProductId} onChange={(e) => setSetSpecsProductId(e.target.value)} required />
            <label htmlFor="setSpecsSpecDefId">specDefinitionId</label>
            <input id="setSpecsSpecDefId" value={setSpecsSpecDefId} onChange={(e) => setSetSpecsSpecDefId(e.target.value)} required />
            <label htmlFor="setSpecsValue">value</label>
            <input id="setSpecsValue" value={setSpecsValue} onChange={(e) => setSetSpecsValue(e.target.value)} required />
            <button type="submit">PUT /products/:id/specs</button>
          </form>
        </div>

        <div className="panel">
          <h2>Compare/Score API</h2>
          <form className="form-box" onSubmit={(e: FormEvent) => {
            e.preventDefault();
            safeRun(async () => {
              const ids = parseNumberList(compareIdsCsv);
              const res = await compareSpecs({ productIds: ids });
              setCompareResult(res.data);
              setMessage('POST /specs/compare success');
            });
          }}>
            <label htmlFor="compareIdsCsv">productIds csv (e.g. 1,2)</label>
            <input id="compareIdsCsv" value={compareIdsCsv} onChange={(e) => setCompareIdsCsv(e.target.value)} required />
            <button type="submit">POST /specs/compare</button>
          </form>

          <div className="button-row mt-12">
            <button type="button" onClick={() => safeRun(async () => {
              const ids = parseNumberList(compareIdsCsv);
              const res = await compareSpecsNumeric({ productIds: ids });
              setCompareResult(res.data);
              setMessage('POST /specs/compare/numeric success');
            })}>compare numeric</button>

            <button type="button" onClick={() => safeRun(async () => {
              const ids = parseNumberList(compareIdsCsv);
              const res = await compareSpecsScored({ productIds: ids });
              setCompareResult(res.data);
              setMessage('POST /specs/compare/scored success');
            })}>compare scored</button>
          </div>

          <form className="form-box mt-12" onSubmit={(e: FormEvent) => {
            e.preventDefault();
            safeRun(async () => {
              const res = await scoreByCategory({ categoryId: Number(scoreCategoryId), productIds: parseNumberList(compareIdsCsv) });
              setCompareResult(res.data);
              setMessage('POST /specs/score success');
            });
          }}>
            <label htmlFor="scoreCategoryId">categoryId</label>
            <input id="scoreCategoryId" value={scoreCategoryId} onChange={(e) => setScoreCategoryId(e.target.value)} required />
            <button type="submit">POST /specs/score</button>
          </form>

          <form className="form-box mt-12" onSubmit={(e: FormEvent) => {
            e.preventDefault();
            safeRun(async () => {
              await setSpecScoresAdmin(Number(setScoresDefId), {
                scores: [{ value: setScoresValue, score: Number(setScoresScore) }],
              });
              setMessage('PUT /specs/scores/:specDefId success');
            });
          }}>
            <label htmlFor="setScoresDefId">specDefId</label>
            <input id="setScoresDefId" value={setScoresDefId} onChange={(e) => setSetScoresDefId(e.target.value)} required />
            <label htmlFor="setScoresValue">value</label>
            <input id="setScoresValue" value={setScoresValue} onChange={(e) => setSetScoresValue(e.target.value)} required />
            <label htmlFor="setScoresScore">score</label>
            <input id="setScoresScore" value={setScoresScore} onChange={(e) => setSetScoresScore(e.target.value)} required />
            <button type="submit">PUT /specs/scores/:specDefId</button>
          </form>

          <form className="form-box mt-12" onSubmit={(e: FormEvent) => {
            e.preventDefault();
            safeRun(async () => {
              const res = await fetchSimilarProductsBySpec(Number(similarProductId));
              setSimilarResult(res.data as any[]);
              setMessage('GET /products/:id/similar-spec-products success');
            });
          }}>
            <label htmlFor="similarProductId">productId</label>
            <input id="similarProductId" value={similarProductId} onChange={(e) => setSimilarProductId(e.target.value)} required />
            <button type="submit">GET similar products</button>
          </form>

          {compareResult ? (
            <pre className="code-view mt-12">{JSON.stringify(compareResult, null, 2)}</pre>
          ) : null}
          {similarResult.length > 0 ? (
            <ul className="list mt-12">
              {similarResult.map((item: any, idx) => <li key={`sim-${idx}`}>{item.id} / {item.name} / {item.similarityScore}</li>)}
            </ul>
          ) : null}
        </div>
      </div>
    </section>
  );
}
