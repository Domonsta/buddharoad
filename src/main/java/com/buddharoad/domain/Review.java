// src/main/java/com/buddharoad/domain/Review.java
package com.buddharoad.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter; // ⭐ @Setter 어노테이션 유지
import org.hibernate.annotations.CreationTimestamp; // @CreatedDate 대신 사용
import org.hibernate.annotations.UpdateTimestamp; // @LastModifiedDate 대신 사용

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter // 모든 필드에 대한 Setter가 필요하므로 @Setter 추가
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PUBLIC) // Builder가 모든 필드를 사용할 수 있도록 PUBLIC으로 변경
@Builder
@Table(name = "REVIEWS")
@SequenceGenerator(
        name = "REVIEW_ID_SEQ_GENERATOR", // 제너레이터 이름
        sequenceName = "REVIEW_ID_SEQ",   // 데이터베이스에 생성된 시퀀스 이름
        initialValue = 1,              // 초기 값
        allocationSize = 1             // 증가량
)
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "REVIEW_ID_SEQ_GENERATOR")
    @Column(name = "REVIEW_ID")
    private Long reviewId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MEMBER_NO", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TEMPLE_ID", nullable = false)
    private Temple temple;

    // ⭐ Temple 엔티티에서 templeId를 직접 저장하는 방식이 아니라, 연관관계 필드를 통해 접근하도록 수정해야 함
    // private Long templeId; // 🚨🚨 이 필드는 제거해야 함 (temple 객체로 접근)

    @Column(name = "TITLE", nullable = false, length = 255)
    private String title;

    @Column(name = "CONTENT", nullable = false, columnDefinition = "CLOB")
    private String content;

    @Column(name = "RATING", nullable = false)
    private Integer rating; // 별점 (1~5점 등)

    // ⭐ 태그 필드 이름이 reviewTags에서 tags로 바뀐듯? DTO에 맞춰 tags로 일단 변경
    @Column(name = "TAGS", length = 255)
    private String tags; // 콤마로 구분된 태그 문자열

    @Column(name = "VISITED_AT", nullable = false)
    private LocalDate visitedAt; // 방문일

    @Column(name = "IS_ACTIVE", nullable = false)
    private Boolean isActive; // 활성화 여부 (관리자 제어)

    @Column(name = "IS_DELETED", nullable = false)
    private Boolean isDeleted; // 삭제 여부 (소프트 삭제)

    // 💡 여기부터 수정! viewCount 필드 타입을 Long으로, 기본값 0L로 설정
    @Column(name = "VIEW_COUNT", nullable = false)
    @Builder.Default // 빌더 사용 시 기본값 설정
    private Long viewCount = 0L; // 📢 조회수 필드 추가! (Long 타입)

    // 💡 좋아요 개수 필드 추가!
    @Column(name = "LIKE_COUNT", nullable = false)
    @Builder.Default
    private Long likeCount = 0L; // 📢 좋아요 개수 필드 추가!


    @CreationTimestamp // @CreatedDate 대신 사용
    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp // @LastModifiedDate 대신 사용
    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    @Column(name = "MEMBER_USERNAME") // 멤버 닉네임 추가
    private String memberUsername;


    // 리뷰 사진 목록 (ReviewPhoto 엔티티와 1:N 관계)
    // Review 엔티티 삭제 시 연관된 ReviewPhoto도 함께 삭제되도록 CASCADE.ALL, orphanRemoval = true 설정
    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ReviewPhoto> photos = new ArrayList<>(); // 사진 목록

    // --- 편의 메서드 ---
    public void setMember(Member member) {
        this.member = member;
    }

    public void setTemple(Temple temple) {
        this.temple = temple;
    }

    // ReviewPhoto 추가 편의 메서드 (양방향 관계 설정)
    public void addPhoto(ReviewPhoto photo) {
        this.photos.add(photo);
        photo.setReview(this);
    }

    // ReviewPhoto 제거 편의 메서드 (양방향 관계 해제)
    public void removePhoto(ReviewPhoto photo) {
        this.photos.remove(photo);
        photo.setReview(null);
    }

    // 💡 조회수 증가 메서드 수정! (Long 타입에 맞춰)
    public void incrementViewCount() {
        this.viewCount = (this.viewCount == null) ? 1L : this.viewCount + 1;
    }

    // 소프트 삭제 메서드
    public void softDelete() {
        this.isDeleted = true;
        this.isActive = false; // 삭제되면 비활성화
    }

    // 업데이트 메서드 (Member, Temple 필드 제외)
    // 이 메서드는 DTO로부터 받은 데이터로 엔티티를 업데이트하는 데 사용
    public void update(String title, String content, Integer rating, String tags, LocalDate visitedAt) {
        this.title = title;
        this.content = content;
        this.rating = rating;
        this.tags = tags;
        this.visitedAt = visitedAt;
    }
    // --- 편의 메서드 ---
    public void incrementLikeCount() {
        this.likeCount = (this.likeCount == null) ? 1L : this.likeCount + 1;
    }

    public void decrementLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }
}