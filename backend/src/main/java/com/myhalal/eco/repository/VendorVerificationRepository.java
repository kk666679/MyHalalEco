package com.myhalal.eco.repository;

import com.myhalal.eco.entity.VendorVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VendorVerificationRepository extends JpaRepository<VendorVerification, Long> {
    
    List<VendorVerification> findByVendorId(Long vendorId);
    
    List<VendorVerification> findByVendorIdAndVerificationType(Long vendorId, String verificationType);
    
    List<VendorVerification> findByStatus(VendorVerification.VerificationStatus status);
    
    List<VendorVerification> findByAssignedTo(String assignedTo);
    
    List<VendorVerification> findByPriority(VendorVerification.Priority priority);
    
    @Query("SELECT v FROM VendorVerification v WHERE v.vendor.id = :vendorId " +
           "AND v.verificationType = :type AND v.status = 'COMPLETED'")
    Optional<VendorVerification> findCompletedVerification(
        @Param("vendorId") Long vendorId, 
        @Param("type") String verificationType
    );
    
    @Query("SELECT v FROM VendorVerification v WHERE v.nextReviewDate <= :date")
    List<VendorVerification> findOverdueVerifications(@Param("date") LocalDateTime date);
    
    @Query("SELECT v FROM VendorVerification v WHERE v.expiryDate BETWEEN :startDate AND :endDate")
    List<VendorVerification> findExpiringVerifications(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    @Query("SELECT COUNT(v) FROM VendorVerification v WHERE v.vendor.id = :vendorId " +
           "AND v.status = 'COMPLETED'")
    Long countCompletedVerifications(@Param("vendorId") Long vendorId);
    
    @Query("SELECT COUNT(v) FROM VendorVerification v WHERE v.vendor.id = :vendorId " +
           "AND v.status IN ('PENDING', 'IN_PROGRESS')")
    Long countPendingVerifications(@Param("vendorId") Long vendorId);
    
    @Query("SELECT AVG(v.verificationScore) FROM VendorVerification v " +
           "WHERE v.vendor.id = :vendorId AND v.status = 'COMPLETED' " +
           "AND v.verificationScore IS NOT NULL")
    Double calculateAverageVerificationScore(@Param("vendorId") Long vendorId);
    
    @Query(value = "SELECT * FROM VENDOR_VERIFICATIONS " +
           "WHERE STATUS IN ('PENDING', 'IN_PROGRESS') " +
           "ORDER BY PRIORITY DESC, INITIATED_DATE ASC " +
           "FETCH FIRST :limit ROWS ONLY", 
           nativeQuery = true)
    List<VendorVerification> findHighPriorityPendingVerifications(@Param("limit") int limit);
    
    @Query("SELECT v FROM VendorVerification v WHERE v.vendor.id = :vendorId " +
           "AND v.initiatedDate BETWEEN :startDate AND :endDate")
    List<VendorVerification> findVerificationsByDateRange(
        @Param("vendorId") Long vendorId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    @Query("SELECT v.verificationType, COUNT(v) FROM VendorVerification v " +
           "WHERE v.vendor.id = :vendorId " +
           "GROUP BY v.verificationType")
    List<Object[]> getVerificationTypeDistribution(@Param("vendorId") Long vendorId);
    
    boolean existsByVendorIdAndVerificationTypeAndStatus(
        Long vendorId, 
        String verificationType, 
        VendorVerification.VerificationStatus status
    );
}
