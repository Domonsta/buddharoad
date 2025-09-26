package com.buddharoad.service.Temples;

import com.buddharoad.domain.Member;
import com.buddharoad.domain.Temple;
import com.buddharoad.domain.TempleLike;
import com.buddharoad.dto.Temples.Likes.TempleLikeResponseDTO;
import com.buddharoad.repository.Member.MemberRepository;
import com.buddharoad.repository.Temple.TempleLikeRepository;
import com.buddharoad.repository.Temple.TempleRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
@Transactional
public class TempleLikeService {

    private final TempleLikeRepository templeLikeRepository;
    private final MemberRepository memberRepository;
    private final TempleRepository templeRepository;

    public boolean toggleLike(Long memberNo, Long templeId) {
        Member member = memberRepository.findById(memberNo)
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다: " + memberNo));
        Temple temple = templeRepository.findById(templeId)
                .orElseThrow(() -> new EntityNotFoundException("사찰을 찾을 수 없습니다: " + templeId));

        Optional<TempleLike> existingLike = templeLikeRepository.findByMemberNoAndTempleId(memberNo, templeId);

        if (existingLike.isPresent()) {
            log.info("🗑️ 사찰 좋아요 삭제: 회원 {}번, 사찰 {}번", memberNo, templeId);
            templeLikeRepository.deleteByMemberNoAndTempleId(memberNo, templeId);
            return false;
        } else {
            log.info("❤️ 사찰 좋아요 추가: 회원 {}번, 사찰 {}번", memberNo, templeId);

            // ✨✨✨ 빌더 패턴을 사용해서 연관 객체(member, temple)를 함께 설정 ✨✨✨
            TempleLike like = TempleLike.builder()
                    .memberNo(member.getMemberNo())
                    .templeId(temple.getTempleId())
                    .member(member) // 💡 연관 객체인 Member 엔티티를 직접 설정
                    .temple(temple) // 💡 연관 객체인 Temple 엔티티를 직접 설정
                    .createdAt(LocalDateTime.now())
                    .build();
            templeLikeRepository.save(like);

            return true;
        }
    }

    @Transactional(readOnly = true)
    public boolean isLiked(Long memberNo, Long templeId) {
        log.info("🔍 사찰 좋아요 상태 조회: 회원 {}번, 사찰 {}번", memberNo, templeId);
        return templeLikeRepository.existsByMemberNoAndTempleId(memberNo, templeId);
    }

    @Transactional(readOnly = true)
    public List<TempleLikeResponseDTO> getLikesByMemberNo(Long memberNo) {
        log.info("📜 회원 {}번의 좋아요 목록 조회", memberNo);
        List<TempleLike> likes = templeLikeRepository.findByMemberNo(memberNo);

        return likes.stream()
                .map(like -> {
                    return TempleLikeResponseDTO.builder()
                            .memberNo(like.getMemberNo())
                            .templeId(like.getTempleId())
                            .templeName(like.getTemple().getTempleName())
                            .createdAt(like.getCreatedAt())
                            .isLiked(true)
                            .build();
                })
                .collect(Collectors.toList());
    }
}