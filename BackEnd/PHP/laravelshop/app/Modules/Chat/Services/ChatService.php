<?php

namespace App\Modules\Chat\Services;

use App\Common\Exceptions\BusinessException;
use App\Models\ChatMessage;
use App\Models\ChatRoom;
use App\Models\ChatRoomMember;
use App\Models\User;
use Illuminate\Support\Facades\DB;
use Symfony\Component\HttpFoundation\Response;

class ChatService
{
    public function listRooms(User $user, int $page = 1, int $limit = 20): array
    {
        $query = ChatRoom::query()
            ->with(['members.user', 'messages.sender'])
            ->whereHas('members', fn ($builder) => $builder->where('user_id', $user->id))
            ->orderByDesc('last_message_at')
            ->orderByDesc('id');

        return [
            'items' => $query->forPage($page, $limit)->get()
                ->map(fn (ChatRoom $room): array => $this->serializeRoom($room))
                ->values()->all(),
            'pagination' => [
                'page' => $page,
                'limit' => $limit,
                'total' => $query->count(),
            ],
        ];
    }

    public function createRoom(User $user, array $payload): array
    {
        $room = DB::transaction(function () use ($user, $payload): ChatRoom {
            $room = ChatRoom::query()->create([
                'creator_id' => $user->id,
                'name' => $payload['name'],
                'is_private' => (bool) ($payload['isPrivate'] ?? true),
                'status' => 'OPEN',
            ]);

            ChatRoomMember::query()->create([
                'room_id' => $room->id,
                'user_id' => $user->id,
                'joined_at' => now(),
            ]);

            return $room;
        });

        return $this->serializeRoom($room->fresh(['members.user', 'messages.sender']));
    }

    public function joinRoom(User $user, int $roomId): array
    {
        $room = ChatRoom::query()->find($roomId);
        if (! $room) {
            throw new BusinessException('채팅방을 찾을 수 없습니다.', 'CHAT_ROOM_NOT_FOUND', Response::HTTP_NOT_FOUND);
        }

        ChatRoomMember::query()->firstOrCreate(
            ['room_id' => $room->id, 'user_id' => $user->id],
            ['joined_at' => now()],
        );

        return $this->serializeRoom($room->fresh(['members.user', 'messages.sender']));
    }

    public function listMessages(User $user, int $roomId, int $page = 1, int $limit = 20): array
    {
        $room = $this->getAccessibleRoom($user, $roomId);

        $query = ChatMessage::query()
            ->with('sender')
            ->where('room_id', $room->id)
            ->orderByDesc('id');

        return [
            'room' => $this->serializeRoom($room),
            'items' => $query->forPage($page, $limit)->get()
                ->sortBy('id')
                ->values()
                ->map(fn (ChatMessage $message): array => $this->serializeMessage($message))
                ->all(),
            'pagination' => [
                'page' => $page,
                'limit' => $limit,
                'total' => $query->count(),
            ],
        ];
    }

    public function sendMessage(User $user, int $roomId, array $payload): array
    {
        $room = $this->getAccessibleRoom($user, $roomId);

        $message = ChatMessage::query()->create([
            'room_id' => $room->id,
            'sender_id' => $user->id,
            'message' => $payload['message'],
        ]);

        $room->forceFill([
            'last_message_at' => now(),
        ])->save();

        return $this->serializeMessage($message->fresh('sender'));
    }

    private function getAccessibleRoom(User $user, int $roomId): ChatRoom
    {
        $room = ChatRoom::query()->with(['members.user', 'messages.sender'])->find($roomId);
        if (! $room) {
            throw new BusinessException('채팅방을 찾을 수 없습니다.', 'CHAT_ROOM_NOT_FOUND', Response::HTTP_NOT_FOUND);
        }

        $isMember = ChatRoomMember::query()->where('room_id', $roomId)->where('user_id', $user->id)->exists();
        if (! $isMember && $user->role !== 'ADMIN') {
            throw new BusinessException('채팅방 접근 권한이 없습니다.', 'FORBIDDEN', Response::HTTP_FORBIDDEN);
        }

        return $room;
    }

    private function serializeRoom(ChatRoom $room): array
    {
        $room->loadMissing(['members.user', 'messages.sender']);
        $lastMessage = $room->messages->sortByDesc('id')->first();

        return [
            'id' => $room->id,
            'name' => $room->name,
            'isPrivate' => $room->is_private,
            'status' => $room->status,
            'members' => $room->members->map(fn (ChatRoomMember $member): array => [
                'userId' => $member->user_id,
                'name' => $member->user?->name,
                'nickname' => $member->user?->nickname,
                'joinedAt' => optional($member->joined_at)?->toISOString(),
            ])->values()->all(),
            'lastMessage' => $lastMessage ? $this->serializeMessage($lastMessage) : null,
            'lastMessageAt' => optional($room->last_message_at)?->toISOString(),
            'createdAt' => optional($room->created_at)?->toISOString(),
            'updatedAt' => optional($room->updated_at)?->toISOString(),
        ];
    }

    private function serializeMessage(ChatMessage $message): array
    {
        return [
            'id' => $message->id,
            'roomId' => $message->room_id,
            'sender' => $message->sender ? [
                'id' => $message->sender->id,
                'name' => $message->sender->name,
                'nickname' => $message->sender->nickname,
            ] : null,
            'message' => $message->message,
            'createdAt' => optional($message->created_at)?->toISOString(),
            'updatedAt' => optional($message->updated_at)?->toISOString(),
        ];
    }
}
