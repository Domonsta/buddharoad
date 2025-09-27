import React, { useState, useEffect } from 'react';
import axios from 'axios';

const ContentManagementTab = () => {
  // ⭐ 드롭다운을 위한 새로운 상태 변수들
  const [selectedBoardCategory, setSelectedBoardCategory] = useState(''); // '전체', '사찰정보', '리뷰'
  const [selectedContentType, setSelectedContentType] = useState(''); // '전체', '게시글', '댓글'

  const [searchKeyword, setSearchKeyword] = useState('');
  const [searchNickname, setSearchNickname] = useState('');
  const [inactivePosts, setInactivePosts] = useState([]);
  const [inactiveComments, setInactiveComments] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [currentBoardType, setCurrentBoardType] = useState(''); // 현재 조회 중인 (백엔드) 게시판 타입

  // 검색창 placeholder 동적 변경
  const getSearchPlaceholder = () => {
    if (selectedContentType === '댓글') {
      return '검색 키워드 (댓글 내용)';
    }
    return '검색 키워드 (제목/내용)';
  };

  // ⭐ 선택된 드롭다운 값들을 조합하여 백엔드 boardType 결정 함수
  const getBackendBoardType = (category, type) => {
    if (category === '전체') {
      if (type === '전체') return 'ALL'; // 백엔드에서 모든 타입 조회 (현재 AdminController에 ALL 타입 처리 없음, 필요시 추가)
      if (type === '게시글') return 'ALL_POSTS'; // 백엔드에서 모든 게시글 타입 조회 (필요시 추가)
      if (type === '댓글') return 'ALL_COMMENTS'; // 백엔드에서 모든 댓글 타입 조회 (필요시 추가)
    } else if (category === '사찰정보') {
      if (type === '전체' || type === '게시글') return 'TEMPLE';
      if (type === '댓글') return 'TEMPLE_COMMENT';
    } else if (category === '리뷰') {
      if (type === '전체' || type === '게시글') return 'REVIEW';
      if (type === '댓글') return 'REVIEW_COMMENT';
    }
    return ''; // 유효하지 않은 조합
  };

  // API 요청 함수
  const fetchInactiveContents = async (boardCategory, contentType, keyword = '', nickname = '', page = 0) => {
    setLoading(true);
    setError(null);

    const backendBoardType = getBackendBoardType(boardCategory, contentType);
    if (!backendBoardType) {
      setError('유효하지 않은 게시판/콘텐츠 유형 조합입니다.');
      setLoading(false);
      setInactivePosts([]);
      setInactiveComments([]);
      setTotalPages(0);
      return;
    }

    // ⭐ 현재 백엔드 AdminController는 'ALL', 'ALL_POSTS', 'ALL_COMMENTS'를 직접 처리하지 않음.
    // 각 단일 boardType으로만 요청을 보내도록 임시 처리.
    // 필요 시 AdminController의 switch 문에 'ALL' 등의 case를 추가하고
    // 여러 종류의 Page<?>를 담을 수 있는 응답 DTO를 설계해야 함.
    let actualBackendRequestType = backendBoardType;
    if (backendBoardType === 'ALL' || backendBoardType === 'ALL_POSTS' || backendBoardType === 'ALL_COMMENTS') {
        // 현재는 'ALL' 요청 시 기본값 (예: REVIEW)으로 대체하여 요청하거나,
        // 아니면 이 부분을 프론트에서 여러 번 호출하도록 로직을 바꿔야 함.
        // 여기서는 복잡성을 줄이기 위해 일단 가장 흔한 'REVIEW'로 대체
        // 실제 운영에서는 ALL 타입 요청에 대한 백엔드 처리가 필요!
        console.warn("백엔드 AdminController가 'ALL', 'ALL_POSTS', 'ALL_COMMENTS' 요청을 직접 처리하지 않습니다. 기본값으로 'REVIEW'를 사용하여 첫 번째 요청을 보냅니다.");
        actualBackendRequestType = 'REVIEW';
    }
    setCurrentBoardType(actualBackendRequestType); // 현재 조회 중인 (단일) 게시판 타입 저장

    try {
      const token = localStorage.getItem('accessToken');
      if (!token) {
        alert('로그인이 필요합니다.');
        setLoading(false);
        return;
      }

      const params = {
        boardType: actualBackendRequestType,
        keyword: keyword,
        memberUsername: nickname,
        page: page,
        size: 10,
        sortBy: 'createdAt',
        sortOrder: 'desc',
      };

      const response = await axios.get('http://localhost:8080/api/admin/contents/inactive', {
        headers: {
          Authorization: `Bearer ${token}`,
        },
        params: params,
      });

      console.log('API 응답:', response.data);

      // ⭐ 응답 데이터 처리 로직 수정: 실제 요청한 타입에 맞춰 데이터 설정
      if (actualBackendRequestType === 'TEMPLE') {
        setInactivePosts(response.data.temples?.content || []);
        setTotalPages(response.data.temples?.totalPages || 0);
        setInactiveComments([]);
      } else if (actualBackendRequestType === 'REVIEW') {
        setInactivePosts(response.data.reviews?.content || []);
        setTotalPages(response.data.reviews?.totalPages || 0);
        setInactiveComments([]);
      } else if (actualBackendRequestType === 'TEMPLE_COMMENT') {
        setInactiveComments(response.data.templeComments?.content || []);
        setTotalPages(response.data.templeComments?.totalPages || 0);
        setInactivePosts([]);
      } else if (actualBackendRequestType === 'REVIEW_COMMENT') {
        setInactiveComments(response.data.reviewComments?.content || []);
        setTotalPages(response.data.reviewComments?.totalPages || 0);
        setInactivePosts([]);
      } else {
        // 'ALL' 등으로 요청했을 때 여러 타입의 데이터가 오면 여기서 통합 처리 필요.
        // 현재는 단일 타입으로 요청하고 있으므로 이 else 블록은 거의 실행되지 않음.
        setInactivePosts([]);
        setInactiveComments([]);
        setTotalPages(0);
      }
      setCurrentPage(page);

    } catch (err) {
      console.error('비활성화 콘텐츠 조회 실패:', err);
      setError('콘텐츠를 불러오는 데 실패했습니다: ' + (err.response?.data || err.message));
      setInactivePosts([]);
      setInactiveComments([]);
      setTotalPages(0);
    } finally {
      setLoading(false);
    }
  };

  // 컴포넌트 마운트 시 초기 데이터 로드 (기본값: 전체 게시판, 게시글)
  useEffect(() => {
    setSelectedBoardCategory('전체');
    setSelectedContentType('게시글');
    fetchInactiveContents('전체', '게시글'); // 초기 로드 시 '전체' 게시판의 '게시글' 목록을 가져옴
  }, []);

  const handleSearch = (e) => {
    e.preventDefault();
    fetchInactiveContents(selectedBoardCategory, selectedContentType, searchKeyword, searchNickname, 0);
  };

  // 게시글 상세 보기로 이동
  const handleViewPostDetails = (boardType, postId) => {
    if (boardType === 'TEMPLE') {
      window.location.href = `/temples/${postId}`;
    } else if (boardType === 'REVIEW') {
      window.location.href = `/reviews/${postId}`;
    }
  };

  // 댓글 상세 보기로 이동 (댓글 자체의 상세 페이지가 없다면 부모 게시글로 이동)
  const handleViewCommentDetails = (boardType, commentId, parentId) => {
    if (boardType === 'TEMPLE_COMMENT') {
      alert(`사찰 댓글 ${commentId} 상세 보기 (사찰 ID: ${parentId}) - 상세 페이지는 구현 필요`);
    } else if (boardType === 'REVIEW_COMMENT') {
      alert(`리뷰 댓글 ${commentId} 상세 보기 (리뷰 ID: ${parentId}) - 상세 페이지는 구현 필요`);
    }
  };

  // 게시글 활성화/삭제
  const handlePostAction = async (post, actionType) => {
    const postId = post.reviewId || post.templeId;
    const boardType = post.boardType;
    const contentName = post.title || post.templeName;

    const token = localStorage.getItem('accessToken');
    if (!token) {
      alert('로그인이 필요합니다.');
      return;
    }

    try {
      if (actionType === '활성화') {
        if (!window.confirm(`게시글 "${contentName}"을 활성화하시겠습니까?`)) return;
        if (boardType === 'TEMPLE') {
          await axios.patch(`http://localhost:8080/api/temples/${postId}/status`, null, {
            params: { isActive: true },
            headers: { Authorization: `Bearer ${token}` },
          });
        } else if (boardType === 'REVIEW') {
          await axios.patch(`http://localhost:8080/api/reviews/${postId}/status`, null, {
            params: { isActive: true },
            headers: { Authorization: `Bearer ${token}` },
          });
        }
        alert(`게시글 "${contentName}"을 활성화했습니다.`);
      } else if (actionType === '삭제') {
        if (!window.confirm(`게시글 "${contentName}"을 정말 삭제하시겠습니까? (소프트 삭제)`)) return;
        if (boardType === 'TEMPLE') {
          await axios.delete(`http://localhost:8080/api/temples/${postId}`, {
            headers: { Authorization: `Bearer ${token}` },
          });
        } else if (boardType === 'REVIEW') {
          await axios.patch(`http://localhost:8080/api/reviews/${postId}/soft-delete`, null, {
            headers: { Authorization: `Bearer ${token}` },
          });
        }
        alert(`게시글 "${contentName}"을 삭제(비활성화)했습니다.`);
      }
      // 액션 후 현재 선택된 드롭다운 값으로 목록 새로고침
      fetchInactiveContents(selectedBoardCategory, selectedContentType, searchKeyword, searchNickname, currentPage);

    } catch (err) {
      console.error(`${actionType} 액션 실패:`, err.response ? err.response.data : err.message);
      alert(`${actionType} 실패: ` + (err.response ? err.response.data.message || err.response.data : err.message));
    }
  };

  // 댓글 활성화/삭제
  const handleCommentAction = async (comment, actionType) => {
    const commentId = comment.reviewCommentId || comment.templeCommentId;
    const boardType = comment.boardType;
    const parentId = comment.reviewId || comment.templeId;
    const contentText = comment.content;

    const token = localStorage.getItem('accessToken');
    if (!token) {
      alert('로그인이 필요합니다.');
      return;
    }

    try {
      if (actionType === '활성화') {
        alert(`댓글 "${contentText}" 활성화 기능은 백엔드 API에 직접 구현되어 있지 않습니다. (isDeleted=false API 필요)`);
      } else if (actionType === '삭제') {
        if (!window.confirm(`댓글 "${contentText}"을 정말 삭제하시겠습니까? (소프트 삭제)`)) return;
        if (boardType === 'TEMPLE_COMMENT') {
          await axios.delete(`http://localhost:8080/api/temples/${parentId}/comments/${commentId}`, {
            headers: { Authorization: `Bearer ${token}` },
          });
        } else if (boardType === 'REVIEW_COMMENT') {
          await axios.delete(`http://localhost:8080/api/reviews/${parentId}/comments/${commentId}`, {
            headers: { Authorization: `Bearer ${token}` },
          });
        }
        alert(`댓글 "${contentText}"을 삭제(비활성화)했습니다.`);
      }
      // 액션 후 현재 선택된 드롭다운 값으로 목록 새로고침
      fetchInactiveContents(selectedBoardCategory, selectedContentType, searchKeyword, searchNickname, currentPage);

    } catch (err) {
      console.error(`${actionType} 액션 실패:`, err.response ? err.response.data : err.message);
      alert(`${actionType} 실패: ` + (err.response ? err.response.data.message || err.response.data : err.message));
    }
  };

  // 페이지네이션 렌더링
  const renderPagination = () => {
    const pages = [];
    for (let i = 0; i < totalPages; i++) {
      pages.push(
        <button
          key={i}
          className={`page-button ${i === currentPage ? 'active' : ''}`}
          onClick={() => fetchInactiveContents(selectedBoardCategory, selectedContentType, searchKeyword, searchNickname, i)}
          disabled={loading}
        >
          {i + 1}
        </button>
      );
    }
    return <div className="pagination">{pages}</div>;
  };

  return (
    <div className="tab-content-section content-management-tab">
      <h3 className="tab-content-title">콘텐츠 관리 🛡️</h3>
      <form onSubmit={handleSearch} className="search-form">
        {/* ⭐ 첫 번째 드롭다운: 게시판 카테고리 */}
        <select value={selectedBoardCategory} onChange={(e) => setSelectedBoardCategory(e.target.value)}>
          <option value="">게시판 선택</option> {/* 기본값 */}
          <option value="전체">전체 게시판</option>
          <option value="사찰정보">사찰정보</option>
          <option value="리뷰">리뷰</option>
        </select>

        {/* ⭐ 두 번째 드롭다운: 콘텐츠 유형 */}
        <select value={selectedContentType} onChange={(e) => setSelectedContentType(e.target.value)}>
          <option value="">유형 선택</option> {/* 기본값 */}
          <option value="전체">전체 유형</option>
          <option value="게시글">게시글</option>
          <option value="댓글">댓글</option>
        </select>
        
        <input
          type="text"
          placeholder={getSearchPlaceholder()}
          value={searchKeyword}
          onChange={(e) => setSearchKeyword(e.target.value)}
        />
        <input
          type="text"
          placeholder="작성자 닉네임"
          value={searchNickname}
          onChange={(e) => setSearchNickname(e.target.value)}
        />
        <button type="submit" className="search-button" disabled={loading}>검색</button>
      </form>

      {loading && <p>데이터를 불러오는 중입니다... ⏳</p>}
      {error && <p className="error-message">오류: {error}</p>}

      {/* 게시글 목록 */}
      {/* ⭐ 게시글 또는 전체 유형 선택 시에만 게시글 목록 표시 */}
      { (selectedContentType === '게시글' || selectedContentType === '전체') &&
        (selectedBoardCategory === '전체' || selectedBoardCategory === '사찰정보' || selectedBoardCategory === '리뷰') && (
        <>
          <h4 style={{ marginBottom: '15px' }}>비활성화 게시글 목록 ({inactivePosts.length}개)</h4>
          <div className="admin-table-container">
            <table className="admin-data-table">
              <thead>
                <tr>
                  <th>게시판</th>
                  <th>제목</th>
                  <th>작성자</th>
                  <th>상태</th>
                  <th>관리</th>
                </tr>
              </thead>
              <tbody>
                {inactivePosts.length > 0 ? (
                  inactivePosts.map(post => (
                    <tr key={post.reviewId || post.templeId}>
                      <td>
                        <span className={`board-type-label ${post.boardType === 'TEMPLE' ? 'temple-board' : 'review-board'}`}>
                          {post.boardType === 'TEMPLE' ? '사찰정보' : '리뷰'}
                        </span>
                      </td>
                      <td>
                        <a href="#" onClick={(e) => { e.preventDefault(); handleViewPostDetails(post.boardType, post.reviewId || post.templeId); }}>
                          {post.title || post.templeName}
                        </a>
                      </td>
                      <td>{post.memberUsername || 'N/A'}</td>
                      <td>
                        <span className={post.isDeleted ? 'status-deleted' : (post.isActive ? 'status-active' : 'status-inactive')}>
                          {post.isDeleted ? '삭제됨' : (post.isActive ? '활성' : '비활성')}
                        </span>
                      </td>
                      <td>
                        <div className="action-buttons-group">
                          <button
                            className="action-button small-action-button"
                            onClick={() => handlePostAction(post, '활성화')}
                            disabled={post.isActive === true || post.isDeleted === false}
                          >
                            활성화
                          </button>
                          <button
                            className="action-button delete-button small-action-button"
                            onClick={() => handlePostAction(post, '삭제')}
                            disabled={post.isDeleted === true}
                          >
                            삭제
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))
                ) : (
                  <tr>
                    <td colSpan="5" className="no-data-message">비활성화된 게시글이 없어요. ✨</td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </>
      )}

      <hr style={{ margin: '40px 0', borderColor: '#eee' }} />

      {/* 댓글 목록 */}
      {/* ⭐ 댓글 또는 전체 유형 선택 시에만 댓글 목록 표시 */}
      { (selectedContentType === '댓글' || selectedContentType === '전체') &&
        (selectedBoardCategory === '전체' || selectedBoardCategory === '사찰정보' || selectedBoardCategory === '리뷰') && (
        <>
          <h4 style={{ marginTop: '40px', marginBottom: '15px' }}>비활성화 댓글 목록 ({inactiveComments.length}개)</h4>
          <div className="admin-table-container">
            <table className="admin-data-table">
              <thead>
                <tr>
                  <th>게시판</th>
                  <th>내용</th>
                  <th>작성자</th>
                  <th>상태</th>
                  <th>관리</th>
                </tr>
              </thead>
              <tbody>
                {inactiveComments.length > 0 ? (
                  inactiveComments.map(comment => (
                    <tr key={comment.reviewCommentId || comment.templeCommentId}>
                      <td>
                        <span className={`board-type-label ${comment.boardType === 'TEMPLE' ? 'temple-board' : 'review-board'}`}>
                          {comment.boardType === 'TEMPLE' ? '사찰정보' : '리뷰'}
                        </span>
                      </td>
                      <td>
                        <a href="#" onClick={(e) => { e.preventDefault(); handleViewCommentDetails(comment.boardType, comment.reviewCommentId || comment.templeCommentId, comment.reviewId || comment.templeId); }}>
                          {comment.content}
                        </a>
                      </td>
                      <td>{comment.memberUsername || 'N/A'}</td>
                      <td>
                        <span className={comment.isDeleted ? 'status-deleted' : (comment.isActive ? 'status-active' : 'status-inactive')}>
                          {comment.isDeleted ? '삭제됨' : (comment.isActive ? '활성' : '비활성')}
                        </span>
                      </td>
                      <td>
                        <div className="action-buttons-group">
                          {/* 댓글 활성화 버튼은 백엔드 API에 따라 주석 해제 가능 */}
                          {/*
                          <button
                            className="action-button small-action-button"
                            onClick={() => handleCommentAction(comment, '활성화')}
                            disabled={comment.isDeleted === false}
                          >
                            활성화
                          </button>
                          */}
                          <button
                            className="action-button delete-button small-action-button"
                            onClick={() => handleCommentAction(comment, '삭제')}
                            disabled={comment.isDeleted === true}
                          >
                            삭제
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))
                ) : (
                  <tr>
                    <td colSpan="5" className="no-data-message">비활성화된 댓글이 없어요. ✨</td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </>
      )}

      {/* 페이지네이션 버튼 */}
      {totalPages > 1 && renderPagination()}
    </div>
  );
};

export default ContentManagementTab;