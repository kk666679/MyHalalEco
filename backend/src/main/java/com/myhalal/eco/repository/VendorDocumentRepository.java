package com.myhalal.eco.repository;

import com.myhalal.eco.entity.VendorDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VendorDocumentRepository extends JpaRepository<VendorDocument, Long> {
    
    List<VendorDocument> findByVendor_VendorId(Long vendorId);
    
    List<VendorDocument> findByVendor_VendorIdAndDocumentType(Long vendorId, String documentType);
    
    List<VendorDocument> findByStatus(VendorDocument.DocumentStatus status);
    
    List<VendorDocument> findByVerificationStatus(VendorDocument.VerificationStatus verificationStatus);
    
    @Query("SELECT d FROM VendorDocument d WHERE d.expiryDate <= :date")
    List<VendorDocument> findExpiredDocuments(@Param("date") LocalDateTime date);
    
    @Query("SELECT d FROM VendorDocument d WHERE d.vendor.id = :vendorId AND d.expiryDate BETWEEN :startDate AND :endDate")
    List<VendorDocument> findExpiringDocuments(
        @Param("vendorId") Long vendorId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    @Query("SELECT COUNT(d) FROM VendorDocument d WHERE d.vendor.id = :vendorId AND d.verificationStatus = 'VERIFIED'")
    Long countVerifiedDocuments(@Param("vendorId") Long vendorId);
    
    @Query(value = "SELECT * FROM VENDOR_DOCUMENTS WHERE VENDOR_ID = :vendorId ORDER BY CREATED_DATE DESC FETCH FIRST :limit ROWS ONLY", 
           nativeQuery = true)
    List<VendorDocument> findRecentDocuments(@Param("vendorId") Long vendorId, @Param("limit") int limit);
    
    boolean existsByVendor_VendorIdAndDocumentTypeAndVerificationStatus(
        Long vendorId, 
        String documentType, 
        VendorDocument.VerificationStatus verificationStatus
    );
}
