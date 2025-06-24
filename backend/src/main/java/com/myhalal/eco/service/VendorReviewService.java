package com.myhalal.eco.service;

import com.myhalal.eco.entity.VendorReview;
import com.myhalal.eco.entity.Vendor;
import com.myhalal.eco.repository.VendorReviewRepository;
import com.myhalal.eco.repository.VendorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class VendorReviewService {

    private final VendorReviewRepository reviewRepository;
    private final VendorRepository vendorRepository;
    private final VendorService vendorService;

    public VendorReview createReview(VendorReview review) {
        log.info("Creating review for vendor ID: {}", review.getVendor().getVendorId());
        
        Vendor vendor = vendorRepository.findById(review.getVendor().getVendorId())
            .orElseThrow(() -> new RuntimeException("Vendor not found"));
        
        if (reviewRepository.existsByVendorIdAndCustomerEmailAndStatus(
                vendor.getVendorId(), 
                review.getCustomerEmail(),
                VendorReview.ReviewStatus.APPROVED)) {
            throw new RuntimeException("Customer has already submitted a review");
        }
        
        review.setVendor(vendor);
        review.setStatus(VendorReview.ReviewStatus.PENDING);
        review.setCreatedDate(LocalDateTime.now());
        
        VendorReview savedReview = reviewRepository.save(review);
        
        if (savedReview.getStatus() == VendorReview.ReviewStatus.APPROVED) {
            vendorService.updateVendorMetrics(vendor.getVendorId());
        }
        
        return savedReview;
    }

    @Transactional(readOnly = true)
    public VendorReview getReview(Long reviewId) {
        return reviewRepository.findById(reviewId)
            .orElseThrow(() -> new RuntimeException("Review not found"));
    }

    @Transactional(readOnly = true)
    public Page<VendorReview> getVendorReviews(Long vendorId, Pageable pageable) {
        return reviewRepository.findByVendorId(vendorId, pageable);
    }

    public VendorReview approveReview(Long reviewId) {
        log.info("Approving review ID: {}", reviewId);
        
        VendorReview review = getReview(reviewId);
        review.setStatus(VendorReview.ReviewStatus.APPROVED);
        review.setUpdatedDate(LocalDateTime.now());
        
        VendorReview approvedReview = reviewRepository.save(review);
        vendorService.updateVendorMetrics(review.getVendor().getVendorId());
        
        return approvedReview;
    }

    public VendorReview rejectReview(Long reviewId, String reason) {
        log.info("Rejecting review ID: {}", reviewId);
        
        VendorReview review = getReview(reviewId);
        review.setStatus(VendorReview.ReviewStatus.REJECTED);
        review.setUpdatedDate(LocalDateTime.now());
        review.setModerationNotes(reason);
        
        return reviewRepository.save(review);
    }

    public VendorReview addVendorResponse(Long reviewId, String response) {
        log.info("Adding vendor response to review ID: {}", reviewId);
        
        VendorReview review = getReview(reviewId);
        review.setVendorResponse(response);
        review.setVendorResponseDate(LocalDateTime.now());
        
        return reviewRepository.save(review);
    }

    public VendorReview updateHelpfulCount(Long reviewId) {
        log.info("Updating helpful count for review ID: {}", reviewId);
        
        VendorReview review = getReview(reviewId);
        review.setHelpfulCount(review.getHelpfulCount() + 1);
        
        return reviewRepository.save(review);
    }

    @Transactional(readOnly = true)
    public List<VendorReview> getPositiveReviews(Long vendorId, BigDecimal minRating) {
        return reviewRepository.findPositiveReviews(vendorId, minRating);
    }

    @Transactional(readOnly = true)
    public List<VendorReview> getNegativeReviews(Long vendorId, BigDecimal maxRating) {
        return reviewRepository.findNegativeReviews(vendorId, maxRating);
    }

    @Transactional(readOnly = true)
    public List<VendorReview> getMostHelpfulReviews(Long vendorId, int limit) {
        return reviewRepository.findMostHelpfulReviews(vendorId, limit);
    }

    @Transactional(readOnly = true)
    public List<VendorReview> getReviewsByDateRange(Long vendorId, LocalDateTime startDate, LocalDateTime endDate) {
        return reviewRepository.findReviewsByDateRange(vendorId, startDate, endDate);
    }

    @Transactional(readOnly = true)
    public List<VendorReview> getPendingResponseReviews(Long vendorId) {
        return reviewRepository.findPendingResponseReviews(vendorId);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getReviewStats(Long vendorId) {
        Map<String, Object> stats = new HashMap<>();
        
        BigDecimal averageRating = reviewRepository.calculateAverageRating(vendorId);
        Long totalReviews = reviewRepository.countApprovedReviews(vendorId);
        Long verifiedReviews = reviewRepository.countVerifiedPurchaseReviews(vendorId);
        List<Object[]> ratingDistribution = reviewRepository.getRatingDistribution(vendorId);
        
        stats.put("averageRating", averageRating != null ? averageRating : BigDecimal.ZERO);
        stats.put("totalReviews", totalReviews != null ? totalReviews : 0L);
        stats.put("verifiedReviews", verifiedReviews != null ? verifiedReviews : 0L);
        stats.put("ratingDistribution", ratingDistribution);
        
        return stats;
    }

    public void deleteReview(Long reviewId) {
        log.info("Deleting review ID: {}", reviewId);
        
        VendorReview review = getReview(reviewId);
        reviewRepository.delete(review);
        vendorService.updateVendorMetrics(review.getVendor().getVendorId());
    }
}
