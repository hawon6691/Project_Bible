<?php

namespace App\Modules\Shortform\Services;

use App\Common\Exceptions\BusinessException;
use App\Models\Product;
use App\Models\Shortform;
use App\Models\ShortformComment;
use App\Models\ShortformLike;
use App\Models\ShortformProduct;
use App\Models\User;
use Symfony\Component\HttpFoundation\Response;

class ShortformService
{
    public function create(User $user, array $payload): array
    {
        $shortform = Shortform::query()->create([
            'user_id' => $user->id,
            'title' => $payload['title'],
            'video_url' => $payload['videoUrl'],
            'thumbnail_url' => $payload['thumbnailUrl'] ?? null,
            'transcode_status' => 'COMPLETED',
        ]);

        foreach ($payload['productIds'] ?? [] as $productId) {
            if (Product::query()->where('id', $productId)->exists()) {
                ShortformProduct::query()->create([
                    'shortform_id' => $shortform->id,
                    'product_id' => $productId,
                ]);
            }
        }

        return $this->serializeShortform($shortform->fresh());
    }

    public function list(?string $cursor, int $limit): array
    {
        $query = Shortform::query()->orderByDesc('id');
        if ($cursor !== null) {
            $query->where('id', '<', (int) $cursor);
        }

        $items = $query->limit($limit)->get();

        return [
            'items' => $items->map(fn (Shortform $shortform): array => $this->serializeShortform($shortform))->all(),
            'nextCursor' => $items->count() === $limit ? (string) $items->last()->id : null,
        ];
    }

    public function show(int $id): array
    {
        $shortform = $this->findShortform($id);
        $shortform->increment('view_count');

        return $this->serializeShortform($shortform->fresh());
    }

    public function toggleLike(User $user, int $id): array
    {
        $shortform = $this->findShortform($id);
        $like = ShortformLike::query()->where('shortform_id', $id)->where('user_id', $user->id)->first();

        if ($like) {
            $like->delete();
            $liked = false;
        } else {
            ShortformLike::query()->create(['shortform_id' => $id, 'user_id' => $user->id]);
            $liked = true;
        }

        $shortform->forceFill([
            'like_count' => ShortformLike::query()->where('shortform_id', $id)->count(),
        ])->save();

        return [
            'liked' => $liked,
            'likeCount' => $shortform->fresh()->like_count,
        ];
    }

    public function addComment(User $user, int $id, array $payload): array
    {
        $shortform = $this->findShortform($id);
        $comment = ShortformComment::query()->create([
            'shortform_id' => $shortform->id,
            'user_id' => $user->id,
            'content' => $payload['content'],
        ]);

        $shortform->forceFill([
            'comment_count' => ShortformComment::query()->where('shortform_id', $id)->count(),
        ])->save();

        return $this->serializeComment($comment);
    }

    public function comments(int $id, int $page, int $limit): array
    {
        $this->findShortform($id);
        $result = ShortformComment::query()->where('shortform_id', $id)->orderBy('id')->paginate($limit, ['*'], 'page', $page);

        return [
            'items' => $result->getCollection()->map(fn (ShortformComment $comment): array => $this->serializeComment($comment))->all(),
            'pagination' => $this->pagination($result),
        ];
    }

    public function ranking(string $period, int $limit): array
    {
        return Shortform::query()->orderByDesc('like_count')->orderByDesc('view_count')->limit($limit)->get()->map(fn (Shortform $shortform): array => array_merge($this->serializeShortform($shortform), ['period' => $period]))->all();
    }

    public function transcodeStatus(int $id): array
    {
        $shortform = $this->findShortform($id);

        return [
            'status' => $shortform->transcode_status,
            'errorMessage' => null,
            'transcodedAt' => optional($shortform->updated_at)?->toISOString(),
        ];
    }

    public function retry(User $user, int $id): array
    {
        $shortform = $this->findOwnedShortform($user, $id);
        $shortform->forceFill(['transcode_status' => 'QUEUED'])->save();

        return [
            'message' => '트랜스코딩 재시도가 등록되었습니다.',
            'queued' => true,
        ];
    }

    public function delete(User $user, int $id): array
    {
        $shortform = $this->findOwnedShortform($user, $id);
        $shortform->delete();

        return ['message' => '숏폼이 삭제되었습니다.'];
    }

    public function byUser(int $userId, int $page, int $limit): array
    {
        $result = Shortform::query()->where('user_id', $userId)->orderByDesc('id')->paginate($limit, ['*'], 'page', $page);

        return [
            'items' => $result->getCollection()->map(fn (Shortform $shortform): array => $this->serializeShortform($shortform))->all(),
            'pagination' => $this->pagination($result),
        ];
    }

    private function findShortform(int $id): Shortform
    {
        $shortform = Shortform::query()->find($id);
        if (! $shortform) {
            throw new BusinessException('숏폼을 찾을 수 없습니다.', 'SHORTFORM_NOT_FOUND', Response::HTTP_NOT_FOUND);
        }

        return $shortform;
    }

    private function findOwnedShortform(User $user, int $id): Shortform
    {
        $shortform = Shortform::query()->where('user_id', $user->id)->find($id);
        if (! $shortform) {
            throw new BusinessException('숏폼을 찾을 수 없습니다.', 'SHORTFORM_NOT_FOUND', Response::HTTP_NOT_FOUND);
        }

        return $shortform;
    }

    private function serializeShortform(Shortform $shortform): array
    {
        return [
            'id' => $shortform->id,
            'userId' => $shortform->user_id,
            'title' => $shortform->title,
            'videoUrl' => $shortform->video_url,
            'thumbnailUrl' => $shortform->thumbnail_url,
            'viewCount' => $shortform->view_count,
            'likeCount' => $shortform->like_count,
            'commentCount' => $shortform->comment_count,
            'transcodeStatus' => $shortform->transcode_status,
            'createdAt' => optional($shortform->created_at)?->toISOString(),
            'updatedAt' => optional($shortform->updated_at)?->toISOString(),
        ];
    }

    private function serializeComment(ShortformComment $comment): array
    {
        return [
            'id' => $comment->id,
            'shortformId' => $comment->shortform_id,
            'userId' => $comment->user_id,
            'content' => $comment->content,
            'createdAt' => optional($comment->created_at)?->toISOString(),
        ];
    }

    private function pagination($result): array
    {
        return [
            'page' => $result->currentPage(),
            'limit' => $result->perPage(),
            'total' => $result->total(),
            'totalPages' => $result->lastPage(),
        ];
    }
}
