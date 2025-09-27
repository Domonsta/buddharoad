import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios'; // axios 임포트

// ⭐️ 환경 변수에서 백엔드 기본 URL 가져오기
// .env 파일에 REACT_APP_BACKEND_BASE_URL=http://localhost:8080 이렇게 설정되어 있다고 가정
const API_BASE_URL = process.env.REACT_APP_BACKEND_BASE_URL || 'http://localhost:8080';

const UserInfoTab = ({ userInfo, onUserInfoUpdated }) => { // onUserInfoUpdated prop 추가
  const [isEditing, setIsEditing] = useState(false);
  const [formData, setFormData] = useState({
    // 초기 userInfo가 변경될 수 있으므로, 매번 새로 받아올 때마다 formData 초기화
    email: userInfo.email || '',
    nickname: userInfo.nickname || '', // username을 nickname으로 매핑
    password: '', // 새 비밀번호
    confirmPassword: '', // 새 비밀번호 확인
    oldPassword: '', // 기존 비밀번호 추가
  });

  const [emailStatus, setEmailStatus] = useState(''); // 'available', 'duplicated', 'error', ''
  const [nicknameStatus, setNicknameStatus] = useState(''); // 'available', 'duplicated', 'error', ''
  const [passwordError, setPasswordError] = useState(''); // 비밀번호 관련 에러 메시지 상태

  const navigate = useNavigate();

  // userInfo prop이 변경될 때마다 formData를 업데이트하고 상태 메시지 초기화
  useEffect(() => {
    setFormData({
      email: userInfo.email || '',
      nickname: userInfo.nickname || '',
      password: '',
      confirmPassword: '',
      oldPassword: '',
    });
    setEmailStatus('');
    setNicknameStatus('');
    setPasswordError(''); // useEffect에서 에러 메시지 초기화 추가
  }, [userInfo, isEditing]);

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
    // 입력 변경 시 중복 확인 상태 초기화 (다시 확인하도록 유도)
    if (e.target.name === 'email') setEmailStatus('');
    if (e.target.name === 'nickname') setNicknameStatus('');
    // ⭐ 비밀번호 필드 입력 시 비밀번호 관련 에러 메시지 초기화
    if (e.target.name.includes('Password')) {
      setPasswordError('');
    }
  };

  const handleCheckEmailDuplication = async () => {
    if (!formData.email) {
      setEmailStatus('이메일을 입력해주세요. 🧐');
      return;
    }
    if (formData.email === userInfo.email) {
      setEmailStatus('✅ 현재 사용 중인 이메일입니다.');
      return;
    }
    try {
      // ⭐ 백엔드 API 호출: 회원 정보 수정 시 이메일 중복 확인 (절대 경로 사용)
      const response = await axios.get(`${API_BASE_URL}/api/auth/me/check-email-for-update?email=${formData.email}`, {
        headers: { Authorization: `Bearer ${localStorage.getItem('accessToken')}` }
      });
      if (response.data) { // true면 중복, false면 사용 가능
        setEmailStatus('🚨 이미 사용 중인 이메일입니다! 😢');
      } else {
        setEmailStatus('✅ 사용 가능한 이메일입니다! 👍');
      }
    } catch (error) {
      console.error('이메일 중복 확인 중 오류 발생:', error);
      setEmailStatus('😥 이메일 중복 확인 중 오류가 발생하였습니다.');
    }
  };

  const handleCheckNicknameDuplication = async () => {
    if (!formData.nickname) {
      setNicknameStatus('닉네임을 입력해주세요. 🧐');
      return;
    }
    if (formData.nickname === userInfo.nickname) {
      setNicknameStatus('✅ 현재 사용 중인 닉네임입니다.');
      return;
    }
    try {
      // ⭐ 백엔드 API 호출: 회원 정보 수정 시 닉네임 중복 확인 (절대 경로 사용)
      const response = await axios.get(`${API_BASE_URL}/api/auth/me/check-username-for-update?username=${formData.nickname}`, {
        headers: { Authorization: `Bearer ${localStorage.getItem('accessToken')}` }
      });
      if (response.data) { // true면 중복, false면 사용 가능
        setNicknameStatus('🚨 이미 사용 중인 닉네임입니다! 😢');
      } else {
        setNicknameStatus('✅ 사용 가능한 닉네임입니다! 👍');
      }
    } catch (error) {
      console.error('닉네임 중복 확인 중 오류 발생:', error);
      setNicknameStatus('😥 닉네임 중복 확인 중 오류가 발생하였습니다.');
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setPasswordError(''); // 저장 버튼 클릭 시 에러 메시지 초기화

    // ⭐ 비밀번호 변경 시도 여부 확인 (세 필드 중 하나라도 값이 있으면 시도하는 것으로 간주)
    const isPasswordChangeAttempt = formData.oldPassword || formData.newPassword || formData.confirmPassword;

    if (isPasswordChangeAttempt) {
      // 비밀번호 변경 시 모든 필수 필드 입력 여부 확인
      if (!formData.oldPassword || !formData.newPassword || !formData.confirmPassword) {
        setPasswordError('비밀번호 변경 시 기존 비밀번호와 새 비밀번호를 모두 입력해야 합니다! 🧐');
        return;
      }
      // 새 비밀번호 유효성 검사 (길이 및 패턴)
      if (formData.newPassword.length < 8 || formData.newPassword.length > 20 ||
          !/^(?=.*[a-zA-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]+$/.test(formData.newPassword)) {
        setPasswordError('새 비밀번호는 8~20자이며 영문, 숫자, 특수문자를 포함해야 합니다.');
        return;
      }
      // 새 비밀번호와 확인 비밀번호 일치 여부 확인
      if (formData.newPassword !== formData.confirmPassword) {
        setPasswordError('새 비밀번호와 확인 비밀번호가 일치하지 않습니다! 다시 확인해주세요. 🧐');
        return;
      }
    }
    
    // 이메일 또는 닉네임 변경 시 중복 확인이 완료되었는지 (혹은 변경 없음) 확인
    if (formData.email !== userInfo.email && emailStatus !== '✅ 사용 가능한 이메일입니다! 👍') {
      alert('이메일 중복 확인을 완료하거나 다른 이메일을 입력해주세요!');
      return;
    }
    if (formData.nickname !== userInfo.nickname && nicknameStatus !== '✅ 사용 가능한 닉네임입니다! 👍') {
      alert('닉네임 중복 확인을 완료하거나 다른 닉네임을 입력해주세요!');
      return;
    }

    try {
      // 백엔드로 보낼 데이터 준비
      const updatePayload = {
        email: formData.email,
        username: formData.nickname, // DTO 필드명에 맞춰 `username`으로 보냄
        oldPassword: formData.oldPassword || null, // 비밀번호 변경 안 할 경우 null 전송
        newPassword: formData.newPassword || null,
      };

      // ⭐ 백엔드 회원 정보 수정 API 호출 (절대 경로 사용)
      const response = await axios.put(`${API_BASE_URL}/api/auth/me`, updatePayload, {
        headers: { Authorization: `Bearer ${localStorage.getItem('accessToken')}` }
      });

      alert(response.data + ' 🎉'); // 백엔드에서 보낸 성공 메시지 표시
      
      // ⭐ 핵심 변경: 비밀번호가 변경되었을 경우 재로그인 요구
      // isPasswordChangeAttempt 조건은 비밀번호 필드에 어떤 값이라도 입력했을 때 true
      // response.status === 200은 서버에서 정상 응답이 왔을 때
      if (isPasswordChangeAttempt && response.status === 200) { 
          alert('비밀번호가 변경되었습니다. 보안을 위해 다시 로그인해주세요! 🔐');
          localStorage.removeItem('accessToken'); // 기존 토큰 삭제
          navigate('/login'); // 로그인 페이지로 이동
      } else {
          // 비밀번호는 변경되지 않고 다른 정보만 수정했을 경우
          if (onUserInfoUpdated) {
              onUserInfoUpdated(); // 최신 정보 다시 불러오기
          }
          setIsEditing(false); // 수정 모드 종료
      }

    } catch (error) {
      console.error('회원 정보 수정 중 오류 발생:', error.response?.data || error.message);
      // ⭐ 백엔드에서 보낸 상세 에러 메시지를 파싱하여 표시 (수정 부분!)
      const serverErrorMessage = error.response?.data?.message // data가 객체이고 message 속성이 있는 경우
                               || (typeof error.response?.data === 'string' ? error.response.data : '알 수 없는 오류'); // data가 문자열인 경우, 아니면 기본 메시지

      // 비밀번호 오류는 passwordError 상태로, 그 외는 alert로
      if (serverErrorMessage.includes("기존 비밀번호가 일치하지 않습니다.") || serverErrorMessage.includes("비밀번호 변경 시에는 기존 비밀번호와 새 비밀번호를 모두 입력해야 합니다.")) {
          setPasswordError(serverErrorMessage + ' 😥');
      } else {
          alert(`🚨 회원 정보 수정 중 오류가 발생하였습니다: ${serverErrorMessage} 😥`);
      }
    }
  };

  const handleWithdrawal = async () => {
    if (!window.confirm('🚨 정말로 회원 탈퇴하시겠습니까? 계정은 비활성화되며 복구되지 않을 수 있습니다! 😱\n\n확인을 누르면 비밀번호 입력 창이 나옵니다.')) {
      return;
    }

    const passwordToConfirm = prompt('회원 탈퇴를 계속하려면 비밀번호를 입력해주세요.');
    if (!passwordToConfirm) {
      alert('비밀번호를 입력하지 않아 회원 탈퇴가 취소되었습니다. ↩️');
      return;
    }

    try {
      // ⭐ 백엔드 회원 탈퇴 API 호출 (절대 경로 사용)
      const response = await axios.delete(`${API_BASE_URL}/api/auth/me`, {
        headers: { Authorization: `Bearer ${localStorage.getItem('accessToken')}` },
        data: { password: passwordToConfirm } // DELETE 요청의 body에 데이터 전송
      });

      alert(response.data + ' 👋'); // 백엔드에서 보낸 성공 메시지 표시
      localStorage.removeItem('accessToken'); // 토큰 삭제
      navigate('/login'); // 탈퇴 후 로그인 페이지로 이동

    } catch (error) {
      console.error('회원 탈퇴 중 오류 발생:', error.response?.data || error.message);
      // ⭐ 백엔드에서 보낸 상세 에러 메시지를 파싱하여 표시 (수정 부분!)
      const serverErrorMessage = error.response?.data?.message // data가 객체이고 message 속성이 있는 경우
                               || (typeof error.response?.data === 'string' ? error.response.data : '알 수 없는 오류'); // data가 문자열인 경우, 아니면 기본 메시지
      alert(`🚨 회원 탈퇴 중 오류가 발생하였습니다: ${serverErrorMessage} 😥`);
    }
  };

  return (
    <div className="tab-content-section user-info-tab">
      <h3 className="tab-content-title">회원 정보 {!isEditing ? '보기' : '수정'}</h3>

      {!isEditing ? (
        <>
          <div className="info-item">
            <span className="info-label">아이디</span>
            <span className="info-value">{userInfo.loginId || '정보 없음'}</span> {/* loginId 사용 */}
          </div>
          <div className="info-item">
            <span className="info-label">이메일</span>
            <span className="info-value">{userInfo.email || '정보 없음'}</span>
          </div>
          <div className="info-item">
            <span className="info-label">닉네임</span>
            <span className="info-value">{userInfo.nickname || '정보 없음'}</span> {/* nickname (username) 사용 */}
          </div>
          <div className="info-item">
            <span className="info-label">가입일</span>
            <span className="info-value">{userInfo.createdAt ? new Date(userInfo.createdAt).toLocaleDateString() : '정보 없음'}</span> {/* createdAt을 보기 좋게 포맷 */}
          </div>
          <button className="action-button" onClick={() => setIsEditing(true)}>정보 수정 ✏️</button>
        </>
      ) : (
        <form onSubmit={handleSubmit}>
          {/* 이메일 수정 필드 */}
          <div className="info-item action-input-group">
            <label className="info-label" htmlFor="email">이메일</label>
            <div className="input-with-button">
              <input
                type="email"
                id="email"
                name="email"
                value={formData.email}
                onChange={handleChange}
                onBlur={handleCheckEmailDuplication} // 포커스 잃으면 중복 확인
                className="info-value-input"
                required
              />
              <button type="button" className="check-button" onClick={handleCheckEmailDuplication}>중복 확인</button>
            </div>
            {emailStatus && <p className={`status-message ${emailStatus.includes('사용 가능') ? 'success' : 'error'}`}>{emailStatus}</p>}
          </div>

          {/* 닉네임 수정 필드 */}
          <div className="info-item action-input-group">
            <label className="info-label" htmlFor="nickname">닉네임</label>
            <div className="input-with-button">
              <input
                type="text"
                id="nickname"
                name="nickname"
                value={formData.nickname}
                onChange={handleChange}
                onBlur={handleCheckNicknameDuplication} // 포커스 잃으면 중복 확인
                className="info-value-input"
                required
              />
              <button type="button" className="check-button" onClick={handleCheckNicknameDuplication}>중복 확인</button>
            </div>
            {nicknameStatus && <p className={`status-message ${nicknameStatus.includes('사용 가능') ? 'success' : 'error'}`}>{nicknameStatus}</p>}
          </div>

          {/* 비밀번호 변경 필드 */}
          <div className="info-item action-input-group password-change-section">
            <h4 style={{ width: '100%', textAlign: 'left', marginBottom: '15px', color: '#555' }}>비밀번호 변경</h4>
            <label className="info-label" htmlFor="oldPassword">기존 비밀번호</label>
            <input
              type="password"
              id="oldPassword"
              name="oldPassword"
              value={formData.oldPassword}
              onChange={handleChange}
              className="info-value-input"
              placeholder="현재 비밀번호를 입력해주세요."
              autoComplete="current-password"
            />
            <label className="info-label" htmlFor="newPassword">새 비밀번호</label>
            <input
              type="password"
              id="newPassword"
              name="newPassword"
              value={formData.newPassword}
              onChange={handleChange}
              className="info-value-input"
              placeholder="새 비밀번호 (8~20자, 영문/숫자/특수문자 포함)"
              autoComplete="new-password"
            />
            <label className="info-label" htmlFor="confirmPassword">새 비밀번호 확인</label>
            <input
              type="password"
              id="confirmPassword"
              name="confirmPassword"
              value={formData.confirmPassword}
              onChange={handleChange}
              className="info-value-input"
              placeholder="새 비밀번호를 다시 한번 입력해주세요."
              autoComplete="new-password"
            />
            {passwordError && <p className="status-message error">{passwordError}</p>}
          </div>

          <div className="button-group" style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px', marginTop: '30px' }}>
            <button type="submit" className="action-button">저장 ✅</button>
            <button type="button" className="action-button" onClick={() => setIsEditing(false)}>취소 ↩️</button>
            <button type="button" className="action-button delete-button" onClick={handleWithdrawal}>회원 탈퇴 🗑️</button>
          </div>
        </form>
      )}
    </div>
  );
};

export default UserInfoTab;