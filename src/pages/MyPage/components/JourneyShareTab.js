// src/components/JourneyShareTab.js (수정할 부분)

import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { Link } from 'react-router-dom';
import './JourneyShareTab.css';

const API_BASE_URL = process.env.REACT_APP_BACKEND_BASE_URL || 'http://localhost:8080';

const JourneyShareTab = () => {
  const [activeSubTab, setActiveSubTab] = useState('myReviews');

  const [reviews, setReviews] = useState([]);
  const [reviewLoading, setReviewLoading] = useState(true);
  const [reviewError, setReviewError] = useState(null);
  const [reviewFilter, setReviewFilter] = useState({
    keyword: '',
    sortBy: 'createdAt',
    sortOrder: 'desc',
    page: 0,
    size: 5,
  });
  const [reviewTotalPages, setReviewTotalPages] = useState(0);

  const [comments, setComments] = useState([]);
  const [commentLoading, setCommentLoading] = useState(true);
  const [commentError, setCommentError] = useState(null);
  const [commentFilter, setCommentFilter] = useState({
    boardType: 'REVIEW',
    keyword: '',
    page: 0,
    size: 5,
  });
  const [commentTotalPages, setCommentTotalPages] = useState(0);

  const [myReviews, setMyReviews] = useState([]);
  const [myComments, setMyComments] = useState([]);

  const fetchMyReviews = async () => {
    setReviewLoading(true);
    setReviewError(null);
    try {
      const params = new URLSearchParams(reviewFilter); // reviewFilter 객체를 URL 파라미터로 변환
      const response = await axios.get(`/api/auth/me/reviews?${params.toString()}`);
      setReviews(response.data.content);
      setReviewTotalPages(response.data.totalPages);
    } catch (err) {
      console.error('🚨 내 리뷰 목록 불러오기 실패:', err.response?.data || err.message);
      setReviewError(`리뷰 목록을 불러오는데 실패했습니다: ${err.response?.data?.message || err.response?.data || err.message} 😥`);
    } finally {
      setReviewLoading(false);
    }
  };

  const fetchMyComments = async () => {
    setCommentLoading(true);
    setCommentError(null);
    try {
      const params = new URLSearchParams(commentFilter); // commentFilter 객체를 URL 파라미터로 변환
      const response = await axios.get(`/api/auth/me/comments?${params.toString()}`);
      setComments(response.data.content);
      setCommentTotalPages(response.data.totalPages);
    } catch (err) {
      console.error(`🚨 내 ${commentFilter.boardType} 댓글 목록 불러오기 실패:`, err.response?.data || err.message);
      setCommentError(`댓글 목록을 불러오는데 실패했습니다: ${err.response?.data?.message || err.response?.data || err.message} 😥`);
    } finally {
      setCommentLoading(false);
    }
  };

  useEffect(() => {
    if (activeSubTab === 'myReviews') {
      fetchMyReviews();
    } else if (activeSubTab === 'myComments') {
      fetchMyComments();
    }
  }, [activeSubTab, reviewFilter.keyword, reviewFilter.sortBy, reviewFilter.sortOrder, reviewFilter.page, reviewFilter.size,
      commentFilter.boardType, commentFilter.keyword, commentFilter.page, commentFilter.size]);

  const handleReviewFilterChange = (e) => {
    setReviewFilter({ ...reviewFilter, [e.target.name]: e.target.value, page: 0 });
  };

  const handleCommentFilterChange = (e) => {
    setCommentFilter({ ...commentFilter, [e.target.name]: e.target.value, page: 0 });
  };

  const handleReviewPageChange = (newPage) => {
    setReviewFilter({ ...reviewFilter, page: newPage });
  };

  const handleCommentPageChange = (newPage) => {
    setCommentFilter({ ...commentFilter, page: newPage });
  };

  // ⭐⭐ 핵심 추가: 페이징 그룹 계산 함수 ⭐⭐
  const getPaginationGroup = (currentPage, totalPages) => {
    const pages = [];
    const maxPageButtons = 10; // 한 번에 보여줄 최대 페이지 버튼 수
    let startPage = Math.floor(currentPage / maxPageButtons) * maxPageButtons;
    let endPage = Math.min(startPage + maxPageButtons, totalPages);

    // 마지막 그룹이 10개가 안될 경우, 시작 페이지를 조정하여 항상 10개 (또는 그 이하)를 채움
    if (endPage === totalPages && (endPage - startPage) < maxPageButtons) {
        startPage = Math.max(0, totalPages - maxPageButtons);
    }

    for (let i = startPage; i < endPage; i++) {
      pages.push(i);
    }
    return pages;
  };

  return (
    <div className="tab-content-section journey-share-tab">
      <h3 className="tab-content-title">여정 나누기 ✍️</h3>

      <div className="sub-tabs">
        <button
          className={`sub-tab-button ${activeSubTab === 'myReviews' ? 'active' : ''}`}
          onClick={() => setActiveSubTab('myReviews')}
        >
          내가 쓴 리뷰 📝
        </button>
        <button
          className={`sub-tab-button ${activeSubTab === 'myComments' ? 'active' : ''}`}
          onClick={() => setActiveSubTab('myComments')}
        >
          내가 작성한 댓글 💬
        </button>
      </div>

      {/* 내가 쓴 리뷰 목록 섹션 */}
      {activeSubTab === 'myReviews' && (
        <div className="content-panel my-reviews-panel">
          <h4>내 리뷰 목록</h4>
          <div className="filter-sort-section">
            <div className="search-group">
              <input
                type="text"
                name="keyword"
                placeholder="제목 또는 내용을 검색하세요"
                value={reviewFilter.keyword}
                onChange={handleReviewFilterChange}
                className="search-input"
              />
              <button onClick={fetchMyReviews} className="search-button">검색 🔍</button>
            </div>
            <div className="sort-group">
              <label htmlFor="reviewSortBy">정렬 기준:</label>
              <select id="reviewSortBy" name="sortBy" value={reviewFilter.sortBy} onChange={handleReviewFilterChange}>
                <option value="createdAt">작성일</option>
                <option value="viewCount">조회순</option>
              </select>
              <select name="sortOrder" value={reviewFilter.sortOrder} onChange={handleReviewFilterChange}>
                <option value="desc">최신순</option>
                <option value="asc">오래된순</option>
              </select>
            </div>
          </div>

          {reviewLoading ? (
            <p className="loading-message">리뷰를 불러오는 중입니다... ⏳</p>
          ) : reviewError ? (
            <p className="error-message">🚨 {reviewError}</p>
          ) : reviews.length === 0 ? (
            <p className="no-content-message">아직 작성한 리뷰가 없습니다. 첫 리뷰를 남겨보세요! ✨</p>
          ) : (
            <div className="list-container">
              {reviews.map(review => (
                <div key={review.reviewId} className="list-item review-item">
                  <div className="item-main-info">
                    <span className="board-tag">[리뷰]</span>
                    <Link to={`/reviews/${review.reviewId}`} className="item-title">{review.title}</Link>
                  </div>
                  <div className="item-meta-info">
                    <span className="item-date">{review.createdAt ? new Date(review.createdAt).toLocaleDateString() : '날짜 없음'}</span>
                    <span className="item-stats">
                      조회수 {review.viewCount || 0} | 별점 {review.rating || 0} ⭐
                    </span>
                  </div>
                </div>
              ))}
            </div>
          )}
          {/* 리뷰 페이징 버튼 */}
          {!reviewLoading && !reviewError && reviews.length > 0 && (
            <div className="pagination">
              {reviewTotalPages > 10 && reviewFilter.page >= 10 && ( // 이전 10개 버튼
                <button className="page-button" onClick={() => handleReviewPageChange(Math.floor(reviewFilter.page / 10) * 10 - 10)}>
                  &lt;&lt;
                </button>
              )}
              {reviewFilter.page > 0 && ( // 이전 버튼
                  <button className="page-button" onClick={() => handleReviewPageChange(reviewFilter.page - 1)}>
                    &lt;
                  </button>
              )}
              {getPaginationGroup(reviewFilter.page, reviewTotalPages).map(i => (
                <button
                  key={i}
                  className={`page-button ${reviewFilter.page === i ? 'active' : ''}`}
                  onClick={() => handleReviewPageChange(i)}
                >
                  {i + 1}
                </button>
              ))}
              {reviewFilter.page < reviewTotalPages - 1 && ( // 다음 버튼
                  <button className="page-button" onClick={() => handleReviewPageChange(reviewFilter.page + 1)}>
                    &gt;
                  </button>
              )}
              {reviewTotalPages > 10 && Math.floor(reviewFilter.page / 10) < Math.floor((reviewTotalPages - 1) / 10) && ( // 다음 10개 버튼
                <button className="page-button" onClick={() => handleReviewPageChange(Math.floor(reviewFilter.page / 10) * 10 + 10)}>
                  &gt;&gt;
                </button>
              )}
            </div>
          )}
        </div>
      )}

      {/* 내가 작성한 댓글 목록 섹션 */}
      {activeSubTab === 'myComments' && (
        <div className="content-panel my-comments-panel">
          <h4>내 댓글 목록</h4>
          <div className="filter-sort-section">
            <div className="search-group">
              <label htmlFor="commentBoardType">게시판 선택:</label>
              <select id="commentBoardType" name="boardType" value={commentFilter.boardType} onChange={handleCommentFilterChange}>
                <option value="REVIEW">리뷰 게시판</option>
                <option value="TEMPLE">사찰 정보 게시판</option>
              </select>
              <input
                type="text"
                name="keyword"
                placeholder="내용 또는 원본 게시글 제목 검색"
                value={commentFilter.keyword}
                onChange={handleCommentFilterChange}
                className="search-input"
              />
              <button onClick={fetchMyComments} className="search-button">검색 🔍</button>
            </div>
          </div>

          {commentLoading ? (
            <p className="loading-message">댓글을 불러오는 중입니다... ⏳</p>
          ) : commentError ? (
            <p className="error-message">🚨 {commentError}</p>
          ) : comments.length === 0 ? (
            <p className="no-content-message">아직 작성한 댓글이 없습니다. 소통에 참여해보세요! ✨</p>
          ) : (
            <div className="list-container">
              {comments.map(comment => (
                <div key={comment.reviewCommentId || comment.templeCommentId} className="list-item comment-item">
                  <div className="item-main-info">
                    <span className="board-tag">
                      [{comment.boardType === 'REVIEW' ? '리뷰' : (comment.boardType === 'TEMPLE' ? '사찰 정보' : '알 수 없음')}]
                    </span>
                    {comment.boardType === 'REVIEW' ? (
                      <Link to={`/reviews/${comment.reviewId}#comment-${comment.reviewCommentId}`} className="item-content">
                        {comment.content && comment.content.length > 50 ? comment.content.substring(0, 50) + '...' : comment.content}
                      </Link>
                    ) : comment.boardType === 'TEMPLE' ? (
                      <Link to={`/temples/${comment.templeId}#comment-${comment.templeCommentId}`} className="item-content">
                        {comment.content && comment.content.length > 50 ? comment.content.substring(0, 50) + '...' : comment.content}
                      </Link>
                    ) : (
                      <span className="item-content">
                        {comment.content && comment.content.length > 50 ? comment.content.substring(0, 50) + '...' : comment.content}
                      </span>
                    )}
                  </div>
                  <div className="item-meta-info">
                    <span className="item-date">{comment.createdAt ? new Date(comment.createdAt).toLocaleDateString() : '날짜 없음'}</span>
                    <span className="item-stats">
                      좋아요 {comment.likeCount || 0} | 조회수 {comment.viewCount || 0}
                      {comment.boardType === 'TEMPLE' && comment.templeName && ` | 원본 사찰: ${comment.templeName}`}
                      {comment.boardType === 'REVIEW' && comment.reviewTitle && ` | 원본 리뷰: ${comment.reviewTitle}`}
                    </span>
                  </div>
                </div>
              ))}
            </div>
          )}
          {/* 댓글 페이징 버튼 */}
          {!commentLoading && !commentError && comments.length > 0 && (
            <div className="pagination">
              {commentTotalPages > 10 && commentFilter.page >= 10 && ( // 이전 10개 버튼
                <button className="page-button" onClick={() => handleCommentPageChange(Math.floor(commentFilter.page / 10) * 10 - 10)}>
                  &lt;&lt;
                </button>
              )}
              {commentFilter.page > 0 && ( // 이전 버튼
                  <button className="page-button" onClick={() => handleCommentPageChange(commentFilter.page - 1)}>
                    &lt;
                  </button>
              )}
              {getPaginationGroup(commentFilter.page, commentTotalPages).map(i => (
                <button
                  key={i}
                  className={`page-button ${commentFilter.page === i ? 'active' : ''}`}
                  onClick={() => handleCommentPageChange(i)}
                >
                  {i + 1}
                </button>
              ))}
              {commentFilter.page < commentTotalPages - 1 && ( // 다음 버튼
                  <button className="page-button" onClick={() => handleCommentPageChange(commentFilter.page + 1)}>
                    &gt;
                  </button>
              )}
              {commentTotalPages > 10 && Math.floor(commentFilter.page / 10) < Math.floor((commentTotalPages - 1) / 10) && ( // 다음 10개 버튼
                <button className="page-button" onClick={() => handleCommentPageChange(Math.floor(commentFilter.page / 10) * 10 + 10)}>
                  &gt;&gt;
                </button>
              )}
            </div>
          )}
        </div>
      )}
    </div>
  );
};

export default JourneyShareTab;