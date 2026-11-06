package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.error.OperationType
import com.example.data.error.handleFirestoreError
import com.example.data.model.Checkin
import com.example.data.model.DailyReport
import com.example.data.model.Guard
import com.example.data.model.Incident
import com.example.data.model.Patrol
import com.example.data.model.Site
import com.example.data.model.Ticket
import com.example.data.model.TicketMessage
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

class GuardViewRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid ?: "anonymous_guard_user"
    }

    // --- SITES ---
    fun observeSites(): Flow<List<Site>> =
        db.collection("sites")
            .snapshots()
            .map { it.toObjects(Site::class.java) }
            .catch { e ->
                if (e is Exception) handleFirestoreError(e, OperationType.LIST, "sites")
                throw e
            }

    suspend fun getSite(siteId: String): Site? =
        db.collection("sites").document(siteId).get().await().toObject(Site::class.java)

    suspend fun updateSiteStatus(siteId: String, status: String) {
        val ref = db.collection("sites").document(siteId)
        try {
            ref.update("currentStatus", status).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, ref.path)
            throw e
        }
    }

    // --- GUARDS ---
    fun observeGuards(): Flow<List<Guard>> =
        db.collection("guards")
            .snapshots()
            .map { it.toObjects(Guard::class.java) }
            .catch { e ->
                if (e is Exception) handleFirestoreError(e, OperationType.LIST, "guards")
                throw e
            }

    // --- CHECKINS ---
    fun observeCheckins(): Flow<List<Checkin>> =
        db.collection("checkins")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(50)
            .snapshots()
            .map { it.toObjects(Checkin::class.java) }
            .catch { e ->
                if (e is Exception) handleFirestoreError(e, OperationType.LIST, "checkins")
                throw e
            }

    suspend fun clockIn(
        guardId: String,
        guardName: String,
        siteId: String,
        siteName: String,
        lat: Double,
        lng: Double,
        isWithinGeofence: Boolean,
        selfieUrl: String = "",
        lateMinutes: Long = 0L
    ): String {
        val checkinId = "chk_${System.currentTimeMillis()}"
        val docRef = db.collection("checkins").document(checkinId)
        val payload = mapOf(
            "guardId" to guardId,
            "guardName" to guardName,
            "siteId" to siteId,
            "siteName" to siteName,
            "type" to "clock_in",
            "lat" to lat,
            "lng" to lng,
            "isWithinGeofence" to isWithinGeofence,
            "selfieUrl" to selfieUrl,
            "lateMinutes" to lateMinutes,
            "createdAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            // Update site status to on_site
            db.collection("sites").document(siteId).update("currentStatus", "on_site").await()
            return checkinId
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun clockOut(
        guardId: String,
        guardName: String,
        siteId: String,
        siteName: String,
        lat: Double,
        lng: Double
    ): String {
        val checkinId = "chk_${System.currentTimeMillis()}"
        val docRef = db.collection("checkins").document(checkinId)
        val payload = mapOf(
            "guardId" to guardId,
            "guardName" to guardName,
            "siteId" to siteId,
            "siteName" to siteName,
            "type" to "clock_out",
            "lat" to lat,
            "lng" to lng,
            "isWithinGeofence" to true,
            "selfieUrl" to "",
            "lateMinutes" to 0L,
            "createdAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            db.collection("sites").document(siteId).update("currentStatus", "not_arrived").await()
            return checkinId
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    // --- PATROLS ---
    fun observePatrols(siteId: String? = null): Flow<List<Patrol>> {
        val query = if (siteId != null) {
            db.collection("patrols").whereEqualTo("siteId", siteId)
        } else {
            db.collection("patrols")
        }
        return query.orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(30)
            .snapshots()
            .map { it.toObjects(Patrol::class.java) }
            .catch { e ->
                if (e is Exception) handleFirestoreError(e, OperationType.LIST, "patrols")
                throw e
            }
    }

    suspend fun logPatrol(
        siteId: String,
        guardId: String,
        guardName: String,
        checkpointsScanned: Long,
        totalCheckpoints: Long,
        notes: String
    ): String {
        val patrolId = "patrol_${System.currentTimeMillis()}"
        val docRef = db.collection("patrols").document(patrolId)
        val payload = mapOf(
            "siteId" to siteId,
            "guardId" to guardId,
            "guardName" to guardName,
            "checkpointsScannedCount" to checkpointsScanned,
            "totalCheckpoints" to totalCheckpoints,
            "status" to "completed",
            "notes" to notes,
            "createdAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            return patrolId
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    // --- INCIDENTS ---
    fun observeIncidents(siteId: String? = null): Flow<List<Incident>> {
        val query = if (siteId != null) {
            db.collection("incidents").whereEqualTo("siteId", siteId)
        } else {
            db.collection("incidents")
        }
        return query.orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(30)
            .snapshots()
            .map { it.toObjects(Incident::class.java) }
            .catch { e ->
                if (e is Exception) handleFirestoreError(e, OperationType.LIST, "incidents")
                throw e
            }
    }

    suspend fun reportIncident(
        siteId: String,
        siteName: String,
        guardId: String,
        guardName: String,
        category: String,
        severity: String,
        description: String,
        photoUrl: String = ""
    ): String {
        val incidentId = "inc_${System.currentTimeMillis()}"
        val docRef = db.collection("incidents").document(incidentId)
        val payload = mapOf(
            "siteId" to siteId,
            "siteName" to siteName,
            "guardId" to guardId,
            "guardName" to guardName,
            "category" to category,
            "severity" to severity,
            "description" to description,
            "photoUrl" to photoUrl,
            "resolved" to false,
            "createdAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            return incidentId
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    // --- TICKETS ---
    fun observeTickets(siteId: String? = null): Flow<List<Ticket>> {
        val query = if (siteId != null) {
            db.collection("tickets").whereEqualTo("siteId", siteId)
        } else {
            db.collection("tickets")
        }
        return query.orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(30)
            .snapshots()
            .map { it.toObjects(Ticket::class.java) }
            .catch { e ->
                if (e is Exception) handleFirestoreError(e, OperationType.LIST, "tickets")
                throw e
            }
    }

    suspend fun createTicket(
        clientId: String,
        siteId: String,
        siteName: String,
        category: String,
        description: String,
        supervisorName: String,
        autoEvidenceText: String = ""
    ): String {
        val ticketId = "tkt_${System.currentTimeMillis()}"
        val docRef = db.collection("tickets").document(ticketId)
        val payload = mapOf(
            "clientId" to clientId,
            "siteId" to siteId,
            "siteName" to siteName,
            "category" to category,
            "description" to description,
            "status" to "open",
            "supervisorName" to supervisorName,
            "firstResponseTargetMinutes" to 30L,
            "resolutionTargetMinutes" to 240L,
            "satisfactionRating" to 0L,
            "autoEvidenceText" to autoEvidenceText,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            return ticketId
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun updateTicketStatus(ticketId: String, status: String, rating: Long = 0L) {
        val docRef = db.collection("tickets").document(ticketId)
        val updates = mutableMapOf<String, Any>(
            "status" to status,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        if (rating > 0L) {
            updates["satisfactionRating"] = rating
        }
        try {
            docRef.update(updates).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    // --- TICKET MESSAGES ---
    fun observeTicketMessages(ticketId: String): Flow<List<TicketMessage>> =
        db.collection("tickets").document(ticketId).collection("messages")
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .snapshots()
            .map { it.toObjects(TicketMessage::class.java) }
            .catch { e ->
                if (e is Exception) handleFirestoreError(e, OperationType.LIST, "tickets/$ticketId/messages")
                throw e
            }

    suspend fun sendTicketMessage(
        ticketId: String,
        senderId: String,
        senderName: String,
        senderRole: String,
        message: String
    ): String {
        val msgId = "msg_${System.currentTimeMillis()}"
        val docRef = db.collection("tickets").document(ticketId).collection("messages").document(msgId)
        val payload = mapOf(
            "ticketId" to ticketId,
            "senderId" to senderId,
            "senderName" to senderName,
            "senderRole" to senderRole,
            "message" to message,
            "createdAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            return msgId
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    // --- DAILY REPORTS ---
    fun observeDailyReports(siteId: String? = null): Flow<List<DailyReport>> {
        val query = if (siteId != null) {
            db.collection("daily_reports").whereEqualTo("siteId", siteId)
        } else {
            db.collection("daily_reports")
        }
        return query.orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(20)
            .snapshots()
            .map { it.toObjects(DailyReport::class.java) }
            .catch { e ->
                if (e is Exception) handleFirestoreError(e, OperationType.LIST, "daily_reports")
                throw e
            }
    }

    suspend fun createDailyReport(
        siteId: String,
        siteName: String,
        clientId: String,
        date: String,
        guardName: String,
        clockInTime: String,
        patrolsCompleted: Long,
        patrolsRequired: Long,
        incidentsCount: Long,
        statusSummary: String
    ): String {
        val reportId = "rep_${System.currentTimeMillis()}"
        val docRef = db.collection("daily_reports").document(reportId)
        val payload = mapOf(
            "siteId" to siteId,
            "siteName" to siteName,
            "clientId" to clientId,
            "date" to date,
            "guardName" to guardName,
            "clockInTime" to clockInTime,
            "patrolsCompleted" to patrolsCompleted,
            "patrolsRequired" to patrolsRequired,
            "incidentsCount" to incidentsCount,
            "statusSummary" to statusSummary,
            "whatsappDeliveryStatus" to "delivered",
            "createdAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            return reportId
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    // --- SEED SAMPLE DATA ---
    suspend fun seedInitialDataIfEmpty() {
        try {
            val sitesSnapshot = db.collection("sites").limit(1).get().await()
            if (!sitesSnapshot.isEmpty) return

            // Seed Sites
            val metroSite = mapOf(
                "name" to "Metro Plaza Complex",
                "address" to "100 Commercial Way, Financial District",
                "clientId" to "client_metro",
                "geofenceLat" to 37.7749,
                "geofenceLng" to -122.4194,
                "geofenceRadiusMeters" to 150.0,
                "requiredPatrolsPerShift" to 6L,
                "supervisorName" to "Capt. David Ramos",
                "supervisorPhone" to "+1 555-019-2834",
                "currentStatus" to "on_site",
                "createdAt" to FieldValue.serverTimestamp()
            )
            db.collection("sites").document("site_metro").set(metroSite).await()

            val northpointSite = mapOf(
                "name" to "Northpoint Logistics Hub",
                "address" to "450 Industrial Parkway, Cargo Zone B",
                "clientId" to "client_logistics",
                "geofenceLat" to 37.7833,
                "geofenceLng" to -122.4167,
                "geofenceRadiusMeters" to 200.0,
                "requiredPatrolsPerShift" to 8L,
                "supervisorName" to "Lt. Elena Torres",
                "supervisorPhone" to "+1 555-019-7462",
                "currentStatus" to "on_site",
                "createdAt" to FieldValue.serverTimestamp()
            )
            db.collection("sites").document("site_northpoint").set(northpointSite).await()

            val apexSite = mapOf(
                "name" to "Apex Tower Executive Suites",
                "address" to "88 Skyline Blvd, Tech Corridor",
                "clientId" to "client_apex",
                "geofenceLat" to 37.7690,
                "geofenceLng" to -122.4467,
                "geofenceRadiusMeters" to 100.0,
                "requiredPatrolsPerShift" to 4L,
                "supervisorName" to "Capt. David Ramos",
                "supervisorPhone" to "+1 555-019-2834",
                "currentStatus" to "not_arrived",
                "createdAt" to FieldValue.serverTimestamp()
            )
            db.collection("sites").document("site_apex").set(apexSite).await()

            // Seed Guards
            val guards = listOf(
                mapOf(
                    "name" to "Marcus Vance",
                    "badgeNumber" to "GV-8402",
                    "phone" to "+1 555-234-8901",
                    "assignedSiteId" to "site_metro",
                    "status" to "on_duty",
                    "createdAt" to FieldValue.serverTimestamp()
                ),
                mapOf(
                    "name" to "Sarah Jenkins",
                    "badgeNumber" to "GV-9114",
                    "phone" to "+1 555-234-5542",
                    "assignedSiteId" to "site_northpoint",
                    "status" to "on_duty",
                    "createdAt" to FieldValue.serverTimestamp()
                ),
                mapOf(
                    "name" to "Kenji Sato",
                    "badgeNumber" to "GV-7230",
                    "phone" to "+1 555-234-1189",
                    "assignedSiteId" to "site_apex",
                    "status" to "off_duty",
                    "createdAt" to FieldValue.serverTimestamp()
                )
            )
            guards.forEachIndexed { i, g ->
                db.collection("guards").document("guard_${i + 1}").set(g).await()
            }

            // Seed Checkins
            db.collection("checkins").document("chk_seed_1").set(
                mapOf(
                    "guardId" to "guard_1",
                    "guardName" to "Marcus Vance",
                    "siteId" to "site_metro",
                    "siteName" to "Metro Plaza Complex",
                    "type" to "clock_in",
                    "lat" to 37.7749,
                    "lng" to -122.4194,
                    "isWithinGeofence" to true,
                    "lateMinutes" to 0L,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()

            // Seed Patrols
            db.collection("patrols").document("patrol_seed_1").set(
                mapOf(
                    "siteId" to "site_metro",
                    "guardId" to "guard_1",
                    "guardName" to "Marcus Vance",
                    "checkpointsScannedCount" to 4L,
                    "totalCheckpoints" to 4L,
                    "status" to "completed",
                    "notes" to "Perimeter scan complete. All fire exits secured.",
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()

            // Seed Incidents
            db.collection("incidents").document("inc_seed_1").set(
                mapOf(
                    "siteId" to "site_metro",
                    "siteName" to "Metro Plaza Complex",
                    "guardId" to "guard_1",
                    "guardName" to "Marcus Vance",
                    "category" to "equipment",
                    "severity" to "medium",
                    "description" to "East exterior gate hydraulic closer leaking fluid; latch operates slowly.",
                    "resolved" to false,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()

            // Seed Ticket
            val ticketId = "tkt_seed_1"
            db.collection("tickets").document(ticketId).set(
                mapOf(
                    "clientId" to "client_metro",
                    "siteId" to "site_metro",
                    "siteName" to "Metro Plaza Complex",
                    "category" to "uniform",
                    "description" to "Request confirmation of guard night shift rotation schedule for weekend.",
                    "status" to "in_progress",
                    "supervisorName" to "Capt. David Ramos",
                    "firstResponseTargetMinutes" to 30L,
                    "resolutionTargetMinutes" to 240L,
                    "satisfactionRating" to 0L,
                    "autoEvidenceText" to "Shift #104: Marcus Vance clocked in 06:00:12 UTC. All 4 patrols logged.",
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()

            // Seed Ticket Message
            db.collection("tickets").document(ticketId).collection("messages").document("msg_seed_1").set(
                mapOf(
                    "ticketId" to ticketId,
                    "senderId" to "sup_ramos",
                    "senderName" to "Capt. David Ramos",
                    "senderRole" to "supervisor",
                    "message" to "Hello Metro Plaza team. Marcus Vance is confirmed for the weekend rotation with relief officer Davis.",
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()

            // Seed Daily Report
            db.collection("daily_reports").document("rep_seed_1").set(
                mapOf(
                    "siteId" to "site_metro",
                    "siteName" to "Metro Plaza Complex",
                    "clientId" to "client_metro",
                    "date" to "Today",
                    "guardName" to "Marcus Vance",
                    "clockInTime" to "06:00 AM",
                    "patrolsCompleted" to 5L,
                    "patrolsRequired" to 6L,
                    "incidentsCount" to 1L,
                    "statusSummary" to "All clear - 1 minor equipment note recorded.",
                    "whatsappDeliveryStatus" to "delivered",
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()

        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, "seed")
        }
    }
}
