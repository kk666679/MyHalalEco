package com.myhalal.eco.service;

import com.myhalal.eco.entity.VendorVerification;
import com.myhalal.eco.entity.Vendor;
import com.myhalal.eco.repository.VendorVerificationRepository;
import com.myhalal.eco.repository.VendorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class VendorVerificationService {

    private final VendorVerificationRepository verificationRepository;
    private final VendorRepository vendorRepository;
    private final VendorService vendorService;

    @Transactional
    public VendorVerification initiateVerification(Long vendorId, String verificationType, String initiatedBy) {
        log.info("Initiating verification for vendor ID: {}, type: {}", vendorId, verificationType);
        
        Vendor vendor = vendorRepository.findById(vendorId)
            .orElseThrow(() -> new RuntimeException("Vendor not found"));
        
        VendorVerification verification = new VendorVerification();
        verification.setVendor(vendor);
        verification.setVerificationType(verificationType);
        verification.setStatus(VendorVerification.VerificationStatus.PENDING);
        verification.setPriority(VendorVerification.Priority.MEDIUM);
        verification.setInitiatedBy(initiatedBy);
        verification.setInitiatedDate(LocalDateTime.now());
        
        return verificationRepository.save(verification);
    }

    @Transactional(readOnly = true)
    public VendorVerification getVerification(Long verificationId) {
        return verificationRepository.findById(verificationId)
            .orElseThrow(() -> new RuntimeException("Verification not found"));
    }

    @Transactional(readOnly = true)
    public List<VendorVerification> getVendorVerifications(Long vendorId) {
        return verificationRepository.findByVendorId(vendorId);
    }

    @Transactional
    public VendorVerification assignVerification(Long verificationId, String assignedTo) {
        log.info("Assigning verification ID: {} to: {}", verificationId, assignedTo);
        
        VendorVerification verification = getVerification(verificationId);
        verification.setAssignedTo(assignedTo);
        verification.setStatus(VendorVerification.VerificationStatus.IN_PROGRESS);
        verification.setAssignedDate(LocalDateTime.now());
        
        return verificationRepository.save(verification);
    }

    @Transactional
    public VendorVerification completeVerification(Long verificationId, boolean approved, String verifiedBy, String notes) {
        log.info("Completing verification ID: {} by: {}", verificationId, verifiedBy);
        
        VendorVerification verification = getVerification(verificationId);
        verification.setStatus(approved ? 
            VendorVerification.VerificationStatus.COMPLETED : 
            VendorVerification.VerificationStatus.REJECTED);
        verification.setCompletedBy(verifiedBy);
        verification.setCompletedDate(LocalDateTime.now());
        verification.setNotes(notes);
        
        VendorVerification completedVerification = verificationRepository.save(verification);
        
        if (approved) {
            vendorService.verifyVendor(verification.getVendor().getVendorId(), verifiedBy);
        }
        
        return completedVerification;
    }

    @Transactional
    public VendorVerification updatePriority(Long verificationId, VendorVerification.Priority priority) {
        log.info("Updating priority for verification ID: {} to: {}", verificationId, priority);
        
        VendorVerification verification = getVerification(verificationId);
        verification.setPriority(priority);
        
        return verificationRepository.save(verification);
    }

    @Transactional(readOnly = true)
    public List<VendorVerification> getOverdueVerifications() {
        return verificationRepository.findOverdueVerifications(LocalDateTime.now());
    }

    @Transactional(readOnly = true)
    public List<VendorVerification> getExpiringVerifications(int daysThreshold) {
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = startDate.plusDays(daysThreshold);
        return verificationRepository.findExpiringVerifications(startDate, endDate);
    }

    @Transactional(readOnly = true)
    public List<VendorVerification> getHighPriorityVerifications(int limit) {
        return verificationRepository.findHighPriorityPendingVerifications(limit);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getVerificationStats(Long vendorId) {
        Map<String, Object> stats = new HashMap<>();
        
        Long completed = verificationRepository.countCompletedVerifications(vendorId);
        Long pending = verificationRepository.countPendingVerifications(vendorId);
        Double avgScore = verificationRepository.calculateAverageVerificationScore(vendorId);
        List<Object[]> typeDistribution = verificationRepository.getVerificationTypeDistribution(vendorId);
        
        stats.put("completed", completed != null ? completed : 0L);
        stats.put("pending", pending != null ? pending : 0L);
        stats.put("averageScore", avgScore != null ? avgScore : 0.0);
        stats.put("typeDistribution", typeDistribution);
        
        return stats;
    }

    @Transactional
    public void cancelVerification(Long verificationId, String cancelledBy, String reason) {
        log.info("Cancelling verification ID: {} by: {}", verificationId, cancelledBy);
        
        VendorVerification verification = getVerification(verificationId);
        verification.setStatus(VendorVerification.VerificationStatus.CANCELLED);
        verification.setNotes(reason);
        verification.setUpdatedBy(cancelledBy);
        verification.setUpdatedDate(LocalDateTime.now());
        
        verificationRepository.save(verification);
    }
}
