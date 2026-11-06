package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.Checkin
import com.example.data.model.DailyReport
import com.example.data.model.Guard
import com.example.data.model.Incident
import com.example.data.model.Patrol
import com.example.data.model.Site
import com.example.data.model.Ticket
import com.example.data.model.TicketMessage
import com.example.data.repository.GuardViewRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

enum class GuardViewRole(val title: String, val subtitle: String) {
    CLIENT("Client Portal", "Live proof of presence & SLA tracking"),
    GUARD("Guard Check-in", "GPS geofence, selfie & QR patrol rounds"),
    ADMIN("Admin & Supervisor", "Multi-site live map, alerts & roster"),
    WHATSAPP_BOT("WhatsApp Bot", "Daily 6pm summary & 2-way bot chat")
}

data class QueuedAction(
    val id: String = System.currentTimeMillis().toString(),
    val description: String,
    val timestamp: String = "Just now",
    val execute: suspend () -> Unit
)

data class WhatsAppMessage(
    val sender: String, // "user" or "bot"
    val text: String,
    val time: String,
    val isRead: Boolean = true
)

class GuardViewViewModel(
    private val repository: GuardViewRepository,
    val currentUserId: String = "user_demo"
) : ViewModel() {

    private val _currentRole = MutableStateFlow(GuardViewRole.CLIENT)
    val currentRole: StateFlow<GuardViewRole> = _currentRole.asStateFlow()

    private val _selectedSiteId = MutableStateFlow<String?>("site_metro")
    val selectedSiteId: StateFlow<String?> = _selectedSiteId.asStateFlow()

    private val _isOnline = MutableStateFlow(true)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private val _offlineQueue = MutableStateFlow<List<QueuedAction>>(emptyList())
    val offlineQueue: StateFlow<List<QueuedAction>> = _offlineQueue.asStateFlow()

    private val _whatsAppMessages = MutableStateFlow<List<WhatsAppMessage>>(
        listOf(
            WhatsAppMessage(
                sender = "bot",
                text = "🛡️ GuardView Daily Report - Metro Plaza Complex\n📅 Date: Today | Status: ✅ All clear\n👮 Guard: Marcus Vance (GV-8402)\n⏰ Clock-in: 06:00 UTC | Clock-out: On duty\n🚶 Patrols completed: 5 / 6 scheduled\n⚠️ Incidents today: 0\n🔗 Live portal: guardview.io/portal/metro",
                time = "18:00"
            )
        )
    )
    val whatsAppMessages: StateFlow<List<WhatsAppMessage>> = _whatsAppMessages.asStateFlow()

    private val _userFeedback = MutableStateFlow<String?>(null)
    val userFeedback: StateFlow<String?> = _userFeedback.asStateFlow()

    val sitesState: StateFlow<UiState<List<Site>>> = repository.observeSites()
        .map<List<Site>, UiState<List<Site>>> { UiState.Success(it) }
        .catch { emit(UiState.Error(it.message ?: "Failed to load sites")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    val guardsState: StateFlow<UiState<List<Guard>>> = repository.observeGuards()
        .map<List<Guard>, UiState<List<Guard>>> { UiState.Success(it) }
        .catch { emit(UiState.Error(it.message ?: "Failed to load guards")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    val checkinsState: StateFlow<UiState<List<Checkin>>> = repository.observeCheckins()
        .map<List<Checkin>, UiState<List<Checkin>>> { UiState.Success(it) }
        .catch { emit(UiState.Error(it.message ?: "Failed to load checkins")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    val patrolsState: StateFlow<UiState<List<Patrol>>> = repository.observePatrols()
        .map<List<Patrol>, UiState<List<Patrol>>> { UiState.Success(it) }
        .catch { emit(UiState.Error(it.message ?: "Failed to load patrols")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    val incidentsState: StateFlow<UiState<List<Incident>>> = repository.observeIncidents()
        .map<List<Incident>, UiState<List<Incident>>> { UiState.Success(it) }
        .catch { emit(UiState.Error(it.message ?: "Failed to load incidents")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    val ticketsState: StateFlow<UiState<List<Ticket>>> = repository.observeTickets()
        .map<List<Ticket>, UiState<List<Ticket>>> { UiState.Success(it) }
        .catch { emit(UiState.Error(it.message ?: "Failed to load tickets")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    val dailyReportsState: StateFlow<UiState<List<DailyReport>>> = repository.observeDailyReports()
        .map<List<DailyReport>, UiState<List<DailyReport>>> { UiState.Success(it) }
        .catch { emit(UiState.Error(it.message ?: "Failed to load daily reports")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UiState.Loading)

    init {
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
        }
    }

    fun selectRole(role: GuardViewRole) {
        _currentRole.value = role
    }

    fun selectSite(siteId: String) {
        _selectedSiteId.value = siteId
    }

    fun toggleOnlineStatus() {
        _isOnline.value = !_isOnline.value
        if (_isOnline.value) {
            syncOfflineQueue()
        }
    }

    fun clearFeedback() {
        _userFeedback.value = null
    }

    fun showFeedback(msg: String) {
        _userFeedback.value = msg
    }

    // --- GUARD ACTIONS ---
    fun clockIn(
        guardId: String = "guard_1",
        guardName: String = "Marcus Vance",
        siteId: String = "site_metro",
        siteName: String = "Metro Plaza Complex",
        lat: Double = 37.7749,
        lng: Double = -122.4194,
        isWithinGeofence: Boolean = true,
        selfieUrl: String = "selfie_verified"
    ) {
        if (!_isOnline.value) {
            val action = QueuedAction(description = "Clock-in for $guardName at $siteName") {
                repository.clockIn(guardId, guardName, siteId, siteName, lat, lng, isWithinGeofence, selfieUrl)
            }
            _offlineQueue.value = _offlineQueue.value + action
            showFeedback("Offline: Clock-in queued. Will sync when back online.")
            return
        }

        viewModelScope.launch {
            try {
                repository.clockIn(guardId, guardName, siteId, siteName, lat, lng, isWithinGeofence, selfieUrl)
                showFeedback("✅ Verified Clock-in recorded! GPS & Selfie timestamped.")
            } catch (e: Exception) {
                showFeedback("Error clocking in: ${e.message}")
            }
        }
    }

    fun clockOut(
        guardId: String = "guard_1",
        guardName: String = "Marcus Vance",
        siteId: String = "site_metro",
        siteName: String = "Metro Plaza Complex",
        lat: Double = 37.7749,
        lng: Double = -122.4194
    ) {
        if (!_isOnline.value) {
            val action = QueuedAction(description = "Clock-out for $guardName at $siteName") {
                repository.clockOut(guardId, guardName, siteId, siteName, lat, lng)
            }
            _offlineQueue.value = _offlineQueue.value + action
            showFeedback("Offline: Clock-out queued.")
            return
        }

        viewModelScope.launch {
            try {
                repository.clockOut(guardId, guardName, siteId, siteName, lat, lng)
                showFeedback("Clock-out recorded. Shift concluded.")
            } catch (e: Exception) {
                showFeedback("Error clocking out: ${e.message}")
            }
        }
    }

    fun logPatrol(
        siteId: String = "site_metro",
        guardId: String = "guard_1",
        guardName: String = "Marcus Vance",
        scannedCount: Long = 4L,
        totalCheckpoints: Long = 4L,
        notes: String = "All checkpoints validated via QR."
    ) {
        if (!_isOnline.value) {
            val action = QueuedAction(description = "Patrol round ($scannedCount/$totalCheckpoints)") {
                repository.logPatrol(siteId, guardId, guardName, scannedCount, totalCheckpoints, notes)
            }
            _offlineQueue.value = _offlineQueue.value + action
            showFeedback("Offline: Patrol round queued.")
            return
        }

        viewModelScope.launch {
            try {
                repository.logPatrol(siteId, guardId, guardName, scannedCount, totalCheckpoints, notes)
                showFeedback("✅ Patrol round completed! 4 of 4 checkpoints verified.")
            } catch (e: Exception) {
                showFeedback("Error logging patrol: ${e.message}")
            }
        }
    }

    fun reportIncident(
        siteId: String = "site_metro",
        siteName: String = "Metro Plaza Complex",
        guardId: String = "guard_1",
        guardName: String = "Marcus Vance",
        category: String,
        severity: String,
        description: String,
        photoUrl: String = ""
    ) {
        if (!_isOnline.value) {
            val action = QueuedAction(description = "Incident: $category ($severity)") {
                repository.reportIncident(siteId, siteName, guardId, guardName, category, severity, description, photoUrl)
            }
            _offlineQueue.value = _offlineQueue.value + action
            showFeedback("Offline: Incident report queued.")
            return
        }

        viewModelScope.launch {
            try {
                repository.reportIncident(siteId, siteName, guardId, guardName, category, severity, description, photoUrl)
                showFeedback("🚨 Incident reported & dispatched to supervisor!")
            } catch (e: Exception) {
                showFeedback("Error reporting incident: ${e.message}")
            }
        }
    }

    fun syncOfflineQueue() {
        val queue = _offlineQueue.value
        if (queue.isEmpty()) return
        viewModelScope.launch {
            val remaining = queue.toMutableList()
            for (action in queue) {
                try {
                    action.execute()
                    remaining.remove(action)
                } catch (e: Exception) {
                    Log.e("GuardViewVM", "Error syncing queued action: ${e.message}")
                }
            }
            _offlineQueue.value = remaining
            showFeedback("Synced ${queue.size - remaining.size} queued items to GuardView cloud.")
        }
    }

    // --- TICKET ACTIONS ---
    fun createTicket(
        clientId: String = "client_metro",
        siteId: String = "site_metro",
        siteName: String = "Metro Plaza Complex",
        category: String,
        description: String,
        supervisorName: String = "Capt. David Ramos",
        autoEvidenceText: String = ""
    ) {
        viewModelScope.launch {
            try {
                repository.createTicket(clientId, siteId, siteName, category, description, supervisorName, autoEvidenceText)
                showFeedback("Ticket created with 30-min SLA timer assigned to $supervisorName.")
            } catch (e: Exception) {
                showFeedback("Error creating ticket: ${e.message}")
            }
        }
    }

    fun updateTicketStatus(ticketId: String, status: String, rating: Long = 0L) {
        viewModelScope.launch {
            try {
                repository.updateTicketStatus(ticketId, status, rating)
                showFeedback("Ticket updated: $status")
            } catch (e: Exception) {
                showFeedback("Error updating ticket: ${e.message}")
            }
        }
    }

    fun sendTicketMessage(ticketId: String, senderName: String, senderRole: String, message: String) {
        viewModelScope.launch {
            try {
                repository.sendTicketMessage(ticketId, currentUserId, senderName, senderRole, message)
            } catch (e: Exception) {
                showFeedback("Error sending message: ${e.message}")
            }
        }
    }

    // --- WHATSAPP BOT ACTIONS ---
    fun sendWhatsAppCommand(input: String) {
        val trimmed = input.trim()
        val userMsg = WhatsAppMessage(sender = "user", text = trimmed, time = "Now")
        _whatsAppMessages.value = _whatsAppMessages.value + userMsg

        val commandUpper = trimmed.uppercase()
        val botReply = when {
            commandUpper == "STATUS" -> {
                "🛡️ GuardView Live Status\n📍 Site: Metro Plaza Complex\n🟢 Status: Guard ON SITE\n👤 Guard: Marcus Vance (GV-8402)\n⏱️ On duty: 2 hrs 35 mins\n🚶 Last patrol: 18 mins ago (All clear)\n🔗 Live Portal: guardview.io/live"
            }
            commandUpper == "COMPLAINT" -> {
                "📝 Guided Ticket Flow:\nPlease reply with your issue:\n1 - Guard absent or late\n2 - Patrol missed\n3 - Uniform / Conduct\n4 - Gate / Equipment issue\n(Or type description directly to create ticket)"
            }
            commandUpper == "REPORT" -> {
                "📊 Daily Summary Re-sent:\nMetro Plaza: 5/6 patrols completed, 0 active incidents. Overall attendance 100%. Supervisor: Capt. Ramos."
            }
            commandUpper.startsWith("1") || commandUpper.contains("ABSENT") -> {
                viewModelScope.launch {
                    createTicket(
                        category = "guard_absent",
                        description = "WhatsApp Bot created complaint: Guard reported absent/late",
                        autoEvidenceText = "Auto-attached via WhatsApp bot intake."
                    )
                }
                "✅ Ticket #GV-${System.currentTimeMillis().toString().takeLast(4)} created! Assigned to Capt. David Ramos. 30-min first response SLA active."
            }
            else -> {
                "👋 Hello from GuardView Security Bot!\nSend:\n• STATUS - View live guard presence\n• COMPLAINT - Report an issue instantly\n• REPORT - Get today's patrol & incident summary"
            }
        }

        viewModelScope.launch {
            kotlinx.coroutines.delay(600) // Realistic conversational latency
            _whatsAppMessages.value = _whatsAppMessages.value + WhatsAppMessage(
                sender = "bot",
                text = botReply,
                time = "Now"
            )
        }
    }
}
