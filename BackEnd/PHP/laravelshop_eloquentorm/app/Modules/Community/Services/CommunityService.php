<?php

namespace App\Modules\Community\Services;

use App\Common\Exceptions\BusinessException;
use App\Models\Board;
use App\Models\Post;
use App\Models\PostComment;
use App\Models\PostLike;
use App\Models\User;
use Illuminate\Support\Facades\DB;
use Symfony\Component\HttpFoundation\Response;

class CommunityService
{
    public function listBoards(): array
    {
        return Board::query()
            ->withCount('posts')
            ->orderBy('id')
            ->get()
            ->map(fn (Board $board): array => [
                'id' => $board->id,
                'name' => $board->name,
                'slug' => $board->slug,
                'description' => $board->description,
                'postCount' => $board->posts_count,
                'createdAt' => optional($board->created_at)?->toISOString(),
                'updatedAt' => optional($board->updated_at)?->toISOString(),
            ])
            ->values()
            ->all();
    }

    public function listPosts(int $boardId): array
    {
        $board = Board::query()->find($boardId);
        if (! $board) {
            throw new BusinessException('게시판을 찾을 수 없습니다.', 'BOARD_NOT_FOUND', Response::HTTP_NOT_FOUND);
        }

        $items = Post::query()
            ->with(['board', 'user'])
            ->where('board_id', $boardId)
            ->orderByDesc('id')
            ->get()
            ->map(fn (Post $post): array => $this->serializePost($post))
            ->values()
            ->all();

        return [
            'board' => [
                'id' => $board->id,
                'name' => $board->name,
                'slug' => $board->slug,
            ],
            'items' => $items,
        ];
    }

    public function showPost(int $postId): array
    {
        $post = Post::query()->with(['board', 'user'])->find($postId);
        if (! $post) {
            throw new BusinessException('게시글을 찾을 수 없습니다.', 'POST_NOT_FOUND', Response::HTTP_NOT_FOUND);
        }

        $post->increment('view_count');
        $post->refresh()->load(['board', 'user']);

        return $this->serializePost($post);
    }

    public function createPost(User $user, int $boardId, array $payload): array
    {
        $board = Board::query()->find($boardId);
        if (! $board) {
            throw new BusinessException('게시판을 찾을 수 없습니다.', 'BOARD_NOT_FOUND', Response::HTTP_NOT_FOUND);
        }

        $post = Post::query()->create([
            'board_id' => $boardId,
            'user_id' => $user->id,
            'title' => $payload['title'],
            'content' => $payload['content'],
        ]);

        return $this->serializePost($post->fresh(['board', 'user']));
    }

    public function updatePost(User $actor, int $postId, array $payload): array
    {
        $post = Post::query()->with(['board', 'user'])->find($postId);
        if (! $post) {
            throw new BusinessException('게시글을 찾을 수 없습니다.', 'POST_NOT_FOUND', Response::HTTP_NOT_FOUND);
        }
        if ($actor->role !== 'ADMIN' && $post->user_id !== $actor->id) {
            throw new BusinessException('게시글 수정 권한이 없습니다.', 'FORBIDDEN', Response::HTTP_FORBIDDEN);
        }

        $updates = [];
        if (array_key_exists('title', $payload)) {
            $updates['title'] = $payload['title'];
        }
        if (array_key_exists('content', $payload)) {
            $updates['content'] = $payload['content'];
        }

        if ($updates !== []) {
            $post->forceFill($updates)->save();
        }

        return $this->serializePost($post->fresh(['board', 'user']));
    }

    public function deletePost(User $actor, int $postId): array
    {
        $post = Post::query()->find($postId);
        if (! $post) {
            throw new BusinessException('게시글을 찾을 수 없습니다.', 'POST_NOT_FOUND', Response::HTTP_NOT_FOUND);
        }
        if ($actor->role !== 'ADMIN' && $post->user_id !== $actor->id) {
            throw new BusinessException('게시글 삭제 권한이 없습니다.', 'FORBIDDEN', Response::HTTP_FORBIDDEN);
        }

        $post->delete();

        return ['message' => '게시글이 삭제되었습니다.'];
    }

