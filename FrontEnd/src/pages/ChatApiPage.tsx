import { FormEvent, useEffect, useMemo, useRef, useState } from 'react';
import { io, type Socket } from 'socket.io-client';
import {
  createChatRoom,
  fetchChatMessages,
  fetchChatRooms,
  joinChatRoom,
  sendChatMessage,
} from '@/lib/endpoints';
import { getAccessToken } from '@/lib/auth';
import { API_BASE_URL } from '@/lib/config';

type ChatRoomItem = {
  id: number;
  name: string;
  isPrivate: boolean;
};

type ChatMessageItem = {
  id: number;
  senderId: number;
  message: string;
  createdAt?: string;
};

function resolveSocketUrl() {
  const base = API_BASE_URL.replace(/\/api\/v\d+\/?$/, '');
  return `${base}/chat`;
}

export default function ChatApiPage() {
  const [rooms, setRooms] = useState<ChatRoomItem[]>([]);
  const [messages, setMessages] = useState<ChatMessageItem[]>([]);
  const [socketMessages, setSocketMessages] = useState<ChatMessageItem[]>([]);

  const [createRoomName, setCreateRoomName] = useState('');
  const [createRoomPrivate, setCreateRoomPrivate] = useState(true);

  const [joinRoomId, setJoinRoomId] = useState('');
  const [messageRoomId, setMessageRoomId] = useState('');
  const [messageText, setMessageText] = useState('');

  const [roomPage, setRoomPage] = useState('1');
  const [roomLimit, setRoomLimit] = useState('20');
  const [msgPage, setMsgPage] = useState('1');
  const [msgLimit, setMsgLimit] = useState('20');

  const [socketRoomId, setSocketRoomId] = useState('');
  const [socketMessageText, setSocketMessageText] = useState('');
  const [socketStatus, setSocketStatus] = useState<'idle' | 'connecting' | 'connected' | 'disconnected'>('idle');

  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  const socketRef = useRef<Socket | null>(null);
  const socketUrl = useMemo(() => resolveSocketUrl(), []);

  const run = async (fn: () => Promise<void>) => {
    setMessage('');
    setError('');
    try {
      await fn();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'request failed');
    }
  };

  useEffect(() => {
    return () => {
      socketRef.current?.disconnect();
      socketRef.current = null;
    };
  }, []);

  const connectSocket = () => {
    const token = getAccessToken();
    if (!token) {
      setError('socket connection requires login');
      return;
    }

    socketRef.current?.disconnect();
    setSocketStatus('connecting');

    const socket = io(socketUrl, {
      transports: ['websocket'],
      auth: {
        token,
      },
      withCredentials: true,
    });

    socket.on('connect', () => {
      setSocketStatus('connected');
      setMessage(`socket connected: ${socket.id}`);
    });

    socket.on('disconnect', () => {
      setSocketStatus('disconnected');
    });

    socket.on('connect_error', (err) => {
      setSocketStatus('disconnected');
      setError(err.message || 'socket connect failed');
    });

    socket.on('newMessage', (payload: ChatMessageItem) => {
      setSocketMessages((prev) => [payload, ...prev]);
    });

    socketRef.current = socket;
  };

  const disconnectSocket = () => {
    socketRef.current?.disconnect();
    socketRef.current = null;
    setSocketStatus('disconnected');
    setMessage('socket disconnected');
  };

  const emitJoinRoom = () => {
    if (!socketRef.current) {
      setError('socket is not connected');
      return;
    }
    if (!socketRoomId) {
      setError('roomId is required');
      return;
    }

    setError('');
    setMessage('');
    socketRef.current.emit('joinRoom', { roomId: Number(socketRoomId) }, (ack: unknown) => {
      setMessage(`socket joinRoom ack: ${JSON.stringify(ack)}`);
    });
  };

  const emitSocketMessage = () => {
    if (!socketRef.current) {
      setError('socket is not connected');
      return;
    }
    if (!socketRoomId || !socketMessageText.trim()) {
      setError('roomId and message are required');
      return;
    }

    setError('');
    setMessage('');
    socketRef.current.emit(
      'sendMessage',
      { roomId: Number(socketRoomId), message: socketMessageText.trim() },
      (ack: unknown) => {
        setMessage(`socket sendMessage ack: ${JSON.stringify(ack)}`);
      },
    );
  };

  return (
    <section>
      <h1>Chat API Step</h1>
      <p className="sub">spec step 20 - chat API integration (HTTP + Socket.IO)</p>
      <p className="sub">socket namespace: <code>{socketUrl}</code></p>
      {message ? <p className="sub">{message}</p> : null}
      {error ? <p className="error">{error}</p> : null}

      <div className="panel-grid">
        <div className="panel">
          <h2>HTTP - Rooms</h2>
          <form
            className="form-box"
            onSubmit={(e: FormEvent) => {
              e.preventDefault();
              run(async () => {
                const res = await fetchChatRooms({ page: Number(roomPage), limit: Number(roomLimit) });
                const data = res.data as unknown;
                const items = Array.isArray(data)
                  ? data
                  : typeof data === 'object' && data && 'items' in data
                    ? ((data as { items?: ChatRoomItem[] }).items ?? [])
                    : [];
                setRooms(items);
                setMessage('GET /chat/rooms success');
              });
            }}
          >
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

          <form
            className="form-box mt-12"
            onSubmit={(e: FormEvent) => {
              e.preventDefault();
              run(async () => {
                await createChatRoom({ name: createRoomName, isPrivate: createRoomPrivate });
                setMessage('POST /chat/rooms success');
              });
            }}
          >
            <label htmlFor="createRoomName">name</label>
            <input id="createRoomName" value={createRoomName} onChange={(e) => setCreateRoomName(e.target.value)} required />
            <label>
              <input type="checkbox" checked={createRoomPrivate} onChange={(e) => setCreateRoomPrivate(e.target.checked)} />
              isPrivate
            </label>
            <button type="submit">create room</button>
          </form>

          <form
            className="form-box mt-12"
            onSubmit={(e: FormEvent) => {
              e.preventDefault();
              run(async () => {
                await joinChatRoom(Number(joinRoomId));
                setMessage('POST /chat/rooms/:id/join success');
              });
            }}
          >
            <label htmlFor="joinRoomId">roomId</label>
            <input id="joinRoomId" value={joinRoomId} onChange={(e) => setJoinRoomId(e.target.value)} required />
            <button type="submit">join room</button>
          </form>
        </div>

        <div className="panel">
          <h2>HTTP - Messages</h2>
          <form
            className="form-box"
            onSubmit={(e: FormEvent) => {
              e.preventDefault();
              run(async () => {
                const res = await fetchChatMessages(Number(messageRoomId), {
                  page: Number(msgPage),
                  limit: Number(msgLimit),
                });
                const data = res.data as unknown;
                const items = Array.isArray(data)
                  ? data
                  : typeof data === 'object' && data && 'items' in data
                    ? ((data as { items?: ChatMessageItem[] }).items ?? [])
                    : [];
                setMessages(items);
                setMessage('GET /chat/rooms/:id/messages success');
              });
            }}
          >
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

          <form
            className="form-box mt-12"
            onSubmit={(e: FormEvent) => {
              e.preventDefault();
              run(async () => {
                await sendChatMessage(Number(messageRoomId), { message: messageText });
                setMessage('POST /chat/rooms/:id/messages success');
              });
            }}
          >
            <label htmlFor="messageText">message</label>
            <input id="messageText" value={messageText} onChange={(e) => setMessageText(e.target.value)} required />
            <button type="submit">send HTTP message</button>
          </form>
        </div>
      </div>

      <div className="panel-grid mt-12">
        <div className="panel">
          <h2>Socket.IO - Connect / Join</h2>
          <div className="form-box">
            <label>status</label>
            <input value={socketStatus} readOnly />
            <label htmlFor="socketRoomId">roomId</label>
            <input id="socketRoomId" value={socketRoomId} onChange={(e) => setSocketRoomId(e.target.value)} placeholder="1" />
            <div className="button-row">
              <button type="button" onClick={connectSocket}>connect socket</button>
              <button type="button" onClick={disconnectSocket}>disconnect</button>
              <button type="button" onClick={emitJoinRoom}>emit joinRoom</button>
            </div>
          </div>
        </div>

        <div className="panel">
          <h2>Socket.IO - Send / Receive</h2>
          <div className="form-box">
            <label htmlFor="socketMessageText">message</label>
            <input
              id="socketMessageText"
              value={socketMessageText}
              onChange={(e) => setSocketMessageText(e.target.value)}
              placeholder="real-time message"
            />
            <div className="button-row">
              <button type="button" onClick={emitSocketMessage}>emit sendMessage</button>
              <button type="button" onClick={() => setSocketMessages([])}>clear socket log</button>
            </div>
          </div>

          <ul className="list mt-12">
            {socketMessages.map((msg, index) => (
              <li key={`${msg.id}-${index}`}>
                {msg.id} / sender {msg.senderId} / {msg.message}
              </li>
            ))}
          </ul>
        </div>
      </div>
    </section>
  );
}
