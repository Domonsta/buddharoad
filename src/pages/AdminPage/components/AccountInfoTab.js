import React, { useState, useEffect } from 'react'; // useEffect 추가
import axios from 'axios'; // axios 임포트
import { useNavigate } from 'react-router-dom'; // 페이지 이동을 위해

// ⭐️ 환경 변수에서 백엔드 기본 URL 가져오기
const API_BASE_URL = process.env.REACT_APP_BACKEND_BASE_URL || 'http://localhost:8080';

// AccountInfoTab 컴포넌트
// props로 adminInfo (초기 정보)와 onAdminInfoUpdated (정보 수정 후 콜백)를 받음
const AccountInfoTab = ({ adminInfo, onAdminInfoUpdated }) => {
  const [isEditing, setIsEditing] = useState(false); // 수정 모드 여부
  const [formData, setFormData] = useState({
    // 초기 adminInfo를 바탕으로 formData 초기화
    email: adminInfo.email || '',
    nickname: adminInfo.name || '', // adminInfo.name이 닉네임으로 사용된다고 가정
    password: '',
    confirmPassword: '',
    oldPassword: '', // 기존 비밀번호 필드
  });

  const [emailStatus, setEmailStatus] = useState(''); // 이메일 중복 확인 상태 메시지
  const [nicknameStatus, setNicknameStatus] = useState(''); // 닉네임 중복 확인 상태 메시지
  const [passwordError, setPasswordError] = useState(''); // 비밀번호 관련 에러 메시지

  const navigate = useNavigate();

  // adminInfo prop이 변경되거나 수정 모드가 전환될 때 formData 및 상태 메시지 초기화
  useEffect(() => {
    setFormData({
      email: adminInfo.email || '',
      nickname: adminInfo.name || '', // adminInfo.name -> nickname
      password: '',
      confirmPassword: '',
      oldPassword: '',
    });
    setEmailStatus('');
    setNicknameStatus('');
    setPasswordError('');
  }, [adminInfo, isEditing]);

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
    // 입력 변경 시 상태 메시지 초기화
    if (e.target.name === 'email') setEmailStatus('');
    if (e.target.name === 'nickname') setNicknameStatus('');
    if (e.target.name.includes('Password')) setPasswordError('');
  };

  // ⭐ 이메일 중복 확인 (기존 회원 정보 수정 API 재활용)
  const handleCheckEmailDuplication = async () => {
    if (!formData.email) {
      setEmailStatus('이메일을 입력해주세요. 🧐');
      return;
    }
    // 현재 이메일과 같으면 중복 확인 불필요
    if (formData.email === adminInfo.email) {
      setEmailStatus('✅ 현재 사용 중인 이메일입니다.');
      return;
    }
    try {
      // /api/auth/me/check-email-for-update API를 관리자 계정 정보 수정에서도 재활용
      // 백엔드에서는 AuthenticationPrincipal로 현재 로그인된 사용자(관리자)의 ID를 가져와 검사함
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

  // ⭐ 닉네임 중복 확인 (기존 회원 정보 수정 API 재활용)
  const handleCheckNicknameDuplication = async () => {
    if (!formData.nickname) {
      setNicknameStatus('닉네임을 입력해주세요. 🧐');
      return;
    }
    // 현재 닉네임과 같으면 중복 확인 불필요
    if (formData.nickname === adminInfo.name) { // adminInfo.name이 닉네임이라고 가정
      setNicknameStatus('✅ 현재 사용 중인 닉네임입니다.');
      return;
    }
    try {
      // /api/auth/me/check-username-for-update API를 관리자 계정 정보 수정에서도 재활용
      const response = await axios.get(`${API_BASE_URL}/api/auth/me/check-username-for-update?username=${formData.nickname}`, {
        headers: { Authorization: `Bearer ${localStorage.getItem('accessToken')}` }
      });
      if (response.data) {
        setNicknameStatus('🚨 이미 사용 중인 닉네임입니다! 😢');
      } else {
        setNicknameStatus('✅ 사용 가능한 닉네임입니다! 👍');
      }
    } catch (error) {
      console.error('닉네임 중복 확인 중 오류 발생:', error);
      setNicknameStatus('😥 닉네임 중복 확인 중 오류가 발생하였습니다.');
    }
  };

  // ⭐ 관리자 계정 정보 수정
  const handleSubmit = async (e) => {
    e.preventDefault();
    setPasswordError(''); // 에러 메시지 초기화

    // 비밀번호 변경 시도 여부 확인
    const isPasswordChangeAttempt = formData.oldPassword || formData.newPassword || formData.confirmPassword;

    if (isPasswordChangeAttempt) {
      // 비밀번호 변경 시 필수 필드 입력 확인
      if (!formData.oldPassword || !formData.newPassword || !formData.confirmPassword) {
        setPasswordError('비밀번호 변경 시 기존 비밀번호와 새 비밀번호를 모두 입력해야 합니다! 🧐');
        return;
      }
      // 새 비밀번호 유효성 검사 (8~20자, 영문/숫자/특수문자 포함)
      if (formData.newPassword.length < 8 || formData.newPassword.length > 20 ||
          !/^(?=.*[a-zA-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]+$/.test(formData.newPassword)) {
        setPasswordError('새 비밀번호는 8~20자이며 영문, 숫자, 특수문자를 포함해야 합니다.');
        return;
      }
      // 새 비밀번호와 확인 비밀번호 일치 여부
      if (formData.newPassword !== formData.confirmPassword) {
        setPasswordError('새 비밀번호와 확인 비밀번호가 일치하지 않습니다! 다시 확인해주세요. 🧐');
        return;
      }
    }
    
    // 이메일 또는 닉네임 변경 시 중복 확인 완료 여부
    if (formData.email !== adminInfo.email && emailStatus !== '✅ 사용 가능한 이메일입니다! 👍') {
      alert('이메일 중복 확인을 완료하거나 다른 이메일을 입력해주세요!');
      return;
    }
    if (formData.nickname !== adminInfo.name && nicknameStatus !== '✅ 사용 가능한 닉네임입니다! 👍') {
      alert('닉네임 중복 확인을 완료하거나 다른 닉네임을 입력해주세요!');
      return;
    }

    try {
      // 백엔드로 보낼 데이터 (AdminUpdateDTO 형식)
      const updatePayload = {
        email: formData.email,
        username: formData.nickname, // DTO 필드명은 username (백엔드 Member 엔티티의 username과 일치)
        oldPassword: formData.oldPassword || null,
        newPassword: formData.newPassword || null,
      };

      // ⭐ 백엔드 관리자 계정 정보 수정 API 호출
      const response = await axios.put(`${API_BASE_URL}/api/auth/admin/me`, updatePayload, {
        headers: { Authorization: `Bearer ${localStorage.getItem('accessToken')}` }
      });

      alert(response.data + ' 🎉'); // 백엔드 성공 메시지 표시
      
      // ⭐ 비밀번호가 변경되었을 경우 재로그인 요구 (보안상 필수)
      if (isPasswordChangeAttempt && response.status === 200) {
          alert('비밀번호가 변경되었습니다. 보안을 위해 다시 로그인해주세요! 🔐');
          localStorage.removeItem('accessToken'); // 기존 토큰 삭제
          navigate('/admin/login'); // 관리자 로그인 페이지로 이동 (경로 확인 필요)
      } else {
          // 비밀번호는 변경되지 않고 다른 정보만 수정했을 경우
          if (onAdminInfoUpdated) { // 부모 컴포넌트(AdminPage)에 정보 업데이트 알림
              onAdminInfoUpdated();
          }
          setIsEditing(false); // 수정 모드 종료
      }

    } catch (error) {
      console.error('관리자 계정 정보 수정 중 오류 발생:', error.response?.data || error.message);
      const serverErrorMessage = error.response?.data?.message || (typeof error.response?.data === 'string' ? error.response.data : '알 수 없는 오류');

      // 비밀번호 오류는 passwordError 상태로, 그 외는 alert로
      if (serverErrorMessage.includes("기존 비밀번호가 일치하지 않습니다.") || serverErrorMessage.includes("비밀번호 변경 시에는 기존 비밀번호와 새 비밀번호를 모두 입력해야 합니다.")) {
          setPasswordError(serverErrorMessage + ' 😥');
      } else {
          alert(`🚨 관리자 계정 정보 수정 중 오류가 발생하였습니다: ${serverErrorMessage} 😥`);
      }
    }
  };

  return (
    <div className="tab-content-section account-info-tab">
      <h3 className="tab-content-title">계정 정보 {!isEditing ? '보기' : '수정'}</h3>

      {!isEditing ? (
        <>
          {/* 정보 보기: 아이디, 닉네임, 이메일 */}
          <div className="info-item view-mode">
            <span className="info-label">아이디</span>
            <span className="info-value">{adminInfo.loginId || '정보 없음'}</span> {/* 백엔드에서 loginId를 받아와야 함 */}
          </div>
          <div className="info-item view-mode">
            <span className="info-label">닉네임</span>
            <span className="info-value">{adminInfo.username || '정보 없음'}</span> {/* 백엔드에서 username을 받아와야 함 */}
          </div>
          <div className="info-item view-mode">
            <span className="info-label">이메일</span>
            <span className="info-value">{adminInfo.email || '정보 없음'}</span>
          </div>
          <div className="button-group" style={{ display: 'flex', justifyContent: 'center', gap: '15px', marginTop: '40px' }}>
            <button className="action-button" onClick={() => setIsEditing(true)}>정보 수정 ✏️</button>
          </div>
        </>
      ) : (
        <form onSubmit={handleSubmit}>
          {/* 정보 수정 폼 */}
          
          {/* 닉네임 필드 (중복확인 버튼 포함) */}
          <div className="info-item edit-mode">
            <label className="info-label" htmlFor="adminNickname">닉네임</label> {/* htmlFor 변경 */}
            <div className="input-with-button">
              <input
                type="text"
                id="adminNickname" // id 변경
                name="nickname"
                value={formData.nickname}
                onChange={handleChange}
                className="info-value-input"
                required
              />
              <button
                type="button"
                className="small-action-button"
                onClick={handleCheckNicknameDuplication} // 함수 이름 변경
              >
                중복확인
              </button>
            </div>
            {nicknameStatus && <p className={`status-message ${nicknameStatus.includes('사용 가능') ? 'success' : 'error'}`}>{nicknameStatus}</p>}
          </div>

          {/* 이메일 필드 (중복확인 버튼 포함) */}
          <div className="info-item edit-mode">
            <label className="info-label" htmlFor="adminEmail">이메일</label>
            <div className="input-with-button">
              <input
                type="email"
                id="adminEmail"
                name="email"
                value={formData.email}
                onChange={handleChange}
                className="info-value-input"
                required
              />
              <button
                type="button"
                className="small-action-button"
                onClick={handleCheckEmailDuplication} // 함수 이름 변경
              >
                중복확인
              </button>
            </div>
            {emailStatus && <p className={`status-message ${emailStatus.includes('사용 가능') ? 'success' : 'error'}`}>{emailStatus}</p>}
          </div>

          {/* 비밀번호 필드 */}
          <div className="info-item edit-mode password-change-section"> {/* password-change-section 클래스 추가 */}
            <h4 style={{ width: '100%', textAlign: 'left', marginBottom: '15px', color: '#555' }}>비밀번호 변경</h4> {/* 타이틀 추가 */}
            <label className="info-label" htmlFor="adminOldPassword">기존 비밀번호</label> {/* htmlFor 변경 */}
            <input
              type="password"
              id="adminOldPassword" // id 변경
              name="oldPassword"
              value={formData.oldPassword}
              onChange={handleChange}
              className="info-value-input"
              placeholder="현재 비밀번호를 입력해주세요."
              autoComplete="current-password"
            />
            <label className="info-label" htmlFor="adminNewPassword">새 비밀번호</label> {/* htmlFor 변경 */}
            <input
              type="password"
              id="adminNewPassword" // id 변경
              name="newPassword"
              value={formData.newPassword}
              onChange={handleChange}
              className="info-value-input"
              placeholder="새 비밀번호 (8~20자, 영문/숫자/특수문자 포함)"
              autoComplete="new-password"
            />
            <label className="info-label" htmlFor="adminConfirmNewPassword">새 비밀번호 확인</label> {/* htmlFor 변경 */}
            <input
              type="password"
              id="adminConfirmNewPassword" // id 변경
              name="confirmPassword"
              value={formData.confirmPassword}
              onChange={handleChange}
              className="info-value-input"
              placeholder="새 비밀번호를 다시 한번 입력해주세요."
              autoComplete="new-password"
            />
            {passwordError && <p className="status-message error">{passwordError}</p>}
          </div>

          <div className="button-group" style={{ display: 'flex', justifyContent: 'center', gap: '15px', marginTop: '40px' }}>
            <button type="submit" className="action-button">저장 ✅</button>
            <button type="button" className="action-button secondary" onClick={() => setIsEditing(false)}>취소 ↩️</button>
          </div>
        </form>
      )}
    </div>
  );
};

export default AccountInfoTab;