import { FormEvent, useState } from 'react';
import {
  createBoardPost,
  createPostComment,
  fetchBoardPosts,
  fetchBoards,
  fetchPost,
  fetchPostComments,
  removeComment,
  removePost,
  togglePostLike,
  updatePost,
} from '@/lib/endpoints';

export default function CommunityApiPage() {
  const [boards, setBoards] = useState<any[]>([]);
  const [posts, setPosts] = useState<any[]>([]);
  const [postDetail, setPostDetail] = useState<any | null>(null);
  const [comments, setComments] = useState<any[]>([]);

  const [boardId, setBoardId] = useState('');
  const [page, setPage] = useState('1');
  const [limit, setLimit] = useState('20');
  const [search, setSearch] = useState('');
  const [sort, setSort] = useState('newest');

  const [detailPostId, setDetailPostId] = useState('');
  const [createBoardId, setCreateBoardId] = useState('');
  const [createTitle, setCreateTitle] = useState('');
  const [createContent, setCreateContent] = useState('');
  const [updatePostId, setUpdatePostId] = useState('');
  const [updateTitle, setUpdateTitle] = useState('');
  const [updateContent, setUpdateContent] = useState('');
  const [deletePostId, setDeletePostId] = useState('');
  const [likePostId, setLikePostId] = useState('');
  const [commentPostId, setCommentPostId] = useState('');
  const [commentContent, setCommentContent] = useState('');
  const [deleteCommentId, setDeleteCommentId] = useState('');

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
      <h1>Community API Step</h1>
      <p className="sub">spec step 15 - community API integration</p>
      {message ? <p className="sub">{message}</p> : null}
      {error ? <p className="error">{error}</p> : null}

      <div className="panel-grid">
        <div className="panel">
          <h2>GET /boards</h2>
          <div className="button-row">
            <button
              type="button"
              onClick={() => run(async () => {
                const res = await fetchBoards();
                setBoards(res.data as any[]);
                setMessage('GET /boards success');
              })}
            >
              load boards
            </button>
          </div>
          <ul className="list mt-12">
            {boards.map((board) => (
              <li key={board.id}>
                {board.id} / {board.slug || '-'} / {board.name}
              </li>
            ))}
          </ul>
        </div>

        <div className="panel">
          <h2>GET /boards/:boardId/posts</h2>
          <form className="form-box" onSubmit={(e: FormEvent) => {
            e.preventDefault();
            run(async () => {
              const res = await fetchBoardPosts(Number(boardId), {
                page: Number(page),
                limit: Number(limit),
                ...(search ? { search } : {}),
                ...(sort ? { sort: sort as 'newest' | 'popular' | 'most_commented' } : {}),
              });
              setPosts(res.data as any[]);
              setMessage('GET /boards/:boardId/posts success');
            });
          }}>
            <label htmlFor="boardId">boardId</label>
            <input id="boardId" value={boardId} onChange={(e) => setBoardId(e.target.value)} required />
            <label htmlFor="page">page</label>
            <input id="page" value={page} onChange={(e) => setPage(e.target.value)} required />
            <label htmlFor="limit">limit</label>
            <input id="limit" value={limit} onChange={(e) => setLimit(e.target.value)} required />
            <label htmlFor="search">search(optional)</label>
            <input id="search" value={search} onChange={(e) => setSearch(e.target.value)} />
            <label htmlFor="sort">sort</label>
            <select id="sort" value={sort} onChange={(e) => setSort(e.target.value)}>
              <option value="newest">newest</option>
              <option value="popular">popular</option>
              <option value="most_commented">most_commented</option>
            </select>
            <button type="submit">load posts</button>
          </form>
          <ul className="list mt-12">
            {posts.map((post) => (
              <li key={post.id}>
                {post.id} / {post.title} / likes {post.likeCount} / comments {post.commentCount}
              </li>
            ))}
          </ul>
        </div>
      </div>

      <div className="panel-grid mt-12">
        <div className="panel">
          <h2>GET /posts/:id</h2>
          <form className="form-box" onSubmit={(e: FormEvent) => {
            e.preventDefault();
            run(async () => {
              const res = await fetchPost(Number(detailPostId));
              setPostDetail(res.data);
              setMessage('GET /posts/:id success');
            });
          }}>
            <label htmlFor="detailPostId">postId</label>
            <input id="detailPostId" value={detailPostId} onChange={(e) => setDetailPostId(e.target.value)} required />
            <button type="submit">load post</button>
          </form>
          {postDetail ? <pre className="code-view mt-12">{JSON.stringify(postDetail, null, 2)}</pre> : null}
        </div>

        <div className="panel">
          <h2>POST /boards/:boardId/posts</h2>
          <form className="form-box" onSubmit={(e: FormEvent) => {
            e.preventDefault();
            run(async () => {
              await createBoardPost(Number(createBoardId), { title: createTitle, content: createContent });
              setMessage('POST /boards/:boardId/posts success');
            });
          }}>
            <label htmlFor="createBoardId">boardId</label>
            <input id="createBoardId" value={createBoardId} onChange={(e) => setCreateBoardId(e.target.value)} required />
            <label htmlFor="createTitle">title</label>
            <input id="createTitle" value={createTitle} onChange={(e) => setCreateTitle(e.target.value)} required />
            <label htmlFor="createContent">content</label>
            <input id="createContent" value={createContent} onChange={(e) => setCreateContent(e.target.value)} required />
            <button type="submit">create post</button>
          </form>
        </div>
      </div>

      <div className="panel-grid mt-12">
        <div className="panel">
          <h2>PATCH /posts/:id</h2>
          <form className="form-box" onSubmit={(e: FormEvent) => {
            e.preventDefault();
            run(async () => {
              await updatePost(Number(updatePostId), {
                ...(updateTitle ? { title: updateTitle } : {}),
                ...(updateContent ? { content: updateContent } : {}),
              });
              setMessage('PATCH /posts/:id success');
            });
          }}>
            <label htmlFor="updatePostId">postId</label>
            <input id="updatePostId" value={updatePostId} onChange={(e) => setUpdatePostId(e.target.value)} required />
            <label htmlFor="updateTitle">title(optional)</label>
            <input id="updateTitle" value={updateTitle} onChange={(e) => setUpdateTitle(e.target.value)} />
            <label htmlFor="updateContent">content(optional)</label>
            <input id="updateContent" value={updateContent} onChange={(e) => setUpdateContent(e.target.value)} />
            <button type="submit">update post</button>
          </form>
        </div>

        <div className="panel">
          <h2>DELETE /posts/:id & POST /posts/:id/like</h2>
          <form className="form-box" onSubmit={(e: FormEvent) => e.preventDefault()}>
            <label htmlFor="deletePostId">delete postId</label>
            <input id="deletePostId" value={deletePostId} onChange={(e) => setDeletePostId(e.target.value)} />
            <button
              type="button"
              onClick={() => run(async () => {
                await removePost(Number(deletePostId));
                setMessage('DELETE /posts/:id success');
              })}
            >
              delete post
            </button>

            <label htmlFor="likePostId">like postId</label>
            <input id="likePostId" value={likePostId} onChange={(e) => setLikePostId(e.target.value)} />
            <button
              type="button"
              onClick={() => run(async () => {
                const res = await togglePostLike(Number(likePostId));
                setMessage(`POST /posts/:id/like success (liked=${String(res.data.liked)}, likeCount=${res.data.likeCount})`);
              })}
            >
              toggle like
            </button>
          </form>
        </div>
      </div>

      <div className="panel-grid mt-12">
        <div className="panel">
          <h2>GET /posts/:id/comments & POST /posts/:id/comments</h2>
          <form className="form-box" onSubmit={(e: FormEvent) => e.preventDefault()}>
            <label htmlFor="commentPostId">postId</label>
            <input id="commentPostId" value={commentPostId} onChange={(e) => setCommentPostId(e.target.value)} required />
            <button
              type="button"
              onClick={() => run(async () => {
                const res = await fetchPostComments(Number(commentPostId));
                setComments(res.data as any[]);
                setMessage('GET /posts/:id/comments success');
              })}
            >
              load comments
            </button>

            <label htmlFor="commentContent">comment content</label>
            <input id="commentContent" value={commentContent} onChange={(e) => setCommentContent(e.target.value)} />
            <button
              type="button"
              onClick={() => run(async () => {
                await createPostComment(Number(commentPostId), { content: commentContent });
                setMessage('POST /posts/:id/comments success');
              })}
            >
              create comment
            </button>
          </form>
          <ul className="list mt-12">
            {comments.map((comment) => (
              <li key={comment.id}>
                {comment.id} / {comment.author?.nickname || comment.author?.name || '-'} / {comment.content}
              </li>
            ))}
          </ul>
        </div>

        <div className="panel">
          <h2>DELETE /comments/:id</h2>
          <form className="form-box" onSubmit={(e: FormEvent) => {
            e.preventDefault();
            run(async () => {
              await removeComment(Number(deleteCommentId));
              setMessage('DELETE /comments/:id success');
            });
          }}>
            <label htmlFor="deleteCommentId">commentId</label>
            <input id="deleteCommentId" value={deleteCommentId} onChange={(e) => setDeleteCommentId(e.target.value)} required />
            <button type="submit">delete comment</button>
          </form>
        </div>
      </div>
    </section>
  );
}
