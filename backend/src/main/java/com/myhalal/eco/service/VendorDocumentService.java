package com.myhalal.eco.service;

import com.myhalal.eco.entity.VendorDocument;
import com.myhalal.eco.entity.Vendor;
import com.myhalal.eco.repository.VendorDocumentRepository;
import com.myhalal.eco.repository.VendorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class VendorDocumentService {

    private final VendorDocumentRepository documentRepository;
    private final VendorRepository vendorRepository;
    
    private final String uploadDir = "uploads/vendor-documents/";

    public VendorDocument uploadDocument(MultipartFile file, Long vendorId, String documentType) {
        log.info("Uploading document for vendor ID: {}, type: {}", vendorId, documentType);
        
        Vendor vendor = vendorRepository.findById(vendorId)
            .orElseThrow(() -> new RuntimeException("Vendor not found"));
        
        if (file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }
        
        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
            
            Path filePath = uploadPath.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            VendorDocument document = new VendorDocument();
            document.setVendor(vendor);
            document.setDocumentType(documentType);
            document.setDocumentName(originalFilename);
            document.setFilePath(filePath.toString());
            document.setFileSize(file.getSize());
            document.setMimeType(file.getContentType());
            document.setStatus(VendorDocument.DocumentStatus.PENDING);
            document.setVerificationStatus(VendorDocument.VerificationStatus.NOT_VERIFIED);
            
            VendorDocument savedDocument = documentRepository.save(document);
            log.info("Document uploaded successfully with ID: {}", savedDocument.getDocumentId());
            
            return savedDocument;
            
        } catch (IOException e) {
            log.error("Error uploading file: {}", e.getMessage());
            throw new RuntimeException("Failed to upload file: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public VendorDocument getDocument(Long documentId) {
        return documentRepository.findById(documentId)
            .orElseThrow(() -> new RuntimeException("Document not found"));
    }

    @Transactional(readOnly = true)
    public Resource downloadDocument(Long documentId) {
        VendorDocument document = getDocument(documentId);
        
        try {
            Path filePath = Paths.get(document.getFilePath());
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("File not found or not readable");
            }
        } catch (Exception e) {
            log.error("Error downloading document {}: {}", documentId, e.getMessage());
            throw new RuntimeException("Failed to download file: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public List<VendorDocument> getVendorDocuments(Long vendorId) {
        return documentRepository.findByVendorId(vendorId);
    }

    @Transactional(readOnly = true)
    public List<VendorDocument> getVendorDocumentsByType(Long vendorId, String documentType) {
        return documentRepository.findByVendor_VendorIdAndDocumentType(vendorId, documentType);
    }

    public VendorDocument verifyDocument(Long documentId, String verifiedBy, String notes) {
        log.info("Verifying document ID: {} by {}", documentId, verifiedBy);
        
        VendorDocument document = getDocument(documentId);
        document.setVerificationStatus(VendorDocument.VerificationStatus.VERIFIED);
        document.setStatus(VendorDocument.DocumentStatus.APPROVED);
        document.setVerifiedBy(verifiedBy);
        document.setVerifiedDate(LocalDateTime.now());
        document.setNotes(notes);
        
        return documentRepository.save(document);
    }

    public VendorDocument rejectDocument(Long documentId, String rejectedBy, String reason) {
        log.info("Rejecting document ID: {} by {}", documentId, rejectedBy);
        
        VendorDocument document = getDocument(documentId);
        document.setVerificationStatus(VendorDocument.VerificationStatus.FAILED);
        document.setStatus(VendorDocument.DocumentStatus.REJECTED);
        document.setVerifiedBy(rejectedBy);
        document.setVerifiedDate(LocalDateTime.now());
        document.setNotes(reason);
        
        return documentRepository.save(document);
    }

    @Transactional(readOnly = true)
    public Page<VendorDocument> getPendingVerificationDocuments(Pageable pageable) {
        return documentRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public List<VendorDocument> getExpiringDocuments(int daysThreshold) {
        LocalDateTime thresholdDate = LocalDateTime.now().plusDays(daysThreshold);
        return documentRepository.findExpiringDocuments(null, LocalDateTime.now(), thresholdDate);
    }

    @Transactional(readOnly = true)
    public List<VendorDocument> getExpiredDocuments() {
        return documentRepository.findExpiredDocuments(LocalDateTime.now());
    }

    public VendorDocument updateDocument(Long documentId, VendorDocument updatedDocument) {
        log.info("Updating document ID: {}", documentId);
        
        VendorDocument existingDocument = getDocument(documentId);
        
        existingDocument.setDocumentName(updatedDocument.getDocumentName());
        existingDocument.setDocumentType(updatedDocument.getDocumentType());
        existingDocument.setExpiryDate(updatedDocument.getExpiryDate());
        existingDocument.setNotes(updatedDocument.getNotes());
        
        return documentRepository.save(existingDocument);
    }

    public void deleteDocument(Long documentId) {
        log.info("Deleting document ID: {}", documentId);
        
        VendorDocument document = getDocument(documentId);
        
        try {
            Path filePath = Paths.get(document.getFilePath());
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.warn("Failed to delete physical file: {}", e.getMessage());
        }
        
        documentRepository.delete(document);
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getVendorDocumentStats(Long vendorId) {
        Long total = (long) documentRepository.findByVendorId(vendorId).size();
        Long verified = documentRepository.countVerifiedDocuments(vendorId);
        
        return Map.of(
            "total", total,
            "verified", verified != null ? verified : 0L,
            "pending", total - (verified != null ? verified : 0L)
        );
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getVerificationStats() {
        Long pending = (long) documentRepository.findByVerificationStatus(
            VendorDocument.VerificationStatus.NOT_VERIFIED).size();
        Long verified = (long) documentRepository.findByVerificationStatus(
            VendorDocument.VerificationStatus.VERIFIED).size();
        Long rejected = (long) documentRepository.findByVerificationStatus(
            VendorDocument.VerificationStatus.FAILED).size();
        
        return Map.of(
            "pending", pending,
            "verified", verified,
            "rejected", rejected,
            "total", pending + verified + rejected
        );
    }
}
