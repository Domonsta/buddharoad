import React, { useState, useEffect, useCallback, useRef } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import axios from 'axios';
import { useAuth } from '../Context/AuthContext'; // AuthContext 경로 확인

// 💡 커스텀 모달 컴포넌트 (alert/confirm 대체) - 기존 코드와 동일
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

function ReviewRegister() {
    const navigate = useNavigate();
    const { isLoggedIn, user, loadingAuth } = useAuth(); // AuthContext에서 user 객체 가져옴
    const location = useLocation();

    // 💡 리뷰 폼 상태 관리
    const [formData, setFormData] = useState({
        title: '',
        content: '',
        rating: 0,
        templeId: null, // 사찰 ID를 직접 입력 대신 검색으로 설정
        imageFiles: [],
        reviewTags: '', // 🚨🚨🚨 태그를 문자열로 관리하도록 다시 변경 🚨🚨🚨
        visitedAt: '', // 방문일 필드 유지 (type="date"에 맞춰YYYY-MM-DD 형식)
    });

    // 💡 사찰 검색 관련 상태
    const [searchTerm, setSearchTerm] = useState('');
    const [searchResults, setSearchResults] = useState([]);
    const [showSearchResults, setShowSearchResults] = useState(false);
    const [selectedTemple, setSelectedTemple] = useState(null);
    const searchTimeoutRef = useRef(null);

    // 💡 이미지 미리보기 관련 상태
    const [imagePreviews, setImagePreviews] = useState([]);
    const [newPhotoDescriptions, setNewPhotoDescriptions] = useState({}); // 💡 새로 추가된 사진 설명을 위한 상태
    const fileInputRef = useRef(null);

    // 💡 커스텀 모달 상태
    const [modal, setModal] = useState({
        show: false,
        message: '',
        onConfirm: () => {},
        onCancel: () => {},
        showCancel: false,
    });

    // 💡 자주 쓰는 태그 목록
    const commonTags = [
        '힐링', '조용함', '경치 좋음', '사진찍기좋은곳', '걷기좋은길',
        '역사', '문화유산', '템플스테이', '가족여행', '친구와함께',
        '나홀로여행', '사찰음식', '아름다운', '고즈넉한', '도심속힐링'
    ];
    // 💡 선택된 체크박스 태그 상태 (Set으로 관리하여 중복 방지)
    const [selectedCheckboxTags, setSelectedCheckboxTags] = useState(new Set());

    const BACKEND_BASE_URL = "http://localhost:8080";

    const showCustomModal = useCallback((message, onConfirm, showCancel = false, onCancel = () => {}) => {
        setModal({ show: true, message, onConfirm, onCancel, showCancel });
    }, []);

    const hideCustomModal = useCallback(() => {
        setModal(prev => ({ ...prev, show: false }));
    }, []);

    useEffect(() => {
        if (!loadingAuth) {
            console.log("ReviewRegister - Current User:", user);

            // GUEST 역할은 리뷰 작성 불가능 (로그아웃과 동일 처리)
            // user.role 대신 user?.role을 사용하여 user가 null일 경우 에러 방지
            if (!isLoggedIn || (user && user.role === 'GUEST') || user === null) {
                showCustomModal(
                    '리뷰 작성은 회원만 가능합니다. 로그인 해주세요.',
                    () => {
                        hideCustomModal();
                        navigate('/login', { state: { from: location.pathname } });
                    },
                    false
                );
            }
        }
    }, [isLoggedIn, user, loadingAuth, navigate, location.pathname, showCustomModal, hideCustomModal]);


    // --- 사찰 검색 함수 ---
    const searchTemples = useCallback(async (name) => {
        if (!name.trim()) {
            setSearchResults([]);
            setShowSearchResults(false);
            return;
        }
        try {
            const response = await axios.get(`${BACKEND_BASE_URL}/api/temples/search?name=${name}`);
            console.log('사찰 검색 결과:', response.data);
            if (response.data && response.data.content) {
                setSearchResults(response.data.content);
                setShowSearchResults(true);
            } else {
                setSearchResults([]);
                setShowSearchResults(false);
            }
        } catch (error) {
            console.error('사찰 검색 실패:', error);
            setSearchResults([]);
            setShowSearchResults(false);
            showCustomModal('사찰 검색 중 오류가 발생했습니다.', hideCustomModal);
        }
    }, [showCustomModal, hideCustomModal]);

    useEffect(() => {
        if (searchTimeoutRef.current) {
            clearTimeout(searchTimeoutRef.current);
        }
        if (searchTerm) {
            searchTimeoutRef.current = setTimeout(() => {
                searchTemples(searchTerm);
            }, 300);
        } else {
            setSearchResults([]);
            setShowSearchResults(false);
        }
        return () => {
            if (searchTimeoutRef.current) {
                clearTimeout(searchTimeoutRef.current);
            }
        };
    }, [searchTerm, searchTemples]);

    // --- 핸들러 함수 ---
    const handleChange = useCallback((e) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
    }, []);

    const handleRatingChange = useCallback((newRating) => {
        setFormData(prev => ({ ...prev, rating: newRating }));
    }, []);

    const handleImageChange = useCallback((e) => {
        const files = Array.from(e.target.files);
        setFormData(prev => ({ ...prev, imageFiles: [...prev.imageFiles, ...files] }));

        const newPreviews = files.map(file => URL.createObjectURL(file));
        setImagePreviews(prev => [...prev, ...newPreviews]);

        // 💡 새로 추가되는 사진의 설명을 빈 문자열로 초기화
        const newDescriptions = {};
        files.forEach((file, i) => {
            // 현재 imagePreviews 배열의 길이를 기준으로 인덱스 부여
            // (새로 추가될 이미지들이 기존 이미지들 뒤에 붙으므로)
            newDescriptions[imagePreviews.length + i] = '';
        });
        setNewPhotoDescriptions(prev => ({ ...prev, ...newDescriptions }));
    }, [imagePreviews.length]); // imagePreviews.length를 의존성 배열에 추가

    const handleRemoveImage = useCallback((indexToRemove) => {
        setFormData(prev => ({
            ...prev,
            imageFiles: prev.imageFiles.filter((_, i) => i !== indexToRemove)
        }));
        setImagePreviews(prev => {
            const newPreviews = prev.filter((_, i) => i !== indexToRemove);
            URL.revokeObjectURL(prev[indexToRemove]); // 메모리 해제
            return newPreviews;
        });
        setNewPhotoDescriptions(prev => {
            const updatedDescriptions = { ...prev };
            delete updatedDescriptions[indexToRemove];
            // 인덱스 재정렬
            const reindexedDescriptions = {};
            for (const key in updatedDescriptions) {
                const oldIndex = parseInt(key);
                if (oldIndex > indexToRemove) {
                    reindexedDescriptions[oldIndex - 1] = updatedDescriptions[key];
                } else if (oldIndex < indexToRemove) {
                    reindexedDescriptions[oldIndex] = updatedDescriptions[key];
                }
            }
            return reindexedDescriptions;
        });
    }, []);

    // 💡 새로 추가된 사진 설명을 위한 핸들러
    const handleNewPhotoDescriptionChange = useCallback((index, description) => {
        setNewPhotoDescriptions(prev => ({ ...prev, [index]: description }));
    }, []);

    // 🚨🚨🚨 태그 입력 변경 핸들러 수정 (텍스트 입력 방식) 🚨🚨🚨
    const handleTagChange = useCallback((e) => {
        const { value } = e.target;
        setFormData(prev => ({ ...prev, reviewTags: value }));
    }, []);

    // 💡 체크박스 태그 변경 핸들러 추가!
    const handleCheckboxTagChange = useCallback((tag) => {
        setSelectedCheckboxTags(prev => {
            const newSet = new Set(prev);
            if (newSet.has(tag)) {
                newSet.delete(tag);
            } else {
                newSet.add(tag);
            }
            // 🚨 formData.reviewTags도 함께 업데이트!
            // Set을 쉼표로 구분된 문자열로 변환하여 formData에 저장
            const updatedTagsString = Array.from(newSet).join(', ');
            setFormData(formPrev => ({ ...formPrev, reviewTags: updatedTagsString }));
            return newSet;
        });
    }, []);

    const handleSearchTermChange = useCallback((e) => {
        setSearchTerm(e.target.value);
        setSelectedTemple(null);
        setFormData(prev => ({ ...prev, templeId: null }));
    }, []);

    const handleSelectTemple = useCallback((temple) => {
        setSelectedTemple({ id: temple.templeId, name: temple.templeName });
        setFormData(prev => ({ ...prev, templeId: temple.templeId }));
        setSearchTerm(temple.templeName);
        setShowSearchResults(false);
        setSearchResults([]);
    }, []);

    // 💡 폼 제출 핸들러
    const handleSubmit = useCallback(async (e) => {
        e.preventDefault();
        if (loadingAuth || !isLoggedIn) {
            showCustomModal('인증 정보를 로딩 중이거나 로그인되지 않았습니다. 잠시 후 다시 시도해 주세요.', hideCustomModal);
            return;
        }

        if (!user) {
            showCustomModal('사용자 정보가 없어 리뷰를 등록할 수 없습니다. 다시 로그인 해주세요.', () => {
                hideCustomModal();
                navigate('/login');
            });
            return;
        }

        if (!formData.templeId) {
            showCustomModal('리뷰를 작성할 사찰을 선택해 주세요.', hideCustomModal);
            return;
        }

        if (formData.rating === 0) {
            showCustomModal('별점을 1점 이상 선택해주세요.', hideCustomModal);
            return;
        }

        if (!formData.visitedAt) {
            showCustomModal('방문일을 입력해주세요.', hideCustomModal);
            return;
        }

        const submitData = new FormData();
        // 🚨🚨🚨 태그 처리 로직 수정: 텍스트 입력과 체크박스 선택된 태그를 모두 합쳐서 백엔드로 보낼 배열 생성 🚨🚨🚨
        const allTags = new Set();
        // 1. 텍스트 입력 필드의 태그 추가
        formData.reviewTags.split(',').map(tag => tag.trim().replace(/^#/, '')).filter(tag => tag !== '').forEach(tag => allTags.add(tag));
        // 2. 체크박스에서 선택된 태그 추가 (selectedCheckboxTags는 이미 Set 형태)
        selectedCheckboxTags.forEach(tag => allTags.add(tag));
        const parsedTags = Array.from(allTags);


        // 🚨🚨🚨 백엔드에 사진 설명을 보내기 위한 fileDescriptions 배열 생성 🚨🚨🚨
        // 백엔드 ReviewRegisterRequestDTO에 List<String> fileDescriptions; 필드가 존재해야 합니다.
        const fileDescriptionsArray = Object.values(newPhotoDescriptions); // newPhotoDescriptions 객체의 값들만 배열로 변환

        const requestDTO = new Blob([JSON.stringify({
            title: formData.title,
            content: formData.content,
            rating: formData.rating,
            templeId: formData.templeId,
            reviewTags: parsedTags, // 파싱된 태그 배열 (이제 텍스트와 체크박스 태그가 모두 포함됨)
            visitedAt: formData.visitedAt,
            // 🚨🚨🚨 여기서 user.username 대신 user.displayName 또는 user.memberUsername (AuthContext에 있다면) 사용 🚨🚨🚨
            memberUsername: user.displayName || user.username, // ⭐ 이 부분을 수정! AuthContext에 displayName이 있으면 그걸 쓰고, 없으면 username (loginId)을 사용 ⭐
            memberNo: user.memberNo,
            fileDescriptions: fileDescriptionsArray // 💡 파일 설명 배열 추가
        })], { type: 'application/json' });

        // 🌟🌟🌟 이 부분을 수정했어! 'request' 대신 'reviewRegisterRequest'로 변경! 🌟🌟🌟
        submitData.append('reviewRegisterRequest', requestDTO);

        formData.imageFiles.forEach((file) => {
            submitData.append('files', file);
        });

        try {
            console.log("리뷰 등록 요청 DTO 데이터:", {
                title: formData.title,
                content: formData.content,
                rating: formData.rating,
                templeId: formData.templeId,
                reviewTags: parsedTags, // ⭐ 로그에도 반영 ⭐
                visitedAt: formData.visitedAt,
                memberUsername: user.displayName || user.username, // ⭐ 로그에도 반영 ⭐
                memberNo: user.memberNo,
                fileDescriptions: fileDescriptionsArray // 로그에도 사진 설명 포함
            });

            const response = await axios.post(`${BACKEND_BASE_URL}/api/reviews`, submitData, {
                headers: {
                    'Content-Type': 'multipart/form-data',
                    'Authorization': `Bearer ${localStorage.getItem('accessToken')}`
                },
            });
            console.log('리뷰 등록 성공:', response.data);
            showCustomModal('리뷰가 성공적으로 등록되었습니다! 🎉', () => {
                hideCustomModal();
                navigate('/reviews');
            });
        } catch (error) {
            console.error('리뷰 등록 실패:', error);
            if (error.response) {
                console.error('응답 데이터:', error.response.data);
                console.error('응답 상태:', error.response.status);
                if (error.response.data && error.response.data.errors && error.response.data.errors.length > 0) {
                    // 백엔드에서 보낸 유효성 검사 에러 메시지를 좀 더 명확하게 보여주기
                    const validationErrors = error.response.data.errors.map(err => err.defaultMessage).join('\n');
                    showCustomModal(`리뷰 등록 실패:\n${validationErrors}`, hideCustomModal);
                } else {
                    showCustomModal(`리뷰 등록 실패: ${error.response.data.message || error.message}`, hideCustomModal);
                }
            } else if (error.request) {
                console.error('요청을 보내지 못함:', error.request);
                showCustomModal('네트워크 오류: 서버에 연결할 수 없습니다.', hideCustomModal);
            } else {
                showCustomModal(`리뷰 등록 중 알 수 없는 오류: ${error.message}`, hideCustomModal);
            }
        }
    }, [formData, loadingAuth, isLoggedIn, navigate, showCustomModal, hideCustomModal, user, BACKEND_BASE_URL, newPhotoDescriptions, selectedCheckboxTags]); // selectedCheckboxTags 의존성 추가

    // --- 렌더링 ---
    return (
        // 💡 컨테이너 여백 및 너비 조정: p-6 -> p-4, max-w-screen-md 추가
        <div className="container mx-auto p-4 bg-white shadow-lg rounded-lg my-8 max-w-screen-md"> 
            {modal.show && (
                <CustomModal
                    message={modal.message}
                    onConfirm={modal.onConfirm}
                    onCancel={modal.onCancel}
                    showCancel={modal.showCancel}
                />
            )}

            {/* 💡 제목 여백 조정: mb-8 -> mb-6 */}
            <h1 className="text-4xl font-bold text-center mb-6 text-blue-700">리뷰 등록 ✍️</h1>
            {/* 💡 폼 내부 요소 간격 조정: space-y-6 -> space-y-4 */}
            <form onSubmit={handleSubmit} className="space-y-4"> 
                {/* 💡 사찰 검색 필드 */}
                {/* label과 input 사이 mb-2 -> mb-1 또는 mb-0.5, div의 mb 조정 */}
                <div className="relative mb-4"> {/* mb-6 -> mb-4 */}
                    <label htmlFor="templeSearch" className="block text-xl font-semibold text-gray-800 mb-1"> {/* mb-2 -> mb-1 */}
                        사찰 이름 검색 <span className="text-red-500">*</span>
                    </label>
                    <input
                        type="text"
                        id="templeSearch"
                        value={searchTerm}
                        onChange={handleSearchTermChange}
                        onFocus={() => setShowSearchResults(true)}
                        onBlur={() => setTimeout(() => setShowSearchResults(false), 100)}
                        placeholder="리뷰할 사찰 이름을 검색하세요 (예: 불국사)"
                        className="w-full p-3 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 text-lg"
                        required
                    />
                    {showSearchResults && searchResults.length > 0 && (
                        <ul className="absolute z-10 w-full bg-white border border-gray-300 rounded-md shadow-lg mt-1 max-h-60 overflow-y-auto">
                            {searchResults.map((temple) => (
                                <li
                                    key={temple.templeId}
                                    onMouseDown={() => handleSelectTemple(temple)}
                                    className="p-3 cursor-pointer hover:bg-blue-100 text-lg border-b border-gray-200 last:border-b-0"
                                >
                                    {temple.templeName}
                                </li>
                            ))}
                        </ul>
                    )}
                    {selectedTemple && (
                        <p className="mt-2 text-md text-green-700 font-semibold">
                            선택된 사찰: {selectedTemple.name} (ID: {selectedTemple.id})
                        </p>
                    )}
                    {!selectedTemple && searchTerm && searchResults.length === 0 && !showSearchResults && (
                        <p className="mt-2 text-md text-red-500">
                            검색 결과가 없습니다. 다시 시도하거나 사찰을 정확히 입력해주세요.
                        </p>
                    )}
                </div>

                {/* 제목 */}
                {/* div의 mb 조정 */}
                <div className="mb-4"> {/* mb-6 -> mb-4 */}
                    <label htmlFor="title" className="block text-xl font-semibold text-gray-800 mb-1"> {/* mb-2 -> mb-1 */}
                        제목 <span className="text-red-500">*</span>
                    </label>
                    <input
                        type="text"
                        id="title"
                        name="title"
                        value={formData.title}
                        onChange={handleChange}
                        placeholder="리뷰 제목을 입력하세요"
                        className="w-full p-3 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 text-lg"
                        required
                    />
                </div>

                {/* 내용 */}
                {/* div의 mb 조정 */}
                <div className="mb-4"> {/* mb-6 -> mb-4 */}
                    <label htmlFor="content" className="block text-xl font-semibold text-gray-800 mb-1"> {/* mb-2 -> mb-1 */}
                        내용 <span className="text-red-500">*</span>
                    </label>
                    <textarea
                        id="content"
                        name="content"
                        value={formData.content}
                        onChange={handleChange}
                        placeholder="리뷰 내용을 작성해주세요"
                        rows="8"
                        className="w-full p-3 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 text-lg resize-y"
                        required
                    ></textarea>
                </div>

                {/* 별점 */}
                {/* div의 mb 조정 */}
                <div className="mb-4"> {/* mb-6 -> mb-4 */}
                    <label className="block text-xl font-semibold text-gray-800 mb-1"> {/* mb-2 -> mb-1 */}
                        별점 <span className="text-red-500">*</span>
                    </label>
                    <div className="flex items-center space-x-1">
                        {[1, 2, 3, 4, 5].map((star) => (
                            <span
                                key={star}
                                className={`text-4xl cursor-pointer ${formData.rating >= star ? 'text-yellow-400' : 'text-gray-300'
                                    }`}
                                onClick={() => handleRatingChange(star)}
                            >
                                ★
                            </span>
                        ))}
                    </div>
                </div>

                {/* 💡 방문일 필드 추가 */}
                {/* div의 mb 조정 */}
                <div className="mb-4"> {/* mb-6 -> mb-4 */}
                    <label htmlFor="visitedAt" className="block text-xl font-semibold text-gray-800 mb-1"> {/* mb-2 -> mb-1 */}
                        방문일 <span className="text-red-500">*</span>
                    </label>
                    <input
                        type="date"
                        id="visitedAt"
                        name="visitedAt"
                        value={formData.visitedAt}
                        onChange={handleChange}
                        className="w-full p-3 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 text-lg"
                        required
                        max={new Date().toISOString().split('T')[0]} // ⭐ 이 부분이 추가되었어! ⭐
                    />
                </div>

                {/* 태그 입력 필드 */}
                {/* div의 mb 조정 */}
                <div className="mb-4"> {/* mb-6 -> mb-4 */}
                    <label htmlFor="reviewTags" className="block text-xl font-semibold text-gray-800 mb-1"> {/* mb-2 -> mb-1 */}
                        태그 입력 (쉼표로 구분)
                    </label>
                    <input
                        type="text"
                        id="reviewTags"
                        name="reviewTags"
                        value={formData.reviewTags}
                        onChange={handleTagChange}
                        placeholder="예: #힐링, #조용함, #경치 좋음"
                        className="w-full p-3 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 text-lg"
                    />
                {/* 💡 자주 쓰는 태그 목록 및 체크박스 */}
                <div className="mt-3"> {/* mt-4 -> mt-3 (여백 줄임) */}
                    <p className="text-lg font-semibold text-gray-800 mb-2">자주 쓰는 태그:</p>
                    <div className="flex flex-wrap gap-2">
                        {commonTags.map(tag => (
                            <label
                                key={tag}
                                className={`inline-flex items-center px-3 py-1 border rounded-full text-sm font-medium cursor-pointer transition-colors duration-200
                                    ${selectedCheckboxTags.has(tag)
                                        ? 'bg-blue-500 text-white border-blue-500'
                                        : 'bg-gray-100 text-gray-700 border-gray-300 hover:bg-blue-50 hover:border-blue-300'
                                    }`}
                            >
                                <input
                                    type="checkbox"
                                    className="form-checkbox h-4 w-4 text-blue-600 rounded mr-2"
                                    checked={selectedCheckboxTags.has(tag)}
                                    onChange={() => handleCheckboxTagChange(tag)}
                                />
                                #{tag}
                            </label>
                        ))}
                    </div>
                </div>
                </div>

                {/* 💡 사진 업로드 필드 및 사진 설명 입력 추가 */}
                {/* border p-4 -> border p-3, bg-gray-50 -> bg-gray-10 */}
                <div className="border p-3 rounded-lg bg-gray-10"> 
                    <label htmlFor="imageFiles" className="block text-xl font-semibold text-gray-800 mb-1"> {/* mb-2 -> mb-1 */}
                        사진 첨부 (최대 5장)
                    </label>
                    <input
                        type="file"
                        id="imageFiles"
                        ref={fileInputRef}
                        multiple
                        accept="image/*"
                        onChange={handleImageChange}
                        className="block w-full text-lg text-gray-700
                                        file:mr-4 file:py-2 file:px-4
                                        file:rounded-md file:border-0
                                        file:text-sm file:font-semibold
                                        file:bg-blue-50 file:text-blue-700
                                        hover:file:bg-blue-100"
                    />
                    {/* mt-4 -> mt-3 */}
                    <div className="mt-3 grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-3"> {/* gap-4 -> gap-3 */}
                        {imagePreviews.map((src, index) => (
                            <div key={index} className="relative border rounded-lg overflow-hidden shadow-sm">
                                <img
                                    src={src}
                                    alt={`리뷰 이미지 ${index + 1}`}
                                    className="w-full h-32 object-cover" // Fixed height for image
                                />
                                <div className="p-2"> {/* Padding for content inside */}
                                    {/* 💡 사진 설명 입력 필드 추가 */}
                                    <input
                                        type="text"
                                        placeholder="사진 설명 (선택 사항)"
                                        value={newPhotoDescriptions[index] || ''}
                                        onChange={(e) => handleNewPhotoDescriptionChange(index, e.target.value)}
                                        className="w-full px-2 py-1 border rounded-md text-sm mt-1"
                                    />
                                    <button
                                        type="button"
                                        onClick={() => handleRemoveImage(index)}
                                        className="w-full bg-red-500 text-white py-1 rounded-md hover:bg-red-600 transition duration-200 mt-2" // Full width button
                                    >
                                        삭제
                                    </button>
                                </div>
                            </div>
                        ))}
                    </div>
                </div>

                {/* 제출 및 취소 버튼 */}
                {/* mt-8 -> mt-6 */}
                <div className="flex justify-end space-x-4 mt-6"> 
                    <button
                        type="button"
                        onClick={() => navigate('/reviews')}
                        className="px-6 py-3 bg-gray-600 text-white font-semibold rounded-lg shadow-md hover:bg-gray-700 transition duration-300"
                    >
                        취소
                    </button>
                    <button
                        type="submit"
                        className="px-6 py-3 bg-green-600 text-white font-semibold rounded-lg shadow-md hover:bg-green-700 transition duration-300"
                    >
                        리뷰 등록하기
                    </button>
                </div>
            </form>
        </div>
    );
}

export default ReviewRegister;