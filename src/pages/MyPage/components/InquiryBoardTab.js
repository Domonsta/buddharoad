import React, { useState } from 'react';

const InquiryBoardTab = () => {
  // 가상의 문의 글 목록 (실제로는 백엔드에서 가져올 데이터)
  const [inquiries] = useState([
    { id: 1, title: '로그인 오류 문의', status: '답변 대기', date: '2025-07-01' },
    { id: 2, title: '게시글 삭제 요청', status: '답변 완료', date: '2025-06-25' },
    { id: 3, title: '사진 업로드 문제', status: '답변 대기', date: '2025-07-08' },
  ]);

  return (
    <div className="tab-content-section inquiry-board-tab">
      <h3 className="tab-content-title">문의 게시판 ❓</h3>
      <p>궁금한 점이 있으신가요? 문의 글을 남겨주세요! ✉️</p>
      <button className="action-button" style={{marginBottom: '20px'}}>새 문의 작성</button>

      <h4>나의 문의 내역</h4>
      <div className="post-list">
        {inquiries.length > 0 ? (
          inquiries.map(inquiry => (
            <div key={inquiry.id} className="post-item">
              <span className="post-title">{inquiry.title}</span>
              <div>
                <span className="post-meta">{inquiry.date}</span>
                <span className={`status-label ${inquiry.status === '답변 완료' ? 'status-active' : 'status-inactive'}`}>
                  {inquiry.status}
                </span>
              </div>
              <button className="action-button">자세히 보기</button>
            </div>
          ))
        ) : (
          <p>작성한 문의 글이 없어요. 😥</p>
        )}
      </div>
    </div>
  );
};

export default InquiryBoardTab;