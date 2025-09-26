//package com.buddaroad01.utill;
//
//import java.util.UUID;
//
//public class KeyGenerator {
//    public static void main(String[] args) {
//        // UUID 2개를 합쳐서 64자 이상의 긴 문자열을 만듦 (HS256 권장 길이에 맞춤)
//        String secretKey = UUID.randomUUID().toString().replace("-", "") +
//                UUID.randomUUID().toString().replace("-", "");
//        System.out.println("⭐ 생성된 JWT Secret Key: " + secretKey + " ⭐");
//    }
//}
