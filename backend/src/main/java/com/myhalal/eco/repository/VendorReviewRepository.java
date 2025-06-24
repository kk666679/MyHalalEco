package com.myhalal.eco.repository;

import com.myhalal.eco.entity.VendorReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VendorReviewRepository extends JpaRepository<VendorReview, Long> {
    
    Page<VendorReview> findByVendorId(Long vendorId, Pageable pageable);
    
    List<VendorReview> findByVendorIdAndStatus(Long vendorId, VendorReview.ReviewStatus status);
    
    @Query("SELECT r FROM VendorReview r WHERE r.vendor.id = :vendorId AND r.rating >= :minRating")
    List<VendorReview> findPositiveReviews(
        @Param("vendorId") Long vendorId, 
        @Param("minRating") BigDecimal minRating
    );
    
    @Query("SELECT r FROM VendorReview r WHERE r.vendor.id = :vendorId AND r.rating < :maxRating")
    List<VendorReview> findNegativeReviews(
        @Param("vendorId") Long vendorId, 
        @Param("maxRating") BigDecimal maxRating
    );
    
    @Query("SELECT AVG(r.rating) FROM VendorReview r WHERE r.vendor.id = :vendorId AND r.status = 'APPROVED'")
    BigDecimal calculateAverageRating(@Param("vendorId") Long vendorId);
    
    @Query("SELECT COUNT(r) FROM VendorReview r WHERE r.vendor.id = :vendorId AND r.status = 'APPROVED'")
    Long countApprovedReviews(@Param("vendorId") Long vendorId);
    
    @Query("SELECT COUNT(r) FROM VendorReview r WHERE r.vendor.id = :vendorId AND r.isVerifiedPurchase = true")
    Long countVerifiedPurchaseReviews(@Param("vendorId") Long vendorId);
    
    @Query(value = "SELECT * FROM VENDOR_REVIEWS " +
           "WHERE VENDOR_ID = :vendorId AND STATUS = 'APPROVED' " +
           "ORDER BY HELPFUL_COUNT DESC FETCH FIRST :limit ROWS ONLY", 
           nativeQuery = true)
    List<VendorReview> findMostHelpfulReviews(
        @Param("vendorId") Long vendorId, 
        @Param("limit") int limit
    );
    
    @Query("SELECT r FROM VendorReview r WHERE r.vendor.id = :vendorId " +
           "AND r.createdDate BETWEEN :startDate AND :endDate")
    List<VendorReview> findReviewsByDateRange(
        @Param("vendorId") Long vendorId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    @Query("SELECT COUNT(r) FROM VendorReview r WHERE r.vendor.id = :vendorId " +
           "AND r.rating >= :rating AND r.status = 'APPROVED'")
    Long countReviewsByRatingAndAbove(
        @Param("vendorId") Long vendorId, 
        @Param("rating") BigDecimal rating
    );
    
    @Query("SELECT r.rating, COUNT(r) FROM VendorReview r " +
           "WHERE r.vendor.id = :vendorId AND r.status = 'APPROVED' " +
           "GROUP BY r.rating ORDER BY r.rating DESC")
    List<Object[]> getRatingDistribution(@Param("vendorId") Long vendorId);
    
    @Query("SELECT r FROM VendorReview r WHERE r.vendor.id = :vendorId " +
           "AND r.vendorResponse IS NULL AND r.status = 'APPROVED'")
    List<VendorReview> findPendingResponseReviews(@Param("vendorId") Long vendorId);
    
    boolean existsByVendorIdAndCustomerEmailAndStatus(
        Long vendorId, 
        String customerEmail, 
        VendorReview.ReviewStatus status
    );
}
