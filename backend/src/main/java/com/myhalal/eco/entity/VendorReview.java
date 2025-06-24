package com.myhalal.eco.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "VENDOR_REVIEWS")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VendorReview {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "vendor_review_seq")
    @SequenceGenerator(name = "vendor_review_seq", sequenceName = "VENDOR_REVIEW_SEQ", allocationSize = 1)
    @Column(name = "REVIEW_ID")
    private Long reviewId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "VENDOR_ID", nullable = false)
    private Vendor vendor;

    @Column(name = "CUSTOMER_ID")
    private Long customerId;

    @Column(name = "CUSTOMER_NAME", length = 100)
    @Size(max = 100, message = "Customer name must not exceed 100 characters")
    private String customerName;

    @Column(name = "CUSTOMER_EMAIL", length = 100)
    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String customerEmail;

    @Column(name = "RATING", precision = 2, scale = 1, nullable = false)
    @NotNull(message = "Rating is required")
    @DecimalMin(value = "1.0", message = "Rating must be at least 1.0")
    @DecimalMax(value = "5.0", message = "Rating must not exceed 5.0")
    private BigDecimal rating;

    @Column(name = "TITLE", length = 200)
    @Size(max = 200, message = "Review title must not exceed 200 characters")
    private String title;

    @Column(name = "COMMENT", length = 2000)
    @Size(max = 2000, message = "Comment must not exceed 2000 characters")
    private String comment;

    @Column(name = "ORDER_ID")
    private Long orderId;

    @Column(name = "PRODUCT_ID")
    private Long productId;

    @Column(name = "STATUS", length = 20)
    @Enumerated(EnumType.STRING)
    private ReviewStatus status = ReviewStatus.PENDING;

    @Column(name = "IS_VERIFIED_PURCHASE")
    private Boolean isVerifiedPurchase = false;

    @Column(name = "HELPFUL_COUNT")
    @Min(value = 0, message = "Helpful count must be non-negative")
    private Integer helpfulCount = 0;

    @Column(name = "NOT_HELPFUL_COUNT")
    @Min(value = 0, message = "Not helpful count must be non-negative")
    private Integer notHelpfulCount = 0;

    @Column(name = "VENDOR_RESPONSE", length = 1000)
    @Size(max = 1000, message = "Vendor response must not exceed 1000 characters")
    private String vendorResponse;

    @Column(name = "VENDOR_RESPONSE_DATE")
    private LocalDateTime vendorResponseDate;

    @Column(name = "MODERATED_BY", length = 100)
    private String moderatedBy;

    @Column(name = "MODERATED_DATE")
    private LocalDateTime moderatedDate;

    @Column(name = "MODERATION_NOTES", length = 500)
    @Size(max = 500, message = "Moderation notes must not exceed 500 characters")
    private String moderationNotes;

    @CreationTimestamp
    @Column(name = "CREATED_DATE", nullable = false)
    private LocalDateTime createdDate;

    @UpdateTimestamp
    @Column(name = "UPDATED_DATE")
    private LocalDateTime updatedDate;

    @Column(name = "CREATED_BY", length = 100)
    private String createdBy;

    @Column(name = "UPDATED_BY", length = 100)
    private String updatedBy;

    public enum ReviewStatus {
        PENDING("Review submitted, awaiting moderation"),
        APPROVED("Review approved and visible"),
        REJECTED("Review rejected"),
        FLAGGED("Review flagged for review"),
        HIDDEN("Review hidden by moderator");

        private final String description;

        ReviewStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // Helper methods
    public boolean isPositive() {
        return rating != null && rating.compareTo(new BigDecimal("3.0")) > 0;
    }

    public boolean isNegative() {
        return rating != null && rating.compareTo(new BigDecimal("3.0")) < 0;
    }

    public boolean isNeutral() {
        return rating != null && rating.compareTo(new BigDecimal("3.0")) == 0;
    }

    public boolean hasVendorResponse() {
        return vendorResponse != null && !vendorResponse.trim().isEmpty();
    }

    public int getTotalVotes() {
        return (helpfulCount != null ? helpfulCount : 0) + (notHelpfulCount != null ? notHelpfulCount : 0);
    }

    public double getHelpfulPercentage() {
        int total = getTotalVotes();
        if (total == 0) return 0.0;
        return (helpfulCount != null ? helpfulCount : 0) * 100.0 / total;
    }

    public String getRatingStars() {
        if (rating == null) return "";
        
        int fullStars = rating.intValue();
        boolean hasHalfStar = rating.remainder(BigDecimal.ONE).compareTo(new BigDecimal("0.5")) >= 0;
        
        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < fullStars; i++) {
            stars.append("★");
        }
        if (hasHalfStar && fullStars < 5) {
            stars.append("☆");
        }
        while (stars.length() < 5) {
            stars.append("☆");
        }
        return stars.toString();
    }
}
