// src/Temples/TempleList.js
import React, { useState, useEffect, useCallback, useRef } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import axios from 'axios';
import { useAuth } from '../Context/AuthContext';
import { FaBookmark, FaRegBookmark, FaHeart, FaRegHeart } from 'react-icons/fa';

function TempleList() {
  const [temples, setTemples] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const navigate = useNavigate();
  const { hasAdminPermission, isLoggedIn, accessToken, logout } = useAuth();
  const [isBookmarkedMap, setIsBookmarkedMap] = useState({});
  const [isLikedMap, setIsLikedMap] = useState({});

  const BACKEND_BASE_URL = "http://localhost:8080";

  const initialSearchFilters = {
    region: '',
    sortBy: 'createdAt',
    sortOrder: 'DESC',
    page: 0,
    size: 9
  };

  const [searchFilters, setSearchFilters] = useState(initialSearchFilters);
  const [tempTempleName, setTempTempleName] = useState('');
  const [tempFeature, setTempFeature] = useState('');
  const [debouncedTempleName, setDebouncedTempleName] = useState('');
  const [debouncedFeature, setDebouncedFeature] = useState('');

  const [totalPages, setTotalPages] = useState(0);

  const debounceTimer = useRef(null);

  const regionOptions = [
    { value: '', label: '전체' },
    { value: 'SEOUL', label: '서울' },
    { value: 'BUSAN', label: '부산' },
    { value: 'DAEGU', label: '대구' },
    { value: 'INCHEON', label: '인천' },
    { value: 'GWANGJU', label: '광주' },
    { value: 'DAEJEON', label: '대전' },
    { value: 'ULSAN', label: '울산' },
    { value: 'SEJONG', label: '세종' },
    { value: 'GYEONGGI', label: '경기' },
    { value: 'GANGWON', label: '강원' },
    { value: 'CHUNGCHEONGBUK', label: '충북' },
    { value: 'CHUNGCHEONGNAM', label: '충남' },
    { value: 'JEOLLABUK', label: '전북' },
    { value: 'JEOLLANAM', label: '전남' },
    { value: 'GYEONGSANGBUK', label: '경북' },
    { value: 'GYEONGSANGNAM', label: '경남' },
    { value: 'JEJU', label: '제주' },
    { value: 'ETC', label: '기타' }
  ];

  const sortOptions = [
    { value: 'createdAt,DESC', label: '최신순' },
    { value: 'viewCount,DESC', label: '조회수 높은순' },
    { value: 'templeName,ASC', label: '이름순 (가나다)' }
  ];

  const fetchTemples = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const params = {
        ...searchFilters,
        templeName: debouncedTempleName || undefined,
        feature: debouncedFeature || undefined,
        region: searchFilters.region || undefined,
      };
      console.log("Axios 요청 파라미터:", params);

      const response = await axios.get(`${BACKEND_BASE_URL}/api/temples/search`, { params });
      setTemples(response.data.content);
      setTotalPages(response.data.totalPages);
    } catch (err) {
      console.error("사찰 목록 가져오기 실패:", err);
      setError("사찰 목록을 가져오는 데 실패했습니다.");
    } finally {
      setLoading(false);
    }
  }, [searchFilters, debouncedTempleName, debouncedFeature]);

  const checkAllBookmarks = useCallback(async (templeList) => {
    if (!isLoggedIn || !accessToken || templeList.length === 0) {
      setIsBookmarkedMap({});
      return;
    }
    const newBookmarkMap = {};
    const token = accessToken;
    const bookmarkChecks = templeList.map(temple =>
      axios.get(`${BACKEND_BASE_URL}/api/temples/${temple.templeId}/bookmarks/status`, {
        headers: { Authorization: `Bearer ${token}` }
      })
      .then(response => ({ templeId: temple.templeId, isBookmarked: response.data.bookmarked }))
      .catch(error => {
        if (error.response?.status === 401 || error.response?.status === 403) logout();
        return { templeId: temple.templeId, isBookmarked: false };
      })
    );
    const results = await Promise.all(bookmarkChecks);
    results.forEach(result => { newBookmarkMap[result.templeId] = result.isBookmarked; });
    setIsBookmarkedMap(newBookmarkMap);
  }, [isLoggedIn, accessToken, logout]);

  const checkAllLikes = useCallback(async (templeList) => {
    if (!isLoggedIn || !accessToken || templeList.length === 0) {
        setIsLikedMap({});
        return;
    }
    const newLikeMap = {};
    const token = accessToken;
    const likeChecks = templeList.map(temple =>
        axios.get(`${BACKEND_BASE_URL}/api/temples/${temple.templeId}/likes/status`, {
            headers: { Authorization: `Bearer ${token}` }
        })
        .then(response => ({ templeId: temple.templeId, isLiked: response.data.isLiked }))
        .catch(error => {
            if (error.response?.status === 401 || error.response?.status === 403) logout();
            return { templeId: temple.templeId, isLiked: false };
        })
    );
    const results = await Promise.all(likeChecks);
    results.forEach(result => { newLikeMap[result.templeId] = result.isLiked; });
    setIsLikedMap(newLikeMap);
  }, [isLoggedIn, accessToken, logout]);

  useEffect(() => {
    fetchTemples();
  }, [fetchTemples]);

  useEffect(() => {
    if (temples.length > 0 && isLoggedIn) {
        checkAllBookmarks(temples);
        checkAllLikes(temples);
    }
  }, [temples, isLoggedIn, checkAllBookmarks, checkAllLikes]);

  useEffect(() => {
    if (debounceTimer.current) {
      clearTimeout(debounceTimer.current);
    }
    debounceTimer.current = setTimeout(() => {
      setDebouncedTempleName(tempTempleName);
      setDebouncedFeature(tempFeature);
    }, 500);

    return () => {
      if (debounceTimer.current) {
        clearTimeout(debounceTimer.current);
      }
    };
  }, [tempTempleName, tempFeature]);

  const handleFilterChange = (e) => {
    const { name, value } = e.target;
    setSearchFilters(prevFilters => ({
      ...prevFilters,
      [name]: value,
      page: 0
    }));
  };

  const handleTextFilterChange = (e) => {
    const { name, value } = e.target;
    if (name === 'templeName') {
      setTempTempleName(value);
    } else if (name === 'feature') {
      setTempFeature(value);
    }
  };

  const handlePageChange = (newPage) => {
    setSearchFilters(prevFilters => ({
      ...prevFilters,
      page: newPage
    }));
  };

  const handleGoToMain = () => {
    navigate('/');
  };

  const handleResetFilters = () => {
    setTempTempleName('');
    setTempFeature('');
    setSearchFilters(initialSearchFilters);
  };
  
  const handleToggleBookmark = async (templeId, event) => {
    event.preventDefault();
    if (!isLoggedIn) {
      alert('로그인 후 찜하기 기능을 이용할 수 있습니다.');
      return;
    }
    const token = accessToken;
    try {
      const response = await axios.post(
        `${BACKEND_BASE_URL}/api/temples/${templeId}/bookmarks`,
        null,
        { headers: { Authorization: `Bearer ${token}` } }
      );
      setIsBookmarkedMap(prevMap => ({
        ...prevMap,
        [templeId]: response.data.bookmarked
      }));
    } catch (error) {
      console.error('찜하기 토글 실패:', error);
      if (error.response?.status === 401 || error.response?.status === 403) {
        alert('로그인이 만료되었거나 권한이 없습니다. 다시 로그인 해주세요.');
        logout();
      } else {
        alert('찜하기 기능에 문제가 발생했습니다.');
      }
    }
  };

  const handleToggleLike = async (templeId, event) => {
    event.preventDefault();
    if (!isLoggedIn) {
      alert('로그인 후 좋아요 기능을 이용할 수 있습니다.');
      return;
    }
    const token = accessToken;
    try {
      const response = await axios.post(
        `${BACKEND_BASE_URL}/api/temples/${templeId}/likes`,
        null,
        { headers: { Authorization: `Bearer ${token}` } }
      );
      setIsLikedMap(prevMap => ({
        ...prevMap,
        [templeId]: response.data.isLiked
      }));
    } catch (error) {
      console.error('좋아요 토글 실패:', error);
      if (error.response?.status === 401 || error.response?.status === 403) {
        alert('로그인이 만료되었거나 권한이 없습니다. 다시 로그인 해주세요.');
        logout();
      } else {
        alert('좋아요 기능에 문제가 발생했습니다.');
      }
    }
  };

  if (loading) return <div className="text-center mt-8 text-xl">사찰 목록 로딩 중...</div>;
  if (error) return <div className="text-center mt-8 text-red-500 text-xl">{error}</div>;

  return (
    <div className="container mx-auto p-8 bg-white shadow-lg rounded-lg my-8">
      <h1 className="text-4xl font-bold text-center text-blue-800 mb-8">
        📜 붓다로드 사찰 목록 📜
      </h1>

      <div className="mb-8 p-6 bg-gray-50 rounded-lg shadow-inner">
        <h2 className="text-2xl font-semibold text-gray-700 mb-4">사찰 검색</h2>
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          <div>
            <label htmlFor="templeName" className="block text-sm font-medium text-gray-600">사찰명</label>
            <input
              type="text"
              id="templeName"
              name="templeName"
              value={tempTempleName}
              onChange={handleTextFilterChange}
              className="mt-1 block w-full border border-gray-300 rounded-md shadow-sm p-2"
              placeholder="사찰명 검색"
            />
          </div>
          <div>
            <label htmlFor="region" className="block text-sm font-medium text-gray-600">지역</label>
            <select
              id="region"
              name="region"
              value={searchFilters.region}
              onChange={handleFilterChange}
              className="mt-1 block w-full border border-gray-300 rounded-md shadow-sm p-2"
            >
              {regionOptions.map(option => (
                <option key={option.value} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>
          </div>
          <div>
            <label htmlFor="feature" className="block text-sm font-medium text-gray-600">특징</label>
            <input
              type="text"
              id="feature"
              name="feature"
              value={tempFeature}
              onChange={handleTextFilterChange}
              className="mt-1 block w-full border border-gray-300 rounded-md shadow-sm p-2"
              placeholder="특징 검색 (예: 산사, 해안사찰)"
            />
          </div>
          <div>
            <label htmlFor="sortBy" className="block text-sm font-medium text-gray-600">정렬 기준</label>
            <select
              id="sortBy"
              name="sortBy"
              value={searchFilters.sortBy}
              onChange={handleFilterChange}
              className="mt-1 block w-full border border-gray-300 rounded-md shadow-sm p-2"
            >
              <option value="createdAt">등록일</option>
              <option value="viewCount">조회수</option>
            </select>
          </div>
          <div>
            <label htmlFor="sortOrder" className="block text-sm font-medium text-gray-600">정렬 순서</label>
            <select
              id="sortOrder"
              name="sortOrder"
              value={searchFilters.sortOrder}
              onChange={handleFilterChange}
              className="mt-1 block w-full border border-gray-300 rounded-md shadow-sm p-2"
            >
              <option value="DESC">내림차순</option>
              <option value="ASC">오름차순</option>
            </select>
          </div>
        </div>
        <div className="mt-6 flex justify-end space-x-4">
          <button
            onClick={handleResetFilters}
            className="px-6 py-2 bg-red-600 text-white font-semibold rounded-lg shadow-md hover:bg-red-700 transition duration-300"
          >
            🔄 초기화
          </button>
        </div>
      </div>

      <div className="mb-6 flex justify-end space-x-4">
        <button
          onClick={handleGoToMain}
          className="px-6 py-3 bg-gray-600 text-white font-semibold rounded-lg shadow-md hover:bg-gray-700 transition duration-300"
        >
          🏠 메인으로 돌아가기
        </button>
        {hasAdminPermission() && (
          <Link
            to="/temples/register"
            className="px-6 py-3 bg-green-600 text-white font-semibold rounded-lg shadow-md hover:bg-green-700 transition duration-300"
          >
            ➕ 사찰 정보 등록
          </Link>
        )}
      </div>

      {temples.length === 0 ? (
        <p className="text-center text-gray-600 text-xl mt-10">검색 결과가 없습니다. 다른 필터로 시도해보세요.</p>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {temples.map((temple) => (
            <div key={temple.templeId} className="relative bg-white border border-gray-200 rounded-lg shadow-md hover:shadow-xl transition-shadow duration-300 overflow-hidden">
              {isLoggedIn && (
                <>
                  <button
                      onClick={(e) => handleToggleBookmark(temple.templeId, e)}
                      className="absolute top-3 right-3 z-10 text-2xl"
                      aria-label="찜하기"
                  >
                      {isBookmarkedMap[temple.templeId] ? (
                          <FaBookmark className="text-blue-500 hover:text-blue-600 transition-colors" />
                      ) : (
                          <FaRegBookmark className="text-gray-400 hover:text-blue-500 transition-colors" />
                      )}
                  </button>
                </>
              )}
              <Link to={`/temples/${temple.templeId}`}>
                {temple.photos && temple.photos.length > 0 ? (
                  <img
                    src={`${BACKEND_BASE_URL}${temple.photos[0].photoUrl}`}
                    alt={temple.templeName}
                    className="w-full h-48 object-cover object-center"
                  />
                ) : (
                  <div className="w-full h-48 bg-gray-200 flex items-center justify-center text-gray-500">
                    사진 없음
                  </div>
                )}
                <div className="p-5">
                  <h3 className="text-2xl font-bold text-gray-900 mb-2">{temple.templeName}</h3>
                  <p className="text-gray-700 text-sm mb-1">📍 {regionOptions.find(opt => opt.value === temple.region)?.label || temple.region}</p>
                  <p className="text-gray-600 text-sm mb-1">✨ {temple.feature}</p>
                  <div className="flex justify-between items-center mt-2">
                    <p className="text-gray-500 text-xs">👀 조회수: {temple.viewCount}</p>
                    {isLoggedIn && (
                        <button
                          onClick={(e) => handleToggleLike(temple.templeId, e)}
                          className="text-xl"
                          aria-label="좋아요"
                        >
                          {isLikedMap[temple.templeId] ? (
                            <FaHeart className="text-red-500 hover:text-red-600 transition-colors" />
                          ) : (
                            <FaRegHeart className="text-gray-400 hover:text-red-500 transition-colors" />
                          )}
                        </button>
                    )}
                  </div>
                </div>
              </Link>
            </div>
          ))}
        </div>
      )}

      {totalPages > 1 && (
        <div className="flex justify-center mt-8 space-x-2">
          <button
            onClick={() => handlePageChange(searchFilters.page - 1)}
            disabled={searchFilters.page === 0}
            className="px-4 py-2 border rounded-lg bg-gray-100 hover:bg-gray-200 disabled:opacity-50"
          >
            이전
          </button>
          {[...Array(totalPages)].map((_, i) => (
            <button
              key={i}
              onClick={() => handlePageChange(i)}
              className={`px-4 py-2 border rounded-lg ${
                searchFilters.page === i ? 'bg-blue-600 text-white' : 'bg-gray-100 hover:bg-gray-200'
              }`}
            >
              {i + 1}
            </button>
          ))}
          <button
            onClick={() => handlePageChange(searchFilters.page + 1)}
            disabled={searchFilters.page === totalPages - 1}
            className="px-4 py-2 border rounded-lg bg-gray-100 hover:bg-gray-200 disabled:opacity-50"
          >
            다음
          </button>
        </div>
      )}
    </div>
  );
}

export default TempleList;