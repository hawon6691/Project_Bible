import { FormEvent, useState } from 'react';
import {
  acceptFriendRequest,
  blockUser,
  fetchFriendFeed,
  fetchFriends,
  fetchReceivedFriendRequests,
  fetchSentFriendRequests,
  rejectFriendRequest,
  removeFriend,
  requestFriend,
  unblockUser,
} from '@/lib/endpoints';
import type { FriendFeedItem, FriendListItem } from '@/lib/types';

export default function FriendApiPage() {
  const [userId, setUserId] = useState('');
  const [friendshipId, setFriendshipId] = useState('');
  const [friends, setFriends] = useState<FriendListItem[]>([]);
  const [received, setReceived] = useState<FriendListItem[]>([]);
  const [sent, setSent] = useState<FriendListItem[]>([]);
  const [feed, setFeed] = useState<FriendFeedItem[]>([]);
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
      <h1>Friend API Step</h1>
      <p className="sub">step 34 - friend API integration</p>
      {message ? <p className="sub">{message}</p> : null}
      {error ? <p className="error">{error}</p> : null}

      <div className="panel-grid">
        <div className="panel">
          <h2>Request / Accept / Reject</h2>
          <form className="form-box" onSubmit={(e: FormEvent) => {
            e.preventDefault();
            run(async () => {
              const res = await requestFriend(Number(userId));
              setLastResult(res.data);
              setMessage('POST /friends/request/:userId success');
            });
          }}>
            <label htmlFor="friend-user-id">target userId</label>
            <input id="friend-user-id" value={userId} onChange={(e) => setUserId(e.target.value)} required />
            <button type="submit">request friend</button>
          </form>

          <form className="form-box mt-12" onSubmit={(e: FormEvent) => {
            e.preventDefault();
            run(async () => {
              const res = await acceptFriendRequest(Number(friendshipId));
              setLastResult(res.data);
              setMessage('PATCH /friends/request/:friendshipId/accept success');
            });
          }}>
            <label htmlFor="friendship-id">friendshipId</label>
            <input id="friendship-id" value={friendshipId} onChange={(e) => setFriendshipId(e.target.value)} required />
            <div className="button-row">
              <button type="submit">accept request</button>
              <button type="button" onClick={() => run(async () => {
                const res = await rejectFriendRequest(Number(friendshipId));
                setLastResult(res.data);
                setMessage('PATCH /friends/request/:friendshipId/reject success');
              })}>reject request</button>
            </div>
          </form>
        </div>

        <div className="panel">
          <h2>List / Feed / Block</h2>
          <div className="button-row">
            <button type="button" onClick={() => run(async () => {
              const res = await fetchFriends();
              setFriends(res.data);
              setMessage('GET /friends success');
            })}>load friends</button>
            <button type="button" onClick={() => run(async () => {
              const res = await fetchReceivedFriendRequests();
              setReceived(res.data);
              setMessage('GET /friends/requests/received success');
            })}>load received</button>
            <button type="button" onClick={() => run(async () => {
              const res = await fetchSentFriendRequests();
              setSent(res.data);
              setMessage('GET /friends/requests/sent success');
            })}>load sent</button>
            <button type="button" onClick={() => run(async () => {
              const res = await fetchFriendFeed();
              setFeed(res.data);
              setMessage('GET /friends/feed success');
            })}>load feed</button>
          </div>

          <form className="form-box mt-12" onSubmit={(e: FormEvent) => {
            e.preventDefault();
            run(async () => {
              const res = await blockUser(Number(userId));
              setLastResult(res.data);
              setMessage('POST /friends/block/:userId success');
            });
          }}>
            <label htmlFor="friend-user-id-2">target userId</label>
            <input id="friend-user-id-2" value={userId} onChange={(e) => setUserId(e.target.value)} required />
            <div className="button-row">
              <button type="submit">block user</button>
              <button type="button" onClick={() => run(async () => {
                const res = await unblockUser(Number(userId));
                setLastResult(res.data);
                setMessage('DELETE /friends/block/:userId success');
              })}>unblock user</button>
              <button type="button" onClick={() => run(async () => {
                const res = await removeFriend(Number(userId));
                setLastResult(res.data);
                setMessage('DELETE /friends/:userId success');
              })}>remove friend</button>
            </div>
          </form>
        </div>
      </div>

      <div className="panel-grid mt-12">
        <div className="panel">
          <h2>Friends</h2>
          <ul className="list">
            {friends.map((item) => (
              <li key={item.friendshipId}>#{item.friendshipId} / user {item.userId} / {item.nickname ?? '-'}</li>
            ))}
          </ul>
        </div>
        <div className="panel">
          <h2>Received Requests</h2>
          <ul className="list">
            {received.map((item) => (
              <li key={item.friendshipId}>#{item.friendshipId} / from {item.userId} / {item.nickname ?? '-'}</li>
            ))}
          </ul>
        </div>
      </div>

      <div className="panel-grid mt-12">
        <div className="panel">
          <h2>Sent Requests</h2>
          <ul className="list">
            {sent.map((item) => (
              <li key={item.friendshipId}>#{item.friendshipId} / to {item.userId} / {item.nickname ?? '-'}</li>
            ))}
          </ul>
        </div>
        <div className="panel">
          <h2>Feed</h2>
          <ul className="list">
            {feed.map((item) => (
              <li key={item.id}>#{item.id} / user {item.userId} / {item.type} / {item.message}</li>
            ))}
          </ul>
        </div>
      </div>

      {lastResult ? <pre className="code-view mt-12">{JSON.stringify(lastResult, null, 2)}</pre> : null}
    </section>
  );
}
