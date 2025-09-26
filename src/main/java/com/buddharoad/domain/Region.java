package com.buddharoad.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Region { //문자열 형태로 저장되도록
    SEOUL("서울"),
    BUSAN("부산"),
    DAEGU("대구"),
    INCHEON("인천"),
    GWANGJU("광주"),
    DAEJEON("대전"),
    ULSAN("울산"),
    SEJONG("세종"),
    GYEONGGI("경기"),
    GANGWON("강원"),
    CHUNGCHEONGBUK("충북"),
    CHUNGCHEONGNAM("충남"),
    JEOLLABUK("전북"),
    JEOLLANAM("전남"),
    GYEONGSANGBUK("경북"),
    GYEONGSANGNAM("경남"),
    JEJU("제주"),
    ETC("기타"); // 혹시 분류에 없는 지역을 위해 '기타' 추가

    private final String displayName; // 화면에 보여줄 이름

    // displayName으로 Enum을 찾는 유틸리티 메서드 (선택 사항)
    public static Region fromDisplayName(String displayName) {
        for (Region region : Region.values()) {
            if (region.displayName.equalsIgnoreCase(displayName)) {
                return region;
            }
        }
        // 찾지 못했을 경우 예외 처리 또는 ETC 반환 등 정책 결정
        throw new IllegalArgumentException("Unknown region display name: " + displayName);
    }

    // 🚨🚨🚨 새로운 메서드 추가: 입력된 한글 문자열이 displayName에 포함되는지 확인 🚨🚨🚨
    public boolean containsKoreanName(String input) {
        if (input == null || input.trim().isEmpty()) {
            return false; // 빈 문자열은 매칭하지 않음
        }
        // 대소문자 구분 없이 포함 여부 확인
        return this.displayName.toLowerCase().contains(input.trim().toLowerCase());
    }
}
