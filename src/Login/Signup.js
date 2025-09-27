import React, { useState, useEffect, useCallback } from 'react'; // useEffect, useCallback 추가
import { useNavigate, Link } from 'react-router-dom';
import axios from 'axios';

function Signup() {
  const [loginId, setLoginId] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [email, setEmail] = useState('');
  const [username, setUsername] = useState('');

  // 💡 중복 검사 및 유효성 검사 상태
  const [loginIdMessage, setLoginIdMessage] = useState('');
  const [emailMessage, setEmailMessage] = useState('');
  const [usernameMessage, setUsernameMessage] = useState('');
  const [passwordMatchMessage, setPasswordMatchMessage] = useState(''); // 비밀번호 일치 메시지

  // 💡 유효성 검사 통과 여부
  const [isLoginIdValid, setIsLoginIdValid] = useState(false);
  const [isEmailValid, setIsEmailValid] = useState(false);
  const [isUsernameValid, setIsUsernameValid] = useState(false);
  const [isPasswordValid, setIsPasswordValid] = useState(false); // 비밀번호 형식 유효성 (DTO와 동일하게)

  const navigate = useNavigate();

  // 💡 비밀번호 유효성 검사 함수 (DTO의 Pattern과 동일하게)
  const validatePassword = (pwd) => {
    const passwordRegex = /^(?=.*[a-zA-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]+$/;
    return passwordRegex.test(pwd) && pwd.length >= 8 && pwd.length <= 20;
  };

  // 💡 비밀번호 확인 입력 시 일치 여부 확인
  useEffect(() => {
    if (confirmPassword === '') {
      setPasswordMatchMessage('');
      return;
    }
    if (password === confirmPassword) {
      setPasswordMatchMessage('✅ 비밀번호가 일치합니다.');
    } else {
      setPasswordMatchMessage('❌ 비밀번호가 일치하지 않습니다.');
    }
  }, [password, confirmPassword]);

  // 💡 아이디 중복 검사 (디바운싱 적용)
  const checkLoginIdDuplicate = useCallback(
    async (id) => {
      if (id.length < 4 || id.length > 20) {
        setLoginIdMessage('아이디는 4자 이상 20자 이하로 입력해주세요.');
        setIsLoginIdValid(false);
        return;
      }
      const idRegex = /^[a-z0-9]+$/;
      if (!idRegex.test(id)) {
        setLoginIdMessage('아이디는 영소문자와 숫자만 사용 가능합니다.');
        setIsLoginIdValid(false);
        return;
      }

      try {
        const response = await axios.get(`http://localhost:8080/api/auth/check-login-id?loginId=${id}`);
        if (response.data) { // true면 중복
          setLoginIdMessage('❌ 이미 사용 중인 아이디입니다.');
          setIsLoginIdValid(false);
        } else {
          setLoginIdMessage('✅ 사용 가능한 아이디입니다.');
          setIsLoginIdValid(true);
        }
      } catch (error) {
        console.error('아이디 중복 확인 실패:', error);
        setLoginIdMessage('아이디 중복 확인 중 오류가 발생했습니다.');
        setIsLoginIdValid(false);
      }
    },
    [] // 의존성 배열 비워둠 (함수 재생성 방지)
  );

  // 💡 이메일 중복 검사 (디바운싱 적용)
  const checkEmailDuplicate = useCallback(
    async (emailValue) => {
      if (emailValue === '') {
        setEmailMessage('');
        setIsEmailValid(false);
        return;
      }
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
      if (!emailRegex.test(emailValue)) {
        setEmailMessage('유효한 이메일 주소를 입력해주세요.');
        setIsEmailValid(false);
        return;
      }
      if (emailValue.length > 100) {
        setEmailMessage('이메일은 100자 이하로 입력해주세요.');
        setIsEmailValid(false);
        return;
      }

      try {
        const response = await axios.get(`http://localhost:8080/api/auth/check-email?email=${emailValue}`);
        if (response.data) { // true면 중복
          setEmailMessage('❌ 이미 사용 중인 이메일입니다.');
          setIsEmailValid(false);
        } else {
          setEmailMessage('✅ 사용 가능한 이메일입니다.');
          setIsEmailValid(true);
        }
      } catch (error) {
        console.error('이메일 중복 확인 실패:', error);
        setEmailMessage('이메일 중복 확인 중 오류가 발생했습니다.');
        setIsEmailValid(false);
      }
    },
    []
  );

  // 💡 닉네임 중복 검사 (디바운싱 적용)
  const checkUsernameDuplicate = useCallback(
    async (name) => {
      if (name.length < 2 || name.length > 50) {
        setUsernameMessage('닉네임은 2자 이상 50자 이하로 입력해주세요.');
        setIsUsernameValid(false);
        return;
      }
      // 닉네임에 대한 추가적인 패턴 검사가 필요하면 여기에 추가

      try {
        const response = await axios.get(`http://localhost:8080/api/auth/check-username?username=${name}`);
        if (response.data) { // true면 중복
          setUsernameMessage('❌ 이미 사용 중인 닉네임입니다.');
          setIsUsernameValid(false);
        } else {
          setUsernameMessage('✅ 사용 가능한 닉네임입니다.');
          setIsUsernameValid(true);
        }
      } catch (error) {
        console.error('닉네임 중복 확인 실패:', error);
        setUsernameMessage('닉네임 중복 확인 중 오류가 발생했습니다.');
        setIsUsernameValid(false);
      }
    },
    []
  );

  // 💡 입력 필드 변경 핸들러 (디바운싱 적용)
  const handleLoginIdChange = (e) => {
    const value = e.target.value;
    setLoginId(value);
    // 입력이 비어있으면 메시지 초기화
    if (value === '') {
      setLoginIdMessage('');
      setIsLoginIdValid(false);
      return;
    }
    // 디바운싱: 500ms 이후에 중복 검사 실행
    if (window.loginIdTimer) clearTimeout(window.loginIdTimer);
    window.loginIdTimer = setTimeout(() => {
      checkLoginIdDuplicate(value);
    }, 500);
  };

  const handleEmailChange = (e) => {
    const value = e.target.value;
    setEmail(value);
    if (value === '') {
      setEmailMessage('');
      setIsEmailValid(false);
      return;
    }
    if (window.emailTimer) clearTimeout(window.emailTimer);
    window.emailTimer = setTimeout(() => {
      checkEmailDuplicate(value);
    }, 500);
  };

  const handleUsernameChange = (e) => {
    const value = e.target.value;
    setUsername(value);
    if (value === '') {
      setUsernameMessage('');
      setIsUsernameValid(false);
      return;
    }
    if (window.usernameTimer) clearTimeout(window.usernameTimer);
    window.usernameTimer = setTimeout(() => {
      checkUsernameDuplicate(value);
    }, 500);
  };

  const handlePasswordChange = (e) => {
    const value = e.target.value;
    setPassword(value);
    setIsPasswordValid(validatePassword(value)); // 비밀번호 형식 유효성 검사
  };

  // 💡 회원가입 버튼 활성화 조건
  const isFormValid =
    isLoginIdValid &&
    isEmailValid &&
    isUsernameValid &&
    isPasswordValid && // 비밀번호 형식 유효성
    password === confirmPassword &&
    passwordMatchMessage === '✅ 비밀번호가 일치합니다.'; // 비밀번호 일치 메시지까지 확인

  const handleSubmit = async (e) => {
    e.preventDefault();

    // 최종 유효성 검사
    if (!isFormValid) {
      alert('⚠️ 모든 필드를 올바르게 입력하고 중복 검사를 완료해주세요.');
      return;
    }

    console.log('회원가입 시도:', { loginId, password, email, username });

    try {
      const response = await axios.post('http://localhost:8080/api/auth/signup', {
        loginId: loginId,
        password: password,
        email: email,
        username: username
      });

      console.log('회원가입 성공 응답:', response.data);
      alert('🎉 회원가입 성공! 이제 로그인 해주세요.');
      navigate('/login');

    } catch (error) {
      console.error('회원가입 실패:', error);
      const errorMessage = error.response?.data?.message || '회원가입 실패! 서버 오류 또는 알 수 없는 오류입니다.';
      alert(`⚠️ ${errorMessage}`);
    }
  };

  return (
    <div className="min-h-screen flex flex-col items-center justify-center bg-gradient-to-br from-green-50 to-blue-50 p-6 font-sans text-gray-800">
      <div className="bg-white p-10 rounded-xl shadow-2xl w-full max-w-md animate-fade-in-down">
        <h2 className="text-4xl font-extrabold text-green-700 mb-8 text-center">
          📝 회원가입 📝
        </h2>
        <form onSubmit={handleSubmit} className="space-y-6">
          {/* 아이디 입력 필드 */}
          <div>
            <label htmlFor="loginId" className="block text-lg font-medium text-gray-700 mb-2">
              아이디:
            </label>
            <input
              type="text"
              id="loginId"
              name="loginId"
              value={loginId}
              onChange={handleLoginIdChange} // 변경된 핸들러 사용
              className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-green-500 focus:border-green-500 text-lg placeholder-gray-400 transition duration-200"
              placeholder="로그인에 사용할 아이디를 입력하세요 (영소문자, 숫자 4-20자)"
              required
            />
            {loginIdMessage && ( // 메시지 표시
              <p className={`text-sm mt-1 ${isLoginIdValid ? 'text-green-600' : 'text-red-600'}`}>
                {loginIdMessage}
              </p>
            )}
          </div>

          {/* 비밀번호 입력 필드 */}
          <div>
            <label htmlFor="password" className="block text-lg font-medium text-gray-700 mb-2">
              비밀번호:
            </label>
            <input
              type="password"
              id="password"
              name="password"
              value={password}
              onChange={handlePasswordChange} // 변경된 핸들러 사용
              className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-green-500 focus:border-green-500 text-lg placeholder-gray-400 transition duration-200"
              placeholder="비밀번호를 입력하세요 (영문, 숫자, 특수문자 포함 8-20자)"
              required
            />
            {!isPasswordValid && password.length > 0 && ( // 비밀번호 형식 유효하지 않을 때만 메시지 표시
              <p className="text-sm mt-1 text-red-600">
                비밀번호는 영문, 숫자, 특수문자를 포함해야 하며 8-20자여야 합니다.
              </p>
            )}
          </div>

          {/* 비밀번호 확인 필드 */}
          <div>
            <label htmlFor="confirmPassword" className="block text-lg font-medium text-gray-700 mb-2">
              비밀번호 확인:
            </label>
            <input
              type="password"
              id="confirmPassword"
              name="confirmPassword"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-green-500 focus:border-green-500 text-lg placeholder-gray-400 transition duration-200"
              placeholder="비밀번호를 다시 입력하세요"
              required
            />
            {passwordMatchMessage && ( // 비밀번호 일치 메시지 표시
              <p className={`text-sm mt-1 ${password === confirmPassword ? 'text-green-600' : 'text-red-600'}`}>
                {passwordMatchMessage}
              </p>
            )}
          </div>

          {/* 이메일 입력 필드 */}
          <div>
            <label htmlFor="email" className="block text-lg font-medium text-gray-700 mb-2">
              이메일:
            </label>
            <input
              type="email"
              id="email"
              name="email"
              value={email}
              onChange={handleEmailChange} // 변경된 핸들러 사용
              className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-green-500 focus:border-green-500 text-lg placeholder-gray-400 transition duration-200"
              placeholder="이메일 주소를 입력하세요"
              required
            />
            {emailMessage && ( // 메시지 표시
              <p className={`text-sm mt-1 ${isEmailValid ? 'text-green-600' : 'text-red-600'}`}>
                {emailMessage}
              </p>
            )}
          </div>

          {/* 닉네임 입력 필드 */}
          <div>
            <label htmlFor="username" className="block text-lg font-medium text-gray-700 mb-2">
              닉네임:
            </label>
            <input
              type="text"
              id="username"
              name="username"
              value={username}
              onChange={handleUsernameChange} // 변경된 핸들러 사용
              className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-green-500 focus:border-green-500 text-lg placeholder-gray-400 transition duration-200"
              placeholder="사용할 닉네임을 입력하세요 (2-50자)"
              required
            />
            {usernameMessage && ( // 메시지 표시
              <p className={`text-sm mt-1 ${isUsernameValid ? 'text-green-600' : 'text-red-600'}`}>
                {usernameMessage}
              </p>
            )}
          </div>

          <button
            type="submit"
            disabled={!isFormValid} // 💡 모든 조건이 충족될 때만 활성화
            className={`w-full py-3 rounded-lg font-semibold text-xl shadow-md transition duration-300 ease-in-out transform ${
              isFormValid
                ? 'bg-green-600 text-white hover:bg-green-700 hover:scale-105 focus:outline-none focus:ring-2 focus:ring-green-500 focus:ring-offset-2'
                : 'bg-gray-400 text-gray-700 cursor-not-allowed'
            }`}
          >
            회원가입
          </button>
        </form>
        <p className="mt-8 text-center text-gray-600 text-lg">
          이미 계정이 있으신가요?{' '}
          <Link to="/login" className="text-green-600 hover:underline font-semibold">
            로그인
          </Link>
        </p>
        <Link to="/" className="block mt-6 text-center text-gray-500 hover:text-gray-700 transition duration-200 text-base">
          메인으로 돌아가기
        </Link>
      </div>
    </div>
  );
}

export default Signup;