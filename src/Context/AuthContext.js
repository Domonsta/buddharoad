// src/context/AuthContext.js
import React, { createContext, useState, useEffect, useContext, useCallback, useMemo } from 'react';
import axios from 'axios';

export const AuthContext = createContext();

// ⭐⭐ 핵심 수정: 환경 변수에서 백엔드 기본 URL 가져오기
// .env 파일에 REACT_APP_BACKEND_BASE_URL=http://localhost:8080 이렇게 설정되어 있다고 가정
const API_BASE_URL = process.env.REACT_APP_BACKEND_BASE_URL || 'http://localhost:8080';

export const AuthProvider = ({ children }) => {
    const [isLoggedIn, setIsLoggedIn] = useState(false);
    const [user, setUser] = useState(null); // user 객체를 통째로 관리
    const [accessToken, setAccessToken] = useState(null);
    const [loadingAuth, setLoadingAuth] = useState(true); // 기본값을 true로 유지

    const logout = useCallback(() => {
        localStorage.removeItem('accessToken');
        setIsLoggedIn(false);
        setUser(null); // 로그아웃 시 user 객체도 null로 설정
        setAccessToken(null);
        setLoadingAuth(false); // 로그아웃 시에는 바로 로딩 끝
        console.log('AuthContext: User logged out.');
    }, []);

    const fetchUserInfo = useCallback(async (token) => {
        if (!token) {
            console.log('AuthContext: fetchUserInfo - Token is null or undefined, logging out.');
            logout();
            return false;
        }
        try {
            // ⭐⭐ 핵심 수정: API_BASE_URL을 사용하여 절대 경로로 호출! ⭐⭐
            const response = await axios.get(`${API_BASE_URL}/api/auth/me`, {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });
            const userData = response.data;
            console.log('AuthContext: fetchUserInfo - Full response data:', userData);

            setUser({
                username: userData.username,
                loginId: userData.loginId, // loginId도 user 객체에 저장 (useAuth().user.loginId로 접근 가능)
                memberNo: userData.memberNo,
                role: userData.role,
                nickname: userData.username
            });
            setIsLoggedIn(true);
            setAccessToken(token);
            console.log('AuthContext: fetchUserInfo - Fetched Role:', userData.role);
            console.log('AuthContext: fetchUserInfo - Fetched Username:', userData.username);
            return true;
        } catch (error) {
            console.error('AuthContext: fetchUserInfo - Error fetching user info:', error);
            logout(); // 에러 발생 시 로그아웃 처리
            return false;
        } finally {
            setLoadingAuth(false); // 정보 로딩이 끝나면 loadingAuth를 false로 설정
        }
    }, [logout]);

    useEffect(() => {
        const token = localStorage.getItem('accessToken');
        if (token) {
            console.log('AuthContext: Initial useEffect - Found token, fetching user info...');
            fetchUserInfo(token);
        } else {
            setIsLoggedIn(false);
            setLoadingAuth(false);
            console.log('AuthContext: Initial useEffect - No token, auth loading finished.');
        }
    }, [fetchUserInfo]);

    const hasAdminPermission = useCallback(() => {
        // user 객체가 존재하고 role이 ADMIN인 경우 확인
        // 백엔드의 Role Enum 값에 'ROLE_' 접두사가 붙어있다면 여기에 맞춰야 해.
        const isAdmin = user && (user.role === 'CONTENT_ADMIN' || user.role === 'SYSTEM_ADMIN');
        console.log('AuthContext: hasAdminPermission called - Current user:', user);
        console.log('AuthContext: hasAdminPermission result:', isAdmin);
        return isAdmin;
    }, [user]);

    const contextValue = useMemo(() => ({
        isLoggedIn,
        user, // user 객체 전체를 전달하도록 변경
        accessToken,
        loading: loadingAuth, // loadingAuth를 loading으로 이름 변경하여 전달
        login: async (loginId, password) => { // username 대신 loginId로 파라미터명 변경 (백엔드와 일치)
            setLoadingAuth(true); // 로그인 시작 시 로딩 시작
            try {
                // ⭐⭐ 핵심 수정: API_BASE_URL을 사용하여 절대 경로로 호출! ⭐⭐
                const response = await axios.post(`${API_BASE_URL}/api/auth/login`, {
                    loginId: loginId, // 백엔드 필드명에 맞게
                    password: password
                });
                const { accessToken } = response.data;
                localStorage.setItem('accessToken', accessToken);
                await fetchUserInfo(accessToken); // 로그인 성공 후 사용자 정보 가져오기
                return true;
            } catch (error) {
                console.error('AuthContext: Login failed:', error);
                setLoadingAuth(false); // 로그인 실패 시 로딩 끝
                if (error.response && error.response.data && error.response.data.message) {
                    throw new Error(error.response.data.message);
                } else {
                    throw new Error('로그인 실패: 서버 응답 없음 또는 네트워크 오류');
                }
            }
        },
        logout,
        hasAdminPermission
    }), [isLoggedIn, user, accessToken, loadingAuth, fetchUserInfo, logout, hasAdminPermission]);

    return (
        <AuthContext.Provider value={contextValue}>
            {children}
        </AuthContext.Provider>
    );
};

// useAuth 훅도 AuthContext에서 가져와서 사용
export const useAuth = () => useContext(AuthContext);