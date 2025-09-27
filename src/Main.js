// src/Main.js
import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from './Context/AuthContext';
import MessageBox from './Components/MessageBox'; // MessageBox 임포트

function Main() {
  const { isLoggedIn, user, logout, hasAdminPermission, loadingAuth } = useAuth();
  const navigate = useNavigate();

  // 메시지 박스 상태 관리
  const [messageBox, setMessageBox] = useState({
    isVisible: false,
    message: '',
    onClose: null,
  });

  // 💡 디버깅용 로그 유지: 콘솔에서 user 객체와 권한 상태 확인
  console.log("Main State:", { isLoggedIn, user, loadingAuth, isAdmin: hasAdminPermission(), isNormal: user && user.role === 'USER' });

  const hasNormalPermission = () => {
    return user && user.role === 'USER'; // 역할명 'USER'로 변경됨
  };

  // 로그아웃 처리 함수
  const handleLogout = () => {
    logout(); // AuthContext의 logout 함수 호출
    setMessageBox({
      isVisible: true,
      message: '👋 로그아웃 되었습니다!',
      onClose: () => {
        navigate('/'); // 메인 페이지로 리다이렉트
      },
    });
  };

  if (loadingAuth) {
    return (
      <div className="min-h-screen flex flex-col items-center justify-center bg-gradient-to-br from-blue-50 to-green-50 p-6 font-sans text-gray-800">
        <h1 className="text-5xl md:text-6xl font-extrabold text-green-700 mb-6 drop-shadow-lg animate-fade-in-down">
          🙏 붓다로드에 오신 것을 환영합니다! 🙏
        </h1>
        <p className="text-xl md::text-2xl text-gray-600 mb-12 text-center max-w-2xl animate-fade-in-up">
          인증 정보 불러오는 중... 잠시만 기다려주세요! ⏳
        </p>
      </div>
    );
  }

  return (
    <div className="min-h-screen flex flex-col items-center justify-center bg-gradient-to-br from-blue-50 to-green-50 p-6 font-sans text-gray-800">
      <h1 className="text-5xl md::text-6xl font-extrabold text-green-700 mb-6 drop-shadow-lg animate-fade-in-down">
        🙏 붓다로드에 오신 것을 환영합니다! 🙏
      </h1>
      <p className="text-xl md::text-2xl text-gray-600 mb-12 text-center max-w-2xl animate-fade-in-up">
        사찰 정보를 탐색하고, 명상과 평화를 찾아보세요.
      </p>

      <div className="flex flex-col md:flex-row space-y-4 md:space-y-0 md:space-x-6">
        {/* 사찰 정보 둘러보기 버튼 */}
        <Link
          to="/temples"
          className="px-8 py-4 bg-blue-600 text-white text-lg font-semibold rounded-xl shadow-lg hover:bg-blue-700 transition-all duration-300 ease-in-out transform hover:-translate-y-1 hover:scale-105 animate-pop-in"
        >
          사찰 정보 둘러보기
        </Link>

        {/* 리뷰 게시판 버튼 */}
        <Link
          to="/reviews"
          className="px-8 py-4 bg-purple-600 text-white text-lg font-semibold rounded-xl shadow-lg hover:bg-purple-700 transition-all duration-300 ease-in-out transform hover:-translate-y-1 hover:scale-105 animate-pop-in animation-delay-100"
        >
          리뷰 게시판
        </Link>

        {isLoggedIn ? (
          <>
            {/* 관리자인 경우 관리자페이지 버튼, 일반 회원인 경우 마이페이지 버튼 */}
            {hasAdminPermission() ? (
              <Link
                to="/admin"
                className="px-8 py-4 bg-orange-600 text-white text-lg font-semibold rounded-xl shadow-lg hover:bg-orange-700 transition-all duration-300 ease-in-out transform hover:-translate-y-1 hover:scale-105 animate-pop-in animation-delay-300"
              >
                관리자페이지 ⚙️
              </Link>
            ) : hasNormalPermission() ? (
              <>
                {/* 👇 만약 마이페이지 버튼이 그래도 안 보이면 이 주석을 풀어서 확인해봐! */}
                {/* <div style={{color: 'cyan', border: '1px solid cyan', padding: '5px'}}>MYPAGE BUTTON DEBUG</div> */}
                <Link
                  to="/mypage"
                  className="px-8 py-4 bg-indigo-600 text-white text-lg font-semibold rounded-xl shadow-lg hover:bg-indigo-700 transition-all duration-300 ease-in-out transform hover:-translate-y-1 hover:scale-105 animate-pop-in animation-delay-300"
                >
                  마이페이지 🧑‍💻
                </Link>
              </>
            ) : null}

            <button
              onClick={handleLogout}
              className="px-8 py-4 bg-red-600 text-white text-lg font-semibold rounded-xl shadow-lg hover:bg-red-700 transition-all duration-300 ease-in-out transform hover:-translate-y-1 hover:scale-105 animate-pop-in animation-delay-200"
            >
              로그아웃
            </button>
          </>
        ) : (
          <>
            <Link
              to="/login"
              className="px-8 py-4 bg-gray-700 text-white text-lg font-semibold rounded-xl shadow-lg hover:bg-gray-800 transition-all duration-300 ease-in-out transform hover:-translate-y-1 hover:scale-105 animate-pop-in animation-delay-200"
            >
              로그인
            </Link>
            <Link
              to="/signup"
              className="px-8 py-4 bg-green-600 text-white text-lg font-semibold rounded-xl shadow-lg hover:bg-green-700 transition-all duration-300 ease-in-out transform hover:-translate-y-1 hover:scale-105 animate-pop-in animation-delay-400"
            >
              회원가입
            </Link>
          </>
        )}
      </div>

      {/* MessageBox 컴포넌트 렌더링 */}
      <MessageBox
        message={messageBox.message}
        isVisible={messageBox.isVisible}
        onClose={() => {
          setMessageBox({ ...messageBox, isVisible: false });
          if (messageBox.onClose) {
            messageBox.onClose();
          }
        }}
      />
    </div>
  );
}

export default Main;