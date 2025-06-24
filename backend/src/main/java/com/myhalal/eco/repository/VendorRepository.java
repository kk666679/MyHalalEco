package com.myhalal.eco.repository;

import com.myhalal.eco.entity.Vendor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VendorRepository extends JpaRepository<Vendor, Long> {
    
    Optional<Vendor> findByContactEmail(String contactEmail);
    
    List<Vendor> findByStatus(Vendor.VendorStatus status);
    
    @Query("SELECT v FROM Vendor v WHERE v.name LIKE %:name%")
    List<Vendor> findByNameContaining(@Param("name") String name);
    
    @Query("SELECT COUNT(v) FROM Vendor v WHERE v.status = :status")
    Long countByStatus(@Param("status") Vendor.VendorStatus status);
    
    @Query(value = "SELECT * FROM VENDORS WHERE ROWNUM <= :limit ORDER BY REG_DATE DESC", nativeQuery = true)
    List<Vendor> findRecentVendors(@Param("limit") int limit);

    // Enhanced query methods
    Page<Vendor> findByBusinessCategory(String category, Pageable pageable);
    
    List<Vendor> findByIsVerifiedTrue();
    
    @Query("SELECT v FROM Vendor v WHERE v.averageRating >= :minRating")
    List<Vendor> findByMinimumRating(@Param("minRating") BigDecimal minRating);
    
    @Query("SELECT v FROM Vendor v WHERE v.totalSales >= :minSales")
    List<Vendor> findByMinimumSales(@Param("minSales") Integer minSales);
    
    @Query("SELECT v FROM Vendor v WHERE v.city = :city AND v.state = :state")
    List<Vendor> findByLocation(@Param("city") String city, @Param("state") String state);
    
    @Query("SELECT v FROM Vendor v WHERE v.status = 'ACTIVE' AND v.isVerified = true " +
           "ORDER BY v.averageRating DESC, v.totalSales DESC")
    Page<Vendor> findTopPerformingVendors(Pageable pageable);
    
    @Query("SELECT v FROM Vendor v WHERE v.verifiedDate IS NOT NULL " +
           "AND v.verifiedDate <= :expiryDate")
    List<Vendor> findVendorsNeedingReverification(@Param("expiryDate") LocalDateTime expiryDate);
    
    @Query("SELECT v.businessCategory, COUNT(v) FROM Vendor v " +
           "WHERE v.status = 'ACTIVE' GROUP BY v.businessCategory")
    List<Object[]> getVendorCategoryDistribution();
    
    @Query("SELECT v.status, COUNT(v) FROM Vendor v GROUP BY v.status")
    List<Object[]> getVendorStatusDistribution();
    
    @Query("SELECT v FROM Vendor v WHERE v.status = 'ACTIVE' " +
           "AND v.totalRevenue >= :minRevenue")
    List<Vendor> findPremiumVendors(@Param("minRevenue") BigDecimal minRevenue);
    
    @Query("SELECT DISTINCT v.city, v.state, COUNT(v) FROM Vendor v " +
           "WHERE v.status = 'ACTIVE' GROUP BY v.city, v.state")
    List<Object[]> getVendorGeographicDistribution();
    
    @Modifying
    @Query("UPDATE Vendor v SET v.status = :newStatus, " +
           "v.updatedDate = CURRENT_TIMESTAMP, v.updatedBy = :updatedBy " +
           "WHERE v.id = :vendorId")
    void updateVendorStatus(
        @Param("vendorId") Long vendorId,
        @Param("newStatus") Vendor.VendorStatus newStatus,
        @Param("updatedBy") String updatedBy
    );
    
    @Modifying
    @Query("UPDATE Vendor v SET v.isVerified = true, " +
           "v.verifiedDate = CURRENT_TIMESTAMP, v.verifiedBy = :verifiedBy, " +
           "v.updatedDate = CURRENT_TIMESTAMP, v.updatedBy = :updatedBy " +
           "WHERE v.id = :vendorId")
    void markVendorAsVerified(
        @Param("vendorId") Long vendorId,
        @Param("verifiedBy") String verifiedBy,
        @Param("updatedBy") String updatedBy
    );
    
    @Query("SELECT v FROM Vendor v WHERE " +
           "LOWER(v.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(v.businessDescription) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(v.businessCategory) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Vendor> searchVendors(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    @Query("SELECT v FROM Vendor v WHERE v.status = 'ACTIVE' " +
           "AND v.isVerified = true " +
           "AND (:category IS NULL OR v.businessCategory = :category) " +
           "AND (:minRating IS NULL OR v.averageRating >= :minRating) " +
           "AND (:city IS NULL OR v.city = :city) " +
           "AND (:state IS NULL OR v.state = :state)")
    Page<Vendor> findVendorsWithFilters(
        @Param("category") String category,
        @Param("minRating") BigDecimal minRating,
        @Param("city") String city,
        @Param("state") String state,
        Pageable pageable
    );
}
