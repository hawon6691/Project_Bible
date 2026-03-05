import { FormEvent, useState } from 'react';
import {
  createBadgeAdmin,
  fetchBadges,
  fetchMyBadges,
  fetchUserBadges,
  grantBadgeAdmin,
  removeBadgeAdmin,
  revokeBadgeAdmin,
  updateBadgeAdmin,
} from '@/lib/endpoints';
import type { BadgeItem, UserBadgeItem } from '@/lib/types';

export default function BadgeApiPage() {
  const [badges, setBadges] = useState<BadgeItem[]>([]);
  const [myBadges, setMyBadges] = useState<UserBadgeItem[]>([]);
  const [userBadges, setUserBadges] = useState<UserBadgeItem[]>([]);
  const [userId, setUserId] = useState('');

  const [badgeId, setBadgeId] = useState('');
  const [name, setName] = useState('리뷰 마스터');
  const [description, setDescription] = useState('리뷰 50개 달성');
  const [iconUrl, setIconUrl] = useState('https://example.com/badge/review-master.png');
  const [type, setType] = useState<'AUTO' | 'MANUAL'>('AUTO');
  const [rarity, setRarity] = useState<'COMMON' | 'UNCOMMON' | 'RARE' | 'EPIC' | 'LEGENDARY'>('RARE');
  const [metric, setMetric] = useState<'review_count' | 'post_count' | 'order_count' | 'point_total' | 'login_streak'>('review_count');
  const [threshold, setThreshold] = useState('50');
  const [grantUserId, setGrantUserId] = useState('');
  const [grantReason, setGrantReason] = useState('');
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

  const condition =
    type === 'AUTO'
      ? { metric, threshold: Number(threshold) }
      : undefined;

  return (
    <section>
      <h1>Badge API Step</h1>
      <p className="sub">step 32 - badge API integration</p>
      {message ? <p className="sub">{message}</p> : null}
      {error ? <p className="error">{error}</p> : null}

      <div className="panel-grid">
        <div className="panel">
          <h2>Badge 조회 APIs</h2>
          <div className="button-row">
            <button
              type="button"
              onClick={() =>
                run(async () => {
                  const res = await fetchBadges();
                  setBadges(res.data);
                  setMessage('GET /badges success');
                })
              }
            >
              load badges
            </button>
            <button
              type="button"
              onClick={() =>
                run(async () => {
                  const res = await fetchMyBadges();
                  setMyBadges(res.data);
                  setMessage('GET /badges/me success');
                })
              }
            >
              load my badges
            </button>
          </div>

          <form
            className="form-box mt-12"
            onSubmit={(e: FormEvent) => {
              e.preventDefault();
              run(async () => {
                const res = await fetchUserBadges(Number(userId));
                setUserBadges(res.data);
                setMessage('GET /users/:id/badges success');
              });
            }}
          >
            <label htmlFor="badge-user-id">userId</label>
            <input
              id="badge-user-id"
              value={userId}
              onChange={(e) => setUserId(e.target.value)}
              required
            />
            <button type="submit">load user badges</button>
          </form>

          <h3 className="mt-12">All Badges</h3>
          <ul className="list">
            {badges.map((item) => (
              <li key={item.id}>
                #{item.id} / {item.name} / {item.type} / {item.rarity} / holders {item.holderCount}
              </li>
            ))}
          </ul>
        </div>

        <div className="panel">
          <h2>Admin Badge APIs</h2>
          <form
            className="form-box"
            onSubmit={(e: FormEvent) => {
              e.preventDefault();
              run(async () => {
                const res = await createBadgeAdmin({
                  name,
                  description,
                  iconUrl,
                  type,
                  condition,
                  rarity,
                });
                setLastResult(res.data);
                setMessage('POST /admin/badges success');
              });
            }}
          >
            <label htmlFor="badge-name">name</label>
            <input id="badge-name" value={name} onChange={(e) => setName(e.target.value)} required />
            <label htmlFor="badge-description">description</label>
            <input
              id="badge-description"
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              required
            />
            <label htmlFor="badge-icon-url">iconUrl</label>
            <input
              id="badge-icon-url"
              value={iconUrl}
              onChange={(e) => setIconUrl(e.target.value)}
              required
            />
            <label htmlFor="badge-type">type</label>
            <input id="badge-type" value={type} onChange={(e) => setType(e.target.value as 'AUTO' | 'MANUAL')} />
            <label htmlFor="badge-rarity">rarity</label>
            <input
              id="badge-rarity"
              value={rarity}
              onChange={(e) => setRarity(e.target.value as 'COMMON' | 'UNCOMMON' | 'RARE' | 'EPIC' | 'LEGENDARY')}
            />
            <label htmlFor="badge-metric">metric (AUTO only)</label>
            <input
              id="badge-metric"
              value={metric}
              onChange={(e) =>
                setMetric(e.target.value as 'review_count' | 'post_count' | 'order_count' | 'point_total' | 'login_streak')
              }
            />
            <label htmlFor="badge-threshold">threshold (AUTO only)</label>
            <input
              id="badge-threshold"
              value={threshold}
              onChange={(e) => setThreshold(e.target.value)}
            />
            <button type="submit">create badge</button>
          </form>

          <form
            className="form-box mt-12"
            onSubmit={(e: FormEvent) => {
              e.preventDefault();
              run(async () => {
                const res = await updateBadgeAdmin(Number(badgeId), {
                  name,
                  description,
                  iconUrl,
                  type,
                  condition,
                  rarity,
                });
                setLastResult(res.data);
                setMessage('PATCH /admin/badges/:id success');
              });
            }}
          >
            <label htmlFor="badge-id">badgeId</label>
            <input
              id="badge-id"
              value={badgeId}
              onChange={(e) => setBadgeId(e.target.value)}
              required
            />
            <div className="button-row">
              <button type="submit">update badge</button>
              <button
                type="button"
                onClick={() =>
                  run(async () => {
                    const res = await removeBadgeAdmin(Number(badgeId));
                    setLastResult(res.data);
                    setMessage('DELETE /admin/badges/:id success');
                  })
                }
              >
                delete badge
              </button>
            </div>
          </form>

          <form
            className="form-box mt-12"
            onSubmit={(e: FormEvent) => {
              e.preventDefault();
              run(async () => {
                const res = await grantBadgeAdmin(Number(badgeId), {
                  userId: Number(grantUserId),
                  reason: grantReason || undefined,
                });
                setLastResult(res.data);
                setMessage('POST /admin/badges/:id/grant success');
              });
            }}
          >
            <label htmlFor="grant-user-id">grant userId</label>
            <input
              id="grant-user-id"
              value={grantUserId}
              onChange={(e) => setGrantUserId(e.target.value)}
              required
            />
            <label htmlFor="grant-reason">reason (optional)</label>
            <input
              id="grant-reason"
              value={grantReason}
              onChange={(e) => setGrantReason(e.target.value)}
            />
            <div className="button-row">
              <button type="submit">grant badge</button>
              <button
                type="button"
                onClick={() =>
                  run(async () => {
                    const res = await revokeBadgeAdmin(Number(badgeId), Number(grantUserId));
                    setLastResult(res.data);
                    setMessage('DELETE /admin/badges/:id/revoke/:userId success');
                  })
                }
              >
                revoke badge
              </button>
            </div>
          </form>

          <h3 className="mt-12">My Badges</h3>
          <ul className="list">
            {myBadges.map((item) => (
              <li key={item.id}>
                #{item.id} / badge {item.badge?.name ?? item.badgeId} / grantedAt {item.grantedAt}
              </li>
            ))}
          </ul>

          <h3 className="mt-12">User Badges</h3>
          <ul className="list">
            {userBadges.map((item) => (
              <li key={item.id}>
                #{item.id} / user {item.userId} / badge {item.badge?.name ?? item.badgeId}
              </li>
            ))}
          </ul>

          {lastResult ? <pre className="code-view mt-12">{JSON.stringify(lastResult, null, 2)}</pre> : null}
        </div>
      </div>
    </section>
  );
}
