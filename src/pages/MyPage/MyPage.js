import React, { useState, useEffect } from 'react';
import axios from 'axios'; // Axios 라이브러리 임포트
import { useNavigate } from 'react-router-dom'; // 페이지 이동을 위한 useNavigate 훅 임포트

import './MyPage.css'; // MyPage 전용 스타일
import UserInfoTab from './components/UserInfoTab';
import TravelDrawerTab from './components/TravelDrawerTab'; // 아직 구현 안 됨
import JourneyShareTab from './components/JourneyShareTab'; // 아직 구현 안 됨
import InquiryBoardTab from './components/InquiryBoardTab'; // 아직 구현 안 됨

// ⭐️ 환경 변수에서 백엔드 기본 URL 가져오기
// .env 파일에 REACT_APP_BACKEND_BASE_URL=http://localhost:8080 이렇게 설정되어 있다고 가정
const API_BASE_URL = process.env.REACT_APP_BACKEND_BASE_URL || 'http://localhost:8080';

const MyPage = () => {
  const [activeTab, setActiveTab] = useState('userInfo'); // 현재 활성화된 탭 상태
  const [userInfo, setUserInfo] = useState(null); // 백엔드에서 받아올 실제 사용자 정보
  const [loading, setLoading] = useState(true); // 데이터 로딩 상태
  const [error, setError] = useState(null); // 에러 상태

  const navigate = useNavigate(); // 페이지 이동을 위한 navigate 훅 사용

  // ⭐ 핵심: 사용자 정보를 백엔드에서 불러오는 비동기 함수
  const fetchUserInfo = async () => {
    setLoading(true); // 로딩 시작
    setError(null);   // 이전 에러 초기화
    try {
      // ⭐️ API 호출 URL을 절대 경로로 변경!
      const response = await axios.get(`${API_BASE_URL}/api/auth/me`, { 
        headers: {
          // 로컬 스토리지에 저장된 JWT Access Token을 Authorization 헤더에 포함
          Authorization: `Bearer ${localStorage.getItem('accessToken')}`
        }
      });
      // 백엔드 MemberResponseDTO의 필드명에 맞춰 userInfo 상태 업데이트
      setUserInfo({
        loginId: response.data.loginId,
        email: response.data.email,
        nickname: response.data.username, // 백엔드의 'username'이 프론트의 닉네임으로 사용됨
        createdAt: response.data.createdAt, // 가입일자 (ISO 8601 형식 문자열)
        // 필요한 다른 필드가 있다면 여기에 추가
      });
      console.log('✅ 사용자 정보 불러오기 성공:', response.data);
    } catch (err) {
      console.error('🚨 사용자 정보 불러오기 실패:', err);
      if (err.response) {
        // 서버 응답이 있을 경우 (예: 401 Unauthorized, 403 Forbidden)
        if (err.response.status === 401 || err.response.status === 403) {
          alert('세션이 만료되었거나 접근 권한이 없습니다. 다시 로그인해주세요. 🚪');
          localStorage.removeItem('accessToken'); // 유효하지 않은 토큰 삭제
          navigate('/login'); // 로그인 페이지로 리다이렉트
        } else {
          setError(`회원 정보를 불러오는데 실패했습니다: ${err.response.data.message || err.response.data || err.message} 😥`);
        }
      } else {
        // 네트워크 오류 등 서버 응답이 없는 경우
        setError('네트워크 오류 또는 서버에 연결할 수 없습니다. 🌐');
      }
    } finally {
      setLoading(false); // 로딩 종료
    }
  };

  // ⭐ 핵심: UserInfoTab에서 정보 수정이 성공했을 때 호출될 콜백 함수
  const handleUserInfoUpdated = () => {
    fetchUserInfo(); // 최신 사용자 정보를 다시 불러와서 UI를 업데이트
    setActiveTab('userInfo'); // 현재 탭을 '회원 정보' 탭으로 유지 또는 전환
    console.log('✨ 회원 정보가 성공적으로 업데이트되었습니다. 최신 정보를 다시 불러옵니다.');
  };

  // 컴포넌트가 마운트될 때 (처음 로드될 때) 사용자 정보를 불러옴
  useEffect(() => {
    fetchUserInfo();
  }, []); // 빈 배열을 넣어 한 번만 실행되도록 설정

  // 탭 내용 렌더링 로직
  const renderTabContent = () => {
    if (loading) {
      return <p className="loading-message">정보를 불러오는 중입니다... ⏳</p>;
    }
    if (error) {
      return <p className="error-message">🚨 {error}</p>;
    }
    if (!userInfo) {
      // 로딩도 아니고 에러도 아닌데 userInfo가 없으면, 데이터를 불러오지 못한 상황
      return <p>회원 정보를 불러올 수 없습니다. 로그인이 유효한지 확인해주세요.</p>;
    }

    switch (activeTab) {
      case 'userInfo':
        // ⭐ UserInfoTab 컴포넌트에 userInfo prop과 onUserInfoUpdated 콜백 함수 전달
        return <UserInfoTab userInfo={userInfo} onUserInfoUpdated={handleUserInfoUpdated} />;
      case 'travelDrawer':
        return <TravelDrawerTab />; // TODO: 여행 서랍 탭 컴포넌트
      case 'journeyShare':
        return <JourneyShareTab />; // TODO: 여정 나누기 탭 컴포넌트
      case 'inquiryBoard':
        return <InquiryBoardTab />; // TODO: 문의 게시판 탭 컴포넌트
      default:
        // 기본값으로 회원 정보 탭을 보여주며, 콜백 함수도 함께 전달
        return <UserInfoTab userInfo={userInfo} onUserInfoUpdated={handleUserInfoUpdated} />;
    }
  };

  return (
    <div className="my-page-container">
      <h1 className="my-page-title">나의 여정 🚶‍♀️</h1>
      <div className="my-page-tabs">
        <button
          className={`tab-button ${activeTab === 'userInfo' ? 'active' : ''}`}
          onClick={() => setActiveTab('userInfo')}
        >
          회원 정보 🧑‍💻
        </button>
        <button
          className={`tab-button ${activeTab === 'travelDrawer' ? 'active' : ''}`}
          onClick={() => setActiveTab('travelDrawer')}
        >
          여행 서랍 🎒
        </button>
        <button
          className={`tab-button ${activeTab === 'journeyShare' ? 'active' : ''}`}
          onClick={() => setActiveTab('journeyShare')}
        >
          여정 나누기 ✍️
        </button>
        <button
          className={`tab-button ${activeTab === 'inquiryBoard' ? 'active' : ''}`}
          onClick={() => setActiveTab('inquiryBoard')}
        >
          문의 게시판 💬
        </button>
      </div>
      <div className="my-page-content">
        {renderTabContent()}
      </div>
    </div>
  );
};

export default MyPage;