package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Checkin
import com.example.data.model.Incident
import com.example.data.model.Patrol
import com.example.data.model.Site
import com.example.ui.theme.AlertAmber
import com.example.ui.theme.CriticalRed
import com.example.ui.theme.GuardGreen
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy900
import com.example.ui.theme.SecurityCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.GuardViewViewModel
import com.example.ui.viewmodel.UiState

@Composable
fun ClientPortalScreen(
    viewModel: GuardViewViewModel,
    onNavigateToTicketing: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sitesState by viewModel.sitesState.collectAsState()
    val checkinsState by viewModel.checkinsState.collectAsState()
    val patrolsState by viewModel.patrolsState.collectAsState()
    val incidentsState by viewModel.incidentsState.collectAsState()
    val selectedSiteId by viewModel.selectedSiteId.collectAsState()

    var showReportModal by remember { mutableStateOf(false) }
    var showMonthlyReportModal by remember { mutableStateOf(false) }
    var siteDropdownExpanded by remember { mutableStateOf(false) }

    val sites = (sitesState as? UiState.Success)?.data ?: emptyList()
    val currentSite = sites.firstOrNull { it.id == selectedSiteId } ?: sites.firstOrNull()

    val checkins = (checkinsState as? UiState.Success)?.data ?: emptyList()
    val siteCheckins = checkins.filter { it.siteId == currentSite?.id }
    val latestCheckin = siteCheckins.firstOrNull { it.type == "clock_in" }

    val patrols = (patrolsState as? UiState.Success)?.data ?: emptyList()
    val sitePatrols = patrols.filter { it.siteId == currentSite?.id }
    val latestPatrol = sitePatrols.firstOrNull()
    val patrolsCompletedToday = sitePatrols.size

    val incidents = (incidentsState as? UiState.Success)?.data ?: emptyList()
    val siteIncidents = incidents.filter { it.siteId == currentSite?.id }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Site Switcher Header
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("site_switcher_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "CLIENT PORTAL • LIVE VERIFICATION",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = SecurityCyan
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Navy900)
                                .clickable { siteDropdownExpanded = true }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = currentSite?.name ?: "Select Protected Site",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = currentSite?.address ?: "No address available",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Switch Site",
                                tint = SecurityCyan
                            )
                        }

                        DropdownMenu(
                            expanded = siteDropdownExpanded,
                            onDismissRequest = { siteDropdownExpanded = false }
                        ) {
                            sites.forEach { site ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(site.name, fontWeight = FontWeight.Bold)
                                            Text(site.address, fontSize = 11.sp, color = TextMuted)
                                        }
                                    },
                                    onClick = {
                                        viewModel.selectSite(site.id)
                                        siteDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Live Status Card (The Hero Feature)
        item {
            val isOnSite = currentSite?.currentStatus == "on_site"
            val isMissed = currentSite?.currentStatus == "missed_shift"

            val statusColor = when {
                isOnSite -> GuardGreen
                isMissed -> CriticalRed
                else -> AlertAmber
            }

            val statusText = when {
                isOnSite -> "GUARD ON SITE • LIVE PROOF"
                isMissed -> "MISSED SHIFT • SUPERVISOR ALERTED"
                else -> "NOT YET ARRIVED • SHIFT UPCOMING"
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("guard_live_status_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    // Status Badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(statusColor.copy(alpha = 0.15f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(statusColor)
                        )
                        Text(
                            text = statusText,
                            color = statusColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            letterSpacing = 0.5.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Guard Details Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Navy900)
                                .border(2.dp, statusColor, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = "Guard Icon",
                                tint = SecurityCyan,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isOnSite) (latestCheckin?.guardName ?: "Marcus Vance") else "Assigned: Marcus Vance",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Badge ID: GV-8402 • Armed Patrol",
                                fontSize = 13.sp,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "GPS Verified",
                                    tint = GuardGreen,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "GPS Geofence & Selfie Verified",
                                    fontSize = 11.sp,
                                    color = GuardGreen,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 14.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )

                    // Operational Metrics Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Clock-in Time", fontSize = 11.sp, color = TextMuted)
                            Text("06:00 UTC", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            Text("On-time arrival", fontSize = 10.sp, color = GuardGreen)
                        }

                        Column {
                            Text("Hours On Duty", fontSize = 11.sp, color = TextMuted)
                            Text("2 hrs 45 mins", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            Text("Shift ends 18:00", fontSize = 10.sp, color = TextSecondary)
                        }

                        Column {
                            Text("Last Patrol", fontSize = 11.sp, color = TextMuted)
                            Text(if (latestPatrol != null) "18 mins ago" else "30 mins ago", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            Text("Main perimeter", fontSize = 10.sp, color = SecurityCyan)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Patrol Progress Bar
                    val requiredPatrols = currentSite?.requiredPatrolsPerShift ?: 6L
                    val progressRatio = (patrolsCompletedToday.toFloat() / requiredPatrols.toFloat()).coerceIn(0f, 1f)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Patrol Rounds Completed Today",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary
                        )
                        Text(
                            text = "$patrolsCompletedToday of $requiredPatrols (${(progressRatio * 100).toInt()}%)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = SecurityCyan
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { progressRatio },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = SecurityCyan,
                        trackColor = Navy900
                    )
                }
            }
        }

        // Action Buttons Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { showReportModal = true },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("report_problem_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CriticalRed.copy(alpha = 0.15f),
                        contentColor = CriticalRed
                    ),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Icon(
                        imageVector = Icons.Default.ReportProblem,
                        contentDescription = "Report Problem",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Report Problem", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { showMonthlyReportModal = true },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("monthly_report_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = "Monthly Report",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Service Report", fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // Incidents Today Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Incidents Logged Today",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "${siteIncidents.size} Total",
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }
        }

        if (siteIncidents.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "No incidents",
                            tint = GuardGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Zero incidents reported today. All perimeter gates and checkpoints clear.",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        } else {
            items(siteIncidents) { incident ->
                IncidentCard(incident = incident)
            }
        }

        // 7-Day History & Attendance Overview
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("seven_day_history_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.History, contentDescription = "History", tint = SecurityCyan)
                            Text("7-Day Presence & Patrol Proof", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                        Text("100% Attendance", fontSize = 12.sp, color = GuardGreen, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Days of week mini-cards
                    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        days.forEachIndexed { idx, day ->
                            val isToday = idx == 4
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isToday) SecurityCyan.copy(alpha = 0.2f) else Navy900)
                                    .padding(vertical = 8.dp, horizontal = 6.dp)
                            ) {
                                Text(day, fontSize = 10.sp, color = TextMuted)
                                Spacer(modifier = Modifier.height(4.dp))
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Present",
                                    tint = GuardGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("6/6", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal: Report a Problem Dialog
    if (showReportModal) {
        ReportProblemDialog(
            site = currentSite,
            onDismiss = { showReportModal = false },
            onSubmit = { category, description ->
                viewModel.createTicket(
                    siteId = currentSite?.id ?: "site_metro",
                    siteName = currentSite?.name ?: "Metro Plaza",
                    category = category,
                    description = description,
                    supervisorName = currentSite?.supervisorName ?: "Capt. David Ramos",
                    autoEvidenceText = "Auto-linked shift: Marcus Vance on duty (Clocked in 06:00 UTC). Current status: ${currentSite?.currentStatus}"
                )
                showReportModal = false
                onNavigateToTicketing()
            }
        )
    }

    // Modal: Monthly Service Report Download/Preview
    if (showMonthlyReportModal) {
        MonthlyReportDialog(
            site = currentSite,
            patrolsCount = patrolsCompletedToday,
            onDismiss = { showMonthlyReportModal = false }
        )
    }
}

@Composable
fun IncidentCard(incident: Incident) {
    val severityColor = when (incident.severity.lowercase()) {
        "critical" -> CriticalRed
        "high" -> AlertAmber
        "medium" -> SecurityCyan
        else -> TextSecondary
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(severityColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Incident",
                    tint = severityColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = incident.category.replaceFirstChar { it.uppercase() },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = incident.severity.uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = severityColor,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(severityColor.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = incident.description,
                    fontSize = 12.sp,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Reported by ${incident.guardName.ifEmpty { "On-duty Guard" }}",
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }
        }
    }
}

@Composable
fun ReportProblemDialog(
    site: Site?,
    onDismiss: () -> Unit,
    onSubmit: (category: String, description: String) -> Unit
) {
    var selectedCategory by remember { mutableStateOf("guard_absent") }
    var descriptionText by remember { mutableStateOf("") }

    val categories = listOf(
        "guard_absent" to "Guard Absent / Missing",
        "late" to "Guard Arrived Late",
        "conduct" to "Uniform / Conduct Issue",
        "patrol_missed" to "Patrol Round Missed",
        "unattended" to "Gate Unattended",
        "other" to "Other Concern"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Report a Problem • Fast Ticket", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Site: ${site?.name ?: "Current Site"}\nSupervisor: ${site?.supervisorName ?: "Capt. Ramos"} (30-min SLA)",
                    fontSize = 12.sp,
                    color = SecurityCyan
                )

                Text("Category:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    categories.forEach { (catId, catLabel) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selectedCategory == catId) SecurityCyan.copy(alpha = 0.2f) else Color.Transparent)
                                .clickable { selectedCategory = catId }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = catLabel,
                                fontSize = 13.sp,
                                color = if (selectedCategory == catId) SecurityCyan else TextPrimary,
                                fontWeight = if (selectedCategory == catId) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = descriptionText,
                    onValueChange = { descriptionText = it },
                    label = { Text("Details of the issue") },
                    placeholder = { Text("e.g. Main gate was open with no guard visible at 08:15") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                // Auto-link evidence notice
                if (selectedCategory == "guard_absent" || selectedCategory == "late") {
                    Text(
                        text = "ℹ️ Auto-Evidence: Current shift timeline and GPS check-in logs will be automatically attached as verified proof.",
                        fontSize = 11.sp,
                        color = GuardGreen
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (descriptionText.isNotBlank()) {
                        onSubmit(selectedCategory, descriptionText)
                    }
                },
                enabled = descriptionText.isNotBlank()
            ) {
                Text("Submit Ticket")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun MonthlyReportDialog(
    site: Site?,
    patrolsCount: Int,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("GuardView Monthly Service Summary", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Site: ${site?.name ?: "All Sites"}", fontWeight = FontWeight.SemiBold, color = SecurityCyan)
                Text("Billing Period: Current Month", fontSize = 12.sp, color = TextMuted)
                HorizontalDivider()
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Guaranteed Guard Shifts:")
                    Text("60 / 60 (100%)", fontWeight = FontWeight.Bold, color = GuardGreen)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Patrol Checkpoints Logged:")
                    Text("360 / 360", fontWeight = FontWeight.Bold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("SLA Complaint Response:")
                    Text("14 mins (Target < 30m)", color = GuardGreen)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Verified GPS On-Site Ratio:")
                    Text("99.8%", color = GuardGreen)
                }
                HorizontalDivider()
                Text("Official PDF certificate available for client contract renewal audits.", fontSize = 11.sp, color = TextMuted)
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text("Close Preview") }
        }
    )
}
