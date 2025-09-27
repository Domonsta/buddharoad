// src/main/frontend/src/reviews/ReviewDetail.js

import React, { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import axios from 'axios';
import { useAuth } from '../Context/AuthContext';
import { FaBookmark, FaRegBookmark, FaHeart, FaRegHeart } from 'react-icons/fa'; // ✨ 하트 아이콘 추가

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

function ReviewDetail() {
    const { reviewId } = useParams();
    const navigate = useNavigate();
    const { isLoggedIn, user, hasAdminPermission, loadingAuth, accessToken, logout } = useAuth();

    const [review, setReview] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const [showImageModal, setShowImageModal] = useState(false);
    const [modalImageUrl, setModalImageUrl] = useState('');
    const [modalImageDescription, setModalImageDescription] = useState('');

    const [modal, setModal] = useState({
        show: false,
        message: '',
        onConfirm: () => {},
        onCancel: () => {},
        showCancel: false,
    });

    const [isBookmarked, setIsBookmarked] = useState(false); // ✨ 찜하기 상태
    const [isLiked, setIsLiked] = useState(false); // ❤️ 좋아요 상태 추가
    const [likeCount, setLikeCount] = useState(0); // ❤️ 좋아요 개수 추가

    const BACKEND_BASE_URL = "http://localhost:8080";

    const showCustomModal = useCallback((message, onConfirm, showCancel = false, onCancel = () => {}) => {
        setModal({ show: true, message, onConfirm, onCancel, showCancel });
    }, []);

    const hideCustomModal = useCallback(() => {
        setModal({ show: false, message: '', onConfirm: () => {}, onCancel: () => {}, showCancel: false });
    }, []);

    const handleImageClick = useCallback((photoUrl, description) => {
        const fullImageUrl = photoUrl.startsWith('http') || photoUrl.startsWith('https')
            ? photoUrl
            : `${BACKEND_BASE_URL}${photoUrl.startsWith('/') ? '' : '/'}${photoUrl}`;
        setModalImageUrl(fullImageUrl);
        setModalImageDescription(description || '');
        setShowImageModal(true);
    }, [BACKEND_BASE_URL]);

    const handleCloseImageModal = useCallback(() => {
        setShowImageModal(false);
        setModalImageUrl('');
        setModalImageDescription('');
    }, []);

    // ✨✨✨ 찜하기 상태 확인 함수 ✨✨✨
    const checkBookmarkStatus = useCallback(async () => {
      if (!isLoggedIn || !accessToken) {
          setIsBookmarked(false);
          return;
      }
      try {
          const response = await axios.get(
              `${BACKEND_BASE_URL}/api/reviews/${reviewId}/bookmarks/status`,
              { headers: { Authorization: `Bearer ${accessToken}` } }
          );
          setIsBookmarked(response.data.isBookmarked);
      } catch (error) {
          console.error('찜하기 상태 확인 실패:', error);
          setIsBookmarked(false);
      }
    }, [isLoggedIn, accessToken, reviewId, BACKEND_BASE_URL]);

    // ⭐⭐ 좋아요 상태 확인 함수 추가 ⭐⭐
    const checkLikeStatus = useCallback(async () => {
        if (!isLoggedIn || !accessToken) {
            setIsLiked(false);
            return;
        }
        try {
            // 백엔드에 좋아요 상태를 확인하는 API가 있다고 가정
            const response = await axios.get(
                `${BACKEND_BASE_URL}/api/reviews/${reviewId}/likes/status`,
                { headers: { Authorization: `Bearer ${accessToken}` } }
            );
            setIsLiked(response.data.isLiked);
        } catch (error) {
            console.error('좋아요 상태 확인 실패:', error);
            setIsLiked(false);
        }
    }, [isLoggedIn, accessToken, reviewId, BACKEND_BASE_URL]);

    // ✨✨✨ 찜하기 토글 핸들러 ✨✨✨
    const handleToggleBookmark = async () => {
        if (!isLoggedIn) {
            showCustomModal('로그인 후 찜하기 기능을 이용할 수 있습니다.', () => { hideCustomModal(); navigate('/login'); });
            return;
        }
        try {
            const response = await axios.post(
                `${BACKEND_BASE_URL}/api/reviews/${reviewId}/bookmarks`,
                null,
                { headers: { Authorization: `Bearer ${accessToken}` } }
            );
            setIsBookmarked(response.data.isBookmarked);
        } catch (error) {
            console.error('찜하기 토글 실패:', error);
            showCustomModal('찜하기 기능에 문제가 발생했습니다.', hideCustomModal);
        }
    };

    // ⭐⭐ 좋아요 토글 핸들러 수정 ⭐⭐
    const handleToggleLike = async () => {
        if (!isLoggedIn) {
            showCustomModal('로그인 후 좋아요 기능을 이용할 수 있습니다.', () => { hideCustomModal(); navigate('/login'); });
            return;
        }
        try {
            const response = await axios.post(
                `${BACKEND_BASE_URL}/api/reviews/${reviewId}/likes`,
                null,
                { headers: { Authorization: `Bearer ${accessToken}` } }
            );
            // 백엔드에서 받은 isLiked와 likeCount를 업데이트
            setIsLiked(response.data.isLiked);
            setLikeCount(response.data.likeCount);
            // ⭐⭐⭐ 좋아요 토글 후 최신 데이터를 다시 불러오도록 추가! ⭐⭐⭐
            fetchReviewDetails();
        } catch (error) {
            console.error('좋아요 토글 실패:', error);
            showCustomModal('좋아요 기능에 문제가 발생했습니다.', hideCustomModal);
        }
    };

    const handleToggleCommentLike = async (commentId, isCurrentlyLiked) => {
        if (!isLoggedIn) {
            showCustomModal('로그인 후 댓글에 좋아요를 누를 수 있습니다.', () => { hideCustomModal(); navigate('/login'); });
            return;
        }
    
        try {
            const url = `${BACKEND_BASE_URL}/api/reviews/comments/${commentId}/likes`;
            const method = isCurrentlyLiked ? 'DELETE' : 'POST';
    
            const response = await axios({
                method: method,
                url: url,
                headers: { Authorization: `Bearer ${accessToken}` }
            });
    
            fetchComments();
            console.log(`🎉 댓글 ID ${commentId} 좋아요 토글 성공:`, response.data);
        } catch (error) {
            console.error('❌ 댓글 좋아요 토글 실패:', error);
            showCustomModal('댓글 좋아요 기능에 문제가 발생했습니다.', hideCustomModal);
        }
    };

    const fetchReviewDetails = useCallback(async () => {
        try {
            setLoading(true);
            const apiUrl = `${BACKEND_BASE_URL}/api/reviews/${reviewId}`;
            console.log(`🚀 리뷰 상세 정보 요청 전송: ${apiUrl}`);
            const response = await axios.get(apiUrl);
            setReview(response.data);
            // 백엔드에서 가져온 리뷰 정보에 포함된 likeCount를 상태에 저장!
            setLikeCount(response.data.likeCount); 
            console.log(`🎉 리뷰 상세 정보 가져오기 성공:`, response.data);
            checkBookmarkStatus();
            checkLikeStatus(); // ⭐ 정보 로드 후 좋아요 상태 확인
        } catch (err) {
            console.error(`❌ 리뷰 상세 정보 가져오기 실패:`, err);
            setError("리뷰 상세 정보를 가져오는 데 실패했습니다.");
        } finally {
            setLoading(false);
        }
    }, [reviewId, BACKEND_BASE_URL, checkBookmarkStatus, checkLikeStatus]);

    useEffect(() => {
        if (reviewId) {
            fetchReviewDetails();
        }
    }, [reviewId, fetchReviewDetails]);

    const handleDeleteReview = useCallback(() => {
        showCustomModal('정말로 이 리뷰를 삭제(비활성화)하시겠습니까?', async () => {
            hideCustomModal();
            const token = localStorage.getItem('accessToken');
            if (!token) {
                showCustomModal('로그인이 필요합니다.', () => { hideCustomModal(); navigate('/login'); });
                return;
            }

            try {
                const loggedInMemberNo = user?.memberNo;
                const reviewAuthorMemberNo = review?.memberNo;

                if (!hasAdminPermission() && loggedInMemberNo !== reviewAuthorMemberNo) {
                    showCustomModal('리뷰를 삭제할 권한이 없습니다. 자신의 리뷰만 삭제할 수 있습니다.', hideCustomModal);
                    return;
                }

                await axios.delete(`${BACKEND_BASE_URL}/api/reviews/${reviewId}`, {
                    headers: { Authorization: `Bearer ${token}` }
                });
                showCustomModal('✅ 리뷰가 성공적으로 삭제(비활성화)되었습니다.', () => { hideCustomModal(); navigate('/reviews'); });
            } catch (err) {
                console.error("❌ 리뷰 삭제 실패:", err);
                showCustomModal(`❌ 리뷰 삭제에 실패했습니다: ${err.response?.data?.message || err.message}`, hideCustomModal);
            }
        }, true, hideCustomModal);
    }, [reviewId, navigate, showCustomModal, hideCustomModal, user, review, hasAdminPermission, BACKEND_BASE_URL]);

    const renderStars = (rating) => {
        const stars = [];
        for (let i = 1; i <= 5; i++) {
            stars.push(
                <span
                    key={i}
                    className={`text-2xl inline-block ${rating >= i ? 'text-yellow-400' : 'text-gray-300'}`}
                    style={{ marginRight: '0.125rem' }}
                >
                    ★
                </span>
            );
        }
        return stars;
    };

    const regionMap = {
        SEOUL: '서울', BUSAN: '부산', DAEGU: '대구', INCHEON: '인천', GWANGJU: '광주',
        DAEJEON: '대전', ULSAN: '울산', SEJONG: '세종', GYEONGGI: '경기', GANGWON: '강원',
        CHUNGCHEONGBUK: '충북', CHUNGCHEONGNAM: '충남', JEOLLABUK: '전북', JEOLLANAM: '전남',
        GYEONGSANGBUK: '경북', GYEONGSANGNAM: '경남', JEJU: '제주', ETC: '기타',
    };

    const formatDate = (dateString, includeTime = false) => {
        if (!dateString) return '정보 없음';
        const date = new Date(dateString.includes('T') ? dateString : `${dateString}T00:00:00`);

        if (isNaN(date.getTime())) {
            console.warn(`Invalid date string provided: ${dateString}`);
            return '잘못된 날짜 형식';
        }

        const options = {
            year: 'numeric', month: 'long', day: 'numeric',
        };
        if (includeTime) {
            options.hour = '2-digit'; options.minute = '2-digit'; options.hour12 = false;
        }

        return date.toLocaleDateString('ko-KR', options);
    };

    const displayRegion = review?.region ? regionMap[review.region] || '알 수 없음' : '지역 정보 없음';


    const [comments, setComments] = useState([]);
    const [newCommentContent, setNewCommentContent] = useState('');
    const [editingCommentId, setEditingCommentId] = useState(null);
    const [editingCommentContent, setEditingCommentContent] = useState('');
    const [commentCurrentPage, setCommentCurrentPage] = useState(0);
    const [commentTotalPages, setCommentTotalPages] = useState(0);
    const [totalCommentsCount, setTotalCommentsCount] = useState(0);

    const [showCommentModal, setShowCommentModal] = useState(false);
    const [commentModalMessage, setCommentModalMessage] = useState('');
    const [onCommentModalConfirm, setOnCommentModalConfirm] = useState(() => () => {});
    const [onCommentModalCancel, setOnCommentModalCancel] = useState(() => () => {});
    const [showCommentModalCancel, setShowCommentModalCancel] = useState(false);

    const fetchComments = useCallback(async () => {
        try {
            const response = await axios.get(`${BACKEND_BASE_URL}/api/reviews/${reviewId}/comments`, {
                params: {
                    page: commentCurrentPage,
                    size: 10,
                    sortBy: 'createdAt',
                    sortOrder: 'desc'
                }
            });

            const fetchedComments = response.data.content;
            setCommentTotalPages(response.data.totalPages);
            setTotalCommentsCount(response.data.totalElements);
            
            // 💡 백엔드 DTO에 좋아요 수가 포함되어 있으므로, 상세 조회 로직은 제거합니다.
            setComments(fetchedComments);
            
        } catch (err) {
            console.error('댓글을 불러오는데 실패했습니다:', err);
            setCommentModalMessage('댓글을 불러오는데 실패했습니다.');
            setShowCommentModal(true);
            setShowCommentModalCancel(false);
            setOnCommentModalConfirm(() => () => setShowCommentModal(false));
        }
    }, [reviewId, commentCurrentPage, BACKEND_BASE_URL]);

    useEffect(() => {
        if (reviewId) {
            fetchComments();
        }
    }, [reviewId, fetchComments]);

    const handleCommentSubmit = async (e) => {
        e.preventDefault();
        if (!newCommentContent.trim()) {
            setCommentModalMessage('댓글 내용을 입력해주세요.');
            setShowCommentModal(true);
            setShowCommentModalCancel(false);
            setOnCommentModalConfirm(() => () => setShowCommentModal(false));
            return;
        }

        const token = localStorage.getItem('accessToken');
        if (!isLoggedIn || !token) {
            setCommentModalMessage('로그인 후 댓글을 작성할 수 있습니다.');
            setShowCommentModal(true);
            setShowCommentModalCancel(false);
            setOnCommentModalConfirm(() => () => {
                setShowCommentModal(false);
                navigate('/login');
            });
            return;
        }

        try {
            // 💡 DTO에 memberId가 백엔드에서 직접 처리되므로 제거합니다.
            await axios.post(`${BACKEND_BASE_URL}/api/reviews/${reviewId}/comments`, {
                reviewId: reviewId,
                content: newCommentContent
            }, {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });
            setNewCommentContent('');
            setCommentCurrentPage(0);
            fetchComments();
            setCommentModalMessage('댓글이 성공적으로 등록되었습니다! 🥳');
            setShowCommentModal(true);
            setShowCommentModalCancel(false);
            setOnCommentModalConfirm(() => () => setShowCommentModal(false));
        } catch (err) {
            console.error('댓글 등록 실패:', err);
            const errorMessage = err.response?.data?.message || '댓글 등록 중 오류가 발생했습니다.';
            setCommentModalMessage(`댓글 등록 실패: ${errorMessage}`);
            setShowCommentModal(true);
            setShowCommentModalCancel(false);
            setOnCommentModalConfirm(() => () => setShowCommentModal(false));
        }
    };

    const handleEditClick = (comment) => {
        setEditingCommentId(comment.reviewCommentId);
        setEditingCommentContent(comment.content);
    };

    const handleCancelEdit = () => {
        setEditingCommentId(null);
        setEditingCommentContent('');
    };

    const handleEditSubmit = async (commentId) => {
        if (!editingCommentContent.trim()) {
            setCommentModalMessage('수정할 내용을 입력해주세요.');
            setShowCommentModal(true);
            setShowCommentModalCancel(false);
            setOnCommentModalConfirm(() => () => setShowCommentModal(false));
            return;
        }

        const token = localStorage.getItem('accessToken');
        if (!isLoggedIn || !token) {
            setCommentModalMessage('로그인 후 댓글을 수정할 수 있습니다.');
            setShowCommentModal(true);
            setShowCommentModalCancel(false);
            setOnCommentModalConfirm(() => () => {
                setShowCommentModal(false);
                navigate('/login');
            });
            return;
        }

        try {
             // 💡 URL과 DTO 수정: reviewCommentId만 사용
            await axios.put(`${BACKEND_BASE_URL}/api/reviews/comments/${commentId}`, {
                reviewCommentId: commentId,
                content: editingCommentContent
            }, {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });
            setEditingCommentId(null);
            setEditingCommentContent('');
            fetchComments();
            setCommentModalMessage('댓글이 성공적으로 수정되었습니다! ✍️');
            setShowCommentModal(true);
            setShowCommentModalCancel(false);
            setOnCommentModalConfirm(() => () => setShowCommentModal(false));
        } catch (err) {
            console.error('댓글 수정 실패:', err);
            const errorMessage = err.response?.data?.message || '댓글 수정 중 오류가 발생했습니다.';
            setCommentModalMessage(`댓글 수정 실패: ${errorMessage}`);
            setShowCommentModal(true);
            setShowCommentModalCancel(false);
            setOnCommentModalConfirm(() => () => setShowCommentModal(false));
        }
    };

    const handleDeleteClick = (commentId) => {
        setCommentModalMessage('정말로 이 댓글을 삭제하시겠습니까?');
        setShowCommentModal(true);
        setShowCommentModalCancel(true);
        setOnCommentModalConfirm(() => async () => {
            setShowCommentModal(false);
            const token = localStorage.getItem('accessToken');
            if (!isLoggedIn || !token) {
                setCommentModalMessage('로그인 후 댓글을 삭제할 수 있습니다.');
                setShowCommentModal(true);
                setShowCommentModalCancel(false);
                setOnCommentModalConfirm(() => () => {
                    setShowCommentModal(false);
                    navigate('/login');
                });
                return;
            }
            try {
                 // 💡 URL 수정: reviewCommentId만 사용
                await axios.delete(`${BACKEND_BASE_URL}/api/reviews/comments/${commentId}`, {
                    headers: {
                        'Authorization': `Bearer ${token}`
                    }
                });
                fetchComments();
                setCommentModalMessage('댓글이 성공적으로 삭제되었습니다! 🗑️');
                setShowCommentModal(true);
                setShowCommentModalCancel(false);
                setOnCommentModalConfirm(() => () => setShowCommentModal(false));
            } catch (err) {
                console.error('댓글 삭제 실패:', err);
                const errorMessage = err.response?.data?.message || '댓글 삭제 중 오류가 발생했습니다.';
                setCommentModalMessage(`댓글 삭제 실패: ${errorMessage}`);
                setShowCommentModal(true);
                setShowCommentModalCancel(false);
                setOnCommentModalConfirm(() => () => setShowCommentModal(false));
            }
        });
        setOnCommentModalCancel(() => () => setShowCommentModal(false));
    };

    const handleCommentPageChange = (newPage) => {
        setCommentCurrentPage(newPage);
    };

    if (loading || loadingAuth) return <div className="text-center mt-8 text-xl">로딩 중...</div>;
    if (error) return <div className="text-center mt-8 text-red-500 text-xl">{error}</div>;
    if (!review) return <div className="text-center mt-8 text-gray-600 text-xl">리뷰 정보를 찾을 수 없습니다.</div>;

    return (
        <div className="container mx-auto p-6 bg-white shadow-lg rounded-lg my-6 font-inter">
            {modal.show && (
                <CustomModal
                    message={modal.message}
                    onConfirm={modal.onConfirm}
                    onCancel={modal.onCancel}
                    showCancel={modal.showCancel}
                />
            )}

            <div className="flex justify-between items-center mb-4">
                <h1 className="text-3xl font-bold text-center text-blue-800">
                    리뷰 상세 정보 📝
                </h1>
                {/* ✨✨✨ 찜하기 버튼 추가 ✨✨✨ */}
                {isLoggedIn && (
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
                )}
            </div>

            <div className="bg-gray-50 p-6 rounded-lg shadow-inner mb-6">
                <h2 className="text-2xl font-bold text-gray-900 mb-2">{review.title}</h2>

                <div className="mb-4 flex items-center justify-between">
                    <div>
                        <p className="text-xl font-semibold text-blue-800 leading-tight">
                            방문 사찰 : {review.templeName || '사찰 정보 없음'}
                        </p>
                        {review.region && (
                            <p className="text-base text-gray-600 mt-1">📍 {displayRegion}</p>
                        )}
                    </div>

                    {review.templeId && (
                        <button
                            onClick={() => navigate(`/temples/${review.templeId}`)}
                            className="ml-4 px-4 py-2 bg-green-600 text-white font-semibold rounded-lg shadow-md hover:bg-green-700 transition duration-300 flex items-center text-sm"
                        >
                            사찰 정보 바로가기 ➡️
                        </button>
                    )}
                </div>

                <div className="flex items-center justify-between mb-4 pb-3 border-b border-gray-200">
                    <div className="flex items-center">
                        {renderStars(review.rating)}
                        <span className="text-xl font-bold text-gray-800 ml-2">{review.rating?.toFixed(1) || '0.0'}</span>
                    </div>
                    <div className="flex items-center space-x-4 text-lg text-gray-600">
                        {/* ❤️ 좋아요 버튼 및 개수 추가 */}
                        <button onClick={handleToggleLike} className="flex items-center space-x-1">
                            {isLiked ? (
                                <FaHeart className="text-red-500 hover:text-red-600 transition-colors" />
                            ) : (
                                <FaRegHeart className="text-gray-400 hover:text-red-500 transition-colors" />
                            )}
                            <span className="font-semibold">{likeCount}</span>
                        </button>
                        <span className="font-semibold">조회수:</span> {review.viewCount || 0} 👀
                    </div>
                </div>

                <div className="text-gray-600 text-base mb-4 space-y-1">
                    <p>
                        <span className="font-semibold">방문일:</span> {formatDate(review.visitedAt, false)}
                    </p>
                    <p>
                        <span className="font-semibold">작성자:</span> {review.memberUsername || '알 수 없는 사용자'}
                    </p>
                </div>

                <div className="mb-4">
                    <p className="font-semibold text-gray-700 text-lg mb-1">태그:</p>
                    <div className="flex flex-wrap gap-2">
                        {review.tags && review.tags.length > 0 ? (
                            review.tags.split(',').map(tag => (
                                <span key={tag} className="inline-block bg-blue-100 text-blue-800 text-base font-medium px-3 py-1 rounded-full">
                                    #{tag.trim()}
                                </span>
                            ))
                        ) : (
                            <span className="text-base text-gray-500">없음</span>
                        )}
                    </div>
                </div>

                <div className="mt-4 p-4 bg-white rounded-md shadow-sm border border-gray-100 mb-4">
                    <h3 className="text-xl font-semibold text-gray-800 mb-2">내용</h3>
                    <p className="text-gray-700 whitespace-pre-wrap leading-relaxed text-lg">{review.content}</p>
                </div>

                <div className="text-gray-600 text-base space-y-1">
                    <p>
                        <span className="font-semibold">작성일:</span> {formatDate(review.createdAt, true)}
                    </p>
                    {review.updatedAt && (
                        <p>
                            <span className="font-semibold">수정일:</span> {formatDate(review.updatedAt, true)}
                        </p>
                    )}
                </div>
            </div>

            {review.photos && review.photos.length > 0 && (
                <div className="mb-8 border-t pt-6">
                    <h2 className="text-2xl font-bold mb-4 text-gray-800">갤러리</h2>
                    <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
                        {review.photos.map((photo) => (
                            <div key={photo.reviewPhotoId} className="relative group cursor-pointer"
                                 onClick={() => handleImageClick(photo.photoUrl, photo.description)}>
                                <img
                                    src={photo.photoUrl.startsWith('http') || photo.photoUrl.startsWith('https')
                                        ? photo.photoUrl
                                        : `${BACKEND_BASE_URL}${photo.photoUrl.startsWith('/') ? '' : '/'}${photo.photoUrl}`}
                                    alt={photo.description || '리뷰 사진'}
                                    className="w-full h-48 object-cover rounded-lg shadow-md transition-transform transform group-hover:scale-105"
                                />
                                <div
                                    className="absolute inset-0 bg-black bg-opacity-50 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity rounded-lg">
                                    <p className="text-white text-center text-sm p-2">
                                        {photo.description || '클릭하여 확대'}
                                    </p>
                                </div>
                            </div>
                        ))}
                    </div>
                </div>
            )}

            <div className="flex justify-between mt-8">
                <button
                    onClick={() => navigate('/reviews')}
                    className="px-6 py-3 bg-blue-600 text-white font-semibold rounded-lg shadow-md hover:bg-blue-700 transition duration-300 text-base"
                >
                    목록으로 돌아가기
                </button>
                {isLoggedIn && user && review && (user.memberNo === review.memberNo || hasAdminPermission()) && (
                    <div className="space-x-4">
                        <button
                            onClick={() => navigate(`/reviews/edit/${reviewId}`)}
                            className="px-6 py-3 bg-yellow-600 text-white font-semibold rounded-lg shadow-md hover:bg-yellow-700 transition duration-300 text-base"
                        >
                            수정
                        </button>
                        <button
                            onClick={handleDeleteReview}
                            className="px-6 py-3 bg-red-600 text-white font-semibold rounded-lg shadow-md hover:bg-red-700 transition duration-300 text-base"
                        >
                            삭제
                        </button>
                    </div>
                )}
            </div>

            <div className="mt-10 border-t pt-8">
                <h2 className="text-3xl font-bold mb-6 text-gray-800">댓글 ({totalCommentsCount})</h2>

                <form onSubmit={handleCommentSubmit} className="mb-8 p-6 bg-gray-50 rounded-lg shadow-inner">
                    <textarea
                        className="w-full p-4 border border-gray-300 rounded-md focus:ring-blue-500 focus:border-blue-500 transition-all duration-200 resize-y min-h-[100px] text-gray-800"
                        placeholder={isLoggedIn ? "댓글을 입력하세요..." : "로그인 후 댓글을 작성할 수 있습니다."}
                        value={newCommentContent}
                        onChange={(e) => setNewCommentContent(e.target.value)}
                        rows="4"
                        disabled={!isLoggedIn}
                    ></textarea>
                    <div className="flex justify-end mt-4">
                        <button
                            type="submit"
                            className={`px-8 py-3 rounded-md font-semibold transition duration-300 ease-in-out ${
                                isLoggedIn ? 'bg-blue-600 text-white hover:bg-blue-700' : 'bg-gray-400 text-gray-200 cursor-not-allowed'
                            }`}
                            disabled={!isLoggedIn}
                        >
                            댓글 등록
                        </button>
                    </div>
                </form>

                <div className="space-y-6">
                    {comments.length === 0 ? (
                        <p className="text-center text-gray-500 text-lg py-8">아직 댓글이 없습니다. 첫 댓글을 작성해 보세요! 😄</p>
                    ) : (
                        comments.map((comment) => (
                            <div key={comment.reviewCommentId} className="p-6 bg-white border border-gray-200 rounded-lg shadow-sm">
                                {editingCommentId === comment.reviewCommentId ? (
                                    <div>
                                        <textarea
                                            className="w-full p-3 border border-gray-300 rounded-md focus:ring-green-500 focus:border-green-500 transition-all duration-200 resize-y min-h-[80px] text-gray-800"
                                            value={editingCommentContent}
                                            onChange={(e) => setEditingCommentContent(e.target.value)}
                                            rows="3"
                                        ></textarea>
                                        <div className="flex justify-end space-x-2 mt-3">
                                            <button
                                                onClick={() => handleEditSubmit(comment.reviewCommentId)}
                                                className="px-5 py-2 bg-green-600 text-white rounded-md hover:bg-green-700 transition duration-200 font-semibold"
                                            >
                                                저장
                                            </button>
                                            <button
                                                onClick={handleCancelEdit}
                                                className="px-5 py-2 bg-gray-400 text-white rounded-md hover:bg-gray-500 transition duration-200 font-semibold"
                                            >
                                                취소
                                            </button>
                                        </div>
                                    </div>
                                ) : (
                                    <>
                                        <div className="flex justify-between items-center mb-3">
                                            <span className="font-semibold text-blue-700 text-lg">{comment.memberUsername}</span>
                                            <span className="text-gray-500 text-sm">
                                                {new Date(comment.createdAt).toLocaleString()}
                                                {comment.updatedAt && new Date(comment.createdAt).getTime() !== new Date(comment.updatedAt).getTime() && (
                                                    <span className="ml-2 text-gray-400">(수정됨)</span>
                                                )}
                                            </span>
                                        </div>
                                        <p className="text-gray-800 leading-relaxed mb-4">{comment.content}</p>
                                        <div className="flex justify-end space-x-2 items-center text-sm text-gray-600">
                                        <button onClick={() => handleToggleCommentLike(comment.reviewCommentId, comment.isLiked)}
                                                className="flex items-center space-x-1">
                                                {comment.isLiked ? (
                                                    <FaHeart className="text-red-500 hover:text-red-600 transition-colors" />
                                                ) : (
                                                    <FaRegHeart className="text-gray-400 hover:text-red-500 transition-colors" />
                                                )}
                                                <span className="font-semibold text-gray-800">{comment.likeCount}</span>
                                            </button>
                                            {isLoggedIn && user && (user.memberNo === comment.memberNo || hasAdminPermission()) && (
                                                <>
                                                    <button
                                                        onClick={() => handleEditClick(comment)}
                                                        className="ml-4 px-3 py-1 bg-yellow-500 text-white rounded-md text-xs hover:bg-yellow-600 transition"
                                                    >
                                                        수정
                                                    </button>
                                                    <button
                                                        onClick={() => handleDeleteClick(comment.reviewCommentId)}
                                                        className="px-3 py-1 bg-red-600 text-white rounded-md text-xs hover:bg-red-700 transition"
                                                    >
                                                        삭제
                                                    </button>
                                                </>
                                            )}
                                        </div>
                                    </>
                                )}
                            </div>
                        ))
                    )}
                </div>

                {commentTotalPages > 1 && (
                    <div className="flex justify-center items-center space-x-2 mt-8">
                        <button
                            onClick={() => handleCommentPageChange(commentCurrentPage - 1)}
                            disabled={commentCurrentPage === 0}
                            className="px-4 py-2 bg-gray-200 rounded-md hover:bg-gray-300 disabled:opacity-50"
                        >
                            이전
                        </button>
                        {[...Array(commentTotalPages).keys()].map((page) => (
                            <button
                                key={page}
                                onClick={() => handleCommentPageChange(page)}
                                className={`px-4 py-2 rounded-md ${
                                    commentCurrentPage === page ? 'bg-blue-600 text-white' : 'bg-gray-200 hover:bg-gray-300'
                                }`}
                            >
                                {page + 1}
                            </button>
                        ))}
                        <button
                            onClick={() => handleCommentPageChange(commentCurrentPage + 1)}
                            disabled={commentCurrentPage === commentTotalPages - 1}
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

            {showCommentModal && (
                <CustomModal
                    message={commentModalMessage}
                    onConfirm={onCommentModalConfirm}
                    onCancel={onCommentModalCancel}
                    showCancel={showCommentModalCancel}
                />
            )}
        </div>
    );
}

export default ReviewDetail;