// src/App.js
import React from 'react';
import { BrowserRouter as Router, Routes, Route, Link, useNavigate } from 'react-router-dom';
import Main from './Main';
import Login from './Login/Login';
import Signup from './Login/Signup';
import TempleList from './Temples/TempleList';
import TempleDetail from './Temples/TempleDetail';
import TempleRegister from './Temples/TempleRegister';
import TempleEdit from './Temples/TempleUpdate';
import ReviewList from './Reviews/ReviewList';
import ReviewDetail from './Reviews/ReviewDetail';
import ReviewRegister from './Reviews/ReviewRegister';
import ReviewUpdate from './Reviews/ReviewUpdate';

import MyPage from './pages/MyPage/MyPage'; // MyPage 컴포넌트 임포트
import AdminPage from './pages/AdminPage/AdminPage'; // AdminPage 컴포넌트 임포트

import ProtectedRoute from './Components/ProtectedRoute'; // 💡 ProtectedRoute 임포트

import { AuthProvider, useAuth } from './Context/AuthContext';
import axios from 'axios';

// --- Axios 인터셉터 설정 ---
axios.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('accessToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

axios.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;
      localStorage.removeItem('accessToken');
      alert('로그인 세션이 만료되었습니다. 다시 로그인해주세요.');
      window.location.href = '/login';
      return Promise.reject(error);
    }
    return Promise.reject(error);
  }
);

// --- Header 컴포넌트 ---
const Header = () => {
  const { isLoggedIn, user, logout, hasAdminPermission, loadingAuth } = useAuth();
  const navigate = useNavigate();

  // 💡 디버깅용 로그 유지: 콘솔에서 user 객체와 권한 상태 확인
  console.log("Header State:", { isLoggedIn, user, loadingAuth, isAdmin: hasAdminPermission(), isNormal: user && user.role === 'USER' });

  const hasNormalPermission = () => {
    return user && user.role === 'USER'; // 역할명 'USER'로 변경됨
  };

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  if (loadingAuth) {
    return (
      <header className="bg-gradient-to-r from-green-700 to-blue-700 text-white p-4 shadow-md flex justify-center items-center h-16">
        <span className="text-xl font-medium">인증 정보 불러오는 중... ⏳</span>
      </header>
    );
  }

  return (
    <header className="bg-gradient-to-r from-green-700 to-blue-700 text-white p-4 shadow-md">
      <nav className="container mx-auto flex justify-between items-center">
        <Link to="/" className="text-3xl font-extrabold flex items-center space-x-2">
          <span role="img" aria-label="buddha" className="text-4xl">🙏</span>
          <span>붓다로드</span>
        </Link>
        <ul className="flex space-x-6 text-lg font-medium">
          <li>
            <Link to="/temples" className="hover:text-gray-200 transition-colors duration-300">사찰</Link>
          </li>
          <li>
            <Link to="/reviews" className="hover:text-gray-200 transition-colors duration-300">리뷰</Link>
          </li>
          {hasAdminPermission() && ( // 관리자 권한이 있는 경우만 보임
            <>
            
              <li>
                <Link to="/admin" className="hover:text-gray-200 transition-colors duration-300">관리자페이지</Link>
              </li>
            </>
          )}
          {isLoggedIn ? (
            <>
              {/* 💡 환영 메시지 수정: nickname -> loginId -> username 순으로 폴백! */}
              <li className="flex items-center text-gray-200">
                <span className="mr-2">😊 {user?.nickname || user?.loginId || user?.username}님 환영합니다!</span>
              </li>
              {hasNormalPermission() && ( // 일반 회원인 경우에만 마이페이지 링크 보임!
                <li>
                  {/* 👇 만약 마이페이지 링크가 그래도 안 보이면 이 주석을 풀어서 확인해봐! */}
                  {/* <div style={{color: 'lime', border: '1px solid lime', padding: '5px'}}>MYPAGE LINK DEBUG</div> */}
                  <Link to="/mypage" className="hover:text-gray-200 transition-colors duration-300">마이페이지</Link>
                </li>
              )}
              <li>
                <button onClick={handleLogout} className="hover:text-gray-200 transition-colors duration-300">로그아웃</button>
              </li>
            </>
          ) : (
            <>
              <li>
                <Link to="/login" className="hover:text-gray-200 transition-colors duration-300">로그인</Link>
              </li>
              <li>
                <Link to="/signup" className="hover:text-gray-200 transition-colors duration-300">회원가입</Link>
              </li>
            </>
          )}
        </ul>
      </nav>
    </header>
  );
};

// --- App 컴포넌트 ---
function App() {
  return (
    <AuthProvider>
      <Router>
        <Header />
        <main className="p-4">
          <Routes>
            <Route path="/" element={<Main />} />
            <Route path="/login" element={<Login />} />
            <Route path="/signup" element={<Signup />} />

            {/* 사찰 관련 라우트 */}
            <Route path="/temples" element={<TempleList />} />
            <Route path="/temples/:templeId" element={<TempleDetail />} />
            <Route path="/temples/register" element={<TempleRegister />} />
            <Route path="/temples/edit/:templeId" element={<TempleEdit />} />

            {/* 리뷰 관련 라우트 (구체적인 경로를 위에 배치) */}
            <Route path="/reviews/register" element={<ReviewRegister />} />
            <Route path="/reviews/edit/:reviewId" element={<ReviewUpdate />} />
            <Route path="/reviews/:reviewId" element={<ReviewDetail />} />
            <Route path="/reviews" element={<ReviewList />} />

            {/* 💡 마이페이지 보호 라우트: 일반회원 역할명을 'USER'로 변경 */}
            <Route element={<ProtectedRoute allowedRoles={['USER']} />}>
              <Route path="/mypage" element={<MyPage />} />
            </Route>

            {/* 💡 관리자 페이지 보호 라우트: 시스템관리자, 콘텐츠관리자만 접근 가능 */}
            <Route element={<ProtectedRoute allowedRoles={['SYSTEM_ADMIN', 'CONTENT_ADMIN']} />}>
              <Route path="/admin" element={<AdminPage />} />
            </Route>

          </Routes>
        </main>
      </Router>
    </AuthProvider>
  );
}

export default App;