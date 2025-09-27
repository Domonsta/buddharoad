// src/Reviews/ReviewUpdate.js

import React, { useState, useEffect, useCallback, useRef } from 'react';
import { useParams, useNavigate, useLocation } from 'react-router-dom';
import axios from 'axios';
import { useAuth } from '../Context/AuthContext'; // AuthContext 경로 확인

// 💡 커스텀 모달 컴포넌트 (alert/confirm 대체) - ReviewRegister와 동일
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

// ⭐ 현재 날짜를-MM-DD 형식으로 반환하는 헬퍼 함수 추가
const getTodayDate = () => {
    const today = new Date();
    const year = today.getFullYear();
    const month = String(today.getMonth() + 1).padStart(2, '0'); // 월은 0부터 시작하므로 +1
    const day = String(today.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
};

function ReviewUpdate() {
    const { reviewId } = useParams(); // URL에서 reviewId 가져오기
    const navigate = useNavigate();
    const location = useLocation();
    // 💡 AuthContext 훅 사용: 로그인 상태, 역할, 회원 번호, 액세스 토큰, 로딩 상태
    const { isLoggedIn, user, accessToken, loading: loadingAuth } = useAuth(); // loading 변수명을 loadingAuth로 변경하여 충돌 방지

    // user 객체에서 role과 memberNo를 추출
    const userRole = user?.role; // user가 null일 경우를 대비하여 옵셔널 체이닝 사용
    const loggedInMemberNo = user?.memberNo; // user가 null일 경우를 대비하여 옵셔널 체이닝 사용

    const [formData, setFormData] = useState({
        title: '',
        content: '',
        rating: 0,
        tags: '', 
        visitDate: '',
        templeId: null, // ⭐ 사찰 ID 추가
        templeName: '', // ⭐ 사찰 이름 추가
    });
    const [loadingReview, setLoadingReview] = useState(true);
    const [error, setError] = useState(null);

    // 💡 사진 관련 상태: 기존 사진, 새로운 사진 파일, 삭제될 사진 ID, 전체 미리보기
    const [existingPhotos, setExistingPhotos] = useState([]); // { photoId, photoUrl, description }
    const [newPhotos, setNewPhotos] = useState([]); // 실제 파일 객체들
    const [deletedPhotoIds, setDeletedPhotoIds] = useState([]); // 삭제할 기존 사진 ID 목록
    const [photoPreviews, setPhotoPreviews] = useState([]); // 미리보기 URL과 설명을 포함한 객체 배열 (기존 + 신규)

    const photoInputRef = useRef(null); // 파일 입력 참조

    // 💡 이미지 확대 모달 관련 상태
    const [showImageModal, setShowImageModal] = useState(false);
    const [modalImageUrl, setModalImageUrl] = useState(''); // 초기값 null 대신 빈 문자열
    const [modalImageDescription, setModalImageDescription] = useState(''); // 초기값 null 대신 빈 문자열

    // 💡 커스텀 모달 상태
    const [modal, setModal] = useState({
        show: false,
        message: '',
        onConfirm: () => {},
        onCancel: () => {},
        showCancel: false,
    });

    // ✨✨✨ ReviewRegister에서 복사해온 태그 관련 상태 및 함수 시작 ✨✨✨
    // 💡 자주 쓰는 태그 목록
    const commonTags = [
        '힐링', '조용함', '경치 좋음', '사진찍기좋은곳', '걷기좋은길',
        '역사', '문화유산', '템플스테이', '가족여행', '친구와함께',
        '나홀로여행', '사찰음식', '아름다운', '고즈넉한', '도심속힐링'
    ];
    // 💡 선택된 체크박스 태그 상태 (Set으로 관리하여 중복 방지)
    const [selectedCheckboxTags, setSelectedCheckboxTags] = useState(new Set());
    // ✨✨✨ 태그 관련 상태 및 함수 끝 ✨✨✨

    // ✅ 백엔드 서버의 기본 URL을 상수로 정의합니다.
    const BACKEND_BASE_URL = "http://localhost:8080";

    // ⭐ showCustomModal 함수를 useCallback으로 감싸기 (무한 렌더링 방지)
    const showCustomModal = useCallback((message, onConfirm, showCancel = false, onCancel = () => {}) => {
        setModal({ show: true, message, onConfirm, onCancel, showCancel });
    }, []); 

    // ⭐ hideCustomModal 함수를 useCallback으로 감싸기 (무한 렌더링 방지)
    const hideCustomModal = useCallback(() => {
        setModal(prev => ({ ...prev, show: false })); 
    }, []); 

    // 💡 ReviewRegister처럼 로그인/권한 체크 useEffect 추가 (AuthContext 로딩 후)
    useEffect(() => {
        if (!loadingAuth) {
            console.log("DEBUG: [ReviewUpdate.js - useEffect] Current User:", user);

            // GUEST 역할은 리뷰 작성/수정 불가능 (로그아웃과 동일 처리)
            if (!isLoggedIn || (user && user.role === 'GUEST') || user === null) {
                console.warn("DEBUG: [ReviewUpdate.js - useEffect] 사용자 권한 없음 또는 로그인 안됨. 로그인 페이지로 리다이렉트.");
                showCustomModal(
                    '리뷰를 수정할 권한이 없습니다. 로그인 해주세요.',
                    () => {
                        hideCustomModal();
                        navigate('/login', { state: { from: location.pathname } });
                    },
                    false
                );
            }
        }
    }, [isLoggedIn, user, loadingAuth, navigate, location.pathname, showCustomModal, hideCustomModal]);


    // 💡 리뷰 상세 정보 및 권한 로드 (기존 useEffect)
    useEffect(() => {
        const fetchReviewAndCheckPermission = async () => {
            console.log("DEBUG: [ReviewUpdate.js - useEffect] fetchReviewAndCheckPermission 시작");
            console.log("DEBUG: [ReviewUpdate.js - useEffect] loadingAuth (AuthContext):", loadingAuth); 
            console.log("DEBUG: [ReviewUpdate.js - useEffect] isLoggedIn:", isLoggedIn);
            console.log("DEBUG: [ReviewUpdate.js - useEffect] userRole:", userRole);
            console.log("DEBUG: [ReviewUpdate.js - useEffect] loggedInMemberNo:", loggedInMemberNo);
            console.log("DEBUG: [ReviewUpdate.js - useEffect] accessToken (초기):", accessToken ? `${accessToken.substring(0, 10)}...` : "없음");

            // ⭐ loadingAuth가 true면 아직 인증 정보 로딩 중이므로 대기
            if (loadingAuth) {
                console.log("DEBUG: [ReviewUpdate.js - useEffect] AuthContext 로딩 중, 대기.");
                return;
            }

            // ⭐ loadingAuth가 false인데 로그인 상태가 아니라면 로그인 페이지로 이동 (중요!)
            if (!isLoggedIn) {
                console.warn("DEBUG: [ReviewUpdate.js - useEffect] 로그인되지 않았습니다. (하단 useEffect) 로그인 페이지로 이동.");
                return;
            }

            // ⭐ loggedInMemberNo가 undefined인 경우 (토큰 문제 등) 추가 방어 로직
            if (loggedInMemberNo === undefined || loggedInMemberNo === null) {
                console.warn("DEBUG: [ReviewUpdate.js - useEffect] 사용자 회원 번호를 가져오지 못했습니다. 인증 문제일 수 있습니다.");
                showCustomModal(
                    '사용자 정보를 가져오는 데 문제가 발생했습니다. 다시 로그인해주세요.',
                    () => { hideCustomModal(); navigate('/login'); },
                    false
                );
                return;
            }

            try {
                setLoadingReview(true);
                const headers = accessToken ? { Authorization: `Bearer ${accessToken}` } : {};
                console.log("DEBUG: [ReviewUpdate.js - useEffect] GET 요청 헤더:", headers);
                
                const response = await axios.get(`${BACKEND_BASE_URL}/api/reviews/${reviewId}`, { headers });
                const reviewData = response.data;
                console.log("DEBUG: [ReviewUpdate.js - useEffect] 리뷰 데이터 성공적으로 가져옴:", reviewData);

                // 💡 권한 확인 (작성자 또는 관리자)
                const isAdmin = userRole === 'CONTENT_ADMIN' || userRole === 'SYSTEM_ADMIN'; 
                console.log("DEBUG: [ReviewUpdate.js - useEffect] 리뷰 작성자 memberNo:", reviewData.memberNo);
                console.log("DEBUG: [ReviewUpdate.js - useEffect] 로그인된 사용자 memberNo:", loggedInMemberNo);
                console.log("DEBUG: [ReviewUpdate.js - useEffect] isAdmin (관리자 여부):", isAdmin);

                if (reviewData.memberNo !== loggedInMemberNo && !isAdmin) {
                    console.warn("DEBUG: [ReviewUpdate.js - useEffect] 권한 없음: 작성자 불일치 및 관리자 아님.");
                    showCustomModal(
                        '이 리뷰를 수정할 권한이 없습니다.',
                        () => { hideCustomModal(); navigate(`/reviews/${reviewId}`); },
                        false
                    );
                    return;
                }
                console.log("DEBUG: [ReviewUpdate.js - useEffect] 리뷰 수정 권한 확인 완료.");

                // 폼 데이터 초기화
                setFormData({
                    title: reviewData.title,
                    content: reviewData.content,
                    rating: reviewData.rating,
                    tags: reviewData.tags ? reviewData.tags.split(',').map(tag => `#${tag.trim()}`).join(', ') : '', 
                    visitDate: reviewData.visitedAt ? reviewData.visitedAt.split('T')[0] : '', // -MM-DD 형식으로 변환
                    templeId: reviewData.templeId, 
                    templeName: reviewData.templeName, 
                });

                // 기존 사진들 초기화
                const initialPhotos = reviewData.photos.map((photo) => ({
                    photoId: photo.photoId,
                    url: `${BACKEND_BASE_URL}${photo.photoUrl}`, 
                    description: photo.description || '', // ⭐ photo.description이 null일 경우 대비
                    isNew: false, 
                }));
                setExistingPhotos(initialPhotos);
                setPhotoPreviews(initialPhotos); // 미리보기도 기존 사진으로 초기화
                console.log("DEBUG: [ReviewUpdate.js - useEffect] 폼 데이터 및 사진 프리뷰 초기화 완료:", initialPhotos);

            } catch (err) {
                console.error("ERROR: [ReviewUpdate.js - useEffect] 리뷰 정보 가져오기 실패:", err);
                setError("리뷰 정보를 가져오는 데 실패했습니다. " + (err.response?.data?.message || err.message));
                showCustomModal(
                    `리뷰 정보를 가져오는 데 실패했습니다: ${err.response?.data?.message || err.message}`,
                    () => { hideCustomModal(); navigate(-1); }
                );
            } finally {
                setLoadingReview(false);
                console.log("DEBUG: [ReviewUpdate.js - useEffect] fetchReviewAndCheckPermission 종료.");
            }
        };

        if (reviewId && !loadingAuth && isLoggedIn && userRole !== undefined && loggedInMemberNo !== undefined) {
            fetchReviewAndCheckPermission();
        } else if (loadingAuth) {
            console.log("DEBUG: [ReviewUpdate.js - useEffect] 아직 AuthContext 로딩 중, fetchReviewAndCheckPermission 실행 대기.");
        }
    }, [reviewId, isLoggedIn, userRole, loggedInMemberNo, accessToken, loadingAuth, navigate, showCustomModal, hideCustomModal, location.pathname, BACKEND_BASE_URL]); 

    // 💡 `formData.tags`가 변경될 때마다 `selectedCheckboxTags` 동기화 (선택된 태그가 input에 반영되도록)
    useEffect(() => {
        const currentInputTags = new Set(formData.tags.split(',').map(tag => tag.trim().replace(/^#/, '')).filter(tag => tag !== ''));
        const newSelectedCheckboxTags = new Set();
        commonTags.forEach(tag => {
            if (currentInputTags.has(tag)) {
                newSelectedCheckboxTags.add(tag);
            }
        });
        setSelectedCheckboxTags(newSelectedCheckboxTags);
        console.log("DEBUG: [ReviewUpdate.js - useEffect] selectedCheckboxTags updated based on formData.tags:", newSelectedCheckboxTags);
    }, [formData.tags]);


    // 폼 데이터 변경 핸들러
    const handleChange = useCallback((e) => {
        const { name, value } = e.target;
        setFormData((prev) => ({ ...prev, [name]: value }));
        console.log(`DEBUG: [ReviewUpdate.js - handleChange] ${name} changed to: ${value}`);
    }, []);

    // 평점 변경 핸들러
    const handleRatingChange = useCallback((newRating) => {
        setFormData((prev) => ({ ...prev, rating: newRating }));
        console.log("DEBUG: [ReviewUpdate.js - handleRatingChange] Rating changed to:", newRating);
    }, []);

    // 💡 새로운 사진 파일 선택 핸들러 (ReviewRegister와 유사하게 수정, 파일 개수 제한)
    const handleNewPhotoChange = useCallback((e) => {
        const files = Array.from(e.target.files);
        // 현재 표시된 사진 개수 (기존 + 신규) + 새로 추가될 사진 개수
        if (photoPreviews.length + files.length > 5) {
            showCustomModal('사진은 최대 5개까지 업로드할 수 있습니다.', hideCustomModal);
            if (photoInputRef.current) {
                photoInputRef.current.value = ''; // input 초기화
            }
            console.warn("WARN: [ReviewUpdate.js - handleNewPhotoChange] 사진 개수 초과 (최대 5개).");
            return;
        }

        const newFilesToAdd = [];

        files.forEach((file) => {
            newFilesToAdd.push({
                file, 
                isNew: true, 
                url: URL.createObjectURL(file), 
                description: '',
            });
        });

        setNewPhotos((prev) => {
            const updatedNewPhotos = [...prev, ...files];
            console.log("DEBUG: [ReviewUpdate.js - handleNewPhotoChange] newPhotos updated:", updatedNewPhotos);
            return updatedNewPhotos;
        });
        setPhotoPreviews((prev) => {
            const updatedPhotoPreviews = [...prev, ...newFilesToAdd];
            console.log("DEBUG: [ReviewUpdate.js - handleNewPhotoChange] photoPreviews (after adding new files):", updatedPhotoPreviews);
            return updatedPhotoPreviews;
        });

        if (photoInputRef.current) {
            photoInputRef.current.value = ''; // input 초기화
        }
    }, [photoPreviews.length, showCustomModal, hideCustomModal]);


    // 💡 사진 설명 변경 핸들러 (기존/신규 모두)
    const handlePhotoDescriptionChange = useCallback((index, description) => {
        setPhotoPreviews((prev) => {
            const newState = prev.map((photo, i) => (i === index ? { ...photo, description } : photo));
            console.log("DEBUG: [ReviewUpdate.js - handlePhotoDescriptionChange] photoPreviews after description change (index: " + index + ", new description: '" + description + "'):", newState);
            return newState;
        });
    }, []);

    // 💡 사진 제거 핸들러 (기존/신규 모두)
    const handleRemovePhoto = useCallback((index) => {
        showCustomModal('이 사진을 삭제하시겠습니까?', () => {
            hideCustomModal();
            const removedPhoto = photoPreviews[index];

            if (removedPhoto.isNew) {
                setNewPhotos((prev) => {
                    const updatedNewPhotos = prev.filter((file) => file !== removedPhoto.file);
                    console.log("DEBUG: [ReviewUpdate.js - handleRemovePhoto] New photo removed, newPhotos:", updatedNewPhotos);
                    return updatedNewPhotos;
                });
                if (removedPhoto.url) {
                    URL.revokeObjectURL(removedPhoto.url); // 메모리 누수 방지
                    console.log("DEBUG: [ReviewUpdate.js - handleRemovePhoto] Revoked URL for new photo:", removedPhoto.url);
                }
            } else {
                setDeletedPhotoIds((prev) => {
                    const updatedDeletedPhotoIds = [...prev, removedPhoto.photoId];
                    console.log("DEBUG: [ReviewUpdate.js - handleRemovePhoto] Existing photo marked for deletion, deletedPhotoIds:", updatedDeletedPhotoIds);
                    return updatedDeletedPhotoIds;
                });
                setExistingPhotos((prev) => {
                    const updatedExistingPhotos = prev.filter((photo) => photo.photoId !== removedPhoto.photoId);
                    console.log("DEBUG: [ReviewUpdate.js - handleRemovePhoto] Existing photo removed from existingPhotos:", updatedExistingPhotos);
                    return updatedExistingPhotos;
                });
            }
            setPhotoPreviews((prev) => {
                const updatedPhotoPreviews = prev.filter((_, i) => i !== index); 
                console.log("DEBUG: [ReviewUpdate.js - handleRemovePhoto] photoPreviews after removal:", updatedPhotoPreviews);
                return updatedPhotoPreviews;
            });
        }, true, hideCustomModal);
    }, [photoPreviews, showCustomModal, hideCustomModal]);


    // 💡 이미지 확대 모달 열기
    const handleImageClick = useCallback((url, description) => {
        setModalImageUrl(url);
        setModalImageDescription(description);
        setShowImageModal(true);
        console.log("DEBUG: [ReviewUpdate.js - handleImageClick] Image modal opened for URL:", url);
    }, []);

    // 💡 이미지 확대 모달 닫기
    const handleCloseImageModal = useCallback(() => {
        setShowImageModal(false);
        setModalImageUrl('');
        setModalImageDescription('');
        console.log("DEBUG: [ReviewUpdate.js - handleCloseImageModal] Image modal closed.");
    }, []);

    // 🚨🚨🚨 태그 입력 변경 핸들러 수정 (텍스트 입력 방식) 🚨🚨🚨
    const handleTagChange = useCallback((e) => {
        const { value } = e.target;
        setFormData(prev => ({ ...prev, tags: value })); 
        console.log("DEBUG: [ReviewUpdate.js - handleTagChange] Tag input changed to:", value);
    }, []);

    // ✨✨✨ ReviewRegister에서 복사해온 체크박스 태그 변경 핸들러 시작 ✨✨✨
    const handleCheckboxTagChange = useCallback((tag) => {
        setSelectedCheckboxTags(prev => {
            const newSet = new Set(prev);
            if (newSet.has(tag)) {
                newSet.delete(tag);
            } else {
                newSet.add(tag);
            }
            // 폼 데이터의 tags 업데이트 (텍스트 입력과 체크박스 선택 합치기)
            const currentTags = formData.tags.split(',').map(t => t.trim().replace(/^#/, '')).filter(t => t !== '');
            const combinedTags = Array.from(new Set([...currentTags, ...Array.from(newSet)])).filter(t => t !== '');
            
            setFormData(prevForm => ({ 
                ...prevForm, 
                tags: combinedTags.map(t => `#${t}`).join(', ') 
            }));
            console.log("DEBUG: [ReviewUpdate.js - handleCheckboxTagChange] Checkbox tag '" + tag + "' toggled. Current selectedCheckboxTags:", newSet);
            console.log("DEBUG: [ReviewUpdate.js - handleCheckboxTagChange] Combined formData.tags:", combinedTags.map(t => `#${t}`).join(', '));
            return newSet;
        });
    }, [formData.tags, setFormData]);
    // ✨✨✨ 체크박스 태그 변경 핸들러 끝 ✨✨✨


    // 폼 제출 핸들러
    const handleSubmit = useCallback(async (e) => {
        e.preventDefault();
        console.log("🚀 [ReviewUpdate.js - handleSubmit] 함수 실행됨!");
        console.log("DEBUG: [ReviewUpdate.js - handleSubmit] isLoggedIn:", isLoggedIn);
        console.log("DEBUG: [ReviewUpdate.js - handleSubmit] userRole:", userRole);
        console.log("DEBUG: [ReviewUpdate.js - handleSubmit] loggedInMemberNo:", loggedInMemberNo);
        console.log("DEBUG: [ReviewUpdate.js - handleSubmit] accessToken (제출 시):", accessToken ? `${accessToken.substring(0, 10)}...` : "없음");

        // 폼 유효성 검사
        if (!formData.title.trim()) {
            showCustomModal('리뷰 제목을 입력해주세요.', hideCustomModal);
            console.warn("WARN: [ReviewUpdate.js - handleSubmit] 유효성 검사 실패: 제목 없음.");
            return;
        }
        if (!formData.content.trim()) {
            showCustomModal('리뷰 내용을 입력해주세요.', hideCustomModal);
            console.warn("WARN: [ReviewUpdate.js - handleSubmit] 유효성 검사 실패: 내용 없음.");
            return;
        }
        if (formData.rating === 0) {
            showCustomModal('평점을 선택해주세요.', hideCustomModal);
            console.warn("WARN: [ReviewUpdate.js - handleSubmit] 유효성 검사 실패: 평점 없음.");
            return;
        }
        if (!formData.visitDate) {
            showCustomModal('방문일을 선택해주세요.', hideCustomModal);
            console.warn("WARN: [ReviewUpdate.js - handleSubmit] 유효성 검사 실패: 방문일 없음.");
            return;
        }
        // ⭐⭐⭐ 방문일이 미래 날짜인지 검사 추가 ⭐⭐⭐
        const today = getTodayDate();
        if (formData.visitDate > today) {
            showCustomModal('방문일은 미래 날짜를 선택할 수 없습니다.', hideCustomModal);
            console.warn("WARN: [ReviewUpdate.js - handleSubmit] 유효성 검사 실패: 방문일이 미래 날짜입니다.");
            return;
        }

        if (!accessToken) {
            console.warn("WARN: [ReviewUpdate.js - handleSubmit] accessToken이 없어 수정 요청 불가. 로그인 필요.");
            showCustomModal('로그인이 필요합니다.', () => { hideCustomModal(); navigate('/login'); });
            return;
        }

        showCustomModal("리뷰를 수정하시겠습니까?", async () => {
            hideCustomModal(); // 모달 닫기

            const submitFormData = new FormData();
            
            // 🚨🚨🚨 태그 처리 로직 수정: 텍스트 입력과 체크박스 선택된 태그를 모두 합쳐서 백엔드로 보낼 배열 생성 🚨🚨🚨
            const allTags = new Set();
            // 1. 텍스트 입력 필드의 태그 추가
            formData.tags.split(',').map(tag => tag.trim().replace(/^#/, '')).filter(tag => tag !== '').forEach(tag => allTags.add(tag));
            // 2. 체크박스에서 선택된 태그 추가
            selectedCheckboxTags.forEach(tag => allTags.add(tag));
            const parsedTags = Array.from(allTags); // 최종 전송될 태그 배열
            console.log("DEBUG: [ReviewUpdate.js - handleSubmit] 최종 전송될 태그 (parsedTags):", parsedTags);


            // 🚨🚨🚨 백엔드에 요청 DTO를 JSON Blob으로 변환하여 추가 (ReviewRegister 방식) 🚨🚨🚨
            const requestDTOData = {
                title: formData.title,
                content: formData.content,
                rating: formData.rating,
                tags: parsedTags.join(', '), // 파싱된 태그 배열을 쉼표로 구분된 문자열로 전송
                // ⭐⭐ visitDate 대신 visitedAt으로 변경 ⭐⭐
                visitedAt: formData.visitDate, 
                deletedPhotoIds: deletedPhotoIds, // 삭제할 photoId 배열
                // 수정된 기존 사진 설명 
                // ✨✨✨ 이 부분을 수정했어! ✨✨✨
                updatedPhotoDescriptions: photoPreviews
                    .filter(p => !p.isNew) // 기존 사진만 필터링
                    .map(p => ({ photoId: p.photoId, description: p.description || '' })), // photoId와 현재 description을 모두 보냄
                // 새로운 사진 파일들의 설명 배열
                newPhotoDescriptions: photoPreviews
                    .filter(p => p.isNew) 
                    .map(p => p.description || ''),
                memberNo: loggedInMemberNo // ⭐⭐ memberNo 추가 ⭐⭐
            };

            console.log("DEBUG: [ReviewUpdate.js - handleSubmit] requestDTOData (JSON object before Blob):", requestDTOData);

            // ⭐⭐ Blob 생성을 위해 JSON.stringify 사용 ⭐⭐
            const requestDTO = new Blob([JSON.stringify(requestDTOData)], { type: 'application/json' });

            // 🌟 'reviewUpdateRequest'라는 이름으로 DTO를 FormData에 추가 🌟
            submitFormData.append('reviewUpdateRequest', requestDTO);

            // 새로운 사진 파일들 추가
            newPhotos.forEach((file) => {
                submitFormData.append('newFiles', file); 
                console.log("DEBUG: [ReviewUpdate.js - handleSubmit] Adding new file to FormData:", file.name);
            });

            console.log("DEBUG: [ReviewUpdate.js - handleSubmit] FormData 구성 완료. 내용 확인:");
            for (let [key, value] of submitFormData.entries()) {
                if (key === 'reviewUpdateRequest') {
                    // Blob인 경우 내용 파싱 시도
                    try {
                        const blobText = await new Response(value).text(); // Blob을 텍스트로 읽음
                        console.log(`${key}:`, JSON.parse(blobText));
                    } catch (e) {
                        console.log(`${key}: (Blob content could not be parsed) ${value}`);
                    }
                } else {
                    console.log(`${key}:`, value);
                }
            }
            console.log("DEBUG: [ReviewUpdate.js - handleSubmit] PUT 요청 전송 시작...");

            try {
                const response = await axios.put(`${BACKEND_BASE_URL}/api/reviews/${reviewId}`, submitFormData, {
                    headers: {
                        'Content-Type': 'multipart/form-data',
                        Authorization: `Bearer ${accessToken}`,
                    },
                });
                console.log('✅ [ReviewUpdate.js - handleSubmit] 리뷰 수정 성공:', response.data);
                showCustomModal('리뷰가 성공적으로 수정되었습니다! 🎉', () => {
                    hideCustomModal();
                    navigate(`/reviews/${reviewId}`); // 수정된 리뷰 상세 페이지로 이동
                });
            } catch (err) {
                console.error('❌ [ReviewUpdate.js - handleSubmit] 리뷰 수정 실패:', err);
                const errorMessage = err.response?.data?.message || err.message || '알 수 없는 오류가 발생했습니다.';
                console.error("DEBUG: [ReviewUpdate.js - handleSubmit] 실패 상세 응답:", err.response); 
                showCustomModal(`❌ 리뷰 수정 실패: ${errorMessage}`, hideCustomModal);
            }
        }, true, hideCustomModal); // showCancel을 true로 설정해서 취소 버튼 보이게 함
    }, [formData, isLoggedIn, userRole, loggedInMemberNo, accessToken, navigate, showCustomModal, hideCustomModal, reviewId, deletedPhotoIds, newPhotos, photoPreviews, selectedCheckboxTags, BACKEND_BASE_URL]);

    // 로딩 중이거나 권한 확인 중일 때
    if (loadingAuth || loadingReview) {
        return <div className="text-center py-8 text-xl">정보를 불러오는 중... ⏳</div>;
    }

    // 에러 발생 시
    if (error) {
        return <div className="text-center py-8 text-red-500">{error}</div>;
    }

    return (
        <div className="container mx-auto p-4 bg-white shadow-lg rounded-lg my-8 max-w-screen-md">
            {modal.show && (
                <CustomModal
                    message={modal.message}
                    onConfirm={modal.onConfirm}
                    onCancel={modal.onCancel}
                    showCancel={modal.showCancel}
                />
            )}

            <h1 className="text-4xl font-bold text-center mb-6 text-blue-700">리뷰 수정 ✏️</h1>
            <form onSubmit={handleSubmit} className="space-y-4">
                {/* ⭐⭐⭐ 사찰 이름 및 ID 표시 부분 ⭐⭐⭐ */}
                <div className="mb-4">
                    <label className="block text-xl font-semibold text-gray-800 mb-1">
                        리뷰 사찰 정보
                    </label>
                    <p className="w-full p-3 border border-gray-300 rounded-md shadow-sm bg-gray-50 text-lg">
                        <span className="font-bold">{formData.templeName}</span> (ID: {formData.templeId})
                    </p>
                </div>

                {/* 제목 */}
                <div className="mb-4">
                    <label htmlFor="title" className="block text-xl font-semibold text-gray-800 mb-1">
                        제목 <span className="text-red-500">*</span>
                    </label>
                    <input
                        type="text"
                        id="title"
                        name="title"
                        value={formData.title}
                        onChange={handleChange}
                        className="w-full p-3 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 text-lg"
                        placeholder="리뷰 제목을 입력하세요."
                        required
                    />
                </div>

                {/* 내용 */}
                <div className="mb-4">
                    <label htmlFor="content" className="block text-xl font-semibold text-gray-800 mb-1">
                        내용 <span className="text-red-500">*</span>
                    </label>
                    <textarea
                        id="content"
                        name="content"
                        value={formData.content}
                        onChange={handleChange}
                        rows="8"
                        className="w-full p-3 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 text-lg resize-y" 
                        placeholder="자세한 리뷰 내용을 입력해주세요."
                        required
                    ></textarea>
                </div>

                {/* 별점 */}
                <div className="mb-4">
                    <label htmlFor="rating" className="block text-xl font-semibold text-gray-800 mb-1">
                        평점 <span className="text-red-500">*</span>
                    </label>
                    <div className="flex items-center space-x-1"> 
                        {[1, 2, 3, 4, 5].map((star) => (
                            <span
                                key={star}
                                className={`text-4xl cursor-pointer ${
                                    formData.rating >= star ? 'text-yellow-400' : 'text-gray-300'
                                }`}
                                onClick={() => handleRatingChange(star)}
                            >
                                ★
                            </span>
                        ))}
                    </div>
                </div>

                {/* 💡 방문일 필드 */}
                <div className="mb-4">
                    <label htmlFor="visitDate" className="block text-xl font-semibold text-gray-800 mb-1">
                        방문일 <span className="text-red-500">*</span>
                    </label>
                    <input
                        type="date"
                        id="visitDate"
                        name="visitDate"
                        value={formData.visitDate}
                        onChange={handleChange}
                        max={getTodayDate()}
                        className="w-full p-3 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 text-lg"
                        required
                    />
                </div>

                {/* 태그 입력 필드 */}
                <div className="mb-4">
                    <label htmlFor="tags" className="block text-xl font-semibold text-gray-800 mb-1">
                        태그 입력 (쉼표로 구분)
                    </label>
                    <input
                        type="text"
                        id="tags"
                        name="tags"
                        value={formData.tags}
                        onChange={handleTagChange}
                        placeholder="예: #힐링, #조용함, #경치 좋음"
                        className="w-full p-3 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 text-lg"
                    />
                    {/* ✨✨✨ 자주 쓰는 태그 섹션 ✨✨✨ */}
                    <div className="mt-3">
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

                {/* 💡 사진 업로드 섹션 */}
                <div className="border p-3 rounded-lg bg-gray-10">
                    <label htmlFor="photos" className="block text-xl font-semibold text-gray-800 mb-1">
                        사진 추가 (최대 5개, 현재 {photoPreviews.length}개)
                    </label>
                    <input
                        type="file"
                        id="photos"
                        ref={photoInputRef}
                        onChange={handleNewPhotoChange}
                        multiple
                        accept="image/*"
                        className="block w-full text-lg text-gray-700
                                         file:mr-4 file:py-2 file:px-4
                                         file:rounded-md file:border-0
                                         file:text-sm file:font-semibold
                                         file:bg-blue-50 file:text-blue-700
                                         hover:file:bg-blue-100" 
                    />
                    <p className="mt-1 text-sm text-gray-500">JPG, PNG, GIF 등 이미지 파일</p>

                    <div className="mt-3 grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-3"> 
                        {photoPreviews.map((photo, index) => (
                            <div
                                key={photo.isNew ? `new-${index}` : photo.photoId} 
                                className="relative border rounded-lg overflow-hidden shadow-sm" 
                            >
                                <img
                                    src={photo.url}
                                    alt={`미리보기 ${index + 1}`}
                                    className="w-full h-32 object-cover" 
                                    onClick={() => handleImageClick(photo.url, photo.description)}
                                />
                                <div className="p-2"> 
                                    <input
                                        type="text"
                                        placeholder="사진 설명 (선택 사항)"
                                        value={photo.description} 
                                        onChange={(e) => handlePhotoDescriptionChange(index, e.target.value)}
                                        className="w-full px-2 py-1 border rounded-md text-sm mt-1"
                                    />
                                    <button
                                        type="button"
                                        onClick={() => handleRemovePhoto(index)}
                                        className="w-full bg-red-500 text-white py-1 rounded-md hover:bg-red-600 transition duration-200 mt-2" 
                                        title="사진 삭제"
                                    >
                                        삭제
                                    </button>
                                </div>
                            </div>
                        ))}
                    </div>
                </div>

                <div className="flex justify-end gap-4 mt-6"> 
                    <button
                        type="button"
                        onClick={() => navigate(`/reviews/${reviewId}`)}
                        className="px-6 py-3 bg-gray-600 text-white font-semibold rounded-lg shadow-md hover:bg-gray-700 transition duration-300"
                    >
                        취소
                    </button>
                    <button
                        type="submit"
                        className="px-6 py-3 bg-blue-600 text-white font-semibold rounded-lg shadow-md hover:bg-blue-700 transition duration-300"
                    >
                        리뷰 수정하기
                    </button>
                </div>
            </form>

            {/* 💡 이미지 확대 모달 */}
            {showImageModal && (
                <div className="fixed inset-0 bg-black bg-opacity-75 flex items-center justify-center z-50 p-4">
                    <div className="relative bg-white rounded-lg p-2 max-w-3xl max-h-full overflow-auto">
                        <button
                            onClick={handleCloseImageModal}
                            className="absolute top-2 right-2 text-gray-800 text-3xl font-bold p-2 rounded-full bg-gray-200 hover:bg-gray-300"
                        >
                            &times;
                        </button>
                        <img
                            src={modalImageUrl}
                            alt={modalImageDescription || "확대 이미지"}
                            className="max-w-full max-h-[80vh] object-contain rounded-lg shadow-md"
                        />
                        {modalImageDescription && (
                            <p className="mt-2 text-center text-gray-700 text-sm">{modalImageDescription}</p>
                        )}
                    </div>
                </div>
            )}
        </div>
    );
}

export default ReviewUpdate;