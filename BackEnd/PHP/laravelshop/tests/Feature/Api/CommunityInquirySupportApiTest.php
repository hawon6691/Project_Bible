<?php

namespace Tests\Feature\Api;

use App\Models\Board;
use App\Models\Category;
use App\Models\Product;
use App\Models\User;
use App\Modules\Auth\Services\JwtService;
use Illuminate\Foundation\Testing\RefreshDatabase;
use Illuminate\Support\Facades\Hash;
use Tests\TestCase;

class CommunityInquirySupportApiTest extends TestCase
{
    use RefreshDatabase;

    public function test_user_can_manage_community_posts_likes_and_comments(): void
    {
        $user = $this->createUser('USER');
        $board = Board::query()->create([
            'name' => '자유게시판',
            'slug' => 'free-board',
            'description' => 'PBShop 커뮤니티',
        ]);

        $boardsResponse = $this->getJson('/api/v1/boards');
        $boardsResponse->assertOk();
        $boardsResponse->assertJsonPath('data.0.slug', 'free-board');

        $createPost = $this->actingAsApiUser($user)->postJson('/api/v1/boards/'.$board->id.'/posts', [
            'title' => '첫 글입니다',
            'content' => '커뮤니티 테스트 본문',
        ]);
        $createPost->assertCreated();
        $postId = $createPost->json('data.id');

        $postsResponse = $this->getJson('/api/v1/boards/'.$board->id.'/posts');
        $postsResponse->assertOk();
        $postsResponse->assertJsonPath('data.items.0.title', '첫 글입니다');

        $showPost = $this->getJson('/api/v1/posts/'.$postId);
        $showPost->assertOk();
        $this->assertEquals(1, $showPost->json('data.viewCount'));

        $likeResponse = $this->actingAsApiUser($user)->postJson('/api/v1/posts/'.$postId.'/like');
        $likeResponse->assertOk();
        $likeResponse->assertJsonPath('data.liked', true);

        $createComment = $this->actingAsApiUser($user)->postJson('/api/v1/posts/'.$postId.'/comments', [
            'content' => '댓글 테스트',
        ]);
        $createComment->assertCreated();
        $commentId = $createComment->json('data.id');

        $listComments = $this->getJson('/api/v1/posts/'.$postId.'/comments');
        $listComments->assertOk();
        $listComments->assertJsonPath('data.0.content', '댓글 테스트');

        $updatePost = $this->actingAsApiUser($user)->patchJson('/api/v1/posts/'.$postId, [
            'title' => '수정된 글',
        ]);
        $updatePost->assertOk();
        $updatePost->assertJsonPath('data.title', '수정된 글');

        $deleteComment = $this->actingAsApiUser($user)->deleteJson('/api/v1/comments/'.$commentId);
        $deleteComment->assertOk();
        $deleteComment->assertJsonPath('data.message', '댓글이 삭제되었습니다.');

        $deletePost = $this->actingAsApiUser($user)->deleteJson('/api/v1/posts/'.$postId);
        $deletePost->assertOk();
        $deletePost->assertJsonPath('data.message', '게시글이 삭제되었습니다.');
    }

    public function test_user_can_create_and_admin_can_answer_product_inquiry(): void
    {
        $user = $this->createUser('USER');
        $outsider = $this->createUser('USER');
        $admin = $this->createUser('ADMIN');
        $category = Category::query()->create([
            'name' => '모니터',
            'slug' => 'monitor',
            'depth' => 0,
            'sort_order' => 0,
            'is_visible' => true,
        ]);
        $product = Product::query()->create([
            'category_id' => $category->id,
            'name' => 'PB Monitor',
            'slug' => 'pb-monitor',
            'status' => 'ACTIVE',
        ]);

        $createInquiry = $this->actingAsApiUser($user)->postJson('/api/v1/products/'.$product->id.'/inquiries', [
            'title' => '재입고 문의',
            'content' => '언제 재입고되나요?',
            'isSecret' => true,
        ]);
        $createInquiry->assertCreated();
        $inquiryId = $createInquiry->json('data.id');

        $listMine = $this->actingAsApiUser($user)->getJson('/api/v1/inquiries/me');
        $listMine->assertOk();
        $listMine->assertJsonPath('data.0.title', '재입고 문의');

        $publicList = $this->actingAsApiUser($outsider)->getJson('/api/v1/products/'.$product->id.'/inquiries');
        $publicList->assertOk();
        $publicList->assertJsonPath('data.0.content', '비밀 문의입니다.');

        $answerInquiry = $this->actingAsApiUser($admin)->postJson('/api/v1/inquiries/'.$inquiryId.'/answer', [
            'answer' => '다음 주 입고 예정입니다.',
        ]);
        $answerInquiry->assertOk();
        $answerInquiry->assertJsonPath('data.answer', '다음 주 입고 예정입니다.');

        $deleteInquiry = $this->actingAsApiUser($user)->deleteJson('/api/v1/inquiries/'.$inquiryId);
        $deleteInquiry->assertOk();
        $deleteInquiry->assertJsonPath('data.message', '문의글이 삭제되었습니다.');
    }

    public function test_user_and_admin_can_handle_support_ticket_flow(): void
    {
        $user = $this->createUser('USER');
        $admin = $this->createUser('ADMIN');

        $createTicket = $this->actingAsApiUser($user)->postJson('/api/v1/support/tickets', [
            'category' => 'delivery',
            'title' => '배송 문의',
            'content' => '배송이 지연되고 있습니다.',
        ]);
        $createTicket->assertCreated();
        $ticketId = $createTicket->json('data.id');
        $createTicket->assertJsonPath('data.status', 'OPEN');

        $listTickets = $this->actingAsApiUser($user)->getJson('/api/v1/support/tickets');
        $listTickets->assertOk();
        $listTickets->assertJsonPath('data.0.title', '배송 문의');

        $adminReply = $this->actingAsApiUser($admin)->postJson('/api/v1/support/tickets/'.$ticketId.'/reply', [
            'content' => '오늘 중으로 확인 후 안내드리겠습니다.',
        ]);
        $adminReply->assertOk();
        $adminReply->assertJsonPath('data.status', 'ANSWERED');
        $adminReply->assertJsonPath('data.replies.0.content', '오늘 중으로 확인 후 안내드리겠습니다.');

        $adminList = $this->actingAsApiUser($admin)->getJson('/api/v1/admin/support/tickets');
        $adminList->assertOk();
        $adminList->assertJsonPath('data.0.ticketNumber', $createTicket->json('data.ticketNumber'));

        $updateStatus = $this->actingAsApiUser($admin)->patchJson('/api/v1/admin/support/tickets/'.$ticketId.'/status', [
            'status' => 'RESOLVED',
        ]);
        $updateStatus->assertOk();
        $updateStatus->assertJsonPath('data.status', 'RESOLVED');

        $showTicket = $this->actingAsApiUser($user)->getJson('/api/v1/support/tickets/'.$ticketId);
        $showTicket->assertOk();
        $showTicket->assertJsonPath('data.status', 'RESOLVED');
    }

    private function createUser(string $role): User
    {
        return User::query()->create([
            'email' => uniqid('cis-', true).'@example.com',
            'password' => Hash::make('Password123!'),
            'name' => 'Community Inquiry Support User',
            'nickname' => 'cis-user',
            'role' => $role,
            'status' => 'ACTIVE',
            'phone' => '01012345678',
            'email_verified_at' => now(),
        ]);
    }

    private function actingAsApiUser(User $user): self
    {
        $token = app(JwtService::class)->createAccessToken($user);

        return $this->withHeader('Authorization', 'Bearer '.$token);
    }
}
