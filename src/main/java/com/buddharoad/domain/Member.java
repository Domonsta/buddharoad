// src/main/java/com/buddharoad/domain/Member.java
package com.buddharoad.domain;

// 필요한 import 문들
import lombok.*;
import com.buddharoad.security.Role;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Table(name = "MEMBERS")
public class Member implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "MEMBER_SEQ")
    @SequenceGenerator(name = "MEMBER_SEQ", sequenceName = "MEMBER_SEQ", allocationSize = 1)
    @Column(name = "MEMBER_NO")
    private Long memberNo;

    @Column(name = "LOGIN_ID", nullable = false, unique = true, length = 50)
    private String loginId;

    @Column(name = "EMAIL", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "PASSWORD", nullable = false, length = 255)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "ROLE", nullable = false, length = 20)
    private Role role;

    @Column(name = "USERNAME", nullable = false, unique = true, length = 50)
    private String username;

    @CreationTimestamp
    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    @Column(name = "ACCOUNT_STATUS", nullable = false, length = 20)
    private String accountStatus = "ACTIVE";

    // --- 연관관계 매핑 필드들 ---
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TempleBookmark> templeBookmarks = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ReviewBookmark> reviewBookmarks = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ReviewComment> reviewComments = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TempleComment> templeComments = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Review> reviews = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TempleLike> templeLikes = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TempleCommentLike> templeCommentLikes = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ReviewLike> reviewLikes = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ReviewCommentLike> reviewCommentLikes = new ArrayList<>();


    // --- UserDetails 인터페이스 구현부 (변동 없음) ---
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(this.role.getKey()));
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.loginId;
    }

    public String getLoginId() {
        return this.loginId;
    }

    public String getDisplayName() {
        return this.username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !this.accountStatus.equals("BLOCKED");
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.accountStatus.equals("ACTIVE");
    }


    // --- 비즈니스 로직 메서드 (변동 없음) ---
    public void updatePassword(String newPassword) {
        this.password = newPassword;
    }

    public void updateEmail(String newEmail) {
        this.email = newEmail;
    }

    public void updateUsername(String newUsername) {
        this.username = newUsername;
    }

    public void updateAccountStatus(String newStatus) {
        this.accountStatus = newStatus;
    }

    public void deactivateAccount() {
        this.accountStatus = "DEACTIVATED";
    }

    public void updateRole(Role newRole) {
        this.role = newRole;
    }

    // --- 연관관계 편의 메서드들 ---
    // ReviewComment 관련 메서드만 수정
    public void addTempleBookmark(TempleBookmark bookmark) {
        this.templeBookmarks.add(bookmark);
        bookmark.setMember(this);
    }
    public void removeTempleBookmark(TempleBookmark bookmark) {
        this.templeBookmarks.remove(bookmark);
        bookmark.setMember(null);
    }
    public void addReviewBookmark(ReviewBookmark bookmark) {
        this.reviewBookmarks.add(bookmark);
        bookmark.setMember(this);
    }
    public void removeReviewBookmark(ReviewBookmark bookmark) {
        this.reviewBookmarks.remove(bookmark);
        bookmark.setMember(null);
    }

    // ⭐⭐ 수정된 부분! ReviewComment 엔티티에 setMember() 메서드가 없으므로 삭제! ⭐⭐
    public void addReviewComment(ReviewComment comment) {
        this.reviewComments.add(comment);
        // comment.setMember(this); // 💡 삭제
    }
    public void removeReviewComment(ReviewComment comment) {
        this.reviewComments.remove(comment);
        // comment.setMember(null); // 💡 삭제
    }
    // ⭐⭐ 수정 끝 ⭐⭐

    public void addTempleComment(TempleComment comment) {
        this.templeComments.add(comment);
        comment.setMember(this);
    }
    public void removeTempleComment(TempleComment comment) {
        this.templeComments.remove(comment);
        comment.setMember(null);
    }
    public void addReview(Review review) {
        this.reviews.add(review);
        review.setMember(this);
    }
    public void removeReview(Review review) {
        this.reviews.remove(review);
        review.setMember(null);
    }
    public void addTempleLike(TempleLike like) {
        this.templeLikes.add(like);
        like.setMember(this);
    }
    public void removeTempleLike(TempleLike like) {
        this.templeLikes.remove(like);
        like.setMember(null);
    }
    public void addTempleCommentLike(TempleCommentLike like) {
        this.templeCommentLikes.add(like);
        like.setMember(this);
    }
    public void removeTempleCommentLike(TempleCommentLike like) {
        this.templeCommentLikes.remove(like);
        like.setMember(null);
    }
    public void addReviewLike(ReviewLike like) {
        this.reviewLikes.add(like);
        like.setMember(this);
    }
    public void removeReviewLike(ReviewLike like) {
        this.reviewLikes.remove(like);
        like.setMember(null);
    }
    public void addReviewCommentLike(ReviewCommentLike like) {
        this.reviewCommentLikes.add(like);
        like.setMember(this);
    }
    public void removeReviewCommentLike(ReviewCommentLike like) {
        this.reviewCommentLikes.remove(like);
        like.setMember(null);
    }

}