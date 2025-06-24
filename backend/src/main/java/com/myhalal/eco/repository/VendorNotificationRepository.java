package com.myhalal.eco.repository;

import com.myhalal.eco.entity.VendorNotification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VendorNotificationRepository extends JpaRepository<VendorNotification, Long> {
    
    Page<VendorNotification> findByVendorId(Long vendorId, Pageable pageable);
    
    List<VendorNotification> findByVendorIdAndStatus(Long vendorId, VendorNotification.NotificationStatus status);
    
    List<VendorNotification> findByVendorIdAndPriority(Long vendorId, VendorNotification.Priority priority);
    
    @Query("SELECT n FROM VendorNotification n WHERE n.vendor.id = :vendorId " +
           "AND n.status = 'UNREAD' ORDER BY n.priority DESC, n.createdDate DESC")
    List<VendorNotification> findUnreadNotifications(@Param("vendorId") Long vendorId);
    
    @Query("SELECT n FROM VendorNotification n WHERE n.vendor.id = :vendorId " +
           "AND n.actionRequired = true AND n.actionCompleted = false")
    List<VendorNotification> findPendingActionNotifications(@Param("vendorId") Long vendorId);
    
    @Query("SELECT n FROM VendorNotification n WHERE n.vendor.id = :vendorId " +
           "AND n.actionDeadline <= :deadline AND n.actionCompleted = false")
    List<VendorNotification> findOverdueActionNotifications(
        @Param("vendorId") Long vendorId,
        @Param("deadline") LocalDateTime deadline
    );
    
    @Query("SELECT COUNT(n) FROM VendorNotification n WHERE n.vendor.id = :vendorId " +
           "AND n.status = 'UNREAD'")
    Long countUnreadNotifications(@Param("vendorId") Long vendorId);
    
    @Query("SELECT COUNT(n) FROM VendorNotification n WHERE n.vendor.id = :vendorId " +
           "AND n.priority = 'URGENT' AND n.status = 'UNREAD'")
    Long countUrgentUnreadNotifications(@Param("vendorId") Long vendorId);
    
    @Query("SELECT n FROM VendorNotification n WHERE n.vendor.id = :vendorId " +
           "AND n.type = :type ORDER BY n.createdDate DESC")
    List<VendorNotification> findByVendorIdAndType(
        @Param("vendorId") Long vendorId, 
        @Param("type") String type
    );
    
    @Query("SELECT n FROM VendorNotification n WHERE n.vendor.id = :vendorId " +
           "AND n.relatedEntityType = :entityType AND n.relatedEntityId = :entityId")
    List<VendorNotification> findByRelatedEntity(
        @Param("vendorId") Long vendorId,
        @Param("entityType") String entityType,
        @Param("entityId") Long entityId
    );
    
    @Query(value = "SELECT * FROM VENDOR_NOTIFICATIONS " +
           "WHERE VENDOR_ID = :vendorId " +
           "ORDER BY CREATED_DATE DESC " +
           "FETCH FIRST :limit ROWS ONLY", 
           nativeQuery = true)
    List<VendorNotification> findRecentNotifications(
        @Param("vendorId") Long vendorId, 
        @Param("limit") int limit
    );
    
    @Modifying
    @Query("UPDATE VendorNotification n SET n.status = 'READ', n.readDate = :readDate " +
           "WHERE n.id = :notificationId")
    void markAsRead(@Param("notificationId") Long notificationId, @Param("readDate") LocalDateTime readDate);
    
    @Modifying
    @Query("UPDATE VendorNotification n SET n.status = 'read', n.readDate = :readDate " +
           "WHERE n.vendor.id = :vendorId AND n.status = 'UNREAD'")
    void markAllAsRead(@Param("vendorId") Long vendorId, @Param("readDate") LocalDateTime readDate);
    
    @Modifying
    @Query("UPDATE VendorNotification n SET n.actionCompleted = true, " +
           "n.actionCompletedDate = :completedDate WHERE n.id = :notificationId")
    void markActionCompleted(
        @Param("notificationId") Long notificationId, 
        @Param("completedDate") LocalDateTime completedDate
    );
    
    @Query("SELECT n.type, COUNT(n) FROM VendorNotification n " +
           "WHERE n.vendor.id = :vendorId " +
           "GROUP BY n.type ORDER BY COUNT(n) DESC")
    List<Object[]> getNotificationTypeDistribution(@Param("vendorId") Long vendorId);
    
    @Query("SELECT n FROM VendorNotification n WHERE n.vendor.id = :vendorId " +
           "AND n.createdDate BETWEEN :startDate AND :endDate")
    List<VendorNotification> findNotificationsByDateRange(
        @Param("vendorId") Long vendorId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
}
