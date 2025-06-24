package com.myhalal.eco.controller;

import com.myhalal.eco.entity.VendorVerification;
import com.myhalal.eco.service.VendorVerificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vendor-verifications")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class VendorVerificationController {

    private final VendorVerificationService verificationService;

    @PostMapping("/vendor/{vendorId}")
    public ResponseEntity<?> initiateVerification(
            @PathVariable Long vendorId,
            @RequestParam String verificationType,
            @RequestParam String initiatedBy) {
        try {
            VendorVerification verification = verificationService.initiateVerification(
                vendorId, verificationType, initiatedBy);
            return ResponseEntity.ok(verification);
        } catch (RuntimeException e) {
            log.error("Error initiating verification: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getVerification(@PathVariable Long id) {
        try {
            VendorVerification verification = verificationService.getVerification(id);
            return ResponseEntity.ok(verification);
        } catch (RuntimeException e) {
            log.error("Error retrieving verification {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/vendor/{vendorId}")
    public ResponseEntity<List<VendorVerification>> getVendorVerifications(@PathVariable Long vendorId) {
        List<VendorVerification> verifications = verificationService.getVendorVerifications(vendorId);
        return ResponseEntity.ok(verifications);
    }

    @PutMapping("/{id}/assign")
    public ResponseEntity<?> assignVerification(
            @PathVariable Long id,
            @RequestParam String assignedTo) {
        try {
            VendorVerification verification = verificationService.assignVerification(id, assignedTo);
            return ResponseEntity.ok(verification);
        } catch (RuntimeException e) {
            log.error("Error assigning verification {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<?> completeVerification(
            @PathVariable Long id,
            @RequestParam boolean approved,
            @RequestParam String verifiedBy,
            @RequestParam(required = false) String notes) {
        try {
            VendorVerification verification = verificationService.completeVerification(
                id, approved, verifiedBy, notes);
            return ResponseEntity.ok(verification);
        } catch (RuntimeException e) {
            log.error("Error completing verification {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/priority")
    public ResponseEntity<?> updatePriority(
            @PathVariable Long id,
            @RequestParam VendorVerification.Priority priority) {
        try {
            VendorVerification verification = verificationService.updatePriority(id, priority);
            return ResponseEntity.ok(verification);
        } catch (RuntimeException e) {
            log.error("Error updating priority for verification {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/overdue")
    public ResponseEntity<List<VendorVerification>> getOverdueVerifications() {
        List<VendorVerification> verifications = verificationService.getOverdueVerifications();
        return ResponseEntity.ok(verifications);
    }

    @GetMapping("/expiring")
    public ResponseEntity<List<VendorVerification>> getExpiringVerifications(
            @RequestParam(defaultValue = "30") int daysThreshold) {
        List<VendorVerification> verifications = verificationService.getExpiringVerifications(daysThreshold);
        return ResponseEntity.ok(verifications);
    }

    @GetMapping("/high-priority")
    public ResponseEntity<List<VendorVerification>> getHighPriorityVerifications(
            @RequestParam(defaultValue = "10") int limit) {
        List<VendorVerification> verifications = verificationService.getHighPriorityVerifications(limit);
        return ResponseEntity.ok(verifications);
    }

    @GetMapping("/stats/vendor/{vendorId}")
    public ResponseEntity<Map<String, Object>> getVerificationStats(@PathVariable Long vendorId) {
        Map<String, Object> stats = verificationService.getVerificationStats(vendorId);
        return ResponseEntity.ok(stats);
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancelVerification(
            @PathVariable Long id,
            @RequestParam String cancelledBy,
            @RequestParam String reason) {
        try {
            verificationService.cancelVerification(id, cancelledBy, reason);
            return ResponseEntity.ok(Map.of("message", "Verification cancelled successfully"));
        } catch (RuntimeException e) {
            log.error("Error cancelling verification {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