    public function toggleLike(User $user, int $postId): array
    {
        $post = Post::query()->find($postId);
        if (! $post) {
            throw new BusinessException('게시글을 찾을 수 없습니다.', 'POST_NOT_FOUND', Response::HTTP_NOT_FOUND);
        }

        $like = PostLike::query()->where('post_id', $postId)->where('user_id', $user->id)->first();
        $liked = false;

        if ($like) {
            $like->delete();
        } else {
            PostLike::query()->create([
                'post_id' => $postId,
                'user_id' => $user->id,
            ]);
            $liked = true;
        }

        $likeCount = PostLike::query()->where('post_id', $postId)->count();
        $post->forceFill(['like_count' => $likeCount])->save();

        return [
            'postId' => $postId,
            'liked' => $liked,
            'likeCount' => $likeCount,
        ];
    }

    public function listComments(int $postId): array
    {
        $post = Post::query()->find($postId);
        if (! $post) {
            throw new BusinessException('게시글을 찾을 수 없습니다.', 'POST_NOT_FOUND', Response::HTTP_NOT_FOUND);
        }

        return PostComment::query()
            ->with('user')
            ->where('post_id', $postId)
            ->orderBy('id')
            ->get()
            ->map(fn (PostComment $comment): array => $this->serializeComment($comment))
            ->values()
            ->all();
    }

    public function createComment(User $user, int $postId, array $payload): array
    {
        $post = Post::query()->find($postId);
        if (! $post) {
            throw new BusinessException('게시글을 찾을 수 없습니다.', 'POST_NOT_FOUND', Response::HTTP_NOT_FOUND);
        }

        $comment = DB::transaction(function () use ($user, $post, $payload): PostComment {
            $comment = PostComment::query()->create([
                'post_id' => $post->id,
                'user_id' => $user->id,
                'content' => $payload['content'],
            ]);

            $post->increment('comment_count');

            return $comment;
        });

        return $this->serializeComment($comment->fresh('user'));
    }

    public function deleteComment(User $actor, int $commentId): array
    {
        $comment = PostComment::query()->find($commentId);
        if (! $comment) {
            throw new BusinessException('댓글을 찾을 수 없습니다.', 'COMMENT_NOT_FOUND', Response::HTTP_NOT_FOUND);
        }
        if ($actor->role !== 'ADMIN' && $comment->user_id !== $actor->id) {
            throw new BusinessException('댓글 삭제 권한이 없습니다.', 'FORBIDDEN', Response::HTTP_FORBIDDEN);
        }

        DB::transaction(function () use ($comment): void {
            Post::query()->whereKey($comment->post_id)->where('comment_count', '>', 0)->decrement('comment_count');
            $comment->delete();
        });

        return ['message' => '댓글이 삭제되었습니다.'];
    }

    private function serializePost(Post $post): array
    {
        return [
            'id' => $post->id,
            'board' => $post->board ? [
                'id' => $post->board->id,
                'name' => $post->board->name,
                'slug' => $post->board->slug,
            ] : null,
            'title' => $post->title,
            'content' => $post->content,
            'viewCount' => $post->view_count,
            'likeCount' => $post->like_count,
            'commentCount' => $post->comment_count,
            'author' => $post->user ? [
                'id' => $post->user->id,
                'name' => $post->user->name,
                'nickname' => $post->user->nickname,
            ] : null,
            'createdAt' => optional($post->created_at)?->toISOString(),
            'updatedAt' => optional($post->updated_at)?->toISOString(),
        ];
    }

    private function serializeComment(PostComment $comment): array
    {
        return [
            'id' => $comment->id,
            'postId' => $comment->post_id,
            'content' => $comment->content,
            'author' => $comment->user ? [
                'id' => $comment->user->id,
                'name' => $comment->user->name,
                'nickname' => $comment->user->nickname,
            ] : null,
            'createdAt' => optional($comment->created_at)?->toISOString(),
            'updatedAt' => optional($comment->updated_at)?->toISOString(),
        ];
    }
}
