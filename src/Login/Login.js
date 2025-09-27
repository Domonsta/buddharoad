// src/Auth/Login.js
import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
// import axios from 'axios'; // 🚨 axios는 AuthContext에서만 사용하도록 제거!
import { useAuth } from '../Context/AuthContext'; // 경로 확인!

function Login() {
  const [loginId, setLoginId] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState(null);
  const navigate = useNavigate();
  const { login } = useAuth(); // AuthContext의 login 함수 가져오기

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null); // 에러 메시지 초기화

    try {
      // 🚨🚨🚨 여기를 수정합니다! 🚨🚨🚨
      // AuthContext의 login 함수를 호출하고, loginId와 password를 인자로 넘겨줍니다.
      // AuthContext의 login 함수가 알아서 백엔드 API 호출 및 토큰 처리를 할 것입니다.
      const success = await login(loginId, password); // AuthContext의 login 함수 호출

      if (success) { // AuthContext의 login 함수가 true를 반환하면 성공으로 간주
        alert('로그인 성공! 환영합니다!'); // 사용자에게 알림
        navigate('/'); // 로그인 성공 후 메인 페이지로 이동
      } else {
        // login 함수에서 false를 반환하는 경우는 catch 블록에서 처리되므로,
        // 이 else 블록은 사실상 필요 없을 수 있지만, 혹시 모를 상황 대비
        setError('로그인에 실패했습니다. 아이디와 비밀번호를 확인해주세요.');
      }

    } catch (err) {
      console.error('로그인 실패 (Login.js catch):', err);
      // AuthContext의 login 함수에서 던진 에러 메시지를 그대로 사용합니다.
      setError(err.message || '로그인에 실패했습니다. 알 수 없는 오류입니다.');
    }
  };

  return (
    <div className="flex items-center justify-center min-h-screen bg-gray-100">
      <div className="bg-white p-8 rounded-lg shadow-md w-full max-w-md">
        <h2 className="text-3xl font-bold text-center text-blue-800 mb-6">로그인</h2>
        <form onSubmit={handleSubmit}>
          <div className="mb-4">
            <label htmlFor="loginId" className="block text-gray-700 text-sm font-bold mb-2">
              아이디:
            </label>
            <input
              type="text"
              id="loginId"
              className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
              value={loginId}
              onChange={(e) => setLoginId(e.target.value)}
              required
            />
          </div>
          <div className="mb-6">
            <label htmlFor="password" className="block text-gray-700 text-sm font-bold mb-2">
              비밀번호:
            </label>
            <input
              type="password"
              id="password"
              className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 mb-3 leading-tight focus:outline-none focus:shadow-outline"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />
          </div>
          {error && <p className="text-red-500 text-xs italic mb-4 text-center">{error}</p>}
          <div className="flex items-center justify-between">
            <button
              type="submit"
              className="bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded focus:outline-none focus:shadow-outline w-full"
            >
              로그인
            </button>
          </div>
        </form>
        <p className="text-center text-gray-600 text-sm mt-4">
          계정이 없으신가요?{" "}
          <Link to="/register" className="text-blue-500 hover:text-blue-800">
            회원가입
          </Link>
        </p>
      </div>
    </div>
  );
}

export default Login;