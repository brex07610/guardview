package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class Site(
    @DocumentId val id: String = "",
    val name: String = "",
    val address: String = "",
    val clientId: String = "",
    val geofenceLat: Double = 37.7749,
    val geofenceLng: Double = -122.4194,
    val geofenceRadiusMeters: Double = 150.0,
    val requiredPatrolsPerShift: Long = 6L,
    val supervisorName: String = "",
    val supervisorPhone: String = "",
    val currentStatus: String = "on_site", // on_site, not_arrived, missed_shift
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Guard(
    @DocumentId val id: String = "",
    val userId: String = "",
    val name: String = "",
    val badgeNumber: String = "",
    val phone: String = "",
    val assignedSiteId: String = "",
    val avatarUrl: String = "",
    val status: String = "on_duty", // on_duty, off_duty
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Checkin(
    @DocumentId val id: String = "",
    val guardId: String = "",
    val guardName: String = "",
    val siteId: String = "",
    val siteName: String = "",
    val type: String = "clock_in", // clock_in, clock_out
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val isWithinGeofence: Boolean = true,
    val selfieUrl: String = "",
    val lateMinutes: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Patrol(
    @DocumentId val id: String = "",
    val siteId: String = "",
    val guardId: String = "",
    val guardName: String = "",
    val checkpointsScannedCount: Long = 0L,
    val totalCheckpoints: Long = 4L,
    val status: String = "completed", // in_progress, completed
    val notes: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Incident(
    @DocumentId val id: String = "",
    val siteId: String = "",
    val siteName: String = "",
    val guardId: String = "",
    val guardName: String = "",
    val category: String = "other", // trespass, damage, suspicious, equipment, gate, other
    val severity: String = "medium", // low, medium, high, critical
    val description: String = "",
    val photoUrl: String = "",
    val resolved: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Ticket(
    @DocumentId val id: String = "",
    val clientId: String = "",
    val siteId: String = "",
    val siteName: String = "",
    val category: String = "other", // guard_absent, late, conduct, patrol_missed, unattended, other
    val description: String = "",
    val status: String = "open", // open, acknowledged, in_progress, resolved, closed
    val supervisorName: String = "",
    val firstResponseTargetMinutes: Long = 30L,
    val resolutionTargetMinutes: Long = 240L,
    val satisfactionRating: Long = 0L,
    val autoEvidenceText: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class TicketMessage(
    @DocumentId val id: String = "",
    val ticketId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val senderRole: String = "client", // client, supervisor, admin
    val message: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class DailyReport(
    @DocumentId val id: String = "",
    val siteId: String = "",
    val siteName: String = "",
    val clientId: String = "",
    val date: String = "",
    val guardName: String = "",
    val clockInTime: String = "",
    val patrolsCompleted: Long = 0L,
    val patrolsRequired: Long = 0L,
    val incidentsCount: Long = 0L,
    val statusSummary: String = "",
    val whatsappDeliveryStatus: String = "delivered", // delivered, sent, failed
    @ServerTimestamp val createdAt: Timestamp? = null
)
