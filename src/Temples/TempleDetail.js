// src/Temples/TempleDetail.js

import React, { useState, useEffect, useRef, useCallback } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import axios from 'axios';
import { useAuth } from '../Context/AuthContext';
import { FaBookmark, FaRegBookmark, FaHeart, FaRegHeart } from 'react-icons/fa';

// 💡 커스텀 모달 컴포넌트 (alert/confirm 대체)
const CustomModal = ({ message, onConfirm, onCancel, showCancel = false }) => {
  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div className="bg-white p-6 rounded-lg shadow-xl max-w-sm w-full text-center">
        <p className="text-lg mb-6">{message}</p>
        <div className="flex justify-center space-x-4">
          <button
            onClick={onConfirm}
            className="px-5 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition"
          >
            확인
          </button>
          {showCancel && (
            <button
              onClick={onCancel}
              className="px-5 py-2 bg-gray-300 text-gray-800 rounded-md hover:bg-gray-400 transition"
            >
              취소
            </button>
          )}
        </div>
      </div>
    </div>
  );
};

function TempleDetail() {
  const { templeId } = useParams();
  const navigate = useNavigate();
  const { isLoggedIn, username, hasAdminPermission, memberNo: loggedInMemberNo, accessToken, logout } = useAuth();
  const [temple, setTemple] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const [showImageModal, setShowImageModal] = useState(false);
  const [modalImageUrl, setModalImageUrl] = useState('');
  const [modalImageDescription, setModalImageDescription] = useState('');

  const [comments, setComments] = useState([]);
  const [commentContent, setCommentContent] = useState('');
  const [commentLoading, setCommentLoading] = useState(false);
  const [commentError, setCommentError] = useState(null);
  const [totalComments, setTotalComments] = useState(0);
  const [editingCommentId, setEditingCommentId] = useState(null);
  const [editingCommentContent, setEditingCommentContent] = useState('');

  const [refreshCommentsTrigger, setRefreshCommentsTrigger] = useState(0);

  const [searchFilter, setSearchFilter] = useState({
    content: '',
    sortBy: 'createdAt',
    sortOrder: 'desc',
    page: 0,
    size: 5,
  });
  const [totalPages, setTotalPages] = useState(0);

  const [modal, setModal] = useState({
    show: false,
    message: '',
    onConfirm: () => {},
    onCancel: () => {},
    showCancel: false,
  });

  const [isBookmarked, setIsBookmarked] = useState(false);
  const [isLiked, setIsLiked] = useState(false);
  const [loginRequiredModal, setLoginRequiredModal] = useState(false);
  // ✨✨✨ 댓글 좋아요 상태 관리 ✨✨✨
  const [commentLikeMap, setCommentLikeMap] = useState({});

  const BACKEND_BASE_URL = "http://localhost:8080";

  const regionMap = {
    'SEOUL': '서울', 'BUSAN': '부산', 'DAEGU': '대구', 'INCHEON': '인천', 'GWANGJU': '광주',
    'DAEJEON': '대전', 'ULSAN': '울산', 'SEJONG': '세종', 'GYEONGGI': '경기', 'GANGWON': '강원',
    'CHUNGCHEONGBUK': '충북', 'CHUNGCHEONGNAM': '충남',
    'JEOLLABUK': '전북', 'JEOLLANAM': '전남',
    'GYEONGSANGBUK': '경북', 'GYEONGSANGNAM': '경남', 'JEJU': '제주', 'ETC': '기타'
  };

  const hasFetchedTemple = useRef(false);

  const handleImageClick = (photoUrl, description) => {
    setModalImageUrl(`${BACKEND_BASE_URL}${photoUrl}`);
    setModalImageDescription(description || '');
    setShowImageModal(true);
  };

  const handleCloseImageModal = () => {
    setShowImageModal(false);
    setModalImageUrl('');
    setModalImageDescription('');
  };

  const showCustomModal = (message, onConfirm, showCancel = false, onCancel = () => {}) => {
    setModal({ show: true, message, onConfirm, onCancel, showCancel });
  };

  const hideCustomModal = () => {
    setModal({ show: false, message: '', onConfirm: () => {}, onCancel: () => {}, showCancel: false });
  };

  const checkBookmarkStatus = useCallback(async () => {
    if (!isLoggedIn) {
      setIsBookmarked(false);
      return;
    }
    const token = accessToken;
    try {
      const response = await axios.get(
        `${BACKEND_BASE_URL}/api/temples/${templeId}/bookmarks/status`,
        { headers: { Authorization: `Bearer ${token}` } }
      );
      setIsBookmarked(response.data.bookmarked);
    } catch (error) {
      console.error('찜하기 상태 확인 실패:', error);
      setIsBookmarked(false);
    }
  }, [isLoggedIn, accessToken, templeId, BACKEND_BASE_URL]);

  const checkLikeStatus = useCallback(async () => {
    if (!isLoggedIn) {
      setIsLiked(false);
      return;
    }
    const token = accessToken;
    try {
      const response = await axios.get(
        `${BACKEND_BASE_URL}/api/temples/${templeId}/likes/status`,
        { headers: { Authorization: `Bearer ${token}` } }
      );
      setIsLiked(response.data.isLiked);
    } catch (error) {
      console.error('좋아요 상태 확인 실패:', error);
      setIsLiked(false);
    }
  }, [isLoggedIn, accessToken, templeId, BACKEND_BASE_URL]);
  
  // ✨✨✨ 댓글 좋아요 상태 확인 함수 ✨✨✨
  const checkAllCommentLikes = useCallback(async (commentList) => {
    if (!isLoggedIn || !accessToken || commentList.length === 0) {
      setCommentLikeMap({});
      return;
    }
    const newLikeMap = {};
    const token = accessToken;

    const likeChecks = commentList.map(comment =>
      axios.get(`${BACKEND_BASE_URL}/api/temples/comments/${comment.templeCommentId}/likes/status`, {
        headers: { Authorization: `Bearer ${token}` }
      })
      .then(response => {
        return {
          commentId: comment.templeCommentId,
          isLiked: response.data.isLiked
        };
      })
      .catch(error => {
        console.error(`❌ 댓글 ${comment.templeCommentId} 좋아요 상태 확인 실패:`, error.response?.status, error.message);
        if (error.response?.status === 401 || error.response?.status === 403) {
            logout();
        }
        return { commentId: comment.templeCommentId, isLiked: false };
      })
    );
    const results = await Promise.all(likeChecks);
    results.forEach(result => {
      newLikeMap[result.commentId] = result.isLiked;
    });
    setCommentLikeMap(newLikeMap);
  }, [isLoggedIn, accessToken, logout, BACKEND_BASE_URL]);

  useEffect(() => {
    if (hasFetchedTemple.current) {
      return;
    }

    hasFetchedTemple.current = true;
    console.log(`[${Date.now()}] 🌟 hasFetchedTemple.current를 true로 설정했습니다 (useEffect 시작 시).`);

    const fetchTempleDetails = async () => {
      try {
        setLoading(true);
        const apiUrl = `${BACKEND_BASE_URL}/api/temples/${templeId}`;
        const response = await axios.get(apiUrl);
        setTemple(response.data);
        console.log(`[${Date.now()}] 🎉 사찰 상세 정보 가져오기 성공:`, response.data);

        checkBookmarkStatus();
        checkLikeStatus();

      } catch (err) {
        console.error(`[${Date.now()}] ❌ 사찰 상세 정보 가져오기 실패:`, err);
        setError("사찰 상세 정보를 가져오는 데 실패했습니다.");
      } finally {
        setLoading(false);
      }
    };

    fetchTempleDetails();

    return () => {
      hasFetchedTemple.current = false;
    };
  }, [templeId, isLoggedIn, accessToken, checkBookmarkStatus, checkLikeStatus, BACKEND_BASE_URL]);

  const fetchComments = useCallback(async () => {
      setCommentLoading(true);
      setCommentError(null);
      try {
        const token = accessToken;
        const headers = token ? { Authorization: `Bearer ${token}` } : {};

        const params = new URLSearchParams(searchFilter);
        const apiUrl = `${BACKEND_BASE_URL}/api/temples/${templeId}/comments?${params.toString()}`;
        const response = await axios.get(apiUrl, { headers });

        setComments(response.data.content);
        setTotalComments(response.data.totalElements);
        setTotalPages(response.data.totalPages);

        // ✨✨✨ 댓글 목록 로드 후 좋아요 상태 확인 ✨✨✨
        checkAllCommentLikes(response.data.content);

      } catch (err) {
        setCommentError("댓글 목록을 가져오는 데 실패했습니다.");
      } finally {
        setCommentLoading(false);
      }
  }, [templeId, searchFilter, accessToken, refreshCommentsTrigger, checkAllCommentLikes, BACKEND_BASE_URL]);

  useEffect(() => {
    if (templeId) {
      fetchComments();
    }
  }, [templeId, searchFilter, fetchComments]);


  const handleToggleBookmark = async () => {
    if (!isLoggedIn) {
      showCustomModal('로그인 후 찜하기 기능을 이용할 수 있습니다.', () => { hideCustomModal(); navigate('/login'); });
      return;
    }
    const token = accessToken;
    try {
      const response = await axios.post(
        `${BACKEND_BASE_URL}/api/temples/${templeId}/bookmarks`,
        null,
        { headers: { Authorization: `Bearer ${token}` } }
      );
      setIsBookmarked(response.data.bookmarked);
    } catch (error) {
      console.error('찜하기 토글 실패:', error);
      showCustomModal('찜하기 기능에 문제가 발생했습니다.', hideCustomModal);
    }
  };

  const handleToggleLike = async () => {
    if (!isLoggedIn) {
      showCustomModal('로그인 후 좋아요 기능을 이용할 수 있습니다.', () => { hideCustomModal(); navigate('/login'); });
      return;
    }
    const token = accessToken;
    try {
      const response = await axios.post(
        `${BACKEND_BASE_URL}/api/temples/${templeId}/likes`,
        null,
        { headers: { Authorization: `Bearer ${token}` } }
      );
      setIsLiked(response.data.isLiked);
    } catch (error) {
      console.error('좋아요 토글 실패:', error);
      showCustomModal('좋아요 기능에 문제가 발생했습니다.', hideCustomModal);
    }
  };

  // ✨✨✨ 댓글 좋아요 토글 핸들러 ✨✨✨
  const handleToggleCommentLike = async (commentId) => {
    if (!isLoggedIn) {
      showCustomModal('로그인 후 댓글 좋아요 기능을 이용할 수 있습니다.', () => { hideCustomModal(); navigate('/login'); });
      return;
    }
    const token = accessToken;
    try {
      const response = await axios.post(
        `${BACKEND_BASE_URL}/api/temples/comments/${commentId}/likes`,
        null,
        { headers: { Authorization: `Bearer ${token}` } }
      );
      setCommentLikeMap(prevMap => ({
        ...prevMap,
        [commentId]: response.data.isLiked
      }));
      // 좋아요 개수 즉시 업데이트 로직 추가
      // (서버에서 좋아요 개수도 함께 응답해주면 좋지만, 프론트에서 임시로 처리)
      setComments(prevComments => prevComments.map(comment => {
        if (comment.templeCommentId === commentId) {
          return {
            ...comment,
            likeCount: response.data.isLiked ? comment.likeCount + 1 : comment.likeCount - 1
          };
        }
        return comment;
      }));

    } catch (error) {
      console.error('댓글 좋아요 토글 실패:', error);
      showCustomModal('댓글 좋아요 기능에 문제가 발생했습니다.', hideCustomModal);
    }
  };


  const handleDeleteTemple = () => {
    showCustomModal('정말로 이 사찰을 삭제(비활성화)하시겠습니까?', async () => {
      hideCustomModal();
      const token = accessToken;
      if (!token) {
        showCustomModal('로그인이 필요합니다.', () => { hideCustomModal(); navigate('/login'); });
        return;
      }
      try {
        await axios.delete(`${BACKEND_BASE_URL}/api/temples/${templeId}`, {
          headers: { Authorization: `Bearer ${token}` }
        });
        showCustomModal('✅ 사찰이 성공적으로 삭제(비활성화)되었습니다.', () => { hideCustomModal(); navigate('/temples'); });
      } catch (err) {
        console.error("❌ 사찰 삭제 실패:", err);
        showCustomModal(`❌ 사찰 삭제에 실패했습니다: ${err.response?.data || err.message}`, hideCustomModal);
      }
    }, true, hideCustomModal);
  };

  const handleCommentSubmit = async (e) => {
    e.preventDefault();

    if (!commentContent.trim()) {
      showCustomModal('댓글 내용을 입력해주세요.', hideCustomModal);
      return;
    }
    if (!isLoggedIn) {
      setLoginRequiredModal(true);
      return;
    }

    const tokenToSend = accessToken || '';
    if (!tokenToSend) {
      showCustomModal('로그인 정보가 유효하지 않습니다. 다시 로그인해주세요.', () => {
        hideCustomModal();
        logout();
        navigate('/login');
      });
      return;
    }

    setCommentLoading(true);
    setCommentError(null);
    try {
      await axios.post(
        `${BACKEND_BASE_URL}/api/temples/${templeId}/comments`,
        { content: commentContent, templeId: parseInt(templeId) },
        { headers: { Authorization: `Bearer ${tokenToSend}` } }
      );
      setCommentContent('');
      setRefreshCommentsTrigger(prev => prev + 1);
      setSearchFilter(prev => ({ ...prev, page: 0 }));
      showCustomModal('✅ 댓글이 성공적으로 작성되었습니다!', hideCustomModal);
    } catch (err) {
      console.error("❌ 댓글 작성 실패:", err);
      const errorMessage = err.response?.data?.message
        || (err.response?.data ? JSON.stringify(err.response.data) : err.message);
      setCommentError(`댓글 작성에 실패했습니다: ${errorMessage}`);
      showCustomModal(`❌ 댓글 작성에 실패했습니다: ${errorMessage}`, hideCustomModal);
    } finally {
      setCommentLoading(false);
    }
  };

  const handleEditClick = (comment) => {
    setEditingCommentId(comment.templeCommentId);
    setEditingCommentContent(comment.content);
  };

  const handleCancelEdit = () => {
    setEditingCommentId(null);
    setEditingCommentContent('');
  };

  const handleUpdateComment = async (commentId) => {
    if (!editingCommentContent.trim()) {
      showCustomModal('댓글 내용을 입력해주세요.', hideCustomModal);
      return;
    }
    if (!isLoggedIn) {
      setLoginRequiredModal(true);
      return;
    }
    const tokenToSend = accessToken || '';
    if (!tokenToSend) {
      showCustomModal('로그인 정보가 유효하지 않습니다. 다시 로그인해주세요.', () => {
        hideCustomModal();
        logout();
        navigate('/login');
      });
      return;
    }

    setCommentLoading(true);
    setCommentError(null);
    try {
      await axios.put(
        `${BACKEND_BASE_URL}/api/temples/${templeId}/comments/${commentId}`,
        { templeCommentId: commentId, content: editingCommentContent, templeId: parseInt(templeId) },
        { headers: { Authorization: `Bearer ${tokenToSend}` } }
      );
      setEditingCommentId(null);
      setEditingCommentContent('');
      setRefreshCommentsTrigger(prev => prev + 1);
      showCustomModal('✅ 댓글이 성공적으로 수정되었습니다!', hideCustomModal);
    } catch (err) {
      console.error("❌ 댓글 수정 실패:", err);
      const errorMessage = err.response?.data?.message
        || (err.response?.data ? JSON.stringify(err.response.data) : err.message);
      setCommentError(`댓글 수정에 실패했습니다: ${errorMessage}`);
      showCustomModal(`❌ 댓글 수정에 실패했습니다: ${errorMessage}`, hideCustomModal);
    } finally {
      setCommentLoading(false);
    }
  };

  const handleDeleteComment = (commentId, commentAuthorMemberNo) => {
    showCustomModal('정말로 이 댓글을 삭제하시겠습니까?', async () => {
      hideCustomModal();
      if (!isLoggedIn) {
        setLoginRequiredModal(true);
        return;
      }
      const tokenToSend = accessToken || '';
      if (!tokenToSend) {
        showCustomModal('로그인 정보가 유효하지 않습니다. 다시 로그인해주세요.', () => {
          hideCustomModal();
          logout();
          navigate('/login');
        });
        return;
      }

      if (loggedInMemberNo !== commentAuthorMemberNo && !hasAdminPermission()) {
        showCustomModal('댓글을 삭제할 권한이 없습니다.', hideCustomModal);
        return;
      }

      setCommentLoading(true);
    setCommentError(null);
      try {
        await axios.delete(
          `${BACKEND_BASE_URL}/api/temples/${templeId}/comments/${commentId}`,
          {
            headers: { Authorization: `Bearer ${tokenToSend}` },
          }
        );
        setRefreshCommentsTrigger(prev => prev + 1);
        showCustomModal('✅ 댓글이 성공적으로 삭제되었습니다!', hideCustomModal);
      } catch (err) {
        console.error("❌ 댓글 삭제 실패:", err);
        const errorMessage = err.response?.data?.message
          || (err.response?.data ? JSON.stringify(err.response.data) : err.message);
        setCommentError(`댓글 삭제에 실패했습니다: ${errorMessage}`);
        showCustomModal(`❌ 댓글 삭제에 실패했습니다: ${errorMessage}`, hideCustomModal);
      } finally {
        setCommentLoading(false);
      }
    }, true, hideCustomModal);
  };

  const handlePageChange = (newPage) => {
    setSearchFilter(prev => ({ ...prev, page: newPage }));
  };

  const handleFilterChange = (e) => {
    const { name, value } = e.target;
    setSearchFilter(prev => ({ ...prev, [name]: value, page: 0 }));
  };

  const handleSortChange = (e) => {
    const { value } = e.target;
    const [sortBy, sortOrder] = value.split(':');
    setSearchFilter(prev => ({ ...prev, sortBy, sortOrder, page: 0 }));
  };


  if (loading) return <div className="text-center mt-8 text-xl">사찰 상세 정보 로딩 중...</div>;
  if (error) return <div className="text-center mt-8 text-red-500 text-xl">{error}</div>;
  if (!temple) return <div className="text-center mt-8 text-gray-600 text-xl">사찰 정보를 찾을 수 없습니다.</div>;

  return (
    <div className="container mx-auto p-8 bg-white shadow-lg rounded-lg my-8">
      {modal.show && (
        <CustomModal
          message={modal.message}
          onConfirm={modal.onConfirm}
          onCancel={modal.onCancel}
          showCancel={modal.showCancel}
        />
      )}
      {loginRequiredModal && (
        <CustomModal
          message="로그인 후 좋아요 기능을 이용할 수 있습니다."
          onConfirm={() => {
            setLoginRequiredModal(false);
            navigate('/login');
          }}
        />
      )}

      <div className="flex justify-between items-center mb-4">
        <h1 className="text-4xl font-bold text-center text-blue-800">
          {temple.templeName} 상세 정보
        </h1>
        <div className="relative flex space-x-4">
            {isLoggedIn && (
                <>
                    <button
                        onClick={handleToggleBookmark}
                        className="text-4xl"
                        aria-label="찜하기"
                    >
                        {isBookmarked ? (
                            <FaBookmark className="text-blue-500 hover:text-blue-600 transition-colors" />
                        ) : (
                            <FaRegBookmark className="text-gray-400 hover:text-blue-500 transition-colors" />
                        )}
                    </button>
                    <button
                        onClick={handleToggleLike}
                        className="text-4xl"
                        aria-label="좋아요"
                    >
                        {isLiked ? (
                            <FaHeart className="text-red-500 hover:text-red-600 transition-colors" />
                        ) : (
                            <FaRegHeart className="text-gray-400 hover:text-red-500 transition-colors" />
                        )}
                    </button>
                </>
            )}
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-8 mb-8">
        <div className="md:col-span-1">
          {temple.photos && temple.photos.length > 0 ? (
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              {temple.photos.map((photo, index) => (
                <img
                  key={index}
                  src={`${BACKEND_BASE_URL}${photo.photoUrl}`}
                  alt={`${temple.templeName} 사진 ${index + 1}`}
                  className="w-full h-64 object-cover rounded-lg shadow-md cursor-pointer"
                  onClick={() => handleImageClick(photo.photoUrl, photo.description)}
                />
              ))}
            </div>
          ) : (
            <div className="w-full h-64 bg-gray-200 flex items-center justify-center text-gray-500 rounded-lg shadow-md">
              사진 없음
            </div>
          )}
        </div>

        <div className="md:col-span-1 space-y-4">
          <p className="text-lg text-gray-800">
            <span className="font-semibold">이름:</span> {temple.templeName}
          </p>
          <p className="text-lg text-gray-800">
            <span className="font-semibold">특징:</span> {temple.feature}
          </p>
          <p className="text-lg text-gray-800">
            <span className="font-semibold">지역:</span> {regionMap[temple.region] || temple.region}
          </p>
          <p className="text-lg text-gray-800">
            <span className="font-semibold">주소:</span> {temple.address}
          </p>
          <p className="text-lg text-gray-800">
            <span className="font-semibold">전화번호:</span> {temple.phoneNumber}
          </p>
          {temple.homepage && (
            <p className="text-lg text-gray-800">
              <span className="font-semibold">홈페이지:</span>{" "}
              <a href={temple.homepage} target="_blank" rel="noopener noreferrer" className="text-blue-600 hover:underline">
                {temple.homepage}
              </a>
            </p>
          )}
          <p className="text-lg text-gray-800">
            <span className="font-semibold">운영 시간:</span> {temple.operatingHours}
          </p>
          <p className="text-lg text-gray-800">
            <span className="font-semibold">휴일:</span> {temple.holidays}
          </p>
          <p className="text-lg text-gray-800">
            <span className="font-semibold">주차 정보:</span> {temple.parkingInfo}
          </p>
          <p className="text-lg text-gray-800">
            <span className="font-semibold">입장료:</span> {temple.admissionFee}
          </p>
          <p className="text-lg text-gray-800">
            <span className="font-semibold">화장실 정보:</span> {temple.restroomInfo}
          </p>
          <p className="text-lg text-gray-800">
            <span className="font-semibold">접근성:</span> {temple.accessibility}
          </p>
          <p className="text-lg text-gray-800">
            <span className="font-semibold">문화재:</span> {temple.culturalAssets}
          </p>
          <p className="text-lg text-gray-800">
            <span className="font-semibold">조회수:</span> {temple.viewCount}
          </p>
          <p className="text-lg text-gray-800">
            <span className="font-semibold">등록일:</span> {new Date(temple.createdAt).toLocaleDateString()}
          </p>
          <p className="text-lg text-gray-800">
            <span className="font-semibold">수정일:</span> {new Date(temple.updatedAt).toLocaleDateString()}
          </p>

          {hasAdminPermission() && (
            <p className="text-lg text-gray-800">
              <span className="font-semibold">활성화 상태:</span>{" "}
              {temple.isActive ? (
                <span className="text-green-600 font-bold">활성화</span>
              ) : (
                <span className="text-red-600 font-bold">비활성화</span>
              )}
            </p>
          )}
        </div>
      </div>

      <div className="mb-8 p-6 bg-gray-50 rounded-lg shadow-inner">
        <h2 className="text-2xl font-semibold text-gray-700 mb-4">상세 설명</h2>
        <p className="text-gray-700 whitespace-pre-wrap">{temple.description}</p>
      </div>

      <div className="flex justify-between mt-8">
        <button
          onClick={() => navigate('/temples')}
          className="px-6 py-3 bg-blue-600 text-white font-semibold rounded-lg shadow-md hover:bg-blue-700 transition duration-300"
        >
          목록으로 돌아가기
        </button>
        {hasAdminPermission() && (
          <div className="space-x-4">
            <Link
              to={`/temples/edit/${templeId}`}
              className="px-6 py-3 bg-yellow-600 text-white font-semibold rounded-lg shadow-md hover:bg-yellow-700 transition duration-300"
            >
              수정
            </Link>
            <button
              onClick={handleDeleteTemple}
              className="px-6 py-3 bg-red-600 text-white font-semibold rounded-lg shadow-md hover:bg-red-700 transition duration-300"
            >
              삭제
            </button>
          </div>
        )}
      </div>

      <div className="mt-12 p-6 bg-gray-100 rounded-lg shadow-md">
        <h2 className="text-3xl font-bold text-gray-800 mb-6 flex items-center">
          댓글 ({totalComments}) <span className="ml-2 text-xl text-gray-600">💬</span>
        </h2>

        {isLoggedIn ? (
          <form onSubmit={handleCommentSubmit} className="mb-8 p-4 bg-white rounded-lg shadow-sm">
            <textarea
              className="w-full p-3 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 mb-3 resize-y"
              rows="3"
              placeholder="댓글을 작성해주세요..."
              value={commentContent}
              onChange={(e) => setCommentContent(e.target.value)}
              disabled={commentLoading}
            ></textarea>
            <button
              type="submit"
              className="w-full px-5 py-2 bg-green-600 text-white font-semibold rounded-md hover:bg-green-700 transition duration-300 disabled:opacity-50"
              disabled={commentLoading}
            >
              {commentLoading ? '작성 중...' : '댓글 작성'}
            </button>
            {commentError && <p className="text-red-500 text-sm mt-2">{commentError}</p>}
          </form>
        ) : (
          <p className="text-center text-gray-600 mb-8 p-4 bg-white rounded-lg shadow-sm">
            댓글을 작성하려면 <Link to="/login" className="text-blue-600 hover:underline">로그인</Link> 해주세요.
          </p>
        )}

        <div className="mb-6 p-4 bg-white rounded-lg shadow-sm flex flex-col sm:flex-row items-center space-y-3 sm:space-y-0 sm:space-x-4">
          <input
            type="text"
            name="content"
            placeholder="댓글 내용 검색..."
            value={searchFilter.content}
            onChange={handleFilterChange}
            className="w-full sm:w-auto flex-grow p-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
          <select
            name="sort"
            onChange={handleSortChange}
            value={`${searchFilter.sortBy}:${searchFilter.sortOrder}`}
            className="w-full sm:w-auto p-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          >
            <option value="createdAt:desc">최신순</option>
            <option value="createdAt:asc">오래된순</option>
            <option value="viewCount:desc">조회수 높은순</option>
            <option value="likeCount:desc">좋아요 많은순</option>
          </select>
        </div>

        {commentLoading ? (
          <div className="text-center text-gray-600 p-4">댓글 로딩 중...</div>
        ) : commentError ? (
          <div className="text-center text-red-500 p-4">{commentError}</div>
        ) : comments.length === 0 ? (
          <div className="text-center text-gray-600 p-4">아직 댓글이 없습니다. 첫 댓글을 작성해보세요!</div>
        ) : (
          <div className="space-y-6">
            {comments.map((comment) => (
              <div key={comment.templeCommentId} className="bg-white p-5 rounded-lg shadow-md border border-gray-200">
                {editingCommentId === comment.templeCommentId ? (
                  <div className="flex flex-col">
                    <textarea
                      className="w-full p-3 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 mb-3 resize-y"
                      rows="3"
                      value={editingCommentContent}
                      onChange={(e) => setEditingCommentContent(e.target.value)}
                    ></textarea>
                    <div className="flex justify-end space-x-2">
                      <button
                        onClick={() => handleUpdateComment(comment.templeCommentId)}
                        className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition"
                      >
                        수정 완료
                      </button>
                      <button
                        onClick={handleCancelEdit}
                        className="px-4 py-2 bg-gray-300 text-gray-800 rounded-md hover:bg-gray-400 transition"
                      >
                        취소
                      </button>
                    </div>
                  </div>
                ) : (
                  <>
                    <div className="flex justify-between items-start mb-3">
                      <div>
                        <p className="font-semibold text-gray-900 text-lg">{comment.memberUsername || '알 수 없는 사용자'}</p>
                        <p className="text-sm text-gray-500">
                          {new Date(comment.createdAt).toLocaleString()}
                          {comment.updatedAt && (
                            <span className="ml-2">(수정됨: {new Date(comment.updatedAt).toLocaleString()})</span>
                          )}
                        </p>
                      </div>
                      <div className="flex space-x-2 text-gray-600 text-sm">
                        <span>조회수 {comment.viewCount}</span>
                        {/* ✨✨✨ 댓글 좋아요 버튼 추가 ✨✨✨ */}
                        {isLoggedIn && (
                          <button
                            onClick={() => handleToggleCommentLike(comment.templeCommentId)}
                            className="text-xl -mt-1"
                            aria-label="댓글 좋아요"
                          >
                            {commentLikeMap[comment.templeCommentId] ? (
                                <FaHeart className="text-red-500 hover:text-red-600 transition-colors" />
                            ) : (
                                <FaRegHeart className="text-gray-400 hover:text-red-500 transition-colors" />
                            )}
                          </button>
                        )}
                        <span className="ml-1">좋아요 {comment.likeCount}</span>
                      </div>
                    </div>
                    <p className="text-gray-800 mb-4 whitespace-pre-wrap">{comment.content}</p>
                    {(isLoggedIn && (loggedInMemberNo === comment.memberNo || hasAdminPermission())) && (
                      <div className="flex justify-end space-x-2">
                        <button
                          onClick={() => handleEditClick(comment)}
                          className="px-3 py-1 bg-yellow-500 text-white text-sm rounded-md hover:bg-yellow-600 transition"
                        >
                          수정
                        </button>
                        <button
                          onClick={() => handleDeleteComment(comment.templeCommentId, comment.memberNo)}
                          className="px-3 py-1 bg-red-500 text-white text-sm rounded-md hover:bg-red-600 transition"
                        >
                          삭제
                        </button>
                      </div>
                    )}
                  </>
                )}
              </div>
            ))}
          </div>
        )}

        {totalPages > 1 && (
          <div className="flex justify-center items-center space-x-2 mt-8">
            <button
              onClick={() => handlePageChange(searchFilter.page - 1)}
              disabled={searchFilter.page === 0}
              className="px-4 py-2 bg-gray-200 rounded-md hover:bg-gray-300 disabled:opacity-50"
            >
              이전
            </button>
            {Array.from({ length: totalPages }, (_, i) => (
              <button
                key={i}
                onClick={() => handlePageChange(i)}
                className={`px-4 py-2 rounded-md ${
                  searchFilter.page === i ? 'bg-blue-600 text-white' : 'bg-gray-200 hover:bg-gray-300'
                }`}
              >
                {i + 1}
              </button>
            ))}
            <button
              onClick={() => handlePageChange(searchFilter.page + 1)}
              disabled={searchFilter.page === totalPages - 1}
              className="px-4 py-2 bg-gray-200 rounded-md hover:bg-gray-300 disabled:opacity-50"
            >
              다음
            </button>
          </div>
        )}
      </div>

      {showImageModal && (
        <div className="fixed inset-0 bg-black bg-opacity-75 flex items-center justify-center z-50 p-4">
          <div className="relative bg-white rounded-lg p-4 max-w-4xl w-full max-h-[90vh] flex flex-col items-center">
            <button
              onClick={handleCloseImageModal}
              className="absolute top-2 right-2 text-gray-800 text-3xl font-bold p-2 rounded-full bg-gray-200 hover:bg-gray-300 z-10"
            >
              &times;
            </button>
            <div className="flex-grow flex items-center justify-center w-full min-h-0">
              <img
                src={modalImageUrl}
                alt={modalImageDescription || "확대 이미지"}
                className="max-w-full max-h-[70vh] object-contain rounded-lg shadow-md"
              />
            </div>
            <p className="flex-shrink-0 mt-4 text-gray-700 text-center text-base font-medium px-4 py-2">
              {modalImageDescription || "설명 없음"}
            </p>
          </div>
        </div>
      )}
    </div>
  );
}

export default TempleDetail;