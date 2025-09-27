import React from 'react';
import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../Context/AuthContext'; // AuthContext 경로 확인

const ProtectedRoute = ({ allowedRoles = [] }) => {
  const { isLoggedIn, user, loading } = useAuth(); // user 객체와 loading 상태 가져오기

  // 인증 정보 로딩 중일 때
  if (loading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh', fontSize: '20px', color: '#6c5ce7' }}>
        인증 정보 확인 중... ⏳
      </div>
    );
  }

  // 로그인되지 않았다면 로그인 페이지로 리디렉션
  if (!isLoggedIn) {
    alert('로그인이 필요합니다! 🔑'); // 사용자에게 알림
    return <Navigate to="/login" replace />;
  }

  // 로그인했지만 user 정보가 아직 없거나, user.role이 없다면 (예상치 못한 상황)
  if (!user || !user.role) {
    alert('사용자 정보를 불러올 수 없거나 권한이 없습니다. 다시 로그인해주세요. 😢');
    return <Navigate to="/login" replace />; // 또는 에러 페이지로
  }

  // 허용된 역할(allowedRoles)이 정의되어 있고, 현재 유저의 역할이 허용된 역할에 포함되지 않는다면
  if (allowedRoles.length > 0 && !allowedRoles.includes(user.role)) {
    alert('접근 권한이 없습니다! 🚫'); // 사용자에게 알림
    // 권한 없는 사용자는 메인 페이지나 접근 거부 페이지로 리디렉션
    return <Navigate to="/" replace />;
  }

  // 모든 조건을 통과하면 자식 라우트를 렌더링
  return <Outlet />;
};

export default ProtectedRoute;