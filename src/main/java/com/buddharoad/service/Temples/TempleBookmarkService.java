// src/main/java/com/buddharoad/service/Temples/TempleBookmarkService.java
package com.buddharoad.service.Temples;

import com.buddharoad.domain.Member;
import com.buddharoad.domain.TempleBookmark;
import com.buddharoad.domain.Temple;
import com.buddharoad.dto.Temples.Bookmark.TempleBookmarkResponseDTO;
import com.buddharoad.repository.Member.MemberRepository;
import com.buddharoad.repository.Temple.TempleBookmarkRepository;
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
public class TempleBookmarkService {

    private final TempleBookmarkRepository templeBookmarkRepository;
    private final MemberRepository memberRepository;
    private final TempleRepository templeRepository;

    /**
     * 사찰 찜하기 상태를 토글(추가/삭제)합니다.
     * @param memberNo 현재 로그인한 회원의 ID
     * @param templeId 찜할 사찰의 ID
     * @return 찜하기 상태 (true: 찜함, false: 찜하지 않음)
     */
    public boolean toggleBookmark(Long memberNo, Long templeId) {
        // ✨✨✨ Member와 Temple 엔티티를 조회해서 Optional 변수에 담는 코드를 추가했어! ✨✨✨
        Member member = memberRepository.findById(memberNo)
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다: " + memberNo));
        Temple temple = templeRepository.findById(templeId)
                .orElseThrow(() -> new EntityNotFoundException("사찰을 찾을 수 없습니다: " + templeId));

        Optional<TempleBookmark> existingBookmark = templeBookmarkRepository.findByMemberNoAndTempleId(memberNo, templeId);

        if (existingBookmark.isPresent()) {
            log.info("🗑️ 사찰 찜하기 삭제: 회원 {}번, 사찰 {}번", memberNo, templeId);
            templeBookmarkRepository.deleteByMemberNoAndTempleId(memberNo, templeId);
            return false;
        } else {
            log.info("❤️ 사찰 찜하기 추가: 회원 {}번, 사찰 {}번", memberNo, templeId);
            TempleBookmark bookmark = new TempleBookmark();
            // ✨✨✨ Optional 변수 대신 바로 Member와 Temple 객체를 설정해줘! ✨✨✨
            bookmark.setMember(member);
            bookmark.setTemple(temple);
            bookmark.setCreatedAt(LocalDateTime.now());
            templeBookmarkRepository.save(bookmark);
            return true;
        }
    }

    /**
     * 💡 특정 사찰의 찜하기 상태를 조회합니다.
     * @param memberNo 현재 로그인한 회원의 ID
     * @param templeId 확인할 사찰의 ID
     * @return 찜하기 상태 (true: 찜함, false: 찜하지 않음)
     */
    @Transactional(readOnly = true)
    public boolean isBookmarked(Long memberNo, Long templeId) {
        log.info("🔍 사찰 찜하기 상태 조회: 회원 {}번, 사찰 {}번", memberNo, templeId);
        return templeBookmarkRepository.existsByMemberNoAndTempleId(memberNo, templeId);
    }

    /**
     * 💡 특정 회원이 찜한 모든 사찰 목록을 조회합니다.
     * @param memberNo 현재 로그인한 회원의 ID
     * @return 찜한 사찰 목록 DTO 리스트
     */
    @Transactional(readOnly = true)
    public List<TempleBookmarkResponseDTO> getBookmarksByMemberNo(Long memberNo) {
        log.info("📜 회원 {}번의 찜 목록 조회", memberNo);
        List<TempleBookmark> bookmarks = templeBookmarkRepository.findByMemberNo(memberNo);

        return bookmarks.stream()
                .map(bookmark -> {
                    // 💡 여기서는 찜 여부 정보가 필요 없으므로, isBookmarked 필드를 제외하고 빌드
                    return TempleBookmarkResponseDTO.builder()
                            .templeId(bookmark.getTempleId())
                            .bookmarked(true) // 💡 찜 목록이니까 항상 true로 설정
                            .build();
                })
                .collect(Collectors.toList());
    }
}