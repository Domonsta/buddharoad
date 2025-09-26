package com.buddharoad.utill;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordEncoderGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        // ⭐ 관리자 계정 비밀번호 (원하는 비밀번호로 변경해!) ⭐
        String systemRawPassword = "system0704!";
        String contentRawPassword = "content0704!";
        // ⭐ 일반 회원 계정 비밀번호 (원하는 비밀번호로 변경해!) ⭐
        String userRawPassword = "user0704!";

        System.out.println("--- 생성된 BCrypt 비밀번호 해시 ---");
        System.out.println("시스템 관리자 비밀번호 '" + systemRawPassword + "'의 해시: " + encoder.encode(systemRawPassword));
        System.out.println("콘텐츠 관리자 비밀번호 '" + contentRawPassword + "'의 해시: " + encoder.encode(contentRawPassword));
        System.out.println("일반 회원 비밀번호 '" + userRawPassword + "'의 해시: " + encoder.encode(userRawPassword));
        System.out.println("---------------------------------");
    }
}