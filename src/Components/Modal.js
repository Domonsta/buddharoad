// src/Components/Modal.js
import React from 'react';

function Modal({ children, onClose }) {
  return (
    // 모달 오버레이: 전체 화면을 덮고 배경을 어둡게 함
    <div className="fixed inset-0 bg-black bg-opacity-75 flex items-center justify-center z-50 p-4">
      {/* 모달 내용 컨테이너 */}
      <div className="relative bg-white rounded-lg shadow-xl max-w-lg w-full max-h-[90vh] overflow-auto transform transition-all duration-300 scale-100 opacity-100">
        {/* 닫기 버튼 */}
        <button
          onClick={onClose}
          className="absolute top-3 right-3 text-gray-600 hover:text-gray-900 text-3xl font-bold p-1 rounded-full bg-gray-200 hover:bg-gray-300 transition-colors"
          aria-label="모달 닫기"
        >
          &times;
        </button>
        {/* 모달 콘텐츠 */}
        <div className="p-6">
          {children} {/* 자식 요소들이 여기에 렌더링됨 */}
        </div>
      </div>
    </div>
  );
}

export default Modal;