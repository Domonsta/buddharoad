import React, { useState } from 'react';

const AdminInquiryBoardTab = () => {
  // 가상의 문의 글 목록 (관리자용)
  const [inquiries, setInquiries] = useState([
    { id: 1, title: '로그인 오류 문의 (도몬)', status: '답변 대기', date: '2025-07-01', user: '도몬' },
    { id: 2, title: '사진 업로드 문제 (여행가Lv1)', status: '답변 대기', date: '2025-07-08', user: '여행가Lv1' },
    { id: 3, title: '회원 탈퇴 처리 문의 (탈퇴예정)', status: '답변 완료', date: '2025-06-25', user: '탈퇴예정' },
  ]);

  const handleReply = (inquiryId) => {
    alert(`${inquiryId}번 문의에 답글 작성 기능을 엽니다. (별도 페이지/모달 구현 필요)`);
    // 실제로는 문의 상세 페이지로 이동하거나, 답글 작성 모달을 띄우는 로직
    setInquiries(inquiries.map(iq => iq.id === inquiryId ? { ...iq, status: '답변 중' } : iq)); // 상태 변경 예시
  };

  return (
    <div className="tab-content-section admin-inquiry-board-tab">
      <h3 className="tab-content-title">문의 게시판 ✉️</h3>
      <div className="search-form">
        <input type="text" placeholder="문의 제목 또는 작성자 검색" />
        <button className="search-button">검색</button>
      </div>

      <table className="admin-member-table"> {/* 회원 관리 테이블 스타일 재활용 */}
        <thead>
          <tr>
            <th>ID</th>
            <th>제목</th>
            <th>작성자</th>
            <th>접수일</th>
            <th>상태</th>
            <th>액션</th>
          </tr>
        </thead>
        <tbody>
          {inquiries.length > 0 ? (
            inquiries.map(inquiry => (
              <tr key={inquiry.id}>
                <td>{inquiry.id}</td>
                <td>{inquiry.title}</td>
                <td>{inquiry.user}</td>
                <td>{inquiry.date}</td>
                <td>
                  <span className={`status-label ${inquiry.status === '답변 완료' ? 'status-active' : inquiry.status === '답변 대기' ? 'status-inactive' : ''}`}>
                    {inquiry.status}
                  </span>
                </td>
                <td>
                  <button className="action-button" onClick={() => handleReply(inquiry.id)}>답글 작성하기</button>
                </td>
              </tr>
            ))
          ) : (
            <tr><td colSpan="6">새로운 문의가 없어요. 🥳</td></tr>
          )}
        </tbody>
      </table>
    </div>
  );
};

export default AdminInquiryBoardTab;