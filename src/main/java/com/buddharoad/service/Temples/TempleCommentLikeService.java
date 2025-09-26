package com.buddharoad.service.Temples;

import com.buddharoad.domain.Member;
import com.buddharoad.domain.TempleComment; // 💡 TempleComment 엔티티 임포트
import com.buddharoad.domain.TempleCommentLike; // 💡 TempleCommentLike 엔티티 임포트
import com.buddharoad.dto.Temples.Likes.TempleCommentLikeResponseDTO; // 💡 DTO 임포트
import com.buddharoad.repository.Member.MemberRepository;
import com.buddharoad.repository.Temple.TempleCommentLikeRepository; // 💡 리포지토리 임포트
import com.buddharoad.repository.Temple.TempleCommentRepository; // 💡 리포지토리 임포트
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
public class TempleCommentLikeService {

    private final TempleCommentLikeRepository templeCommentLikeRepository;
    private final MemberRepository memberRepository;
    private final TempleCommentRepository templeCommentRepository;

    /**
     * 댓글 '좋아요' 상태를 토글(추가/삭제)합니다.
     * @param memberNo 현재 로그인한 회원의 ID
     * @param templeCommentId '좋아요'할 댓글의 ID
     * @return '좋아요' 상태 (true: 좋아요 함, false: 좋아요 하지 않음)
     */
    public boolean toggleLike(Long memberNo, Long templeCommentId) {
        Member member = memberRepository.findById(memberNo)
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다: " + memberNo));
        TempleComment templeComment = templeCommentRepository.findById(templeCommentId)
                .orElseThrow(() -> new EntityNotFoundException("댓글을 찾을 수 없습니다: " + templeCommentId));

        Optional<TempleCommentLike> existingLike = templeCommentLikeRepository.findByMemberNoAndTempleCommentId(memberNo, templeCommentId);

        if (existingLike.isPresent()) {
            log.info("🗑️ 댓글 좋아요 삭제: 회원 {}번, 댓글 {}번", memberNo, templeCommentId);
            templeCommentLikeRepository.deleteByMemberNoAndTempleCommentId(memberNo, templeCommentId);
            return false;
        } else {
            log.info("❤️ 댓글 좋아요 추가: 회원 {}번, 댓글 {}번", memberNo, templeCommentId);

            // ✨✨✨ 빌더 패턴을 사용해서 연관 객체(member, templeComment)를 함께 설정 ✨✨✨
            TempleCommentLike like = TempleCommentLike.builder()
                    .memberNo(member.getMemberNo())
                    .templeCommentId(templeComment.getTempleCommentId())
                    .member(member) // 💡 연관 객체인 Member 엔티티를 직접 설정
                    .templeComment(templeComment) // 💡 연관 객체인 TempleComment 엔티티를 직접 설정
                    .createdAt(LocalDateTime.now())
                    .build();
            templeCommentLikeRepository.save(like);

            return true;
        }
    }

    /**
     * 💡 특정 댓글의 '좋아요' 상태를 조회합니다.
     * @param memberNo 현재 로그인한 회원의 ID
     * @param templeCommentId 확인할 댓글의 ID
     * @return '좋아요' 상태 (true: 좋아요 함, false: 좋아요 하지 않음)
     */
    @Transactional(readOnly = true)
    public boolean isLiked(Long memberNo, Long templeCommentId) {
        log.info("🔍 댓글 좋아요 상태 조회: 회원 {}번, 댓글 {}번", memberNo, templeCommentId);
        return templeCommentLikeRepository.existsByMemberNoAndTempleCommentId(memberNo, templeCommentId);
    }

    /**
     * 💡 특정 회원이 '좋아요'한 모든 댓글 목록을 조회합니다.
     * @param memberNo 현재 로그인한 회원의 ID
     * @return '좋아요'한 댓글 목록 DTO 리스트
     */
    @Transactional(readOnly = true)
    public List<TempleCommentLikeResponseDTO> getLikesByMemberNo(Long memberNo) {
        log.info("📜 회원 {}번의 좋아요 목록 조회", memberNo);
        List<TempleCommentLike> likes = templeCommentLikeRepository.findByMemberNo(memberNo);

        return likes.stream()
                .map(like -> {
                    return TempleCommentLikeResponseDTO.builder()
                            .memberNo(like.getMemberNo())
                            .templeCommentId(like.getTempleCommentId())
                            .commentContent(like.getTempleComment().getContent())
                            .createdAt(like.getCreatedAt())
                            .isLiked(true)
                            .build();
                })
                .collect(Collectors.toList());
    }
}