// src/Temples/TempleUpdate.js
import React, { useState, useEffect, useRef, useCallback, useMemo } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import axios from 'axios';
import { useAuth } from '../Context/AuthContext';

// 💡 regionMap과 reverseRegionMap을 컴포넌트 밖으로 이동하여 한 번만 생성되도록 함
const regionMap = {
  '서울': 'SEOUL', '부산': 'BUSAN', '대구': 'DAEGU', '인천': 'INCHEON',
  '광주': 'GWANGJU', '대전': 'DAEJEON', '울산': 'ULSAN', '세종': 'SEJONG',
  '경기': 'GYEONGGI', '강원': 'GANGWON', '충북': 'CHUNGCHEONGBUK', '충남': 'CHUNGCHEONGNAM',
  '전북': 'JEOLLABUK', '전남': 'JEOLLANAM', '경북': 'GYEONGSANGBUK', '경남': 'GYEONGSANGNAM',
  '제주': 'JEJU', '기타': 'ETC'
};
const reverseRegionMap = Object.fromEntries(
  Object.entries(regionMap).map(([key, value]) => [value, key])
);

function TempleUpdate() {
  const { templeId } = useParams();
  const navigate = useNavigate();
  const { hasAdminPermission } = useAuth(); // hasAdminPermission 훅 임포트

  // ✅ 백엔드 서버의 기본 URL을 상수로 정의합니다.
  const BACKEND_BASE_URL = "http://localhost:8080";

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
    isActive: true,
    deletedPhotoIds: [],
    updatedPhotoInfo: [],
  });

  const [displayRegion, setDisplayRegion] = useState('');
  const debounceTimer = useRef(null);

  const [newPhotosToUpload, setNewPhotosToUpload] = useState([]);
  const [newPhotoPreviews, setNewPhotoPreviews] = useState([]);
  const [existingPhotos, setExistingPhotos] = useState([]);
  const [photoDescriptions, setPhotoDescriptions] = useState({}); // 기존 사진 설명을 위한 상태 유지
  const [newPhotoDescriptions, setNewPhotoDescriptions] = useState({}); // 💡 새로 추가된 사진 설명을 위한 상태

  // 💡 이미지 확대 모달 관련 상태
  const [showImageModal, setShowImageModal] = useState(false);
  const [modalImageUrl, setModalImageUrl] = useState('');
  const [modalImageDescription, setModalImageDescription] = useState(''); // 💡 모달에 표시될 사진 설명 상태 추가

  // 💡 이미지 클릭 시 모달 열기
  const handleImageClick = (imageUrl, description = '') => { // 설명도 함께 받도록 변경
    setModalImageUrl(imageUrl);
    setModalImageDescription(description); // 설명 설정
    setShowImageModal(true);
  };

  // 💡 이미지 모달 닫기
  const handleCloseImageModal = () => {
    setShowImageModal(false);
    setModalImageUrl('');
    setModalImageDescription('');
  };

  const fetchTempleDetails = useCallback(async () => {
    try {
      const response = await axios.get(`${BACKEND_BASE_URL}/api/temples/${templeId}`);
      const data = response.data;

      setFormData({
        templeName: data.templeName || '',
        feature: data.feature || '',
        region: data.region || '',
        phoneNumber: data.phoneNumber || '',
        homepage: data.homepage || '',
        address: data.address || '',
        operatingHours: data.operatingHours || '',
        holidays: data.holidays || '',
        parkingInfo: data.parkingInfo || '',
        admissionFee: data.admissionFee || '',
        restroomInfo: data.restroomInfo || '',
        accessibility: data.accessibility || '',
        culturalAssets: data.culturalAssets || '',
        description: data.description || '',
        isActive: data.isActive !== undefined ? data.isActive : true,
        deletedPhotoIds: [],
        updatedPhotoInfo: [],
      });
      setDisplayRegion(reverseRegionMap[data.region] || data.region || '');

      setExistingPhotos(data.photos || []);
      const initialPhotoDescriptions = {};
      (data.photos || []).forEach(photo => {
        initialPhotoDescriptions[photo.photoId] = photo.description || '';
      });
      setPhotoDescriptions(initialPhotoDescriptions);

    } catch (error) {
      console.error("사찰 정보 불러오기 실패:", error);
      alert('사찰 정보를 불러오는 데 실패했습니다.');
      navigate('/temples');
    }
  }, [templeId, navigate]); // reverseRegionMap 제거

  useEffect(() => {
    if (templeId) {
      fetchTempleDetails();
    }
  }, [templeId, fetchTempleDetails]);

  useEffect(() => {
    return () => {
      if (debounceTimer.current) {
        clearTimeout(debounceTimer.current);
      }
      // 💡 컴포넌트 언마운트 시 생성된 URL 해제
      newPhotoPreviews.forEach(url => URL.revokeObjectURL(url));
    };
  }, [newPhotoPreviews]); // newPhotoPreviews를 의존성 배열에 추가

  // 💡 hasAdminPermission 함수의 결과값을 useMemo로 캐싱
  const canEdit = useMemo(() => hasAdminPermission(), [hasAdminPermission]);

  // 💡 캐싱된 권한 결과 사용
  if (!canEdit) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-red-100">
        <p className="text-red-700 text-2xl font-bold">⚠️ 관리자만 접근 가능합니다.</p>
      </div>
    );
  }

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    console.log(`handleChange called: name=${name}, value=${value}, type=${type}, checked=${checked}`);

    if (type === 'checkbox') {
      setFormData(prev => ({ ...prev, [name]: checked }));
    } else {
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

  const handleNewPhotoChange = (e) => {
    const files = Array.from(e.target.files);
    setNewPhotosToUpload(prev => [...prev, ...files]);

    const filePreviews = files.map(file => URL.createObjectURL(file));
    setNewPhotoPreviews(prev => [...prev, ...filePreviews]);

    // 💡 새로 추가되는 사진의 설명을 빈 문자열로 초기화
    const newDescriptions = {};
    files.forEach((file, i) => {
      newDescriptions[newPhotosToUpload.length + i] = ''; // 현재 배열 길이를 기준으로 인덱스 부여
    });
    setNewPhotoDescriptions(prev => ({ ...prev, ...newDescriptions }));
  };

  const handleNewPhotoDescriptionChange = (index, description) => {
    setNewPhotoDescriptions(prev => ({ ...prev, [index]: description }));
  };

  const handleRemoveNewPhoto = (indexToRemove) => {
    setNewPhotosToUpload(prev => prev.filter((_, index) => index !== indexToRemove));
    setNewPhotoPreviews(prev => {
      const newPreviews = prev.filter((_, index) => index !== indexToRemove);
      URL.revokeObjectURL(prev[indexToRemove]);
      return newPreviews;
    });
    setNewPhotoDescriptions(prev => {
      const updatedDescriptions = { ...prev };
      delete updatedDescriptions[indexToRemove];
      // 인덱스 재정렬 (선택 사항이지만, 일관성을 위해)
      const reindexedDescriptions = {};
      let newIdx = 0;
      Object.keys(updatedDescriptions).sort((a, b) => parseInt(a) - parseInt(b)).forEach(key => {
        reindexedDescriptions[newIdx++] = updatedDescriptions[key];
      });
      return reindexedDescriptions;
    });
  };

  const handleRemoveExistingPhoto = (photoId) => {
    setFormData(prev => ({
      ...prev,
      deletedPhotoIds: [...prev.deletedPhotoIds, photoId],
    }));
    setExistingPhotos(prev => prev.filter(photo => photo.photoId !== photoId));
    setPhotoDescriptions(prev => { // 삭제된 사진의 설명도 제거
      const newDescriptions = { ...prev };
      delete newDescriptions[photoId];
      return newDescriptions;
    });
  };

  const handleExistingPhotoDescriptionChange = (photoId, description) => {
    // console.log(`Existing photo description change: ID=${photoId}, Desc=${description}`); // 디버깅
    setFormData(prev => {
      const existingUpdateIndex = prev.updatedPhotoInfo.findIndex(info => info.photoId === photoId);
      let newUpdatedPhotoInfo;

      if (existingUpdateIndex > -1) {
        // 기존에 해당 photoId에 대한 업데이트 정보가 있다면 수정
        newUpdatedPhotoInfo = prev.updatedPhotoInfo.map((info, index) =>
          index === existingUpdateIndex ? { ...info, description: description } : info
        );
      } else {
        // 없다면 새로 추가
        newUpdatedPhotoInfo = [...prev.updatedPhotoInfo, { photoId, description }];
      }
      return { ...prev, updatedPhotoInfo: newUpdatedPhotoInfo };
    });
    setPhotoDescriptions(prev => ({ ...prev, [photoId]: description }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (debounceTimer.current) {
      clearTimeout(debounceTimer.current);
      setFormData(prev => ({
        ...prev,
        region: regionMap[displayRegion] || formData.region // 최종적으로 displayRegion 값을 regionMap에 매핑
      }));
    }

    const token = localStorage.getItem('accessToken');
    if (!token) {
      alert('로그인이 필요합니다.');
      navigate('/login');
      return;
    }

    const form = new FormData();

    // formData (DTO 데이터)를 JSON으로 변환하여 'request' 파트에 추가
    // region 값을 최종적으로 매핑된 값으로 사용
    const requestData = {
      ...formData,
      region: regionMap[displayRegion] || formData.region,
      // newPhotoDescriptions는 newFiles와 함께 별도로 처리해야 함
      // 여기서는 updatedPhotoInfo와 deletedPhotoIds만 포함
      updatedPhotoInfo: formData.updatedPhotoInfo,
      deletedPhotoIds: formData.deletedPhotoIds
    };
    form.append('request', new Blob([JSON.stringify(requestData)], { type: 'application/json' }));

    // newPhotosToUpload (실제 파일 객체들)을 'newFiles' 파트로 추가
    newPhotosToUpload.forEach((file, index) => {
      form.append(`newFiles`, file);
      // 💡 새로 추가된 사진의 설명을 'newFileDescriptions' 파트로 추가
      // 백엔드에서 List<String> 형태로 받을 수 있도록 해야 함
      form.append(`newFileDescriptions`, newPhotoDescriptions[index] || '');
    });
    
    try {
      await axios.put(`${BACKEND_BASE_URL}/api/temples/${templeId}`, form, {
        headers: {
          'Content-Type': 'multipart/form-data',
          Authorization: `Bearer ${token}`
        }
      });
      alert('✅ 사찰 정보가 성공적으로 수정되었습니다.');
      navigate(`/temples/${templeId}`);
    } catch (err) {
      console.error("사찰 정보 수정 실패:", err);
      alert(`❌ 사찰 정보 수정에 실패했습니다: ${err.response?.data.message || err.message}`);
    }
  };

  return (
    <div className="container mx-auto p-8 bg-white shadow-lg rounded-lg my-8">
      <h1 className="text-4xl font-bold text-center text-blue-700 mb-8">
        ✏️ 사찰 정보 수정 ✏️
      </h1>
      <form onSubmit={handleSubmit} className="space-y-6">
        {/* 기본 정보 필드 */}
        <div>
          <label htmlFor="templeName" className="block text-lg font-medium text-gray-700 mb-2">사찰명 <span className="text-red-500">*</span></label>
          <input type="text" id="templeName" name="templeName" value={formData.templeName} onChange={handleChange} required
            className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-blue-500 focus:border-blue-500" />
        </div>
        <div>
          <label htmlFor="feature" className="block text-lg font-medium text-gray-700 mb-2">특징</label>
          <input type="text" id="feature" name="feature" value={formData.feature} onChange={handleChange}
            className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-blue-500 focus:border-blue-500" />
        </div>
        <div>
          <label htmlFor="region" className="block text-lg font-medium text-gray-700 mb-2">지역 <span className="text-red-500">*</span></label>
          <input type="text" id="region" name="region" value={displayRegion} onChange={handleRegionInputChange} required
            className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-blue-500 focus:border-blue-500" />
        </div>
        <div>
          <label htmlFor="phoneNumber" className="block text-lg font-medium text-gray-700 mb-2">전화번호</label>
          <input type="text" id="phoneNumber" name="phoneNumber" value={formData.phoneNumber} onChange={handleChange}
            className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-blue-500 focus:border-blue-500" />
        </div>
        <div>
          <label htmlFor="homepage" className="block text-lg font-medium text-gray-700 mb-2">홈페이지</label>
          <input type="url" id="homepage" name="homepage" value={formData.homepage} onChange={handleChange}
            className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-blue-500 focus:border-blue-500" />
        </div>
        <div>
          <label htmlFor="address" className="block text-lg font-medium text-gray-700 mb-2">주소 <span className="text-red-500">*</span></label>
          <input type="text" id="address" name="address" value={formData.address} onChange={handleChange} required
            className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-blue-500 focus:border-blue-500" />
        </div>
        <div>
          <label htmlFor="operatingHours" className="block text-lg font-medium text-gray-700 mb-2">운영 시간</label>
          <input type="text" id="operatingHours" name="operatingHours" value={formData.operatingHours} onChange={handleChange}
            className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-blue-500 focus:border-blue-500" />
        </div>
        <div>
          <label htmlFor="holidays" className="block text-lg font-medium text-gray-700 mb-2">휴일</label>
          <input type="text" id="holidays" name="holidays" value={formData.holidays} onChange={handleChange}
            className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-blue-500 focus:border-blue-500" />
        </div>
        <div>
          <label htmlFor="parkingInfo" className="block text-lg font-medium text-gray-700 mb-2">주차 정보</label>
          <input type="text" id="parkingInfo" name="parkingInfo" value={formData.parkingInfo} onChange={handleChange}
            className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-blue-500 focus:border-blue-500" />
        </div>
        <div>
          <label htmlFor="admissionFee" className="block text-lg font-medium text-gray-700 mb-2">입장료</label>
          <input type="text" id="admissionFee" name="admissionFee" value={formData.admissionFee} onChange={handleChange}
            className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-blue-500 focus:border-blue-500" />
        </div>
        <div>
          <label htmlFor="restroomInfo" className="block text-lg font-medium text-gray-700 mb-2">화장실 정보</label>
          <input type="text" id="restroomInfo" name="restroomInfo" value={formData.restroomInfo} onChange={handleChange}
            className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-blue-500 focus:border-blue-500" />
        </div>
        <div>
          <label htmlFor="accessibility" className="block text-lg font-medium text-gray-700 mb-2">접근성</label>
          <input type="text" id="accessibility" name="accessibility" value={formData.accessibility} onChange={handleChange}
            className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-blue-500 focus:border-blue-500" />
        </div>
        <div>
          <label htmlFor="culturalAssets" className="block text-lg font-medium text-gray-700 mb-2">문화재 정보</label>
          <input type="text" id="culturalAssets" name="culturalAssets" value={formData.culturalAssets} onChange={handleChange}
            className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-blue-500 focus:border-blue-500" />
        </div>
        <div>
          <label htmlFor="description" className="block text-lg font-medium text-gray-700 mb-2">상세 설명</label>
          <textarea id="description" name="description" value={formData.description} onChange={handleChange} rows="5"
            className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-blue-500 focus:border-blue-500"
            placeholder="사찰에 대한 자세한 설명을 입력하세요."></textarea>
        </div>

        <div className="flex items-center space-x-2">
          <input
            type="checkbox"
            id="isActive"
            name="isActive"
            checked={formData.isActive}
            onChange={handleChange}
            className="h-5 w-5 text-blue-600 rounded border-gray-300 focus:ring-blue-500"
          />
          <label htmlFor="isActive" className="text-lg font-medium text-gray-700">활성화</label>
        </div>


        {/* 기존 사진 관리 */}
        <div className="border p-4 rounded-lg bg-gray-50">
          <h3 className="text-xl font-semibold text-gray-800 mb-4">기존 사찰 사진 관리</h3>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {existingPhotos.map((photo) => (
              <div key={photo.photoId} className="relative border rounded-lg overflow-hidden shadow-sm">
                {/* 💡 이미지 클릭 시 모달 열기 */}
                <img
                  src={`${BACKEND_BASE_URL}${photo.photoUrl}`} // 💡 썸네일 경로 수정
                  alt={photo.description || '사찰 사진'}
                  className="w-full h-48 object-cover cursor-pointer"
                  onClick={() => handleImageClick(`${BACKEND_BASE_URL}${photo.photoUrl}`, photo.description)} // 💡 클릭 이벤트에 설명 추가
                />
                <div className="p-2">
                  <input
                    type="text"
                    placeholder="사진 설명"
                    value={photoDescriptions[photo.photoId] || ''} // 💡 photoDescriptions 상태 사용
                    onChange={(e) => handleExistingPhotoDescriptionChange(photo.photoId, e.target.value)}
                    className="w-full px-2 py-1 border rounded-md text-sm mb-2"
                  />
                  <button
                    type="button"
                    onClick={() => handleRemoveExistingPhoto(photo.photoId)}
                    className="w-full bg-red-500 text-white py-1 rounded-md hover:bg-red-600 transition duration-200"
                  >
                    삭제
                  </button>
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* 새로운 사진 업로드 필드 */}
        <div className="border p-4 rounded-lg bg-gray-50">
          <label htmlFor="newFiles" className="block text-lg font-medium text-gray-700 mb-2">새로운 사찰 사진 추가</label>
          <input
            type="file"
            id="newFiles"
            name="newFiles"
            multiple
            accept="image/*"
            onChange={handleNewPhotoChange}
            className="w-full text-gray-700 file:mr-4 file:py-2 file:px-4 file:rounded-full file:border-0 file:text-sm file:font-semibold file:bg-blue-50 file:text-blue-700 hover:file:bg-blue-100"
          />
          <div className="mt-4 grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {newPhotoPreviews.map((previewUrl, index) => (
              <div key={index} className="relative border rounded-lg overflow-hidden shadow-sm">
                {/* 💡 이미지 클릭 시 모달 열기 */}
                <img
                  src={previewUrl}
                  alt={`새 사진 ${index + 1}`}
                  className="w-full h-32 object-cover cursor-pointer"
                  onClick={() => handleImageClick(previewUrl, newPhotoDescriptions[index])} // 💡 클릭 이벤트에 설명 추가
                />
                <div className="p-2">
                  <span className="block text-sm text-gray-700 truncate">{newPhotosToUpload[index].name}</span>
                  {/* 💡 새로 추가되는 사진의 설명 입력 필드 */}
                  <input
                    type="text"
                    placeholder="사진 설명 (선택 사항)"
                    value={newPhotoDescriptions[index] || ''} // 💡 기본값을 빈 문자열로 설정
                    onChange={(e) => handleNewPhotoDescriptionChange(index, e.target.value)} // 💡 변경 핸들러 연결
                    className="w-full px-2 py-1 border rounded-md text-sm mt-1"
                  />
                  <button
                    type="button"
                    onClick={() => handleRemoveNewPhoto(index)}
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
            onClick={() => navigate(`/temples/${templeId}`)}
            className="px-6 py-3 bg-gray-600 text-white font-semibold rounded-lg shadow-md hover:bg-gray-700 transition duration-300"
          >
            취소
          </button>
          <button
            type="submit"
            className="px-6 py-3 bg-blue-600 text-white font-semibold rounded-lg shadow-md hover:bg-blue-700 transition duration-300"
          >
            수정하기
          </button>
        </div>
      </form>

      {/* 💡 이미지 확대 모달 */}
      {showImageModal && (
        <div className="fixed inset-0 bg-black bg-opacity-75 flex items-center justify-center z-50 p-4">
          <div className="relative bg-white rounded-lg p-4 max-w-4xl w-full max-h-[90vh] flex flex-col items-center"> {/* max-h 조정 */}
            <button
              onClick={handleCloseImageModal}
              className="absolute top-2 right-2 text-gray-800 text-3xl font-bold p-2 rounded-full bg-gray-200 hover:bg-gray-300 z-10"
            >
              &times;
            </button>
            {/* 이미지 컨테이너: flex-grow로 남은 공간 최대한 차지 */}
            <div className="flex-grow flex items-center justify-center w-full min-h-0">
              <img
                src={modalImageUrl}
                alt={modalImageDescription || "확대 이미지"}
                className="max-w-full max-h-[70vh] object-contain rounded-lg shadow-md" // max-h 조정
              />
            </div>
            {/* 💡 사진 설명 표시: flex-shrink-0로 고정 높이 */}
            <p className="flex-shrink-0 mt-4 text-gray-700 text-center text-base font-medium px-4 py-2">
              {modalImageDescription || "설명 없음"}
            </p>
          </div>
        </div>
      )}
    </div>
  );
}

export default TempleUpdate;