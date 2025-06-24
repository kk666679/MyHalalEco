package com.myhalal.eco.controller;

import com.myhalal.eco.entity.VendorDocument;
import com.myhalal.eco.service.VendorDocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vendor-documents")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class VendorDocumentController {

    private final VendorDocumentService documentService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("vendorId") Long vendorId,
            @RequestParam("documentType") String documentType) {
        try {
            VendorDocument document = documentService.uploadDocument(file, vendorId, documentType);
            return ResponseEntity.ok(document);
        } catch (RuntimeException e) {
            log.error("Error uploading document for vendor {}: {}", vendorId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getDocument(@PathVariable Long id) {
        try {
            VendorDocument document = documentService.getDocument(id);
            return ResponseEntity.ok(document);
        } catch (RuntimeException e) {
            log.error("Error retrieving document {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadDocument(@PathVariable Long id) {
        try {
            Resource resource = documentService.downloadDocument(id);
            VendorDocument document = documentService.getDocument(id);
            
            return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(document.getMimeType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                        "attachment; filename=\"" + document.getDocumentName() + "\"")
                .body(resource);
        } catch (RuntimeException e) {
            log.error("Error downloading document {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/vendor/{vendorId}")
    public ResponseEntity<List<VendorDocument>> getVendorDocuments(@PathVariable Long vendorId) {
        List<VendorDocument> documents = documentService.getVendorDocuments(vendorId);
        return ResponseEntity.ok(documents);
    }

    @GetMapping("/vendor/{vendorId}/type/{documentType}")
    public ResponseEntity<List<VendorDocument>> getVendorDocumentsByType(
            @PathVariable Long vendorId,
            @PathVariable String documentType) {
        List<VendorDocument> documents = documentService.getVendorDocumentsByType(vendorId, documentType);
        return ResponseEntity.ok(documents);
    }

    @PutMapping("/{id}/verify")
    public ResponseEntity<?> verifyDocument(
            @PathVariable Long id,
            @RequestParam String verifiedBy,
            @RequestParam(required = false) String notes) {
        try {
            VendorDocument document = documentService.verifyDocument(id, verifiedBy, notes);
            return ResponseEntity.ok(document);
        } catch (RuntimeException e) {
            log.error("Error verifying document {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<?> rejectDocument(
            @PathVariable Long id,
            @RequestParam String rejectedBy,
            @RequestParam String reason) {
        try {
            VendorDocument document = documentService.rejectDocument(id, rejectedBy, reason);
            return ResponseEntity.ok(document);
        } catch (RuntimeException e) {
            log.error("Error rejecting document {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/pending-verification")
    public ResponseEntity<Page<VendorDocument>> getPendingVerificationDocuments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        Page<VendorDocument> documents = documentService.getPendingVerificationDocuments(pageable);
        return ResponseEntity.ok(documents);
    }

    @GetMapping("/expiring")
    public ResponseEntity<List<VendorDocument>> getExpiringDocuments(
            @RequestParam(defaultValue = "30") int daysThreshold) {
        List<VendorDocument> documents = documentService.getExpiringDocuments(daysThreshold);
        return ResponseEntity.ok(documents);
    }

    @GetMapping("/expired")
    public ResponseEntity<List<VendorDocument>> getExpiredDocuments() {
        List<VendorDocument> documents = documentService.getExpiredDocuments();
        return ResponseEntity.ok(documents);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateDocument(
            @PathVariable Long id,
            @Valid @RequestBody VendorDocument document) {
        try {
            VendorDocument updatedDocument = documentService.updateDocument(id, document);
            return ResponseEntity.ok(updatedDocument);
        } catch (RuntimeException e) {
            log.error("Error updating document {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDocument(@PathVariable Long id) {
        try {
            documentService.deleteDocument(id);
            return ResponseEntity.ok(Map.of("message", "Document deleted successfully"));
        } catch (RuntimeException e) {
            log.error("Error deleting document {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/stats/vendor/{vendorId}")
    public ResponseEntity<Map<String, Long>> getVendorDocumentStats(@PathVariable Long vendorId) {
        Map<String, Long> stats = documentService.getVendorDocumentStats(vendorId);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/stats/verification")
    public ResponseEntity<Map<String, Long>> getVerificationStats() {
        Map<String, Long> stats = documentService.getVerificationStats();
        return ResponseEntity.ok(stats);
    }
}
