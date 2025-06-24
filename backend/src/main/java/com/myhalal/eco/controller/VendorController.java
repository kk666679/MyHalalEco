package com.myhalal.eco.controller;

import com.myhalal.eco.entity.Vendor;
import com.myhalal.eco.service.VendorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/vendors")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class VendorController {

    private final VendorService vendorService;

    @PostMapping
    public ResponseEntity<?> createVendor(@Valid @RequestBody Vendor vendor) {
        try {
            Vendor createdVendor = vendorService.createVendor(vendor);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdVendor);
        } catch (RuntimeException e) {
            log.error("Error creating vendor: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<Page<Vendor>> getAllVendors(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                   Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Vendor> vendors = vendorService.getVendorsWithFilters(null, null, null, null, pageable);
        return ResponseEntity.ok(vendors);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Vendor> getVendorById(@PathVariable Long id) {
        Optional<Vendor> vendor = vendorService.getVendorById(id);
        return vendor.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateVendor(@PathVariable Long id, @Valid @RequestBody Vendor vendor) {
        try {
            Vendor updatedVendor = vendorService.updateVendor(id, vendor);
            return ResponseEntity.ok(updatedVendor);
        } catch (RuntimeException e) {
            log.error("Error updating vendor {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateVendorStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> statusUpdate) {
        try {
            Vendor.VendorStatus newStatus = Vendor.VendorStatus.valueOf(statusUpdate.get("status").toUpperCase());
            Vendor updatedVendor = vendorService.updateVendorStatus(id, newStatus);
            return ResponseEntity.ok(updatedVendor);
        } catch (IllegalArgumentException e) {
            log.error("Invalid status for vendor {}: {}", id, statusUpdate.get("status"));
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid status"));
        } catch (RuntimeException e) {
            log.error("Error updating vendor status for {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/verify")
    public ResponseEntity<?> verifyVendor(
            @PathVariable Long id,
            @RequestParam String verifiedBy) {
        try {
            Vendor verifiedVendor = vendorService.verifyVendor(id, verifiedBy);
            return ResponseEntity.ok(verifiedVendor);
        } catch (RuntimeException e) {
            log.error("Error verifying vendor {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteVendor(@PathVariable Long id) {
        try {
            vendorService.deleteVendor(id);
            return ResponseEntity.ok(Map.of("message", "Vendor deleted successfully"));
        } catch (RuntimeException e) {
            log.error("Error deleting vendor {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/recent")
    public ResponseEntity<List<Vendor>> getRecentVendors(@RequestParam(defaultValue = "10") int limit) {
        List<Vendor> recentVendors = vendorService.getRecentVendors(limit);
        return ResponseEntity.ok(recentVendors);
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<Page<Vendor>> getVendorsByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Vendor> vendors = vendorService.getVendorsByCategory(category, pageable);
        return ResponseEntity.ok(vendors);
    }

    @GetMapping("/verified")
    public ResponseEntity<List<Vendor>> getVerifiedVendors() {
        List<Vendor> verifiedVendors = vendorService.getVerifiedVendors();
        return ResponseEntity.ok(verifiedVendors);
    }

    @GetMapping("/top-rated")
    public ResponseEntity<List<Vendor>> getTopRatedVendors(
            @RequestParam(defaultValue = "4.0") BigDecimal minRating) {
        List<Vendor> topRatedVendors = vendorService.getTopRatedVendors(minRating);
        return ResponseEntity.ok(topRatedVendors);
    }

    @GetMapping("/top-performing")
    public ResponseEntity<Page<Vendor>> getTopPerformingVendors(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Vendor> topVendors = vendorService.getTopPerformingVendors(pageable);
        return ResponseEntity.ok(topVendors);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<Vendor>> searchVendors(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Vendor> vendors = vendorService.searchVendors(query, pageable);
        return ResponseEntity.ok(vendors);
    }

    @GetMapping("/filter")
    public ResponseEntity<Page<Vendor>> getVendorsWithFilters(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) BigDecimal minRating,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String state,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "averageRating") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                   Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Vendor> vendors = vendorService.getVendorsWithFilters(category, minRating, city, state, pageable);
        return ResponseEntity.ok(vendors);
    }

    @GetMapping("/analytics/category-distribution")
    public ResponseEntity<List<Object[]>> getCategoryDistribution() {
        List<Object[]> distribution = vendorService.getVendorCategoryDistribution();
        return ResponseEntity.ok(distribution);
    }

    @GetMapping("/analytics/status-distribution")
    public ResponseEntity<List<Object[]>> getStatusDistribution() {
        List<Object[]> distribution = vendorService.getVendorStatusDistribution();
        return ResponseEntity.ok(distribution);
    }

    @GetMapping("/analytics/geographic-distribution")
    public ResponseEntity<List<Object[]>> getGeographicDistribution() {
        List<Object[]> distribution = vendorService.getVendorGeographicDistribution();
        return ResponseEntity.ok(distribution);
    }

    @PostMapping("/{id}/update-metrics")
    public ResponseEntity<?> updateVendorMetrics(@PathVariable Long id) {
        try {
            vendorService.updateVendorMetrics(id);
            return ResponseEntity.ok(Map.of("message", "Vendor metrics updated successfully"));
        } catch (RuntimeException e) {
            log.error("Error updating metrics for vendor {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}/verification-status")
    public ResponseEntity<Map<String, Boolean>> getVendorVerificationStatus(@PathVariable Long id) {
        try {
            boolean isFullyVerified = vendorService.isVendorFullyVerified(id);
            return ResponseEntity.ok(Map.of("isFullyVerified", isFullyVerified));
        } catch (RuntimeException e) {
            log.error("Error checking verification status for vendor {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", true));
        }
    }

    @GetMapping("/needing-attention")
    public ResponseEntity<List<Vendor>> getVendorsNeedingAttention() {
        List<Vendor> vendors = vendorService.getVendorsNeedingAttention();
        return ResponseEntity.ok(vendors);
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getVendorStats() {
        Map<String, Long> stats = Map.of(
            "total", vendorService.getVendorCount(),
            "active", vendorService.getActiveVendorCount(),
            "pending", vendorService.getPendingVendorCount()
        );
        return ResponseEntity.ok(stats);
    }
}
