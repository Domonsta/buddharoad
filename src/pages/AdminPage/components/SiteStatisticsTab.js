// src/pages/components/SiteStatisticsTab.js
import React, { useState, useEffect } from 'react';
import axios from 'axios'; // axios 임포트 추가!

// 환경 변수에서 백엔드 기본 URL 가져오기 (AdminPage.js와 동일)
const API_BASE_URL = process.env.REACT_APP_BACKEND_BASE_URL || 'http://localhost:8080';

const SiteStatisticsTab = () => {
  const [statistics, setStatistics] = useState({
    totalMembers: 0,
    totalPosts: 0,      // ⭐⭐⭐ totalReviews -> totalPosts로 변경!
    totalComments: 0,
  });
  const [loading, setLoading] = useState(true); // 로딩 상태 추가
  const [error, setError] = useState(null);     // 에러 상태 추가

  useEffect(() => {
    const fetchSiteStatistics = async () => {
      setLoading(true);
      setError(null);
      try {
        // 백엔드 API 호출!
        const response = await axios.get(`${API_BASE_URL}/api/admin/statistics`, {
          headers: {
            Authorization: `Bearer ${localStorage.getItem('accessToken')}` // 토큰 필요!
          }
        });
        // 백엔드 응답 데이터로 상태 업데이트
        setStatistics({
          totalMembers: response.data.totalMembers,
          totalPosts: response.data.totalPosts,      // ⭐⭐⭐ DTO 필드명과 일치시켜!
          totalComments: response.data.totalComments,
        });
        console.log('✅ 사이트 통계 데이터 불러오기 성공:', response.data);
      } catch (err) {
        console.error('🚨 사이트 통계 데이터 불러오기 실패:', err.response?.data || err.message);
        setError('사이트 통계 데이터를 불러오는데 실패했습니다. 😥');
        // 권한 문제 등 상세 에러 처리도 필요하다면 AdminPage.js의 로직 참고!
      } finally {
        setLoading(false);
      }
    };

    fetchSiteStatistics();
  }, []); // 컴포넌트 마운트 시 한 번만 실행

  if (loading) {
    return (
      <div className="tab-content-section site-statistics-tab">
        <h3 className="tab-content-title">사이트 통계 📊</h3>
        <p className="loading-message">통계 데이터를 불러오는 중입니다... ⏳</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="tab-content-section site-statistics-tab">
        <h3 className="tab-content-title">사이트 통계 📊</h3>
        <p className="error-message">🚨 {error}</p>
      </div>
    );
  }

  return (
    <div className="tab-content-section site-statistics-tab">
      <h3 className="tab-content-title">사이트 통계 📊</h3>
      <div className="statistic-cards">
        <div className="statistic-card">
          <h4>총 회원 수</h4>
          <div className="statistic-value">{statistics.totalMembers.toLocaleString()}</div>
          <p className="statistic-description">현재 가입된 회원 수</p>
        </div>
        <div className="statistic-card">
          <h4>총 게시글 수</h4> {/* 텍스트도 '총 게시글 수'로 변경! */}
          <div className="statistic-value">{statistics.totalPosts.toLocaleString()}</div> {/* ⭐⭐⭐ totalReviews -> totalPosts로 변경! */}
          <p className="statistic-description">전체 리뷰 및 사찰 정보 게시글 수</p>
        </div>
        <div className="statistic-card">
          <h4>총 댓글 수</h4>
          <div className="statistic-value">{statistics.totalComments.toLocaleString()}</div>
          <p className="statistic-description">전체 리뷰 댓글 및 사찰 댓글 수</p>
        </div>
      </div>
    </div>
  );
};

export default SiteStatisticsTab;