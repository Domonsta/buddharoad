package com.buddharoad.security;

//멤버별 권한에 대한 내용
public enum Role {
    USER("ROLE_USER", "일반 회원"),
    CONTENT_ADMIN("ROLE_CONTENT_ADMIN", "콘텐츠 관리자"), // 콘텐츠 관리자 추가
    SYSTEM_ADMIN("ROLE_SYSTEM_ADMIN", "시스템 관리자"), // 시스템 관리자 추가
    GUEST("ROLE_GUEST", "비회원"); // 비회원 역할 추가

    private final String key;
    private final String title;

    Role(String key, String title) {
        this.key = key;
        this.title = title;
    }

    public String getKey() {
        return key;
    }
    public String getTitle() {
        return title;
    }
}