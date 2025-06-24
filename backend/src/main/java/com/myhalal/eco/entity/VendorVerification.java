package com.myhalal.eco.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "VENDOR_VERIFICATIONS")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VendorVerification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "vendor_verification_seq")
    @SequenceGenerator(name = "vendor_verification_seq", sequenceName = "VENDOR_VERIFICATION_SEQ", allocationSize = 1)
    @Column(name = "VERIFICATION_ID")
    private Long verificationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "VENDOR_ID", nullable = false)
    private Vendor vendor;

    @Column(name = "VERIFICATION_TYPE", nullable = false, length = 50)
    @NotBlank(message = "Verification type is required")
    @Size(max = 50, message = "Verification type must not exceed 50 characters")
    private String verificationType;

    @Column(name = "STATUS", length = 20)
    @Enumerated(EnumType.STRING)
    private VerificationStatus status = VerificationStatus.PENDING;

    @Column(name = "INITIATED_BY", length = 100)
    @Size(max = 100, message = "Initiated by must not exceed 100 characters")
    private String initiatedBy;

    @Column(name = "INITIATED_DATE", nullable = false)
    private LocalDateTime initiatedDate;

    @Column(name = "ASSIGNED_TO", length = 100)
    @Size(max = 100, message = "Assigned to must not exceed 100 characters")
    private String assignedTo;

    @Column(name = "ASSIGNED_DATE")
    private LocalDateTime assignedDate;

    @Column(name = "COMPLETED_BY", length = 100)
    @Size(max = 100, message = "Completed by must not exceed 100 characters")
    private String completedBy;

    @Column(name = "COMPLETED_DATE")
    private LocalDateTime completedDate;

    @Column(name = "VERIFICATION_METHOD", length = 100)
    @Size(max = 100, message = "Verification method must not exceed 100 characters")
    private String verificationMethod;

    @Column(name = "EXTERNAL_REFERENCE", length = 255)
    @Size(max = 255, message = "External reference must not exceed 255 characters")
    private String externalReference;

    @Column(name = "VERIFICATION_SCORE")
    @Min(value = 0, message = "Verification score must be non-negative")
    @Max(value = 100, message = "Verification score must not exceed 100")
    private Integer verificationScore;

    @Column(name = "NOTES", length = 2000)
    @Size(max = 2000, message = "Notes must not exceed 2000 characters")
    private String notes;

    @Column(name = "REJECTION_REASON", length = 1000)
    @Size(max = 1000, message = "Rejection reason must not exceed 1000 characters")
    private String rejectionReason;

    @Column(name = "REQUIRED_ACTIONS", length = 1000)
    @Size(max = 1000, message = "Required actions must not exceed 1000 characters")
    private String requiredActions;

    @Column(name = "EXPIRY_DATE")
    private LocalDateTime expiryDate;

    @Column(name = "NEXT_REVIEW_DATE")
    private LocalDateTime nextReviewDate;

    @Column(name = "PRIORITY", length = 10)
    @Enumerated(EnumType.STRING)
    private Priority priority = Priority.MEDIUM;

    @Column(name = "ESTIMATED_COMPLETION_TIME")
    private Integer estimatedCompletionTimeHours;

    @Column(name = "ACTUAL_COMPLETION_TIME")
    private Integer actualCompletionTimeHours;

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

    public enum VerificationStatus {
        PENDING("Verification pending"),
        IN_PROGRESS("Verification in progress"),
        COMPLETED("Verification completed successfully"),
        FAILED("Verification failed"),
        REJECTED("Verification rejected"),
        EXPIRED("Verification expired"),
        CANCELLED("Verification cancelled"),
        ON_HOLD("Verification on hold");

        private final String description;

        VerificationStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public enum Priority {
        LOW("Low priority"),
        MEDIUM("Medium priority"),
        HIGH("High priority"),
        URGENT("Urgent priority");

        private final String description;

        Priority(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // Helper methods
    public boolean isCompleted() {
        return VerificationStatus.COMPLETED.equals(this.status);
    }

    public boolean isFailed() {
        return VerificationStatus.FAILED.equals(this.status) || 
               VerificationStatus.REJECTED.equals(this.status);
    }

    public boolean isInProgress() {
        return VerificationStatus.IN_PROGRESS.equals(this.status);
    }

    public boolean isPending() {
        return VerificationStatus.PENDING.equals(this.status);
    }

    public boolean isExpired() {
        return expiryDate != null && expiryDate.isBefore(LocalDateTime.now());
    }

    public boolean isOverdue() {
        return nextReviewDate != null && nextReviewDate.isBefore(LocalDateTime.now());
    }

    public long getDaysInProgress() {
        if (initiatedDate == null) return 0;
        LocalDateTime endDate = completedDate != null ? completedDate : LocalDateTime.now();
        return java.time.Duration.between(initiatedDate, endDate).toDays();
    }

    public String getStatusBadgeColor() {
        switch (status) {
            case COMPLETED: return "success";
            case FAILED:
            case REJECTED: return "danger";
            case IN_PROGRESS: return "warning";
            case PENDING: return "info";
            case EXPIRED:
            case CANCELLED: return "secondary";
            case ON_HOLD: return "dark";
            default: return "light";
        }
    }

    public String getPriorityBadgeColor() {
        switch (priority) {
            case URGENT: return "danger";
            case HIGH: return "warning";
            case MEDIUM: return "info";
            case LOW: return "secondary";
            default: return "light";
        }
    }
}
