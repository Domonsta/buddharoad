package com.buddharoad.repository.Temple;

import com.buddharoad.domain.TemplePhoto; // TemplePhoto 엔티티의 실제 패키지 경로로 수정해줘!
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository // 이 인터페이스가 Spring의 Repository 빈임을 나타내
public interface TemplePhotoRepository extends JpaRepository<TemplePhoto, Long> {
    // JpaRepository는 기본적인 CRUD (생성, 조회, 수정, 삭제) 메서드를 자동으로 제공해줘.

    // 💡 특정 사찰(Temple)에 속한 모든 사진을 조회하는 쿼리 메서드
    // TemplePhoto 엔티티의 'temple' 필드 안에 있는 'templeId'로 검색
    List<TemplePhoto> findByTemple_TempleId(Long templeId);

    // 💡 (선택 사항) 특정 사찰의 특정 사진 URL을 가진 사진을 찾는 경우
    Optional<TemplePhoto> findByTemple_TempleIdAndPhotoUrl(Long templeId, String photoUrl);

    // 💡 (선택 사항) 특정 사찰의 특정 순서를 가진 사진을 찾는 경우
    Optional<TemplePhoto> findByTemple_TempleIdAndPhotoOrder(Long templeId, Integer photoOrder);

    // 💡 (선택 사항) 특정 TemplePhoto ID와 Temple ID를 통해 사진을 찾는 경우 (보안성 강화)
    Optional<TemplePhoto> findByPhotoIdAndTemple_TempleId(Long photoId, Long templeId);
}