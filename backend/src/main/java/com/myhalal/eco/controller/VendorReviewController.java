package com.myhalal.eco.controller;

import com.myhalal.eco.entity.VendorReview;
import com.myhalal.eco.service.VendorReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vendor-reviews")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class VendorReviewController {

    private final VendorReviewService reviewService;

    @PostMapping
    public ResponseEntity<?> createReview(@Valid @RequestBody VendorReview review) {
        try {
            VendorReview createdReview = reviewService.createReview(review);
            return ResponseEntity.ok(createdReview);
        } catch (RuntimeException e) {
            log.error("Error creating review: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getReview(@PathVariable Long id) {
        try {
            VendorReview review = reviewService.getReview(id);
            return ResponseEntity.ok(review);
        } catch (RuntimeException e) {
            log.error("Error retrieving review {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/vendor/{vendorId}")
    public ResponseEntity<Page<VendorReview>> getVendorReviews(
            @PathVariable Long vendorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                   Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<VendorReview> reviews = reviewService.getVendorReviews(vendorId, pageable);
        return ResponseEntity.ok(reviews);
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<?> approveReview(@PathVariable Long id) {
        try {
            VendorReview review = reviewService.approveReview(id);
            return ResponseEntity.ok(review);
        } catch (RuntimeException e) {
            log.error("Error approving review {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<?> rejectReview(
            @PathVariable Long id,
            @RequestParam String reason) {
        try {
            VendorReview review = reviewService.rejectReview(id, reason);
            return ResponseEntity.ok(review);
        } catch (RuntimeException e) {
            log.error("Error rejecting review {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/vendor-response")
    public ResponseEntity<?> addVendorResponse(
            @PathVariable Long id,
            @RequestParam String response) {
        try {
            VendorReview review = reviewService.addVendorResponse(id, response);
            return ResponseEntity.ok(review);
        } catch (RuntimeException e) {
            log.error("Error adding vendor response to review {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/helpful")
    public ResponseEntity<?> markReviewAsHelpful(@PathVariable Long id) {
        try {
            VendorReview review = reviewService.updateHelpfulCount(id);
            return ResponseEntity.ok(review);
        } catch (RuntimeException e) {
            log.error("Error updating helpful count for review {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/vendor/{vendorId}/positive")
    public ResponseEntity<List<VendorReview>> getPositiveReviews(
            @PathVariable Long vendorId,
            @RequestParam(defaultValue = "4.0") BigDecimal minRating) {
        List<VendorReview> reviews = reviewService.getPositiveReviews(vendorId, minRating);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/vendor/{vendorId}/negative")
    public ResponseEntity<List<VendorReview>> getNegativeReviews(
            @PathVariable Long vendorId,
            @RequestParam(defaultValue = "3.0") BigDecimal maxRating) {
        List<VendorReview> reviews = reviewService.getNegativeReviews(vendorId, maxRating);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/vendor/{vendorId}/most-helpful")
    public ResponseEntity<List<VendorReview>> getMostHelpfulReviews(
            @PathVariable Long vendorId,
            @RequestParam(defaultValue = "5") int limit) {
        List<VendorReview> reviews = reviewService.getMostHelpfulReviews(vendorId, limit);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/vendor/{vendorId}/date-range")
    public ResponseEntity<List<VendorReview>> getReviewsByDateRange(
            @PathVariable Long vendorId,
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate) {
        List<VendorReview> reviews = reviewService.getReviewsByDateRange(vendorId, startDate, endDate);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/vendor/{vendorId}/pending-response")
    public ResponseEntity<List<VendorReview>> getPendingResponseReviews(@PathVariable Long vendorId) {
        List<VendorReview> reviews = reviewService.getPendingResponseReviews(vendorId);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/vendor/{vendorId}/stats")
    public ResponseEntity<Map<String, Object>> getReviewStats(@PathVariable Long vendorId) {
        Map<String, Object> stats = reviewService.getReviewStats(vendorId);
        return ResponseEntity.ok(stats);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteReview(@PathVariable Long id) {
        try {
            reviewService.deleteReview(id);
            return ResponseEntity.ok(Map.of("message", "Review deleted successfully"));
        } catch (RuntimeException e) {
            log.error("Error deleting review {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
