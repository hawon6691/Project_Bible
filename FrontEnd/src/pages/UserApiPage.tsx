import { FormEvent, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  deleteMe,
  deleteMyProfileImage,
  fetchMe,
  fetchPublicProfile,
  fetchUsersAdmin,
  updateMe,
  updateMyProfile,
  updateUserStatusAdmin,
} from '@/lib/endpoints';
import { clearAuth, getAccessToken } from '@/lib/auth';
import type { UserProfile } from '@/lib/types';

interface PublicProfile {
  id: number;
  nickname: string;
  bio: string | null;
  profileImageUrl: string | null;
  createdAt: string;
}

export default function UserApiPage() {
  const navigate = useNavigate();
  const [me, setMe] = useState<UserProfile | null>(null);
  const [publicProfile, setPublicProfile] = useState<PublicProfile | null>(null);
  const [adminUsers, setAdminUsers] = useState<UserProfile[]>([]);
  const [publicUserId, setPublicUserId] = useState('1');
  const [name, setName] = useState('');
  const [phone, setPhone] = useState('');
  const [password, setPassword] = useState('');
  const [nickname, setNickname] = useState('');
  const [bio, setBio] = useState('');
  const [adminTargetId, setAdminTargetId] = useState('');
  const [adminStatus, setAdminStatus] = useState<'ACTIVE' | 'INACTIVE' | 'BLOCKED'>('ACTIVE');
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);

  const isLoggedIn = Boolean(getAccessToken());

  const loadMe = async () => {
    if (!isLoggedIn) {
      setError('login required');
      setLoading(false);
      return;
    }

    setLoading(true);
    setError('');
    try {
      const res = await fetchMe();
      setMe(res.data);
      setName(res.data.name || '');
      setPhone(res.data.phone || '');
      setNickname(res.data.nickname || '');
      setBio(res.data.bio || '');
    } catch (err) {
      setError(err instanceof Error ? err.message : 'load failed');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadMe();
  }, []);

  return (
    <section>
      <h1>User API Step</h1>
      <p className="sub">users module APIs from specification order</p>
      {loading ? <p>loading...</p> : null}
      {error ? <p className="error">{error}</p> : null}
      {message ? <p className="sub">{message}</p> : null}

      <div className="panel-grid">
        <div className="panel">
          <h2>USER-01/02/03 (me)</h2>
          {me ? (
            <>
              <p>id: {me.id}</p>
              <p>email: {me.email}</p>
              <p>role: {me.role}</p>
              <p>status: {me.status}</p>
              <p>point: {me.point}</p>
            </>
          ) : null}

          <form
            className="form-box mt-12"
            onSubmit={async (e: FormEvent) => {
              e.preventDefault();
              setMessage('');
              try {
                await updateMe({ name, phone, ...(password ? { password } : {}) });
                setPassword('');
                setMessage('update me success');
                await loadMe();
              } catch (err) {
                setMessage(err instanceof Error ? err.message : 'update failed');
              }
            }}
          >
            <label htmlFor="meName">name</label>
            <input id="meName" value={name} onChange={(e) => setName(e.target.value)} required />
            <label htmlFor="mePhone">phone</label>
            <input id="mePhone" value={phone} onChange={(e) => setPhone(e.target.value)} required />
            <label htmlFor="mePassword">password(optional)</label>
            <input id="mePassword" type="password" value={password} onChange={(e) => setPassword(e.target.value)} />
            <button type="submit">PATCH/PUT users/me API</button>
          </form>

          <button
            type="button"
            className="mt-12"
            onClick={async () => {
              const confirmDelete = window.confirm('정말 회원 탈퇴를 실행할까요?');
              if (!confirmDelete) return;

              try {
                const res = await deleteMe();
                setMessage(res.data.message || 'account deleted');
                clearAuth();
                navigate('/');
              } catch (err) {
                setMessage(err instanceof Error ? err.message : 'delete failed');
              }
            }}
          >
            DELETE users/me API
          </button>
        </div>

        <div className="panel">
          <h2>USER-06/07/09 (profile)</h2>
          <form
            className="form-box"
            onSubmit={async (e: FormEvent) => {
              e.preventDefault();
              try {
                await updateMyProfile({ nickname, bio });
                setMessage('profile updated');
                await loadMe();
              } catch (err) {
                setMessage(err instanceof Error ? err.message : 'profile update failed');
              }
            }}
          >
            <label htmlFor="nickname">nickname</label>
            <input id="nickname" value={nickname} onChange={(e) => setNickname(e.target.value)} />
            <label htmlFor="bio">bio</label>
            <input id="bio" value={bio} onChange={(e) => setBio(e.target.value)} />
            <button type="submit">PATCH users/me/profile API</button>
          </form>

          <button
            type="button"
            className="mt-12"
            onClick={async () => {
              try {
                await deleteMyProfileImage();
                setMessage('profile image reset');
                await loadMe();
              } catch (err) {
                setMessage(err instanceof Error ? err.message : 'delete image failed');
              }
            }}
          >
            DELETE users/me/profile-image API
          </button>

          <form
            className="form-box mt-12"
            onSubmit={async (e: FormEvent) => {
              e.preventDefault();
              try {
                const res = await fetchPublicProfile(Number(publicUserId));
                setPublicProfile(res.data);
                setMessage('public profile loaded');
              } catch (err) {
                setMessage(err instanceof Error ? err.message : 'public profile failed');
              }
            }}
          >
            <label htmlFor="publicUserId">public user id</label>
            <input id="publicUserId" value={publicUserId} onChange={(e) => setPublicUserId(e.target.value)} />
            <button type="submit">GET users/profile/:id API</button>
          </form>

          {publicProfile ? (
            <p className="sub mt-12">public: {publicProfile.nickname} / {publicProfile.bio || '-'}</p>
          ) : null}
        </div>
      </div>

      {me?.role === 'ADMIN' ? (
        <div className="panel mt-12">
          <h2>USER-04/05 (admin)</h2>
          <button
            type="button"
            onClick={async () => {
              try {
                const res = await fetchUsersAdmin({ page: 1, limit: 10 });
                setAdminUsers(res.data);
                setMessage('admin users loaded');
              } catch (err) {
                setMessage(err instanceof Error ? err.message : 'admin list failed');
              }
            }}
          >
            GET users API
          </button>

          <ul className="list mt-12">
            {adminUsers.map((user) => (
              <li key={user.id}>{user.id} / {user.email} / {user.status}</li>
            ))}
          </ul>

          <form
            className="form-box"
            onSubmit={async (e: FormEvent) => {
              e.preventDefault();
              try {
                await updateUserStatusAdmin(Number(adminTargetId), adminStatus);
                setMessage('user status updated');
              } catch (err) {
                setMessage(err instanceof Error ? err.message : 'status update failed');
              }
            }}
          >
            <label htmlFor="adminTargetId">target user id</label>
            <input id="adminTargetId" value={adminTargetId} onChange={(e) => setAdminTargetId(e.target.value)} required />

            <label htmlFor="adminStatus">status</label>
            <select id="adminStatus" value={adminStatus} onChange={(e) => setAdminStatus(e.target.value as 'ACTIVE' | 'INACTIVE' | 'BLOCKED')}>
              <option value="ACTIVE">ACTIVE</option>
              <option value="INACTIVE">INACTIVE</option>
              <option value="BLOCKED">BLOCKED</option>
            </select>

            <button type="submit">PATCH users/:id/status API</button>
          </form>
        </div>
      ) : null}
    </section>
  );
}
