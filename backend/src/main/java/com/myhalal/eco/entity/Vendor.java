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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "VENDORS")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vendor {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "vendor_seq")
    @SequenceGenerator(name = "vendor_seq", sequenceName = "VENDOR_SEQ", allocationSize = 1)
    @Column(name = "VENDOR_ID")
    private Long vendorId;

    @Column(name = "NAME", nullable = false, length = 100)
    @NotBlank(message = "Vendor name is required")
    @Size(max = 100, message = "Vendor name must not exceed 100 characters")
    private String name;

    @Column(name = "CONTACT_EMAIL", nullable = false, length = 100)
    @NotBlank(message = "Contact email is required")
    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String contactEmail;

    @Column(name = "PHONE", length = 20)
    @Pattern(regexp = "^[+]?[0-9\\s\\-()]{10,20}$", message = "Invalid phone number format")
    private String phone;

    @Column(name = "WEBSITE", length = 255)
    @Pattern(regexp = "^(https?://)?(www\\.)?[a-zA-Z0-9\\-\\.]+\\.[a-zA-Z]{2,}(/.*)?$", 
             message = "Invalid website URL format")
    private String website;

    // Address fields
    @Column(name = "STREET_ADDRESS", length = 255)
    @Size(max = 255, message = "Street address must not exceed 255 characters")
    private String streetAddress;

    @Column(name = "CITY", length = 100)
    @Size(max = 100, message = "City must not exceed 100 characters")
    private String city;

    @Column(name = "STATE", length = 100)
    @Size(max = 100, message = "State must not exceed 100 characters")
    private String state;

    @Column(name = "COUNTRY", length = 100)
    @Size(max = 100, message = "Country must not exceed 100 characters")
    private String country;

    @Column(name = "POSTAL_CODE", length = 20)
    @Size(max = 20, message = "Postal code must not exceed 20 characters")
    private String postalCode;

    // Business details
    @Column(name = "BUSINESS_DESCRIPTION", length = 1000)
    @Size(max = 1000, message = "Business description must not exceed 1000 characters")
    private String businessDescription;

    @Column(name = "BUSINESS_CATEGORY", length = 50)
    @Size(max = 50, message = "Business category must not exceed 50 characters")
    private String businessCategory;

    @Column(name = "FOUNDING_DATE")
    private LocalDate foundingDate;

    @Lob
    @Column(name = "BUSINESS_LICENSE")
    private String businessLicense;

    @Column(name = "LICENSE_NUMBER", length = 100)
    @Size(max = 100, message = "License number must not exceed 100 characters")
    private String licenseNumber;

    @Column(name = "TAX_ID", length = 50)
    @Size(max = 50, message = "Tax ID must not exceed 50 characters")
    private String taxId;

    // Status and verification
    @Column(name = "STATUS", length = 20)
    @Enumerated(EnumType.STRING)
    private VendorStatus status = VendorStatus.PENDING;

    @Column(name = "IS_VERIFIED")
    private Boolean isVerified = false;

    @Column(name = "VERIFIED_DATE")
    private LocalDateTime verifiedDate;

    @Column(name = "VERIFIED_BY", length = 100)
    private String verifiedBy;

    // Metrics
    @Column(name = "AVERAGE_RATING", precision = 3, scale = 2)
    @DecimalMin(value = "0.0", message = "Rating must be at least 0.0")
    @DecimalMax(value = "5.0", message = "Rating must not exceed 5.0")
    private BigDecimal averageRating = BigDecimal.ZERO;

    @Column(name = "TOTAL_REVIEWS")
    @Min(value = 0, message = "Total reviews must be non-negative")
    private Integer totalReviews = 0;

    @Column(name = "TOTAL_SALES")
    @Min(value = 0, message = "Total sales must be non-negative")
    private Integer totalSales = 0;

    @Column(name = "TOTAL_REVENUE", precision = 15, scale = 2)
    @DecimalMin(value = "0.0", message = "Total revenue must be non-negative")
    private BigDecimal totalRevenue = BigDecimal.ZERO;

    // Social media
    @Column(name = "FACEBOOK_URL", length = 255)
    private String facebookUrl;

    @Column(name = "INSTAGRAM_URL", length = 255)
    private String instagramUrl;

    @Column(name = "TWITTER_URL", length = 255)
    private String twitterUrl;

    // Audit fields
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

    // Legacy field for backward compatibility
    @Column(name = "REG_DATE", nullable = false)
    private LocalDateTime regDate;

    @PrePersist
    protected void onCreate() {
        if (regDate == null) {
            regDate = LocalDateTime.now();
        }
        if (createdDate == null) {
            createdDate = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedDate = LocalDateTime.now();
    }

    public enum VendorStatus {
        PENDING("Application submitted, awaiting review"),
        UNDER_REVIEW("Application under review"),
        APPROVED("Application approved, awaiting activation"),
        ACTIVE("Vendor is active and can sell products"),
        SUSPENDED("Vendor is temporarily suspended"),
        INACTIVE("Vendor is inactive"),
        REJECTED("Application rejected"),
        BLACKLISTED("Vendor is blacklisted");

        private final String description;

        VendorStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // Helper methods
    public String getFullAddress() {
        StringBuilder address = new StringBuilder();
        if (streetAddress != null && !streetAddress.trim().isEmpty()) {
            address.append(streetAddress);
        }
        if (city != null && !city.trim().isEmpty()) {
            if (address.length() > 0) address.append(", ");
            address.append(city);
        }
        if (state != null && !state.trim().isEmpty()) {
            if (address.length() > 0) address.append(", ");
            address.append(state);
        }
        if (country != null && !country.trim().isEmpty()) {
            if (address.length() > 0) address.append(", ");
            address.append(country);
        }
        if (postalCode != null && !postalCode.trim().isEmpty()) {
            if (address.length() > 0) address.append(" ");
            address.append(postalCode);
        }
        return address.toString();
    }

    public boolean isActive() {
        return VendorStatus.ACTIVE.equals(this.status);
    }

    public boolean canSell() {
        return isActive() && Boolean.TRUE.equals(isVerified);
    }
}
