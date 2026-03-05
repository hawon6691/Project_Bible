import { FormEvent, useState } from 'react';
import {
  addPcBuildPart,
  createCompatibilityRuleAdmin,
  createPcBuild,
  createPcBuildShareLink,
  fetchCompatibilityRulesAdmin,
  fetchMyPcBuilds,
  fetchPcBuildCompatibility,
  fetchPcBuildDetail,
  fetchPopularPcBuilds,
  fetchSharedPcBuild,
  removeCompatibilityRuleAdmin,
  removePcBuild,
  removePcBuildPart,
  updateCompatibilityRuleAdmin,
  updatePcBuild,
} from '@/lib/endpoints';
import type { PcBuildSummaryItem } from '@/lib/types';

export default function PcBuilderApiPage() {
  const [buildId, setBuildId] = useState('');
  const [shareCode, setShareCode] = useState('');
  const [partId, setPartId] = useState('');
  const [ruleId, setRuleId] = useState('');

  const [myBuilds, setMyBuilds] = useState<PcBuildSummaryItem[]>([]);
  const [popularBuilds, setPopularBuilds] = useState<PcBuildSummaryItem[]>([]);
  const [result, setResult] = useState<unknown>(null);

  const [name, setName] = useState('게이밍 PC 2026');
  const [description, setDescription] = useState('고사양 게이밍 목적');
  const [purpose, setPurpose] = useState<'GAMING' | 'OFFICE' | 'DESIGN' | 'DEVELOPMENT' | 'STREAMING'>('GAMING');
  const [budget, setBudget] = useState('3000000');

  const [productId, setProductId] = useState('');
  const [partType, setPartType] = useState<'CPU' | 'MOTHERBOARD' | 'RAM' | 'GPU' | 'SSD' | 'HDD' | 'PSU' | 'CASE' | 'COOLER' | 'MONITOR'>('CPU');
  const [sellerId, setSellerId] = useState('');
  const [quantity, setQuantity] = useState('1');

  const [rulePartType, setRulePartType] = useState<'CPU' | 'MOTHERBOARD' | 'RAM' | 'GPU' | 'SSD' | 'HDD' | 'PSU' | 'CASE' | 'COOLER' | 'MONITOR'>('CPU');
  const [targetPartType, setTargetPartType] = useState('');
  const [ruleTitle, setRuleTitle] = useState('CPU requires motherboard');
  const [ruleDescription, setRuleDescription] = useState('CPU가 있으면 MOTHERBOARD도 필요합니다.');
  const [ruleSeverity, setRuleSeverity] = useState<'LOW' | 'MEDIUM' | 'HIGH'>('MEDIUM');

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
      <h1>PC Builder API Step</h1>
      <p className="sub">step 33 - pc builder API integration</p>
      {message ? <p className="sub">{message}</p> : null}
      {error ? <p className="error">{error}</p> : null}

      <div className="panel-grid">
        <div className="panel">
          <h2>Build APIs</h2>
          <div className="button-row">
            <button type="button" onClick={() => run(async () => {
              const res = await fetchMyPcBuilds();
              setMyBuilds(res.data);
              setMessage('GET /pc-builds success');
            })}>load my builds</button>
            <button type="button" onClick={() => run(async () => {
              const res = await fetchPopularPcBuilds();
              setPopularBuilds(res.data);
              setMessage('GET /pc-builds/popular success');
            })}>load popular builds</button>
          </div>

          <form className="form-box mt-12" onSubmit={(e: FormEvent) => {
            e.preventDefault();
            run(async () => {
              const res = await createPcBuild({
                name,
                description,
                purpose,
                budget: Number(budget),
              });
              setResult(res.data);
              setMessage('POST /pc-builds success');
            });
          }}>
            <label htmlFor="pcb-name">name</label>
            <input id="pcb-name" value={name} onChange={(e) => setName(e.target.value)} required />
            <label htmlFor="pcb-description">description</label>
            <input id="pcb-description" value={description} onChange={(e) => setDescription(e.target.value)} />
            <label htmlFor="pcb-purpose">purpose</label>
            <input id="pcb-purpose" value={purpose} onChange={(e) => setPurpose(e.target.value as any)} required />
            <label htmlFor="pcb-budget">budget</label>
            <input id="pcb-budget" value={budget} onChange={(e) => setBudget(e.target.value)} />
            <button type="submit">create build</button>
          </form>

          <form className="form-box mt-12" onSubmit={(e: FormEvent) => {
            e.preventDefault();
            run(async () => {
              const res = await fetchPcBuildDetail(Number(buildId));
              setResult(res.data);
              setMessage('GET /pc-builds/:id success');
            });
          }}>
            <label htmlFor="pcb-build-id">buildId</label>
            <input id="pcb-build-id" value={buildId} onChange={(e) => setBuildId(e.target.value)} required />
            <div className="button-row">
              <button type="submit">load build detail</button>
              <button type="button" onClick={() => run(async () => {
                const res = await fetchPcBuildCompatibility(Number(buildId));
                setResult(res.data);
                setMessage('GET /pc-builds/:id/compatibility success');
              })}>check compatibility</button>
              <button type="button" onClick={() => run(async () => {
                const res = await createPcBuildShareLink(Number(buildId));
                setResult(res.data);
                setMessage('GET /pc-builds/:id/share success');
              })}>create share link</button>
              <button type="button" onClick={() => run(async () => {
                const res = await removePcBuild(Number(buildId));
                setResult(res.data);
                setMessage('DELETE /pc-builds/:id success');
              })}>delete build</button>
            </div>
          </form>

          <form className="form-box mt-12" onSubmit={(e: FormEvent) => {
            e.preventDefault();
            run(async () => {
              const res = await fetchSharedPcBuild(shareCode);
              setResult(res.data);
              setMessage('GET /pc-builds/shared/:shareCode success');
            });
          }}>
            <label htmlFor="pcb-share-code">shareCode</label>
            <input id="pcb-share-code" value={shareCode} onChange={(e) => setShareCode(e.target.value)} required />
            <button type="submit">load shared build</button>
          </form>
        </div>

        <div className="panel">
          <h2>Part + Rule APIs</h2>
          <form className="form-box" onSubmit={(e: FormEvent) => {
            e.preventDefault();
            run(async () => {
              const res = await addPcBuildPart(Number(buildId), {
                productId: Number(productId),
                partType,
                sellerId: sellerId ? Number(sellerId) : undefined,
                quantity: Number(quantity),
              });
              setResult(res.data);
              setMessage('POST /pc-builds/:id/parts success');
            });
          }}>
            <label htmlFor="pcb-product-id">productId</label>
            <input id="pcb-product-id" value={productId} onChange={(e) => setProductId(e.target.value)} required />
            <label htmlFor="pcb-part-type">partType</label>
            <input id="pcb-part-type" value={partType} onChange={(e) => setPartType(e.target.value as any)} required />
            <label htmlFor="pcb-seller-id">sellerId(optional)</label>
            <input id="pcb-seller-id" value={sellerId} onChange={(e) => setSellerId(e.target.value)} />
            <label htmlFor="pcb-quantity">quantity</label>
            <input id="pcb-quantity" value={quantity} onChange={(e) => setQuantity(e.target.value)} required />
            <div className="button-row">
              <button type="submit">add/update part</button>
              <button type="button" onClick={() => run(async () => {
                const res = await removePcBuildPart(Number(buildId), Number(partId));
                setResult(res.data);
                setMessage('DELETE /pc-builds/:id/parts/:partId success');
              })}>remove part</button>
            </div>
            <label htmlFor="pcb-part-id">partId(for remove)</label>
            <input id="pcb-part-id" value={partId} onChange={(e) => setPartId(e.target.value)} />
          </form>

          <div className="button-row mt-12">
            <button type="button" onClick={() => run(async () => {
              const res = await fetchCompatibilityRulesAdmin();
              setResult(res.data);
              setMessage('GET /admin/compatibility-rules success');
            })}>load rules</button>
          </div>

          <form className="form-box mt-12" onSubmit={(e: FormEvent) => {
            e.preventDefault();
            run(async () => {
              const res = await createCompatibilityRuleAdmin({
                partType: rulePartType,
                targetPartType: targetPartType ? (targetPartType as any) : undefined,
                title: ruleTitle,
                description: ruleDescription,
                severity: ruleSeverity,
                enabled: true,
                metadata: { required: true },
              });
              setResult(res.data);
              setMessage('POST /admin/compatibility-rules success');
            });
          }}>
            <label htmlFor="pcb-rule-part-type">partType</label>
            <input id="pcb-rule-part-type" value={rulePartType} onChange={(e) => setRulePartType(e.target.value as any)} required />
            <label htmlFor="pcb-rule-target-part-type">targetPartType(optional)</label>
            <input id="pcb-rule-target-part-type" value={targetPartType} onChange={(e) => setTargetPartType(e.target.value)} />
            <label htmlFor="pcb-rule-title">title</label>
            <input id="pcb-rule-title" value={ruleTitle} onChange={(e) => setRuleTitle(e.target.value)} required />
            <label htmlFor="pcb-rule-description">description</label>
            <input id="pcb-rule-description" value={ruleDescription} onChange={(e) => setRuleDescription(e.target.value)} required />
            <label htmlFor="pcb-rule-severity">severity</label>
            <input id="pcb-rule-severity" value={ruleSeverity} onChange={(e) => setRuleSeverity(e.target.value as any)} required />
            <button type="submit">create rule</button>
          </form>

          <form className="form-box mt-12" onSubmit={(e: FormEvent) => {
            e.preventDefault();
            run(async () => {
              const res = await updateCompatibilityRuleAdmin(Number(ruleId), {
                title: ruleTitle,
                description: ruleDescription,
                severity: ruleSeverity,
                enabled: true,
              });
              setResult(res.data);
              setMessage('PATCH /admin/compatibility-rules/:id success');
            });
          }}>
            <label htmlFor="pcb-rule-id">ruleId</label>
            <input id="pcb-rule-id" value={ruleId} onChange={(e) => setRuleId(e.target.value)} required />
            <div className="button-row">
              <button type="submit">update rule</button>
              <button type="button" onClick={() => run(async () => {
                const res = await removeCompatibilityRuleAdmin(Number(ruleId));
                setResult(res.data);
                setMessage('DELETE /admin/compatibility-rules/:id success');
              })}>delete rule</button>
            </div>
          </form>
        </div>
      </div>

      <div className="panel-grid mt-12">
        <div className="panel">
          <h2>My Builds</h2>
          <ul className="list">
            {myBuilds.map((item) => (
              <li key={item.id}>#{item.id} / {item.name} / {item.purpose} / {item.totalPrice}</li>
            ))}
          </ul>
        </div>
        <div className="panel">
          <h2>Popular Builds</h2>
          <ul className="list">
            {popularBuilds.map((item) => (
              <li key={item.id}>#{item.id} / {item.name} / views {item.viewCount}</li>
            ))}
          </ul>
        </div>
      </div>

      {result ? <pre className="code-view mt-12">{JSON.stringify(result, null, 2)}</pre> : null}
    </section>
  );
}
