package com.myhalal.eco.controller;

import com.myhalal.eco.entity.VendorNotification;
import com.myhalal.eco.service.VendorNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vendor-notifications")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class VendorNotificationController {

    private final VendorNotificationService notificationService;

    @PostMapping
    public ResponseEntity<?> createNotification(@Valid @RequestBody VendorNotification notification) {
        try {
            VendorNotification createdNotification = notificationService.createNotification(notification);
            return ResponseEntity.ok(createdNotification);
        } catch (RuntimeException e) {
            log.error("Error creating notification: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getNotification(@PathVariable Long id) {
        try {
            VendorNotification notification = notificationService.getNotification(id);
            return ResponseEntity.ok(notification);
        } catch (RuntimeException e) {
            log.error("Error retrieving notification {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/vendor/{vendorId}")
    public ResponseEntity<Page<VendorNotification>> getVendorNotifications(
            @PathVariable Long vendorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                   Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<VendorNotification> notifications = notificationService.getVendorNotifications(vendorId, pageable);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/vendor/{vendorId}/unread")
    public ResponseEntity<List<VendorNotification>> getUnreadNotifications(@PathVariable Long vendorId) {
        List<VendorNotification> notifications = notificationService.getUnreadNotifications(vendorId);
        return ResponseEntity.ok(notifications);
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long id) {
        try {
            VendorNotification notification = notificationService.markAsRead(id);
            return ResponseEntity.ok(notification);
        } catch (RuntimeException e) {
            log.error("Error marking notification {} as read: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/vendor/{vendorId}/read-all")
    public ResponseEntity<?> markAllAsRead(@PathVariable Long vendorId) {
        try {
            notificationService.markAllAsRead(vendorId);
            return ResponseEntity.ok(Map.of("message", "All notifications marked as read"));
        } catch (RuntimeException e) {
            log.error("Error marking all notifications as read for vendor {}: {}", vendorId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/complete-action")
    public ResponseEntity<?> markActionCompleted(@PathVariable Long id) {
        try {
            VendorNotification notification = notificationService.markActionCompleted(id);
            return ResponseEntity.ok(notification);
        } catch (RuntimeException e) {
            log.error("Error marking action completed for notification {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/vendor/{vendorId}/pending-actions")
    public ResponseEntity<List<VendorNotification>> getPendingActionNotifications(@PathVariable Long vendorId) {
        List<VendorNotification> notifications = notificationService.getPendingActionNotifications(vendorId);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/vendor/{vendorId}/overdue-actions")
    public ResponseEntity<List<VendorNotification>> getOverdueActionNotifications(@PathVariable Long vendorId) {
        List<VendorNotification> notifications = notificationService.getOverdueActionNotifications(vendorId);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/vendor/{vendorId}/recent")
    public ResponseEntity<List<VendorNotification>> getRecentNotifications(
            @PathVariable Long vendorId,
            @RequestParam(defaultValue = "10") int limit) {
        List<VendorNotification> notifications = notificationService.getRecentNotifications(vendorId, limit);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/stats/vendor/{vendorId}")
    public ResponseEntity<Map<String, Long>> getNotificationStats(@PathVariable Long vendorId) {
        Map<String, Long> stats = notificationService.getNotificationStats(vendorId);
        return ResponseEntity.ok(stats);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNotification(@PathVariable Long id) {
        try {
            notificationService.deleteNotification(id);
            return ResponseEntity.ok(Map.of("message", "Notification deleted successfully"));
        } catch (RuntimeException e) {
            log.error("Error deleting notification {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
