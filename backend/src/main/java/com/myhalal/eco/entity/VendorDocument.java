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
@Table(name = "VENDOR_DOCUMENTS")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VendorDocument {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "vendor_doc_seq")
    @SequenceGenerator(name = "vendor_doc_seq", sequenceName = "VENDOR_DOC_SEQ", allocationSize = 1)
    @Column(name = "DOCUMENT_ID")
    private Long documentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "VENDOR_ID", nullable = false)
    private Vendor vendor;

    @Column(name = "DOCUMENT_TYPE", nullable = false, length = 50)
    @NotBlank(message = "Document type is required")
    @Size(max = 50, message = "Document type must not exceed 50 characters")
    private String documentType;

    @Column(name = "DOCUMENT_NAME", nullable = false, length = 255)
    @NotBlank(message = "Document name is required")
    @Size(max = 255, message = "Document name must not exceed 255 characters")
    private String documentName;

    @Column(name = "FILE_PATH", length = 500)
    @Size(max = 500, message = "File path must not exceed 500 characters")
    private String filePath;

    @Column(name = "FILE_SIZE")
    @Min(value = 0, message = "File size must be non-negative")
    private Long fileSize;

    @Column(name = "MIME_TYPE", length = 100)
    @Size(max = 100, message = "MIME type must not exceed 100 characters")
    private String mimeType;

    @Lob
    @Column(name = "DOCUMENT_CONTENT")
    private byte[] documentContent;

    @Column(name = "STATUS", length = 20)
    @Enumerated(EnumType.STRING)
    private DocumentStatus status = DocumentStatus.PENDING;

    @Column(name = "VERIFICATION_STATUS", length = 20)
    @Enumerated(EnumType.STRING)
    private VerificationStatus verificationStatus = VerificationStatus.NOT_VERIFIED;

    @Column(name = "VERIFIED_BY", length = 100)
    private String verifiedBy;

    @Column(name = "VERIFIED_DATE")
    private LocalDateTime verifiedDate;

    @Column(name = "EXPIRY_DATE")
    private LocalDateTime expiryDate;

    @Column(name = "NOTES", length = 1000)
    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;

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

    public enum DocumentStatus {
        PENDING("Document uploaded, awaiting review"),
        APPROVED("Document approved"),
        REJECTED("Document rejected"),
        EXPIRED("Document has expired"),
        REPLACED("Document has been replaced");

        private final String description;

        DocumentStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public enum VerificationStatus {
        NOT_VERIFIED("Not verified"),
        VERIFIED("Verified"),
        FAILED("Verification failed"),
        EXPIRED("Verification expired");

        private final String description;

        VerificationStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // Helper methods
    public boolean isExpired() {
        return expiryDate != null && expiryDate.isBefore(LocalDateTime.now());
    }

    public boolean isVerified() {
        return VerificationStatus.VERIFIED.equals(this.verificationStatus);
    }

    public String getFileSizeFormatted() {
        if (fileSize == null) return "Unknown";
        
        long bytes = fileSize;
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }
}
