// src/main/frontend/src/reviews/ReviewList.js

import React, { useState, useEffect, useCallback } from 'react';
import { useNavigate, Link } from 'react-router-dom';
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

function ReviewList() {
    const navigate = useNavigate();
    const { isLoggedIn, user, hasAdminPermission, loadingAuth, accessToken, logout } = useAuth();

    const [reviews, setReviews] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [page, setPage] = useState(0);
    const [size, setSize] = useState(9);
    const [totalPages, setTotalPages] = useState(0);
    const [sortBy, setSortBy] = useState('createdAt');
    const [sortOrder, setSortOrder] = useState('desc');

    const [searchType, setSearchType] = useState('title');
    const [keyword, setKeyword] = useState('');

    const [tags, setTags] = useState('');
    const commonTags = [
        '힐링', '조용함', '경치 좋음', '사진찍기좋은곳', '걷기좋은길',
        '역사', '문화유산', '템플스테이', '가족여행', '친구와함께',
        '나홀로여행', '사찰음식', '아름다운', '고즈넉한', '도심속힐링'
    ];
    
    // ✨✨✨ region 상태와 setRegion 함수 추가 ✨✨✨
    const [region, setRegion] = useState('');

    const [templeName, setTempleName] = useState('');

    const [debouncedKeyword, setDebouncedKeyword] = useState('');
    const [debouncedTempleName, setDebouncedTempleName] = useState('');
    const DEBOUNCE_DELAY = 1000;

    const [modal, setModal] = useState({
        show: false,
        message: '',
        onConfirm: () => {},
        onCancel: () => {},
        showCancel: false,
    });

    const [isBookmarkedMap, setIsBookmarkedMap] = useState({});
    const [isLikedMap, setIsLikedMap] = useState({}); // ❤️ 좋아요 상태 맵 추가

    const BACKEND_BASE_URL = "http://localhost:8080";

    const showCustomModal = useCallback((message, onConfirm, showCancel = false, onCancel = () => {}) => {
        setModal({ show: true, message, onConfirm, onCancel, showCancel });
    }, []);

    const hideCustomModal = useCallback(() => {
        setModal({ show: false, message: '', onConfirm: () => {}, onCancel: () => {}, showCancel: false });
    }, []);

    const checkAllBookmarks = useCallback(async (reviewList) => {
        if (!isLoggedIn || !accessToken || reviewList.length === 0) {
            setIsBookmarkedMap({});
            return;
        }
        const newBookmarkMap = {};
        const token = accessToken;
        const bookmarkChecks = reviewList.map(review =>
            axios.get(`${BACKEND_BASE_URL}/api/reviews/${review.reviewId}/bookmarks/status`, {
                headers: { Authorization: `Bearer ${token}` }
            })
            .then(response => {
                return {
                    reviewId: review.reviewId,
                    isBookmarked: response.data.isBookmarked
                };
            })
            .catch(error => {
                console.error(`❌ 리뷰 ${review.reviewId} 찜 상태 확인 실패:`, error.message);
                if (error.response?.status === 401 || error.response?.status === 403) {
                    logout();
                }
                return { reviewId: review.reviewId, isBookmarked: false };
            })
        );
        const results = await Promise.all(bookmarkChecks);
        results.forEach(result => {
            newBookmarkMap[result.reviewId] = result.isBookmarked;
        });
        setIsBookmarkedMap(newBookmarkMap);
    }, [isLoggedIn, accessToken, logout, BACKEND_BASE_URL]);

    const handleToggleBookmark = async (reviewId, event) => {
        event.preventDefault();
        if (!isLoggedIn) {
          showCustomModal('로그인 후 찜하기 기능을 이용할 수 있습니다.', () => { hideCustomModal(); navigate('/login'); });
          return;
        }
        const token = accessToken;
        try {
          const response = await axios.post(
            `${BACKEND_BASE_URL}/api/reviews/${reviewId}/bookmarks`,
            null,
            { headers: { Authorization: `Bearer ${token}` } }
          );
          setIsBookmarkedMap(prevMap => ({
            ...prevMap,
            [reviewId]: response.data.isBookmarked
          }));
        } catch (error) {
          console.error('찜하기 토글 실패:', error);
          if (error.response?.status === 401 || error.response?.status === 403) {
            showCustomModal('로그인이 만료되었거나 권한이 없습니다. 다시 로그인 해주세요.', () => { hideCustomModal(); logout(); });
          } else {
            showCustomModal('찜하기 기능에 문제가 발생했습니다.', hideCustomModal);
          }
        }
    };

    // ⭐⭐ 좋아요 상태 일괄 확인 함수 수정 ⭐⭐
    const checkAllLikes = useCallback(async (reviewList) => {
        // 🚨🚨 여기에 console.log 추가! 🚨🚨
        console.log("🚀 좋아요 상태 일괄 확인 시작. 리뷰 개수:", reviewList.length);

        if (!isLoggedIn || !accessToken || reviewList.length === 0) {
            console.log("❌ 로그인 상태가 아니거나 리뷰가 없습니다. 좋아요 상태 확인 건너뜀.");
            setIsLikedMap({});
            return;
        }
        const newLikeMap = {};
        const token = accessToken;
        const likeChecks = reviewList.map(review =>
            axios.get(`${BACKEND_BASE_URL}/api/reviews/${review.reviewId}/likes/status`, {
                headers: { Authorization: `Bearer ${token}` }
            })
            .then(response => {
                // 🚨🚨 여기에 console.log 추가! 🚨🚨
                console.log(`✅ 리뷰 ${review.reviewId} 좋아요 상태 확인 성공:`, response.data);
                return {
                    reviewId: review.reviewId,
                    isLiked: response.data.isLiked
                };
            })
            .catch(error => {
                console.error(`❌ 리뷰 ${review.reviewId} 좋아요 상태 확인 실패:`, error.message);
                if (error.response?.status === 401 || error.response?.status === 403) {
                    logout();
                }
                return { reviewId: review.reviewId, isLiked: false };
            })
        );
        const results = await Promise.all(likeChecks);
        results.forEach(result => {
            newLikeMap[result.reviewId] = result.isLiked;
        });
        setIsLikedMap(newLikeMap);
    }, [isLoggedIn, accessToken, logout, BACKEND_BASE_URL]);

    // ⭐⭐ 좋아요 토글 핸들러 수정 ⭐⭐
    const handleToggleLike = async (reviewId, event) => {
        event.preventDefault();
        if (!isLoggedIn) {
            showCustomModal('로그인 후 좋아요 기능을 이용할 수 있습니다.', () => { hideCustomModal(); navigate('/login'); });
            return;
        }
        const token = accessToken;
        try {
            const response = await axios.post(
                `${BACKEND_BASE_URL}/api/reviews/${reviewId}/likes`,
                null,
                { headers: { Authorization: `Bearer ${token}` } }
            );
            // 🚨🚨 여기에 console.log 추가! 🚨🚨
            console.log("✅ 좋아요 토글 API 응답 데이터:", response.data);

            setIsLikedMap(prevMap => ({
                ...prevMap,
                [reviewId]: response.data.isLiked
            }));
            
            // ⭐⭐⭐ 좋아요 개수 실시간 업데이트 로직 추가 ⭐⭐⭐
            setReviews(prevReviews => 
                prevReviews.map(review => 
                    review.reviewId === reviewId 
                        ? { ...review, likeCount: response.data.likeCount }
                        : review
                )
            );
            // 🚨🚨 업데이트된 Reviews 배열 상태 확인! 🚨🚨
            console.log("🌟 업데이트된 리뷰 목록 (토글 후):", reviews);

        } catch (error) {
            console.error('좋아요 토글 실패:', error);
            if (error.response?.status === 401 || error.response?.status === 403) {
                showCustomModal('로그인이 만료되었거나 권한이 없습니다. 다시 로그인 해주세요.', () => { hideCustomModal(); logout(); });
            } else {
                showCustomModal('좋아요 기능에 문제가 발생했습니다.', hideCustomModal);
            }
        }
    };


    const fetchReviews = useCallback(async () => {
        try {
            setLoading(true);
            const params = {
                page,
                size,
                sortBy,
                sortOrder,
            };

            if (debouncedKeyword.trim()) {
                if (searchType === 'title') {
                    params.titleKeyword = debouncedKeyword.trim();
                } else if (searchType === 'content') {
                    params.contentKeyword = debouncedKeyword.trim();
                } else if (searchType === 'author') {
                    params.memberUsername = debouncedKeyword.trim();
                }
            }

            if (debouncedTempleName.trim()) {
                params.templeName = debouncedTempleName.trim();
            }

            if (region.trim()) {
                params.region = region.trim();
            }
            if (tags.trim()) {
                params.tags = tags.trim();
            }

            console.log("🚀 리뷰 목록 요청 전송:", params);
            const response = await axios.get(`${BACKEND_BASE_URL}/api/reviews`, { params });
            // 🚨🚨 여기에 console.log 추가! 🚨🚨
            console.log("🎉 리뷰 목록 불러오기 성공:", response.data.content);

            setReviews(response.data.content);
            setTotalPages(response.data.totalPages);
            setError(null);
        } catch (err) {
            console.error("❌ 리뷰 목록 불러오기 실패:", err.response?.data || err.message);
            setError("리뷰 목록을 불러오는 데 실패했습니다.");
            setReviews([]);
            setTotalPages(0);
        } finally {
            setLoading(false);
        }
    }, [page, size, sortBy, sortOrder, searchType, debouncedKeyword, tags, debouncedTempleName, region, BACKEND_BASE_URL]);

    useEffect(() => {
        const handler = setTimeout(() => {
            setDebouncedKeyword(keyword.trim());
        }, DEBOUNCE_DELAY);

        return () => {
            clearTimeout(handler);
        };
    }, [keyword, DEBOUNCE_DELAY]);

    useEffect(() => {
        const handler = setTimeout(() => {
            setDebouncedTempleName(templeName.trim());
        }, DEBOUNCE_DELAY);

        return () => {
            clearTimeout(handler);
        };
    }, [templeName, DEBOUNCE_DELAY]);

    useEffect(() => {
        fetchReviews();
    }, [fetchReviews, debouncedKeyword, debouncedTempleName, tags, region, searchType, sortBy, sortOrder, page]);

    useEffect(() => {
      if (reviews.length > 0 && isLoggedIn) {
          checkAllBookmarks(reviews);
          checkAllLikes(reviews); // ⭐ 리뷰 목록 로드 후 좋아요 상태 일괄 확인
      }
    }, [reviews, isLoggedIn, checkAllBookmarks, checkAllLikes]);

    const handlePageChange = (newPage) => {
        if (newPage >= 0 && newPage < totalPages) {
            setPage(newPage);
        }
    };

    const handleSortChange = (e) => {
        const [newSortBy, newSortOrder] = e.target.value.split(',');
        setSortBy(newSortBy);
        setSortOrder(newSortOrder);
        setPage(0);
    };

    const handleSearch = (e) => {
        e.preventDefault();
        setPage(0);
        setDebouncedKeyword(keyword.trim());
        setDebouncedTempleName(templeName.trim());
    };

    const handleResetSearch = () => {
        setSearchType('title');
        setKeyword('');
        setTags('');
        setTempleName('');
        setRegion('');
        setSortBy('createdAt');
        setSortOrder('desc');
        setPage(0);
        setDebouncedKeyword('');
        setDebouncedTempleName('');
        setError(null);
    };

    const handleDeleteReview = useCallback((reviewIdToDelete) => {
        showCustomModal('정말로 이 리뷰를 삭제(비활성화)하시겠습니까?', async () => {
            hideCustomModal();
            const token = localStorage.getItem('accessToken');
            if (!token) {
                showCustomModal('로그인이 필요합니다.', () => { hideCustomModal(); navigate('/login'); });
                return;
            }

            try {
                if (!hasAdminPermission()) {
                    showCustomModal('리뷰를 삭제할 권한이 없습니다.', hideCustomModal);
                    return;
                }

                await axios.delete(`${BACKEND_BASE_URL}/api/reviews/${reviewIdToDelete}`, {
                    headers: { Authorization: `Bearer ${token}` }
                });
                showCustomModal('✅ 리뷰가 성공적으로 삭제(비활성화)되었습니다.', () => {
                    hideCustomModal();
                    fetchReviews();
                });
            } catch (err) {
                console.error("❌ 리뷰 삭제 실패:", err);
                showCustomModal(`❌ 리뷰 삭제에 실패했습니다: ${err.response?.data?.message || err.message}`, hideCustomModal);
            }
        }, true, hideCustomModal);
    }, [navigate, showCustomModal, hideCustomModal, hasAdminPermission, fetchReviews, BACKEND_BASE_URL]);

    const renderStars = (rating) => {
        const stars = [];
        for (let i = 1; i <= 5; i++) {
            stars.push(
                <span
                    key={i}
                    className={`text-xl ${rating >= i ? 'text-yellow-400' : 'text-gray-300'}`}
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

    if (loading) {
        return <div className="flex justify-center items-center h-screen text-2xl text-blue-600">리뷰 목록 로딩 중... ⏳</div>;
    }

    return (
        <div className="container mx-auto p-8 bg-white shadow-lg rounded-lg my-8 font-inter">
            {modal.show && (
                <CustomModal
                    message={modal.message}
                    onConfirm={modal.onConfirm}
                    onCancel={modal.onCancel}
                    showCancel={modal.showCancel}
                />
            )}

            <h1 className="text-4xl font-bold text-center text-blue-800 mb-8">
                리뷰 목록 📝
            </h1>

            <div className="flex justify-end mb-6">
                <Link
                    to="/reviews/register"
                    className="px-6 py-3 bg-green-600 text-white font-semibold rounded-lg shadow-md hover:bg-green-700 transition duration-300"
                >
                    리뷰 작성하기 ✍️
                </Link>
            </div>

            <form onSubmit={handleSearch} className="mb-8 p-6 bg-gray-50 rounded-lg shadow-inner grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                <div>
                    <label htmlFor="searchType" className="block text-sm font-medium text-gray-700">검색 기준</label>
                    <select
                        id="searchType"
                        value={searchType}
                        onChange={(e) => setSearchType(e.target.value)}
                        className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                    >
                        <option value="title">제목</option>
                        <option value="content">내용</option>
                        <option value="author">작성자</option>
                    </select>
                </div>
                <div>
                    <label htmlFor="keyword" className="block text-sm font-medium text-gray-700">검색어</label>
                    <input type="text" id="keyword" value={keyword} onChange={(e) => setKeyword(e.target.value)}
                        className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                        placeholder="검색어를 입력하세요."
                    />
                </div>

                <div>
                    <label htmlFor="tags" className="block text-sm font-medium text-gray-700">태그</label>
                    <select id="tags" value={tags} onChange={(e) => setTags(e.target.value)}
                        className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500">
                        <option value="">전체</option>
                        {commonTags.map(tag => (
                            <option key={tag} value={tag}>{tag}</option>
                        ))}
                    </select>
                </div>

                <div>
                    <label htmlFor="templeName" className="block text-sm font-medium text-gray-700">사찰 이름</label>
                    <input type="text" id="templeName" value={templeName} onChange={(e) => setTempleName(e.target.value)}
                        className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                        placeholder="사찰 이름을 입력하세요."
                    />
                </div>

                <div>
                    <label htmlFor="region" className="block text-sm font-medium text-gray-700">지역</label>
                    <select id="region" value={region} onChange={(e) => setRegion(e.target.value)}
                        className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500">
                        <option value="">전체</option>
                        {Object.entries(regionMap).map(([key, value]) => (
                            <option key={key} value={key}>{value}</option>
                        ))}
                    </select>
                </div>

                <div>
                    <label htmlFor="sortBy" className="block text-sm font-medium text-gray-700">정렬 기준</label>
                    <select id="sortBy" value={`${sortBy},${sortOrder}`} onChange={handleSortChange}
                        className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500">
                        <option value="rating,desc">별점 높은순</option>
                        <option value="rating,asc">별점 낮은순</option>
                        <option value="viewCount,desc">조회수 높은순</option>
                        <option value="createdAt,desc">작성일 최신순</option>
                        <option value="createdAt,asc">작성일 오래된 순</option>
                        <option value="visitedAt,desc">방문일 최신순</option>
                        <option value="visitedAt,asc">방문일 오래된 순</option>
                        <option value="likeCount,desc">좋아요순</option> {/* ⭐⭐ 좋아요순 정렬 추가 ⭐⭐ */}
                    </select>
                </div>
                <div className="md:col-span-2 lg:col-span-3 flex justify-end space-x-2">
                    <button type="submit" className="px-6 py-2 bg-blue-600 text-white font-semibold rounded-md shadow-md hover:bg-blue-700 transition duration-300">
                        검색
                    </button>
                    <button type="button" onClick={handleResetSearch} className="px-6 py-2 bg-gray-400 text-white font-semibold rounded-md shadow-md hover:bg-gray-500 transition duration-300">
                        초기화
                    </button>
                </div>
            </form>

            {error ? (
                <div className="text-center text-red-500 text-xl mt-10">
                    {error}
                    {reviews.length === 0 && sortBy !== 'likeCount' && (
                        <p className="text-lg text-gray-600 mt-2">
                            현재 검색 조건에 맞는 리뷰가 없습니다. 다른 조건을 시도해 보세요.
                        </p>
                    )}
                </div>
            ) : (
                reviews.length === 0 && sortBy !== 'likeCount' ? (
                    <div className="text-center text-gray-600 text-xl mt-10">
                        등록된 리뷰가 없습니다. 첫 리뷰를 작성해보세요!
                    </div>
                ) : (
                    <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-6">
                        {reviews.map((review) => (
                            <div key={review.reviewId} className="relative bg-white border border-gray-200 rounded-lg shadow-md hover:shadow-lg transition-shadow duration-200 flex flex-col h-full">
                                {isLoggedIn && (
                                    <button
                                        onClick={(e) => handleToggleBookmark(review.reviewId, e)}
                                        className="absolute top-3 right-3 z-10 text-2xl"
                                        aria-label="찜하기"
                                    >
                                        {isBookmarkedMap[review.reviewId] ? (
                                            <FaBookmark className="text-blue-500 hover:text-blue-600 transition-colors" />
                                        ) : (
                                            <FaRegBookmark className="text-gray-400 hover:text-blue-500 transition-colors" />
                                        )}
                                    </button>
                                )}
                                <Link to={`/reviews/${review.reviewId}`} className="block h-48 w-full">
                                    <div className="w-full h-full bg-gray-100 flex items-center justify-center overflow-hidden rounded-t-lg">
                                        {review.photosUrls ? (
                                            <img
                                                src={(() => {
                                                    const urls = review.photosUrls.split(',').map(url => url.trim()).filter(url => url);
                                                    if (urls.length > 0) {
                                                        let url = urls[0];
                                                        if (!url.startsWith('http://') && !url.startsWith('https://')) {
                                                            const baseUrl = BACKEND_BASE_URL.endsWith('/') ? BACKEND_BASE_URL : `${BACKEND_BASE_URL}/`;
                                                            const path = url.startsWith('/') ? url.substring(1) : url;
                                                            url = `${baseUrl}${path}`;
                                                        }
                                                        console.log(`Loading thumbnail: ${url}`);
                                                        return url;
                                                    }
                                                    return 'https://placehold.co/400x300/CCCCCC/000000?text=No+Image';
                                                })()}
                                                alt={review.title}
                                                className="w-full h-full object-cover"
                                                onError={(e) => {
                                                    e.target.onerror = null;
                                                    e.target.src = 'https://placehold.co/400x300/CCCCCC/000000?text=No+Image';
                                                    console.error(`Failed to load thumbnail: ${e.target.src}`);
                                                }}
                                            />
                                        ) : (
                                            <div className="text-gray-500 text-lg">이미지 없음</div>
                                        )}
                                    </div>
                                </Link>
                                <div className="p-5 flex-grow flex flex-col">
                                    <div className="mb-auto">
                                        <h2 className="text-xl font-semibold text-gray-800 mb-2 truncate">
                                            <Link to={`/reviews/${review.reviewId}`} className="hover:text-blue-600">
                                                {review.title}
                                            </Link>
                                        </h2>
                                        <p className="text-lg font-semibold text-blue-700 mb-1">
                                            {review.templeName || '사찰 정보 없음'}
                                        </p>
                                        {review.region && (
                                            <p className="text-base text-gray-600 mb-2">
                                                📍 {regionMap[review.region] || review.region}
                                            </p>
                                        )}
                                        <div className="flex items-center mb-2">
                                            {renderStars(review.rating)}
                                            <span className="ml-2 text-base text-gray-600">({review.rating}.0)</span>
                                        </div>
                                        <div className="mt-3 flex flex-wrap gap-2 max-h-16 overflow-y-auto pr-2 custom-scrollbar">
                                            {review.tags && review.tags.split(',').slice(0, 3).map(tag => (
                                                <span key={tag} className="bg-blue-100 text-blue-800 text-base font-medium px-2.5 py-0.5 rounded-full whitespace-nowrap">
                                                    #{tag.trim()}
                                                </span>
                                            ))}
                                            {review.tags && review.tags.split(',').length > 3 && (
                                                <span className="text-gray-500 text-sm">...</span>
                                            )}
                                        </div>
                                    </div>

                                    <div className="text-gray-700 text-base mt-4">
                                        <p>작성자: {review.memberUsername}</p>
                                        <p>방문일: {review.visitedAt ? new Date(review.visitedAt).toLocaleDateString('ko-KR') : '정보 없음'}</p>
                                        <div className="flex items-center space-x-4">
                                            <p>조회수: {review.viewCount}</p>
                                            {/* ⭐⭐ 좋아요 버튼 및 개수 추가 ⭐⭐ */}
                                            <button 
                                                onClick={(e) => handleToggleLike(review.reviewId, e)} 
                                                className="flex items-center space-x-1"
                                            >
                                                {isLikedMap[review.reviewId] ? (
                                                    <FaHeart className="text-red-500 hover:text-red-600 transition-colors" />
                                                ) : (
                                                    <FaRegHeart className="text-gray-400 hover:text-red-500 transition-colors" />
                                                )}
                                                <span className="font-semibold text-gray-700">{review.likeCount || 0}</span>
                                            </button>
                                        </div>
                                    </div>
                                    {isLoggedIn && user && (user.memberNo === review.memberNo || hasAdminPermission()) && (
                                        <div className="mt-4 flex justify-end space-x-2">
                                            <Link
                                                to={`/reviews/edit/${review.reviewId}`}
                                                className="px-4 py-2 bg-yellow-500 text-white text-sm font-semibold rounded-md hover:bg-yellow-600 transition"
                                            >
                                                수정
                                            </Link>
                                            <button
                                                onClick={() => handleDeleteReview(review.reviewId)}
                                                className="px-4 py-2 bg-red-500 text-white text-sm font-semibold rounded-md hover:bg-red-600 transition"
                                            >
                                                삭제
                                            </button>
                                        </div>
                                    )}
                                </div>
                            </div>
                        ))}
                    </div>
                )
            )}

            {totalPages > 1 && reviews.length > 0 && sortBy !== 'likeCount' && (
                <div className="flex justify-center items-center space-x-4 mt-8">
                    <button
                        onClick={() => handlePageChange(page - 1)}
                        disabled={page === 0}
                        className="px-4 py-2 bg-gray-200 rounded-md hover:bg-gray-300 disabled:opacity-50"
                    >
                        이전
                    </button>
                    <span className="text-lg font-medium">
                        {page + 1} / {totalPages}
                    </span>
                    <button
                        onClick={() => handlePageChange(page + 1)}
                        disabled={page === totalPages - 1}
                        className="px-4 py-2 bg-gray-200 rounded-md hover:bg-gray-300 disabled:opacity-50"
                    >
                        다음
                    </button>
                    </div>
                )}
            </div>
    );
}

export default ReviewList;