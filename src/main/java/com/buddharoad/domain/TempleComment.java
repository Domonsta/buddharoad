// src/main/java/com/buddharoad/domain/TempleComment.java
package com.buddharoad.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter; // Setter 추가 (필요한 필드에만 제한적으로 사용 권장)
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Table(name = "TEMPLE_COMMENTS") // 사찰 댓글 테이블 이름
public class TempleComment {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "TEMPLE_COMMENT_SEQ")
    @SequenceGenerator(name = "TEMPLE_COMMENT_SEQ", sequenceName = "TEMPLE_COMMENT_SEQ", allocationSize = 1)
    @Column(name = "TEMPLE_COMMENT_ID")
    private Long templeCommentId; // 1. 사찰 댓글 ID (PK)

    // 💡 2. TempleComment는 하나의 Member에 의해 작성된다 (다대일 관계)
    // MEMBER_NO는 Member 엔티티의 PK를 참조하는 외래키
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MEMBER_NO", nullable = false)
    @Setter(AccessLevel.PROTECTED) // 양방향 관계 설정을 위해 setter 추가
    private Member member;

    // 💡 3. 댓글 작성자의 닉네임(username)을 저장하는 필드 추가
    // 이 필드는 Member 테이블의 외래키가 아닌, 단순 String 컬럼으로 저장됩니다.
    @Column(name = "MEMBER_USERNAME", nullable = false, length = 50) // 컬럼 이름을 MEMBER_USERNAME으로 변경
    private String memberUsername; // 필드 이름을 memberUsername으로 변경

    // 💡 4. TempleComment는 하나의 Temple에 속한다 (다대일 관계)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TEMPLE_ID", nullable = false)
    @Setter(AccessLevel.PROTECTED) // 양방향 관계 설정을 위해 setter 추가
    private Temple temple;

    @Column(name = "CONTENT", columnDefinition = "CLOB", nullable = false) // 5. 댓글 내용
    private String content;

    @CreationTimestamp // 6. 글 등록일 (자동 생성)
    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp // 7. 글 수정일 (자동 업데이트)
    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    @Column(name = "VIEW_COUNT", nullable = false) // 8. 조회수 (요청대로 추가)
    @Builder.Default
    private Integer viewCount = 0; // 기본값 0으로 설정

    @Column(name = "IS_ACTIVE", nullable = false) // 9. 활성 여부
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "IS_DELETED", nullable = false) // 10. 삭제 여부 (소프트 삭제)
    @Builder.Default
    private Boolean isDeleted = false;


    // 💡💡💡 사찰 댓글 좋아요 목록 필드 💡💡💡
    // mappedBy = "templeComment": TempleCommentLike 엔티티의 'templeComment' 필드에 의해 매핑됨을 명시
    // cascade = CascadeType.ALL: TempleComment 엔티티의 변경이 TempleCommentLike에도 전파됨
    // orphanRemoval = true: 부모(TempleComment)와의 관계가 끊어진 자식(TempleCommentLike) 엔티티를 자동으로 삭제
    @OneToMany(mappedBy = "templeComment", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default // 빌더 사용 시 기본값으로 빈 리스트 생성
    private List<TempleCommentLike> templeCommentLikes = new ArrayList<>(); // 이 댓글에 대한 좋아요 목록


    // --- 비즈니스 로직 메서드 (선택 사항) ---

    // 댓글 내용 업데이트 메서드
    public void updateContent(String newContent) {
        this.content = newContent;
    }

    // 조회수 증가 메서드
    public void incrementViewCount() {
        this.viewCount++;
    }

    // 활성 상태 변경 메서드
    public void changeActiveStatus(Boolean active) {
        this.isActive = active;
    }

    // 삭제 상태 변경 메서드 (소프트 삭제)
    public void markAsDeleted() {
        this.isDeleted = true;
        this.isActive = false; // 삭제된 항목은 비활성화
    }

    // 💡💡💡 사찰 댓글 좋아요 편의 메서드 💡💡💡
    // 편의 메서드: TempleCommentLike를 추가할 때 양방향 관계를 설정
    public void addTempleCommentLike(TempleCommentLike like) {
        this.templeCommentLikes.add(like);
        // TempleCommentLike 엔티티에 setTempleComment() 메서드가 필요함
        // (TempleCommentLike 엔티티가 없으므로 주석 처리)
        // like.setTempleComment(this);
    }

    // 편의 메서드: TempleCommentLike를 제거할 때 양방향 관계를 해제
    public void removeTempleCommentLike(TempleCommentLike like) {
        this.templeCommentLikes.remove(like);
        // TempleCommentLike 엔티티에 setTempleComment() 메서드가 필요함
        // (TempleCommentLike 엔티티가 없으므로 주석 처리)
        // like.setTempleComment(null);
    }
}
