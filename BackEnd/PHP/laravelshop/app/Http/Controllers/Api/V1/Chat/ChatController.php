<?php

namespace App\Http\Controllers\Api\V1\Chat;

use OpenApi\Attributes as OA;

use App\Http\Controllers\Api\V1\ApiController;
use App\Modules\Chat\Requests\CreateChatRoomRequest;
use App\Modules\Chat\Requests\SendChatMessageRequest;
use App\Modules\Chat\Services\ChatService;

#[OA\Tag(name: 'Chat')]
class ChatController extends ApiController
{
    public function __construct(
        private readonly ChatService $chatService,
    ) {}

    public function rooms()
    {
        return $this->success($this->chatService->listRooms(
            request()->user(),
            (int) request()->query('page', 1),
            (int) request()->query('limit', 20),
        ));
    }

    public function storeRoom(CreateChatRoomRequest $request)
    {
        return $this->success($this->chatService->createRoom($request->user(), [
            ...$request->validated(),
            'isPrivate' => (bool) $request->input('isPrivate', true),
        ]), status: 201);
    }

    public function joinRoom(int $id)
    {
        return $this->success($this->chatService->joinRoom(request()->user(), $id));
    }

    public function messages(int $id)
    {
        return $this->success($this->chatService->listMessages(
            request()->user(),
            $id,
            (int) request()->query('page', 1),
            (int) request()->query('limit', 20),
        ));
    }

    public function storeMessage(SendChatMessageRequest $request, int $id)
    {
        return $this->success($this->chatService->sendMessage($request->user(), $id, $request->validated()), status: 201);
    }
}
