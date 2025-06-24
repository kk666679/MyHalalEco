package com.myhalal.eco.service;

import com.myhalal.eco.entity.VendorNotification;
import com.myhalal.eco.entity.Vendor;
import com.myhalal.eco.repository.VendorNotificationRepository;
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
public class VendorNotificationService {

    private final VendorNotificationRepository notificationRepository;
    private final VendorRepository vendorRepository;

    @Transactional
    public VendorNotification createNotification(VendorNotification notification) {
        log.info("Creating notification for vendor ID: {}", notification.getVendor().getVendorId());
        
        Vendor vendor = vendorRepository.findById(notification.getVendor().getVendorId())
            .orElseThrow(() -> new RuntimeException("Vendor not found"));
        
        notification.setVendor(vendor);
        notification.setStatus(VendorNotification.NotificationStatus.UNREAD);
        notification.setCreatedDate(LocalDateTime.now());
        
        return notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public VendorNotification getNotification(Long notificationId) {
        return notificationRepository.findById(notificationId)
            .orElseThrow(() -> new RuntimeException("Notification not found"));
    }

    @Transactional(readOnly = true)
    public Page<VendorNotification> getVendorNotifications(Long vendorId, Pageable pageable) {
        return notificationRepository.findByVendorId(vendorId, pageable);
    }

    @Transactional(readOnly = true)
    public List<VendorNotification> getUnreadNotifications(Long vendorId) {
        return notificationRepository.findUnreadNotifications(vendorId);
    }

    @Transactional
    public VendorNotification markAsRead(Long notificationId) {
        log.info("Marking notification as read: {}", notificationId);
        
        notificationRepository.markAsRead(notificationId, LocalDateTime.now());
        return getNotification(notificationId);
    }

    @Transactional
    public void markAllAsRead(Long vendorId) {
        log.info("Marking all notifications as read for vendor: {}", vendorId);
        notificationRepository.markAllAsRead(vendorId, LocalDateTime.now());
    }

    @Transactional
    public VendorNotification markActionCompleted(Long notificationId) {
        log.info("Marking action completed for notification: {}", notificationId);
        
        notificationRepository.markActionCompleted(notificationId, LocalDateTime.now());
        return getNotification(notificationId);
    }

    @Transactional(readOnly = true)
    public List<VendorNotification> getPendingActionNotifications(Long vendorId) {
        return notificationRepository.findPendingActionNotifications(vendorId);
    }

    @Transactional(readOnly = true)
    public List<VendorNotification> getOverdueActionNotifications(Long vendorId) {
        return notificationRepository.findOverdueActionNotifications(vendorId, LocalDateTime.now());
    }

    @Transactional(readOnly = true)
    public List<VendorNotification> getRecentNotifications(Long vendorId, int limit) {
        return notificationRepository.findRecentNotifications(vendorId, limit);
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getNotificationStats(Long vendorId) {
        Map<String, Long> stats = new HashMap<>();
        
        Long unread = notificationRepository.countUnreadNotifications(vendorId);
        Long urgent = notificationRepository.countUrgentUnreadNotifications(vendorId);
        List<Object[]> typeDistribution = notificationRepository.getNotificationTypeDistribution(vendorId);
        
        stats.put("unread", unread != null ? unread : 0L);
        stats.put("urgent", urgent != null ? urgent : 0L);
        stats.put("total", unread != null ? unread : 0L);
        
        return stats;
    }

    @Transactional
    public void deleteNotification(Long notificationId) {
        log.info("Deleting notification: {}", notificationId);
        
        VendorNotification notification = getNotification(notificationId);
        notificationRepository.delete(notification);
    }
}
