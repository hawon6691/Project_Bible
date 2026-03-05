import { FormEvent, useState } from 'react';
import {
  createChatRoom,
  fetchChatMessages,
  fetchChatRooms,
  joinChatRoom,
  sendChatMessage,
} from '@/lib/endpoints';

export default function ChatApiPage() {
  const [rooms, setRooms] = useState<any[]>([]);
  const [messages, setMessages] = useState<any[]>([]);

  const [createRoomName, setCreateRoomName] = useState('');
  const [createRoomPrivate, setCreateRoomPrivate] = useState(true);

  const [joinRoomId, setJoinRoomId] = useState('');
  const [messageRoomId, setMessageRoomId] = useState('');
  const [messageText, setMessageText] = useState('');

  const [roomPage, setRoomPage] = useState('1');
  const [roomLimit, setRoomLimit] = useState('20');
  const [msgPage, setMsgPage] = useState('1');
  const [msgLimit, setMsgLimit] = useState('20');

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
      <h1>Chat API Step</h1>
      <p className="sub">spec step 20 - chat API integration</p>
      {message ? <p className="sub">{message}</p> : null}
      {error ? <p className="error">{error}</p> : null}

      <div className="panel-grid">
        <div className="panel">
          <h2>GET /chat/rooms</h2>
          <form className="form-box" onSubmit={(e: FormEvent) => {
            e.preventDefault();
            run(async () => {
              const res = await fetchChatRooms({ page: Number(roomPage), limit: Number(roomLimit) });
              const data = res.data as any;
              setRooms(Array.isArray(data) ? data : (data?.items ?? []));
              setMessage('GET /chat/rooms success');
            });
          }}>
            <label htmlFor="roomPage">page</label>
            <input id="roomPage" value={roomPage} onChange={(e) => setRoomPage(e.target.value)} required />
            <label htmlFor="roomLimit">limit</label>
            <input id="roomLimit" value={roomLimit} onChange={(e) => setRoomLimit(e.target.value)} required />
            <button type="submit">load rooms</button>
          </form>

          <ul className="list mt-12">
            {rooms.map((room) => (
              <li key={room.id}>
                {room.id} / {room.name} / private {String(room.isPrivate)}
              </li>
            ))}
          </ul>
        </div>

        <div className="panel">
          <h2>POST /chat/rooms</h2>
          <form className="form-box" onSubmit={(e: FormEvent) => {
            e.preventDefault();
            run(async () => {
              await createChatRoom({ name: createRoomName, isPrivate: createRoomPrivate });
              setMessage('POST /chat/rooms success');
            });
          }}>
            <label htmlFor="createRoomName">name</label>
            <input id="createRoomName" value={createRoomName} onChange={(e) => setCreateRoomName(e.target.value)} required />
            <label htmlFor="createRoomPrivate">isPrivate</label>
            <input id="createRoomPrivate" type="checkbox" checked={createRoomPrivate} onChange={(e) => setCreateRoomPrivate(e.target.checked)} />
            <button type="submit">create room</button>
          </form>

          <form className="form-box mt-12" onSubmit={(e: FormEvent) => {
            e.preventDefault();
            run(async () => {
              await joinChatRoom(Number(joinRoomId));
              setMessage('POST /chat/rooms/:id/join success');
            });
          }}>
            <label htmlFor="joinRoomId">roomId</label>
            <input id="joinRoomId" value={joinRoomId} onChange={(e) => setJoinRoomId(e.target.value)} required />
            <button type="submit">join room</button>
          </form>
        </div>
      </div>

      <div className="panel-grid mt-12">
        <div className="panel">
          <h2>GET /chat/rooms/:id/messages</h2>
          <form className="form-box" onSubmit={(e: FormEvent) => {
            e.preventDefault();
            run(async () => {
              const res = await fetchChatMessages(Number(messageRoomId), { page: Number(msgPage), limit: Number(msgLimit) });
              const data = res.data as any;
              setMessages(Array.isArray(data) ? data : (data?.items ?? []));
              setMessage('GET /chat/rooms/:id/messages success');
            });
          }}>
            <label htmlFor="messageRoomId">roomId</label>
            <input id="messageRoomId" value={messageRoomId} onChange={(e) => setMessageRoomId(e.target.value)} required />
            <label htmlFor="msgPage">page</label>
            <input id="msgPage" value={msgPage} onChange={(e) => setMsgPage(e.target.value)} required />
            <label htmlFor="msgLimit">limit</label>
            <input id="msgLimit" value={msgLimit} onChange={(e) => setMsgLimit(e.target.value)} required />
            <button type="submit">load messages</button>
          </form>

          <ul className="list mt-12">
            {messages.map((msg) => (
              <li key={msg.id}>
                {msg.id} / sender {msg.senderId} / {msg.message}
              </li>
            ))}
          </ul>
        </div>

        <div className="panel">
          <h2>POST /chat/rooms/:id/messages</h2>
          <form className="form-box" onSubmit={(e: FormEvent) => {
            e.preventDefault();
            run(async () => {
              await sendChatMessage(Number(messageRoomId), { message: messageText });
              setMessage('POST /chat/rooms/:id/messages success');
            });
          }}>
            <label htmlFor="messageText">message</label>
            <input id="messageText" value={messageText} onChange={(e) => setMessageText(e.target.value)} required />
            <button type="submit">send message</button>
          </form>
        </div>
      </div>
    </section>
  );
}
