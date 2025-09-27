// src/pages/components/MemberManagementTab.js (새로 생성할 파일)
import React, { useState, useEffect } from 'react';
import axios from 'axios';

// 환경 변수에서 백엔드 기본 URL 가져오기
const API_BASE_URL = process.env.REACT_APP_BACKEND_BASE_URL || 'http://localhost:8080';

const MemberManagementTab = () => {
    const [members, setMembers] = useState([]); // 실제 회원 목록
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    // 검색 및 페이징 필터 상태
    const [filter, setFilter] = useState({
        searchType: 'USERNAME', // 기본 검색 기준: 닉네임 (USERNAME)
        keyword: '',            // 검색 키워드
        accountStatus: '',      // 계정 상태 필터 ('', 'ACTIVE', 'BLOCKED', 'DEACTIVATED')
        role: '',               // 역할 필터 ('', 'USER', 'CONTENT_ADMIN', 'SYSTEM_ADMIN')
        sortBy: 'createdAt',    // 정렬 기준
        sortOrder: 'desc',      // 정렬 순서
        page: 0,                // 현재 페이지 (0부터 시작)
        size: 10,               // 페이지당 항목 수
    });
    const [totalPages, setTotalPages] = useState(0); // 전체 페이지 수

    // ⭐ 회원 목록 불러오는 함수
    const fetchMembers = async () => {
        setLoading(true);
        setError(null);
        try {
            const params = {
                searchType: filter.searchType,
                keyword: filter.keyword,
                accountStatus: filter.accountStatus,
                role: filter.role,
                sortBy: filter.sortBy,
                sortOrder: filter.sortOrder,
                page: filter.page,
                size: filter.size,
            };
            const response = await axios.get(`${API_BASE_URL}/api/auth/admin/members`, {
                headers: { Authorization: `Bearer ${localStorage.getItem('accessToken')}` },
                params: params,
            });
            setMembers(response.data.content);
            setTotalPages(response.data.totalPages);
            console.log('✅ 회원 목록 불러오기 성공:', response.data);
        } catch (err) {
            console.error('🚨 회원 목록 불러오기 실패:', err.response?.data || err.message);
            setError(`회원 목록을 불러오는데 실패했습니다: ${err.response?.data?.message || err.response?.data || err.message} 😥`);
            // 권한 문제 발생 시 관리자 로그인 페이지로 리다이렉트
            if (err.response && (err.response.status === 401 || err.response.status === 403)) {
                alert('관리자 권한이 없습니다. 관리자 계정으로 다시 로그인해주세요. 🚪');
                localStorage.removeItem('accessToken');
                // navigate('/admin/login'); // AdminPage에서 처리
            }
        } finally {
            setLoading(false);
        }
    };

    // 컴포넌트 마운트 시 또는 필터 변경 시 회원 목록 불러오기
    useEffect(() => {
        fetchMembers();
    }, [filter]); // filter 객체 자체가 변경될 때 (혹은 그 안의 값이 변경될 때) 다시 불러옴

    // 검색/필터링/정렬 필드 변경 핸들러
    const handleFilterChange = (e) => {
        setFilter({ ...filter, [e.target.name]: e.target.value, page: 0 }); // 필터 변경 시 페이지 0으로 초기화
    };

    // 페이지 변경 핸들러
    const handlePageChange = (newPage) => {
        setFilter({ ...filter, page: newPage });
    };

    // ⭐ 회원 상태 변경 핸들러
    const handleStatusChange = async (memberNo, currentStatus) => {
        let newStatus = '';
        let confirmMessage = '';
        if (currentStatus === 'ACTIVE') {
            newStatus = 'BLOCKED';
            confirmMessage = '이 회원을 정지 처리하시겠습니까?';
        } else if (currentStatus === 'BLOCKED') {
            newStatus = 'ACTIVE';
            confirmMessage = '이 회원을 활성 상태로 변경하시겠습니까?';
        } else {
            alert('변경할 수 없는 상태입니다.');
            return;
        }

        if (!window.confirm(confirmMessage)) {
            return;
        }

        try {
            const response = await axios.patch(`${API_BASE_URL}/api/auth/admin/members/${memberNo}/status?newStatus=${newStatus}`, null, {
                headers: { Authorization: `Bearer ${localStorage.getItem('accessToken')}` }
            });
            alert(response.data + ' 🎉');
            fetchMembers(); // 변경 후 목록 새로고침
        } catch (err) {
            console.error('🚨 회원 상태 변경 실패:', err.response?.data || err.message);
            alert(`🚨 회원 상태 변경 실패: ${err.response?.data?.message || err.response?.data || err.message} 😥`);
        }
    };

    // ⭐ 회원 탈퇴 처리 핸들러 (소프트 삭제)
    const handleMemberDelete = async (memberNo, nickname) => {
        if (!window.confirm(`정말로 ${nickname} 님을 탈퇴 처리하시겠습니까? 계정은 비활성화됩니다.`)) {
            return;
        }
        try {
            const response = await axios.patch(`${API_BASE_URL}/api/auth/admin/members/${memberNo}/delete`, null, {
                headers: { Authorization: `Bearer ${localStorage.getItem('accessToken')}` }
            });
            alert(response.data + ' 👋');
            fetchMembers(); // 탈퇴 후 목록 새로고침
        } catch (err) {
            console.error('🚨 회원 탈퇴 실패:', err.response?.data || err.message);
            alert(`🚨 회원 탈퇴 실패: ${err.response?.data?.message || err.response?.data || err.message} 😥`);
        }
    };

    // ⭐ 페이징 그룹 계산 함수 (JourneyShareTab.js에서 가져옴)
    const getPaginationGroup = (currentPage, totalPages) => {
        const pages = [];
        const maxPageButtons = 10;
        let startPage = Math.floor(currentPage / maxPageButtons) * maxPageButtons;
        let endPage = Math.min(startPage + maxPageButtons, totalPages);

        if (endPage === totalPages && (endPage - startPage) < maxPageButtons) {
            startPage = Math.max(0, totalPages - maxPageButtons);
        }

        for (let i = startPage; i < endPage; i++) {
            pages.push(i);
        }
        return pages;
    };

    return (
        <div className="tab-content-section member-management-tab">
            <h3 className="tab-content-title">회원 관리 🧑‍🤝‍🧑</h3>
            <div className="search-form">
                <select
                    className="search-select"
                    name="searchType" // name 속성 추가
                    value={filter.searchType}
                    onChange={handleFilterChange}
                >
                    <option value="LOGIN_ID">아이디</option>
                    <option value="USERNAME">닉네임</option>
                    <option value="EMAIL">이메일</option>
                </select>
                <input
                    type="text"
                    name="keyword" // name 속성 추가
                    value={filter.keyword}
                    onChange={handleFilterChange}
                    placeholder="검색 키워드 입력"
                />
                <button onClick={fetchMembers} className="search-button">검색</button> {/* 클릭 시 fetchMembers 호출 */}
            </div>

            {loading ? (
                <p className="loading-message">회원 목록을 불러오는 중입니다... ⏳</p>
            ) : error ? (
                <p className="error-message">🚨 {error}</p>
            ) : (
                <table className="admin-member-table">
                    <thead>
                        <tr>
                            <th>회원 번호</th>
                            <th>아이디</th>
                            <th>닉네임</th>
                            <th>이메일</th>
                            <th>가입일</th>
                            <th>상태</th>
                            <th>액션</th>
                        </tr>
                    </thead>
                    <tbody>
                        {members.length === 0 ? (
                            <tr>
                                <td colSpan="7" className="no-content-message">검색 결과가 없습니다. 😢</td>
                            </tr>
                        ) : (
                            members.map(member => (
                                <tr key={member.memberNo}>
                                    <td>{member.memberNo}</td>
                                    <td>{member.loginId}</td>
                                    <td>{member.username}</td>
                                    <td>{member.email}</td>
                                    <td>{new Date(member.createdAt).toLocaleDateString()}</td>
                                    <td>
                                        <span className={
                                            member.accountStatus === 'ACTIVE' ? 'member-status-active' : 'member-status-inactive'
                                        }>
                                            {member.accountStatus === 'ACTIVE' ? '정상' : (member.accountStatus === 'BLOCKED' ? '정지' : '탈퇴')}
                                        </span>
                                    </td>
                                    <td className="member-actions">
                                        {/* 상태 변경 버튼 */}
                                        {member.accountStatus === 'ACTIVE' ? (
                                            <button className="action-button secondary" onClick={() => handleStatusChange(member.memberNo, member.accountStatus)}>정지</button>
                                        ) : member.accountStatus === 'BLOCKED' ? (
                                            <button className="action-button" onClick={() => handleStatusChange(member.memberNo, member.accountStatus)}>활성</button>
                                        ) : (
                                            <button className="action-button secondary" disabled>탈퇴됨</button> // DEACTIVATED 상태
                                        )}
                                        {/* 탈퇴 버튼 */}
                                        {member.accountStatus !== 'DEACTIVATED' && (
                                            <button className="action-button delete-button" onClick={() => handleMemberDelete(member.memberNo, member.username)}>탈퇴</button>
                                        )}
                                    </td>
                                </tr>
                            ))
                        )}
                    </tbody>
                </table>
            )}

            {/* 페이징 버튼 */}
            {!loading && !error && members.length > 0 && (
                <div className="pagination">
                    {totalPages > 10 && filter.page >= 10 && ( // 이전 10개 버튼
                        <button className="page-button" onClick={() => handlePageChange(Math.floor(filter.page / 10) * 10 - 10)}>
                            &lt;&lt;
                        </button>
                    )}
                    {filter.page > 0 && ( // 이전 버튼
                        <button className="page-button" onClick={() => handlePageChange(filter.page - 1)}>
                            &lt;
                        </button>
                    )}
                    {getPaginationGroup(filter.page, totalPages).map(i => (
                        <button
                            key={i}
                            className={`page-button ${filter.page === i ? 'active' : ''}`}
                            onClick={() => handlePageChange(i)}
                        >
                            {i + 1}
                        </button>
                    ))}
                    {filter.page < totalPages - 1 && ( // 다음 버튼
                        <button className="page-button" onClick={() => handlePageChange(filter.page + 1)}>
                            &gt;
                        </button>
                    )}
                    {totalPages > 10 && Math.floor(filter.page / 10) < Math.floor((totalPages - 1) / 10) && ( // 다음 10개 버튼
                        <button className="page-button" onClick={() => handlePageChange(Math.floor(filter.page / 10) * 10 + 10)}>
                            &gt;&gt;
                        </button>
                    )}
                </div>
            )}
        </div>
    );
};

export default MemberManagementTab;