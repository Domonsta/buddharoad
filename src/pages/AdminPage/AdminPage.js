// src/pages/AdminPage.js (기존 코드를 아래 내용으로 교체)
import React, { useState, useEffect } from 'react'; // useEffect 추가
import axios from 'axios'; // axios 임포트
import { useNavigate } from 'react-router-dom'; // 페이지 이동을 위해

import './AdminPage.css'; // AdminPage 전용 스타일
import AccountInfoTab from './components/AccountInfoTab';
import MemberManagementTab from './components/MemberManagementTab'; // 아직 구현 안 됨
import ContentManagementTab from './components/ContentManagementTab'; // 아직 구현 안 됨
import AdminInquiryBoardTab from './components/AdminInquiryBoardTab'; // 아직 구현 안 됨
import SiteStatisticsTab from './components/SiteStatisticsTab'; // 아직 구현 안 됨

// 환경 변수에서 백엔드 기본 URL 가져오기
const API_BASE_URL = process.env.REACT_APP_BACKEND_BASE_URL || 'http://localhost:8080';

const AdminPage = () => {
  const [activeTab, setActiveTab] = useState('accountInfo'); // 기본 탭: 계정 정보
  const [adminInfo, setAdminInfo] = useState(null); // 백엔드에서 받아올 실제 관리자 정보
  const [loading, setLoading] = useState(true); // 데이터 로딩 상태
  const [error, setError] = useState(null); // 에러 상태

  const navigate = useNavigate();

  // ⭐ 관리자 계정 정보를 백엔드에서 불러오는 함수
  const fetchAdminInfo = async () => {
    setLoading(true);
    setError(null);
    try {
      // /api/auth/admin/me 엔드포인트 호출
      const response = await axios.get(`${API_BASE_URL}/api/auth/admin/me`, {
        headers: {
          Authorization: `Bearer ${localStorage.getItem('accessToken')}`
        }
      });
      // 백엔드 AdminResponseDTO의 필드명에 맞춰 adminInfo 상태 업데이트
      setAdminInfo({
        loginId: response.data.loginId, // 아이디
        email: response.data.email,
        username: response.data.username, // 닉네임
        role: response.data.role, // 역할 (예: SYSTEM_ADMIN, CONTENT_ADMIN)
        createdAt: response.data.createdAt, // 가입일
        // 필요한 다른 정보가 있다면 추가
      });
      console.log('✅ 관리자 정보 불러오기 성공:', response.data);
    } catch (err) {
      console.error('🚨 관리자 정보 불러오기 실패:', err.response?.data || err.message);
      if (err.response) {
        if (err.response.status === 401 || err.response.status === 403) {
          alert('인증 정보가 없거나 관리자 권한이 없습니다. 다시 로그인해주세요. 🚪');
          localStorage.removeItem('accessToken');
          navigate('/admin/login'); // 관리자 로그인 페이지로 리다이렉트 (경로 확인 필요)
        } else {
          setError(`관리자 정보를 불러오는데 실패했습니다: ${err.response.data.message || err.response.data || err.message} 😥`);
        }
      } else {
        setError('네트워크 오류 또는 서버에 연결할 수 없습니다. 🌐');
      }
    } finally {
      setLoading(false);
    }
  };

  // ⭐ 관리자 정보 수정 후 호출될 콜백 함수
  const handleAdminInfoUpdated = () => {
    fetchAdminInfo(); // 최신 관리자 정보 다시 불러오기
    console.log('✨ 관리자 정보가 성공적으로 업데이트되었습니다. 최신 정보를 다시 불러옵니다.');
  };

  // 컴포넌트 마운트 시 관리자 정보 불러옴
  useEffect(() => {
    fetchAdminInfo();
  }, []);

  const renderTabContent = () => {
    if (loading) {
      return <p className="loading-message">관리자 정보를 불러오는 중입니다... ⏳</p>;
    }
    if (error) {
      return <p className="error-message">🚨 {error}</p>;
    }
    if (!adminInfo) {
      return <p>관리자 정보를 불러올 수 없습니다. 권한을 확인해주세요.</p>;
    }

    switch (activeTab) {
      case 'accountInfo':
        // ⭐ AccountInfoTab에 실제 관리자 정보와 콜백 함수 전달
        return <AccountInfoTab adminInfo={adminInfo} onAdminInfoUpdated={handleAdminInfoUpdated} />;
      case 'memberManagement':
        return <MemberManagementTab />; // TODO
      case 'contentManagement':
        return <ContentManagementTab />; // TODO
      case 'adminInquiryBoard':
        return <AdminInquiryBoardTab />; // TODO
      case 'siteStatistics':
        return <SiteStatisticsTab />; // TODO
      default:
        return <AccountInfoTab adminInfo={adminInfo} onAdminInfoUpdated={handleAdminInfoUpdated} />;
    }
  };

  return (
    <div className="admin-page-container">
      <h1 className="admin-page-title">관리자 페이지 ⚙️</h1>
      <div className="admin-page-tabs">
        <button
          className={`tab-button ${activeTab === 'accountInfo' ? 'active' : ''}`}
          onClick={() => setActiveTab('accountInfo')}
        >
          계정 정보
        </button>
        <button
          className={`tab-button ${activeTab === 'memberManagement' ? 'active' : ''}`}
          onClick={() => setActiveTab('memberManagement')}
        >
          회원 관리
        </button>
        <button
          className={`tab-button ${activeTab === 'contentManagement' ? 'active' : ''}`}
          onClick={() => setActiveTab('contentManagement')}
        >
          콘텐츠 관리
        </button>
        <button
          className={`tab-button ${activeTab === 'adminInquiryBoard' ? 'active' : ''}`}
          onClick={() => setActiveTab('adminInquiryBoard')}
        >
          문의 게시판
        </button>
        <button
          className={`tab-button ${activeTab === 'siteStatistics' ? 'active' : ''}`}
          onClick={() => setActiveTab('siteStatistics')}
        >
          사이트 통계
        </button>
      </div>
      <div className="admin-page-content">
        {renderTabContent()}
      </div>
    </div>
  );
};

export default AdminPage;