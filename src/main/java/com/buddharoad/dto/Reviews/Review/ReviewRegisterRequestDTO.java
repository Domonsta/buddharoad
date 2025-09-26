// src/main/java/com/buddharoad/dto/Reviews/Review/ReviewRegisterRequestDTO.java
package com.buddharoad.dto.Reviews.Review;

import com.buddharoad.domain.Member; // Member 임포트 추가
import com.buddharoad.domain.Review;
import com.buddharoad.domain.Temple; // Temple 엔티티 임포트 추가
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime; // 이 임포트는 필요 없으면 삭제해도 됨 (visitedAt이 LocalDate이므로)
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewRegisterRequestDTO {

    @NotNull(message = "사찰 ID는 필수 입력 항목입니다.")
    private Long templeId; // 이 필드는 유지

    // 🚨🚨🚨 memberNo, memberUsername 필드는 DTO에서 제거되었음.
    // 이 정보들은 서비스 레이어에서 인증된 사용자로부터 가져올 것임.

    @NotBlank(message = "제목은 필수 입력 항목입니다.")
    private String title;

    @NotBlank(message = "내용은 필수 입력 항목입니다.")
    private String content;

    @NotNull(message = "별점은 필수 입력 항목입니다.")
    @Min(value = 1, message = "별점은 최소 1점 이상이어야 합니다.")
    @Max(value = 5, message = "별점은 최대 5점 이하여야 합니다.")
    private Integer rating;

    private List<String> reviewTags; // 태그는 배열로 관리

    @NotNull(message = "방문일은 필수 입력 항목입니다.")
    private LocalDate visitedAt; // 방문일 (날짜만)

    private List<String> fileDescriptions; // 파일 설명을 위한 필드 추가

    /**
     * Review 엔티티로 변환하는 메서드.
     * 이 메서드는 Member, Temple 객체를 직접 인자로 받아서 Review 엔티티를 생성합니다.
     * @param member Review 작성 Member 엔티티
     * @param temple Review 대상 Temple 엔티티
     * @return Review 엔티티
     */
    // ✨✨✨ toEntity() 메서드를 이렇게 바꿔줘! ✨✨✨
    public Review toEntity(Member member, Temple temple) { // Member, Temple 인자 추가!
        return Review.builder()
                .member(member) // Member 객체 설정
                .temple(temple) // Temple 객체 설정
                .memberUsername(member.getDisplayName()) // Member 객체에서 사용자명 가져오기
                .title(this.title)
                .content(this.content)
                .rating(this.rating)
                // reviewTags, visitedAt은 DTO에 있지만, Review 엔티티의 @Builder.Default나
                // 서비스 레이어에서 추가 로직을 통해 처리되므로 여기서는 직접 빌드하지 않습니다.
                // .reviewTags(this.reviewTags != null ? String.join(",", this.reviewTags) : null)
                // .visitedAt(this.visitedAt)
                .build();
    }
}