package com.myhalal.eco.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "VENDOR_NOTIFICATIONS")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VendorNotification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "vendor_notif_seq")
    @SequenceGenerator(name = "vendor_notif_seq", sequenceName = "VENDOR_NOTIF_SEQ", allocationSize = 1)
    @Column(name = "NOTIFICATION_ID")
    private Long notificationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "VENDOR_ID", nullable = false)
    private Vendor vendor;

    @Column(name = "TYPE", nullable = false, length = 50)
    @NotBlank(message = "Notification type is required")
    @Size(max = 50, message = "Notification type must not exceed 50 characters")
    private String type;

    @Column(name = "TITLE", nullable = false, length = 200)
    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    @Column(name = "MESSAGE", nullable = false, length = 1000)
    @NotBlank(message = "Message is required")
    @Size(max = 1000, message = "Message must not exceed 1000 characters")
    private String message;

    @Column(name = "PRIORITY", length = 20)
    @Enumerated(EnumType.STRING)
    private Priority priority = Priority.NORMAL;

    @Column(name = "STATUS", length = 20)
    @Enumerated(EnumType.STRING)
    private NotificationStatus status = NotificationStatus.UNREAD;

    @Column(name = "READ_DATE")
    private LocalDateTime readDate;

    @Column(name = "ACTION_REQUIRED")
    private Boolean actionRequired = false;

    @Column(name = "ACTION_URL", length = 500)
    @Size(max = 500, message = "Action URL must not exceed 500 characters")
    private String actionUrl;

    @Column(name = "ACTION_DEADLINE")
    private LocalDateTime actionDeadline;

    @Column(name = "ACTION_COMPLETED")
    private Boolean actionCompleted = false;

    @Column(name = "ACTION_COMPLETED_DATE")
    private LocalDateTime actionCompletedDate;

    @Column(name = "RELATED_ENTITY_TYPE", length = 50)
    @Size(max = 50, message = "Related entity type must not exceed 50 characters")
    private String relatedEntityType;

    @Column(name = "RELATED_ENTITY_ID")
    private Long relatedEntityId;

    @CreationTimestamp
    @Column(name = "CREATED_DATE", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "CREATED_BY", length = 100)
    private String createdBy;

    public enum Priority {
        LOW("Low priority"),
        NORMAL("Normal priority"),
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

    public enum NotificationStatus {
        UNREAD("Notification not read"),
        READ("Notification read"),
        ARCHIVED("Notification archived"),
        DELETED("Notification deleted");

        private final String description;

        NotificationStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // Helper methods
    public boolean isRead() {
        return NotificationStatus.READ.equals(this.status) || 
               NotificationStatus.ARCHIVED.equals(this.status);
    }

    public boolean isActionOverdue() {
        return actionRequired && !Boolean.TRUE.equals(actionCompleted) && 
               actionDeadline != null && actionDeadline.isBefore(LocalDateTime.now());
    }

    public boolean requiresImmediate() {
        return Priority.URGENT.equals(this.priority) && 
               !Boolean.TRUE.equals(actionCompleted);
    }

    public String getPriorityBadgeColor() {
        switch (priority) {
            case URGENT: return "danger";
            case HIGH: return "warning";
            case NORMAL: return "info";
            case LOW: return "secondary";
            default: return "light";
        }
    }

    public String getStatusBadgeColor() {
        switch (status) {
            case UNREAD: return "primary";
            case READ: return "success";
            case ARCHIVED: return "secondary";
            case DELETED: return "danger";
            default: return "light";
        }
    }
}
