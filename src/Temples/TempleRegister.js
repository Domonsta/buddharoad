// src/Temples/TempleRegister.js
import React, { useState, useEffect, useRef, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import { useAuth } from '../Context/AuthContext';

function TempleRegister() {
  const navigate = useNavigate();
  const { hasAdminPermission } = useAuth(); // 💡 관리자 권한 확인 훅

  const [formData, setFormData] = useState({
    templeName: '',
    feature: '',
    region: '', // Region enum의 String 값 (영어)
    phoneNumber: '',
    homepage: '',
    address: '',
    operatingHours: '',
    holidays: '',
    parkingInfo: '',
    admissionFee: '',
    restroomInfo: '',
    accessibility: '',
    culturalAssets: '',
    description: '',
  });

  const [displayRegion, setDisplayRegion] = useState('');
  const debounceTimer = useRef(null);

  const [photos, setPhotos] = useState([]); // 💡 사진 파일들을 저장할 상태
  const [photoPreviews, setPhotoPreviews] = useState([]); // 💡 사진 미리보기 URL들을 저장할 상태
  const [newPhotoDescriptions, setNewPhotoDescriptions] = useState({}); // 💡 새로 추가된 사진 설명을 위한 상태

  // 💡 이미지 확대 모달 관련 상태
  const [showImageModal, setShowImageModal] = useState(false);
  const [modalImageUrl, setModalImageUrl] = useState('');

  // 💡 이미지 클릭 시 모달 열기
  const handleImageClick = (imageUrl) => {
    setModalImageUrl(imageUrl);
    setShowImageModal(true);
  };

  // 💡 이미지 모달 닫기
  const handleCloseImageModal = () => {
    setShowImageModal(false);
    setModalImageUrl('');
  };

  const regionMap = {
    '서울': 'SEOUL',
    '부산': 'BUSAN',
    '대구': 'DAEGU',
    '인천': 'INCHEON',
    '광주': 'GWANGJU',
    '대전': 'DAEJEON',
    '울산': 'ULSAN',
    '세종': 'SEJONG',
    '경기': 'GYEONGGI',
    '강원': 'GANGWON',
    '충북': 'CHUNGCHEONGBUK',
    '충남': 'CHUNGCHEONGNAM',
    '전북': 'JEOLLABUK',
    '전남': 'JEOLLANAM',
    '경북': 'GYEONGSANGBUK',
    '경남': 'GYEONGSANGNAM',
    '제주': 'JEJU',
    '기타': 'ETC'
  };

  // 💡 디바운스 타이머 클린업
  useEffect(() => {
    return () => {
      if (debounceTimer.current) {
        clearTimeout(debounceTimer.current);
      }
      // 💡 컴포넌트 언마운트 시 생성된 URL 해제
      photoPreviews.forEach(url => URL.revokeObjectURL(url));
    };
  }, [photoPreviews]); // photoPreviews를 의존성 배열에 추가

  // 💡 관리자 권한이 없으면 접근 제한
  if (!hasAdminPermission()) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-red-100">
        <p className="text-red-700 text-2xl font-bold">⚠️ 관리자만 접근 가능합니다.</p>
      </div>
    );
  }

  const handleChange = (e) => {
    const { name, value } = e.target;
    if (name !== 'region') {
      setFormData(prev => ({ ...prev, [name]: value }));
    }
  };

  const handleRegionInputChange = (e) => {
    const inputValue = e.target.value;
    setDisplayRegion(inputValue);

    if (debounceTimer.current) {
      clearTimeout(debounceTimer.current);
    }

    debounceTimer.current = setTimeout(() => {
      setFormData(prev => ({
        ...prev,
        region: regionMap[inputValue] || inputValue
      }));
    }, 500);
  };

  const handlePhotoChange = (e) => {
    const files = Array.from(e.target.files);
    setPhotos(prevPhotos => [...prevPhotos, ...files]);

    const filePreviews = files.map(file => URL.createObjectURL(file));
    setPhotoPreviews(prev => [...prev, ...filePreviews]);

    // 💡 새로 추가되는 사진의 설명을 빈 문자열로 초기화
    const newDescriptions = {};
    files.forEach((file, i) => {
      newDescriptions[photos.length + i] = ''; // 현재 배열 길이를 기준으로 인덱스 부여
    });
    setNewPhotoDescriptions(prev => ({ ...prev, ...newDescriptions }));
  };

  const handleNewPhotoDescriptionChange = (index, description) => {
    setNewPhotoDescriptions(prev => ({ ...prev, [index]: description }));
  };

  const handleRemovePhoto = (indexToRemove) => {
    setPhotos(prevPhotos => prevPhotos.filter((_, index) => index !== indexToRemove));
    setPhotoPreviews(prev => { // 미리보기 URL도 함께 제거
      const newPreviews = prev.filter((_, index) => index !== indexToRemove);
      URL.revokeObjectURL(prev[indexToRemove]); // 메모리 해제
      return newPreviews;
    });
    setNewPhotoDescriptions(prev => {
      const updatedDescriptions = { ...prev };
      delete updatedDescriptions[indexToRemove];
      // 인덱스 재정렬 (선택 사항이지만, 일관성을 위해)
      const reindexedDescriptions = {};
      Object.keys(updatedDescriptions).forEach(key => {
        const newIndex = parseInt(key) > indexToRemove ? parseInt(key) - 1 : parseInt(key);
        reindexedDescriptions[newIndex] = updatedDescriptions[key];
      });
      return reindexedDescriptions;
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (debounceTimer.current) {
      clearTimeout(debounceTimer.current);
      setFormData(prev => ({
        ...prev,
        region: regionMap[displayRegion] || displayRegion
      }));
    }

    const token = localStorage.getItem('accessToken');
    if (!token) {
      alert('로그인이 필요합니다.');
      navigate('/login');
      return;
    }

    const form = new FormData();
    form.append('request', new Blob([JSON.stringify(formData)], { type: 'application/json' }));

    photos.forEach((photo) => {
      form.append(`files`, photo);
    });
    // 💡 newPhotoDescriptions는 현재 백엔드에서 files와 함께 처리할 DTO 필드가 없으므로 전송하지 않음
    // 이 부분은 백엔드 수정이 필요함을 사용자에게 안내해야 함

    try {
      await axios.post('http://localhost:8080/api/temples/register', form, {
        headers: {
          'Content-Type': 'multipart/form-data',
          Authorization: `Bearer ${token}`
        }
      });
      alert('✅ 사찰 정보가 성공적으로 등록되었습니다.');
      navigate('/temples');
    } catch (err) {
      console.error("사찰 등록 실패:", err);
      alert(`❌ 사찰 등록에 실패했습니다: ${err.response?.data.message || err.message}`);
    }
  };

  return (
    <div className="container mx-auto p-8 bg-white shadow-lg rounded-lg my-8">
      <h1 className="text-4xl font-bold text-center text-green-700 mb-8">
        ➕ 새로운 사찰 정보 등록 ➕
      </h1>
      <form onSubmit={handleSubmit} className="space-y-6">
        {/* 기본 정보 필드 */}
        <div>
          <label htmlFor="templeName" className="block text-lg font-medium text-gray-700 mb-2">사찰명 <span className="text-red-500">*</span></label>
          <input type="text" id="templeName" name="templeName" value={formData.templeName} onChange={handleChange} required
            className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-green-500 focus:border-green-500" />
        </div>
        <div>
          <label htmlFor="feature" className="block text-lg font-medium text-gray-700 mb-2">특징</label>
          <input type="text" id="feature" name="feature" value={formData.feature} onChange={handleChange}
            className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-green-500 focus:border-green-500" />
        </div>
        <div>
          <label htmlFor="region" className="block text-lg font-medium text-gray-700 mb-2">지역 <span className="text-red-500">*</span></label>
          <input type="text" id="region" name="region" value={displayRegion} onChange={handleRegionInputChange} required
            className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-green-500 focus:border-green-500"
            placeholder="예: 서울, 부산" />
        </div>
        <div>
          <label htmlFor="phoneNumber" className="block text-lg font-medium text-gray-700 mb-2">전화번호</label>
          <input type="text" id="phoneNumber" name="phoneNumber" value={formData.phoneNumber} onChange={handleChange}
            className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-green-500 focus:border-green-500" />
        </div>
        <div>
          <label htmlFor="homepage" className="block text-lg font-medium text-gray-700 mb-2">홈페이지</label>
          <input type="url" id="homepage" name="homepage" value={formData.homepage} onChange={handleChange}
            className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-green-500 focus:border-green-500" />
        </div>
        <div>
          <label htmlFor="address" className="block text-lg font-medium text-gray-700 mb-2">주소 <span className="text-red-500">*</span></label>
          <input type="text" id="address" name="address" value={formData.address} onChange={handleChange} required
            className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-green-500 focus:border-green-500" />
        </div>
        <div>
          <label htmlFor="operatingHours" className="block text-lg font-medium text-gray-700 mb-2">운영 시간</label>
          <input type="text" id="operatingHours" name="operatingHours" value={formData.operatingHours} onChange={handleChange}
            className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-green-500 focus:border-green-500" />
        </div>
        <div>
          <label htmlFor="holidays" className="block text-lg font-medium text-gray-700 mb-2">휴일</label>
          <input type="text" id="holidays" name="holidays" value={formData.holidays} onChange={handleChange}
            className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-green-500 focus:border-green-500" />
        </div>
        <div>
          <label htmlFor="parkingInfo" className="block text-lg font-medium text-gray-700 mb-2">주차 정보</label>
          <input type="text" id="parkingInfo" name="parkingInfo" value={formData.parkingInfo} onChange={handleChange}
            className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-green-500 focus:border-green-500" />
        </div>
        <div>
          <label htmlFor="admissionFee" className="block text-lg font-medium text-gray-700 mb-2">입장료</label>
          <input type="text" id="admissionFee" name="admissionFee" value={formData.admissionFee} onChange={handleChange}
            className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-green-500 focus:border-green-500" />
        </div>
        <div>
          <label htmlFor="restroomInfo" className="block text-lg font-medium text-gray-700 mb-2">화장실 정보</label>
          <input type="text" id="restroomInfo" name="restroomInfo" value={formData.restroomInfo} onChange={handleChange}
            className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-green-500 focus:border-green-500" />
        </div>
        <div>
          <label htmlFor="accessibility" className="block text-lg font-medium text-gray-700 mb-2">접근성</label>
          <input type="text" id="accessibility" name="accessibility" value={formData.accessibility} onChange={handleChange}
            className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-green-500 focus:border-green-500" />
        </div>
        <div>
          <label htmlFor="culturalAssets" className="block text-lg font-medium text-gray-700 mb-2">문화재 정보</label>
          <input type="text" id="culturalAssets" name="culturalAssets" value={formData.culturalAssets} onChange={handleChange}
            className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-green-500 focus:border-green-500" />
        </div>
        <div>
          <label htmlFor="description" className="block text-lg font-medium text-gray-700 mb-2">상세 설명</label>
          <textarea id="description" name="description" value={formData.description} onChange={handleChange} rows="5"
            className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-green-500 focus:border-green-500"
            placeholder="사찰에 대한 자세한 설명을 입력하세요."></textarea>
        </div>

        {/* 💡 사진 업로드 필드 */}
        <div className="border p-4 rounded-lg bg-gray-50">
          <label htmlFor="files" className="block text-lg font-medium text-gray-700 mb-2">사찰 사진 업로드</label>
          <input
            type="file"
            id="files"
            name="files"
            multiple
            accept="image/*"
            onChange={handlePhotoChange}
            className="w-full text-gray-700 file:mr-4 file:py-2 file:px-4 file:rounded-full file:border-0 file:text-sm file:font-semibold file:bg-green-50 file:text-green-700 hover:file:bg-green-100"
          />
          <div className="mt-4 grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4"> {/* Grid layout for previews */}
            {photoPreviews.map((previewUrl, index) => (
              <div key={index} className="relative border rounded-lg overflow-hidden shadow-sm">
                {/* 💡 이미지 클릭 시 모달 열기 */}
                <img
                  src={previewUrl}
                  alt={`새 사진 ${index + 1}`}
                  className="w-full h-32 object-cover cursor-pointer"
                  onClick={() => handleImageClick(previewUrl)}
                />
                <div className="p-2">
                  <span className="block text-sm text-gray-700 truncate">{photos[index].name}</span>
                  {/* 💡 사진 설명 입력 필드 */}
                  <input
                    type="text"
                    placeholder="사진 설명 (선택 사항)"
                    value={newPhotoDescriptions[index] || ''} // 💡 기본값을 빈 문자열로 설정
                    onChange={(e) => handleNewPhotoDescriptionChange(index, e.target.value)} // 💡 변경 핸들러 연결
                    className="w-full px-2 py-1 border rounded-md text-sm mt-1"
                  />
                  <button
                    type="button"
                    onClick={() => handleRemovePhoto(index)}
                    className="w-full bg-red-500 text-white py-1 rounded-md hover:bg-red-600 transition duration-200 mt-2"
                  >
                    삭제
                  </button>
                </div>
              </div>
            ))}
          </div>
        </div>

        <div className="flex justify-end gap-4 mt-8">
          <button
            type="button"
            onClick={() => navigate('/temples')}
            className="px-6 py-3 bg-gray-600 text-white font-semibold rounded-lg shadow-md hover:bg-gray-700 transition duration-300"
          >
            취소
          </button>
          <button
            type="submit"
            className="px-6 py-3 bg-green-600 text-white font-semibold rounded-lg shadow-md hover:bg-green-700 transition duration-300"
          >
            등록하기
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
            <img src={modalImageUrl} alt="확대 이미지" className="max-w-full max-h-screen object-contain" />
          </div>
        </div>
      )}
    </div>
  );
}

export default TempleRegister;