// src/Components/MessageBox.js
import React, { useEffect, useState } from 'react';

/**
 * @brief 사용자에게 메시지를 표시하는 커스텀 메시지 박스 컴포넌트입니다.
 * alert() 대신 사용하여 UI를 블록하지 않고, 디자인을 커스터마이징할 수 있습니다.
 *
 * @param {object} props - 컴포넌트 속성
 * @param {string} props.message - 표시할 메시지 텍스트
 * @param {boolean} props.isVisible - 메시지 박스의 가시성 제어 (true: 보임, false: 숨김)
 * @param {function} props.onClose - 메시지 박스가 닫힐 때 호출될 콜백 함수
 * @param {number} [props.duration=3000] - 메시지 박스가 자동으로 사라지는 시간 (밀리초), 0이면 수동 닫기
 */
const MessageBox = ({ message, isVisible, onClose, duration = 3000 }) => {
  // 메시지 박스의 내부 가시성 상태를 관리합니다.
  const [showBox, setShowBox] = useState(isVisible);

  // isVisible prop이 변경될 때마다 내부 상태를 업데이트합니다.
  useEffect(() => {
    setShowBox(isVisible);
  }, [isVisible]);

  // 메시지 박스가 보일 때 일정 시간 후 자동으로 사라지도록 타이머를 설정합니다.
  // duration이 0이 아니면 타이머를 설정하고, 0이면 수동으로 닫아야 합니다.
  useEffect(() => {
    let timer;
    if (showBox && duration > 0) {
      timer = setTimeout(() => {
        setShowBox(false); // 메시지 박스 숨김
        if (onClose) {
          onClose(); // onClose 콜백 호출
        }
      }, duration);
    }
    // 컴포넌트가 언마운트되거나 showBox, duration이 변경될 때 타이머를 정리합니다.
    return () => clearTimeout(timer);
  }, [showBox, duration, onClose]);

  // isVisible이 false이거나 showBox가 false이면 아무것도 렌더링하지 않습니다.
  if (!isVisible && !showBox) return null;

  return (
    <div
      className={`fixed inset-0 bg-gray-900 bg-opacity-50 flex items-center justify-center p-4 z-50 transition-opacity duration-300 ${
        showBox ? 'opacity-100' : 'opacity-0 pointer-events-none'
      }`}
    >
      <div
        className={`bg-white rounded-xl shadow-2xl p-8 max-w-sm w-full text-center transform transition-all duration-300 ${
          showBox ? 'scale-100 opacity-100' : 'scale-90 opacity-0'
        }`}
      >
        <p className="text-xl font-semibold text-gray-800 mb-6">{message}</p>
        {duration === 0 && ( // duration이 0일 경우에만 닫기 버튼을 표시합니다.
          <button
            onClick={() => {
              setShowBox(false);
              if (onClose) {
                onClose();
              }
            }}
            className="mt-4 px-6 py-3 bg-blue-600 text-white rounded-lg shadow-md hover:bg-blue-700 transition-colors duration-200 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-opacity-50"
          >
            확인
          </button>
        )}
      </div>
    </div>
  );
};

export default MessageBox;