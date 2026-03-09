<?php

namespace App\Modules\Support\Services;

use App\Common\Exceptions\BusinessException;
use App\Models\SupportTicket;
use App\Models\SupportTicketReply;
use App\Models\User;
use Illuminate\Support\Str;
use Symfony\Component\HttpFoundation\Response;

class SupportService
{
    public function listForUser(User $user): array
    {
        return SupportTicket::query()
            ->with(['user', 'replies.user'])
            ->where('user_id', $user->id)
            ->orderByDesc('id')
            ->get()
            ->map(fn (SupportTicket $ticket): array => $this->serializeTicket($ticket))
            ->values()
            ->all();
    }

    public function create(User $user, array $payload): array
    {
        $ticket = SupportTicket::query()->create([
            'user_id' => $user->id,
            'ticket_number' => $this->makeTicketNumber(),
            'category' => strtoupper($payload['category']),
            'title' => $payload['title'],
            'content' => $payload['content'],
            'status' => 'OPEN',
        ]);

        return $this->serializeTicket($ticket->fresh(['user', 'replies.user']));
    }

    public function show(User $actor, int $ticketId): array
    {
        $ticket = SupportTicket::query()->with(['user', 'replies.user'])->find($ticketId);
        if (! $ticket) {
            throw new BusinessException('지원 티켓을 찾을 수 없습니다.', 'SUPPORT_TICKET_NOT_FOUND', Response::HTTP_NOT_FOUND);
        }
        if ($actor->role !== 'ADMIN' && $ticket->user_id !== $actor->id) {
            throw new BusinessException('지원 티켓 조회 권한이 없습니다.', 'FORBIDDEN', Response::HTTP_FORBIDDEN);
        }

        return $this->serializeTicket($ticket);
    }

    public function reply(User $actor, int $ticketId, array $payload): array
    {
        $ticket = SupportTicket::query()->with(['user', 'replies.user'])->find($ticketId);
        if (! $ticket) {
            throw new BusinessException('지원 티켓을 찾을 수 없습니다.', 'SUPPORT_TICKET_NOT_FOUND', Response::HTTP_NOT_FOUND);
        }
        if ($actor->role !== 'ADMIN' && $ticket->user_id !== $actor->id) {
            throw new BusinessException('지원 티켓 답변 권한이 없습니다.', 'FORBIDDEN', Response::HTTP_FORBIDDEN);
        }

        SupportTicketReply::query()->create([
            'ticket_id' => $ticket->id,
            'user_id' => $actor->id,
            'content' => $payload['content'],
        ]);

        if ($actor->role === 'ADMIN' && $ticket->status === 'OPEN') {
            $ticket->forceFill(['status' => 'ANSWERED'])->save();
        } elseif ($actor->role !== 'ADMIN' && in_array($ticket->status, ['ANSWERED', 'RESOLVED'], true)) {
            $ticket->forceFill(['status' => 'OPEN'])->save();
        }

        return $this->serializeTicket($ticket->fresh(['user', 'replies.user']));
    }

    public function adminList(?string $status = null): array
    {
        $query = SupportTicket::query()->with(['user', 'replies.user'])->orderByDesc('id');
        if ($status) {
            $query->where('status', strtoupper($status));
        }

        return $query->get()
            ->map(fn (SupportTicket $ticket): array => $this->serializeTicket($ticket))
            ->values()
            ->all();
    }

    public function adminUpdateStatus(User $actor, int $ticketId, array $payload): array
    {
        if ($actor->role !== 'ADMIN') {
            throw new BusinessException('지원 티켓 상태 변경 권한이 없습니다.', 'FORBIDDEN', Response::HTTP_FORBIDDEN);
        }

        $ticket = SupportTicket::query()->with(['user', 'replies.user'])->find($ticketId);
        if (! $ticket) {
            throw new BusinessException('지원 티켓을 찾을 수 없습니다.', 'SUPPORT_TICKET_NOT_FOUND', Response::HTTP_NOT_FOUND);
        }

        $ticket->forceFill([
            'status' => strtoupper($payload['status']),
        ])->save();

        return $this->serializeTicket($ticket);
    }

    private function makeTicketNumber(): string
    {
        return 'SUP-' . now()->format('YmdHis') . '-' . Str::upper(Str::random(6));
    }

    private function serializeTicket(SupportTicket $ticket): array
    {
        $ticket->loadMissing(['user', 'replies.user']);

        return [
            'id' => $ticket->id,
            'ticketNumber' => $ticket->ticket_number,
            'category' => $ticket->category,
            'title' => $ticket->title,
            'content' => $ticket->content,
            'status' => $ticket->status,
            'author' => $ticket->user ? [
                'id' => $ticket->user->id,
                'name' => $ticket->user->name,
                'nickname' => $ticket->user->nickname,
            ] : null,
            'replies' => $ticket->replies->map(fn (SupportTicketReply $reply): array => [
                'id' => $reply->id,
                'content' => $reply->content,
                'author' => $reply->user ? [
                    'id' => $reply->user->id,
                    'name' => $reply->user->name,
                    'nickname' => $reply->user->nickname,
                ] : null,
                'createdAt' => optional($reply->created_at)?->toISOString(),
                'updatedAt' => optional($reply->updated_at)?->toISOString(),
            ])->values()->all(),
            'createdAt' => optional($ticket->created_at)?->toISOString(),
            'updatedAt' => optional($ticket->updated_at)?->toISOString(),
        ];
    }
}
