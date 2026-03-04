import { FormEvent, useState } from 'react';
import {
  answerSupportTicketAdmin,
  createSupportTicket,
  fetchAdminSupportTickets,
  fetchMySupportTicket,
  fetchMySupportTickets,
} from '@/lib/endpoints';

export default function SupportApiPage() {
  const [myTickets, setMyTickets] = useState<any[]>([]);
  const [adminTickets, setAdminTickets] = useState<any[]>([]);
  const [ticketDetail, setTicketDetail] = useState<any | null>(null);

  const [createCategory, setCreateCategory] = useState('ORDER');
  const [createTitle, setCreateTitle] = useState('');
  const [createContent, setCreateContent] = useState('');
  const [createAttachmentUrl, setCreateAttachmentUrl] = useState('');

  const [detailId, setDetailId] = useState('');
  const [statusFilter, setStatusFilter] = useState('');

  const [answerTicketId, setAnswerTicketId] = useState('');
  const [answerContent, setAnswerContent] = useState('');

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
      <h1>Support API Step</h1>
      <p className="sub">spec step 17 - support API integration</p>
      {message ? <p className="sub">{message}</p> : null}
      {error ? <p className="error">{error}</p> : null}

      <div className="panel-grid">
        <div className="panel">
          <h2>POST /support/tickets</h2>
          <form className="form-box" onSubmit={(e: FormEvent) => {
            e.preventDefault();
            run(async () => {
              const res = await createSupportTicket({
                category: createCategory,
                title: createTitle,
                content: createContent,
                ...(createAttachmentUrl ? { attachmentUrl: createAttachmentUrl } : {}),
              });
              setTicketDetail(res.data);
              setMessage('POST /support/tickets success');
            });
          }}>
            <label htmlFor="createCategory">category</label>
            <input id="createCategory" value={createCategory} onChange={(e) => setCreateCategory(e.target.value)} required />
            <label htmlFor="createTitle">title</label>
            <input id="createTitle" value={createTitle} onChange={(e) => setCreateTitle(e.target.value)} required />
            <label htmlFor="createContent">content</label>
            <input id="createContent" value={createContent} onChange={(e) => setCreateContent(e.target.value)} required />
            <label htmlFor="createAttachmentUrl">attachmentUrl(optional)</label>
            <input id="createAttachmentUrl" value={createAttachmentUrl} onChange={(e) => setCreateAttachmentUrl(e.target.value)} />
            <button type="submit">create ticket</button>
          </form>
        </div>

        <div className="panel">
          <h2>GET /support/tickets/me</h2>
          <div className="button-row">
            <button
              type="button"
              onClick={() => run(async () => {
                const res = await fetchMySupportTickets(statusFilter ? { status: statusFilter as 'OPEN' | 'ANSWERED' } : undefined);
                setMyTickets(res.data as any[]);
                setMessage('GET /support/tickets/me success');
              })}
            >
              load my tickets
            </button>
          </div>
          <div className="form-row mt-12">
            <label htmlFor="statusFilter">status</label>
            <input id="statusFilter" value={statusFilter} onChange={(e) => setStatusFilter(e.target.value)} placeholder="OPEN / ANSWERED" />
          </div>

          <ul className="list mt-12">
            {myTickets.map((ticket) => (
              <li key={ticket.id}>
                {ticket.id} / {ticket.category} / {ticket.title} / {ticket.status}
              </li>
            ))}
          </ul>
        </div>
      </div>

      <div className="panel-grid mt-12">
        <div className="panel">
          <h2>GET /support/tickets/me/:id</h2>
          <form className="form-box" onSubmit={(e: FormEvent) => {
            e.preventDefault();
            run(async () => {
              const res = await fetchMySupportTicket(Number(detailId));
              setTicketDetail(res.data);
              setMessage('GET /support/tickets/me/:id success');
            });
          }}>
            <label htmlFor="detailId">ticketId</label>
            <input id="detailId" value={detailId} onChange={(e) => setDetailId(e.target.value)} required />
            <button type="submit">load my ticket detail</button>
          </form>
        </div>

        <div className="panel">
          <h2>GET /admin/support/tickets</h2>
          <div className="button-row">
            <button
              type="button"
              onClick={() => run(async () => {
                const res = await fetchAdminSupportTickets(statusFilter ? { status: statusFilter as 'OPEN' | 'ANSWERED' } : undefined);
                setAdminTickets(res.data as any[]);
                setMessage('GET /admin/support/tickets success');
              })}
            >
              load admin tickets
            </button>
          </div>

          <ul className="list mt-12">
            {adminTickets.map((ticket) => (
              <li key={ticket.id}>
                {ticket.id} / {ticket.category} / {ticket.title} / {ticket.status}
              </li>
            ))}
          </ul>
        </div>
      </div>

      <div className="panel-grid mt-12">
        <div className="panel">
          <h2>POST /admin/support/tickets/:id/answer</h2>
          <form className="form-box" onSubmit={(e: FormEvent) => {
            e.preventDefault();
            run(async () => {
              const res = await answerSupportTicketAdmin(Number(answerTicketId), { content: answerContent });
              setTicketDetail(res.data);
              setMessage('POST /admin/support/tickets/:id/answer success');
            });
          }}>
            <label htmlFor="answerTicketId">ticketId</label>
            <input id="answerTicketId" value={answerTicketId} onChange={(e) => setAnswerTicketId(e.target.value)} required />
            <label htmlFor="answerContent">content</label>
            <input id="answerContent" value={answerContent} onChange={(e) => setAnswerContent(e.target.value)} required />
            <button type="submit">answer ticket</button>
          </form>
        </div>

        <div className="panel">
          <h2>Ticket Detail</h2>
          {ticketDetail ? (
            <pre className="code-view">{JSON.stringify(ticketDetail, null, 2)}</pre>
          ) : (
            <p className="sub">선택한 티켓 상세가 여기에 표시됩니다.</p>
          )}
        </div>
      </div>
    </section>
  );
}
