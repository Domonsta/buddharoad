package com.buddharoad.service.Member;

import com.buddharoad.domain.Member;
import com.buddharoad.repository.Member.MemberRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor // final 필드에 대한 생성자를 자동으로 생성해줘서 의존성 주입 (DI)을 쉽게 해줌
public class MemberDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    // Spring Security의 loadUserByUsername 메서드의 'username' 파라미터는
    // 우리가 로그인 시 사용할 'loginId'를 의미해.
    // 실제로는 'loginId'로 간주하면 됨
    public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {
        // DB에서 loginId를 기반으로 Member 엔티티 조회
        // memberRepository의 findByLoginId 메서드를 사용하도록 변경
        Member member = memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + loginId));
        // 에러 메시지도 어떤 loginId를 찾지 못했는지 명확하게 해주는 게 좋아.

        // ⭐⭐⭐ 핵심 변경 사항: Member 엔티티가 이미 UserDetails를 구현했으므로,
        // 조회된 Member 객체를 UserDetails 타입으로 바로 반환할 수 있어. ⭐⭐⭐
        return member;
    }
}