package com.myhalal.eco.service;

import com.myhalal.eco.entity.Vendor;
import com.myhalal.eco.repository.VendorRepository;
import com.myhalal.eco.repository.VendorDocumentRepository;
import com.myhalal.eco.repository.VendorReviewRepository;
import com.myhalal.eco.repository.VendorVerificationRepository;
import com.myhalal.eco.repository.VendorNotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class VendorService {

    private final VendorRepository vendorRepository;
    private final VendorDocumentRepository vendorDocumentRepository;
    private final VendorReviewRepository vendorReviewRepository;
    private final VendorVerificationRepository vendorVerificationRepository;
    private final VendorNotificationRepository vendorNotificationRepository;

    public Vendor createVendor(Vendor vendor) {
        log.info("Creating new vendor: {}", vendor.getName());
        
        // Validation
        if (vendorRepository.findByContactEmail(vendor.getContactEmail()).isPresent()) {
            throw new RuntimeException("Vendor with this email already exists");
        }
        
        // Set default values
        vendor.setStatus(Vendor.VendorStatus.PENDING);
        vendor.setIsVerified(false);
        vendor.setAverageRating(BigDecimal.ZERO);
        vendor.setTotalReviews(0);
        vendor.setTotalSales(0);
        vendor.setTotalRevenue(BigDecimal.ZERO);
        
        Vendor savedVendor = vendorRepository.save(vendor);
        log.info("Vendor created successfully with ID: {}", savedVendor.getVendorId());
        
        return savedVendor;
    }

    @Transactional(readOnly = true)
    public List<Vendor> getAllVendors() {
        return vendorRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Vendor> getVendorById(Long id) {
        return vendorRepository.findById(id);
    }

    public Vendor updateVendor(Long id, Vendor updatedVendor) {
        log.info("Updating vendor with ID: {}", id);
        
        Vendor existingVendor = vendorRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Vendor not found"));
        
        // Update fields
        existingVendor.setName(updatedVendor.getName());
        existingVendor.setContactEmail(updatedVendor.getContactEmail());
        existingVendor.setPhone(updatedVendor.getPhone());
        existingVendor.setWebsite(updatedVendor.getWebsite());
        existingVendor.setStreetAddress(updatedVendor.getStreetAddress());
        existingVendor.setCity(updatedVendor.getCity());
        existingVendor.setState(updatedVendor.getState());
        existingVendor.setCountry(updatedVendor.getCountry());
        existingVendor.setPostalCode(updatedVendor.getPostalCode());
        existingVendor.setBusinessDescription(updatedVendor.getBusinessDescription());
        existingVendor.setBusinessCategory(updatedVendor.getBusinessCategory());
        existingVendor.setFoundingDate(updatedVendor.getFoundingDate());
        existingVendor.setLicenseNumber(updatedVendor.getLicenseNumber());
        existingVendor.setTaxId(updatedVendor.getTaxId());
        existingVendor.setFacebookUrl(updatedVendor.getFacebookUrl());
        existingVendor.setInstagramUrl(updatedVendor.getInstagramUrl());
        existingVendor.setTwitterUrl(updatedVendor.getTwitterUrl());
        
        return vendorRepository.save(existingVendor);
    }

    public Vendor updateVendorStatus(Long id, Vendor.VendorStatus status) {
        log.info("Updating vendor status for ID: {} to {}", id, status);
        
        Vendor vendor = vendorRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Vendor not found"));
        
        Vendor.VendorStatus oldStatus = vendor.getStatus();
        vendor.setStatus(status);
        
        Vendor updatedVendor = vendorRepository.save(vendor);
        
        log.info("Vendor status updated from {} to {} for vendor ID: {}", 
                oldStatus, status, id);
        
        return updatedVendor;
    }

    public Vendor verifyVendor(Long id, String verifiedBy) {
        log.info("Verifying vendor with ID: {} by {}", id, verifiedBy);
        
        Vendor vendor = vendorRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Vendor not found"));
        
        vendor.setIsVerified(true);
        vendor.setVerifiedDate(LocalDateTime.now());
        vendor.setVerifiedBy(verifiedBy);
        
        if (vendor.getStatus() == Vendor.VendorStatus.APPROVED) {
            vendor.setStatus(Vendor.VendorStatus.ACTIVE);
        }
        
        return vendorRepository.save(vendor);
    }

    @Transactional(readOnly = true)
    public List<Vendor> getRecentVendors(int limit) {
        return vendorRepository.findRecentVendors(limit);
    }

    @Transactional(readOnly = true)
    public Page<Vendor> getVendorsByCategory(String category, Pageable pageable) {
        return vendorRepository.findByBusinessCategory(category, pageable);
    }

    @Transactional(readOnly = true)
    public List<Vendor> getVerifiedVendors() {
        return vendorRepository.findByIsVerifiedTrue();
    }

    @Transactional(readOnly = true)
    public List<Vendor> getTopRatedVendors(BigDecimal minRating) {
        return vendorRepository.findByMinimumRating(minRating);
    }

    @Transactional(readOnly = true)
    public Page<Vendor> getTopPerformingVendors(Pageable pageable) {
        return vendorRepository.findTopPerformingVendors(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Vendor> searchVendors(String searchTerm, Pageable pageable) {
        return vendorRepository.searchVendors(searchTerm, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Vendor> getVendorsWithFilters(String category, BigDecimal minRating, 
                                            String city, String state, Pageable pageable) {
        return vendorRepository.findVendorsWithFilters(category, minRating, city, state, pageable);
    }

    @Transactional(readOnly = true)
    public List<Object[]> getVendorCategoryDistribution() {
        return vendorRepository.getVendorCategoryDistribution();
    }

    @Transactional(readOnly = true)
    public List<Object[]> getVendorStatusDistribution() {
        return vendorRepository.getVendorStatusDistribution();
    }

    @Transactional(readOnly = true)
    public List<Object[]> getVendorGeographicDistribution() {
        return vendorRepository.getVendorGeographicDistribution();
    }

    public void updateVendorMetrics(Long vendorId) {
        log.info("Updating metrics for vendor ID: {}", vendorId);
        
        Vendor vendor = vendorRepository.findById(vendorId)
            .orElseThrow(() -> new RuntimeException("Vendor not found"));
        
        // Update review metrics
        BigDecimal averageRating = vendorReviewRepository.calculateAverageRating(vendorId);
        Long totalReviews = vendorReviewRepository.countApprovedReviews(vendorId);
        
        vendor.setAverageRating(averageRating != null ? averageRating : BigDecimal.ZERO);
        vendor.setTotalReviews(totalReviews != null ? totalReviews.intValue() : 0);
        
        vendorRepository.save(vendor);
        
        log.info("Metrics updated for vendor ID: {} - Rating: {}, Reviews: {}", 
                vendorId, averageRating, totalReviews);
    }

    @Transactional(readOnly = true)
    public boolean isVendorFullyVerified(Long vendorId) {
        Vendor vendor = vendorRepository.findById(vendorId)
            .orElseThrow(() -> new RuntimeException("Vendor not found"));
        
        if (!vendor.getIsVerified()) {
            return false;
        }
        
        // Check if required documents are verified
        Long verifiedDocs = vendorDocumentRepository.countVerifiedDocuments(vendorId);
        Long completedVerifications = vendorVerificationRepository.countCompletedVerifications(vendorId);
        
        return verifiedDocs > 0 && completedVerifications > 0;
    }

    @Transactional(readOnly = true)
    public List<Vendor> getVendorsNeedingAttention() {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        return vendorRepository.findVendorsNeedingReverification(thirtyDaysAgo);
    }

    public void deleteVendor(Long id) {
        log.info("Deleting vendor with ID: {}", id);
        
        Vendor vendor = vendorRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Vendor not found"));
        
        // Soft delete by setting status to INACTIVE
        vendor.setStatus(Vendor.VendorStatus.INACTIVE);
        vendorRepository.save(vendor);
        
        log.info("Vendor with ID: {} marked as inactive", id);
    }

    @Transactional(readOnly = true)
    public Long getVendorCount() {
        return vendorRepository.count();
    }

    @Transactional(readOnly = true)
    public Long getActiveVendorCount() {
        return vendorRepository.countByStatus(Vendor.VendorStatus.ACTIVE);
    }

    @Transactional(readOnly = true)
    public Long getPendingVendorCount() {
        return vendorRepository.countByStatus(Vendor.VendorStatus.PENDING);
    }
}
