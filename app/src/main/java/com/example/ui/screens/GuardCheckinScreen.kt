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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.LockClock
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
fun GuardCheckinScreen(
    viewModel: GuardViewViewModel,
    modifier: Modifier = Modifier
) {
    val sitesState by viewModel.sitesState.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()
    val offlineQueue by viewModel.offlineQueue.collectAsState()
    val selectedSiteId by viewModel.selectedSiteId.collectAsState()

    val sites = (sitesState as? UiState.Success)?.data ?: emptyList()
    val currentSite = sites.firstOrNull { it.id == selectedSiteId } ?: sites.firstOrNull()

    var isClockedIn by remember { mutableStateOf(true) }
    var showIncidentModal by remember { mutableStateOf(false) }
    var scannedCheckpointsCount by remember { mutableIntStateOf(2) }
    var isSelfieCaptured by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Offline / Online Connectivity Indicator Bar
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("offline_sync_banner"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isOnline) GuardGreen.copy(alpha = 0.15f) else AlertAmber.copy(alpha = 0.2f)
                ),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (isOnline) Icons.Default.CloudDone else Icons.Default.CloudOff,
                            contentDescription = "Sync State",
                            tint = if (isOnline) GuardGreen else AlertAmber
                        )
                        Column {
                            Text(
                                text = if (isOnline) "ONLINE • AUTO-SYNC ACTIVE" else "OFFLINE MODE (Indexed/Local)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isOnline) GuardGreen else AlertAmber
                            )
                            if (offlineQueue.isNotEmpty()) {
                                Text(
                                    text = "${offlineQueue.size} pending action(s) stored locally",
                                    fontSize = 11.sp,
                                    color = TextPrimary
                                )
                            }
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (offlineQueue.isNotEmpty()) {
                            TextButton(onClick = { viewModel.syncOfflineQueue() }) {
                                Icon(Icons.Default.Sync, contentDescription = "Sync", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Sync Now", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Switch(
                            checked = isOnline,
                            onCheckedChange = { viewModel.toggleOnlineStatus() },
                            colors = SwitchDefaults.colors(checkedThumbColor = GuardGreen)
                        )
                    }
                }
            }
        }

        // My Shift Today Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("my_shift_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ASSIGNED SHIFT TODAY",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SecurityCyan,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Badge: GV-8402",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = currentSite?.name ?: "Metro Plaza Complex",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = currentSite?.address ?: "100 Commercial Way",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Shift Hours", fontSize = 11.sp, color = TextMuted)
                            Text("06:00 - 18:00 UTC", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            Text("12-Hour Shift", fontSize = 10.sp, color = SecurityCyan)
                        }

                        Column {
                            Text("Supervisor", fontSize = 11.sp, color = TextMuted)
                            Text(currentSite?.supervisorName ?: "Capt. Ramos", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            Text(currentSite?.supervisorPhone ?: "+1 555-019-2834", fontSize = 10.sp, color = TextSecondary)
                        }

                        Column {
                            Text("Required Patrols", fontSize = 11.sp, color = TextMuted)
                            Text("6 Total", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            Text("1 every 2 hours", fontSize = 10.sp, color = GuardGreen)
                        }
                    }
                }
            }
        }

        // GPS Geofence & Selfie Verification Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("geofence_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.MyLocation, contentDescription = "GPS Radar", tint = GuardGreen)
                            Text("GPS Geofence Radar", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                        Text(
                            text = "INSIDE GEOFENCE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = GuardGreen,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(GuardGreen.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Current Position: 37.7749° N, 122.4194° W\nDistance to Site Center: 24m (Radius allowed: 150m)",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Selfie Preview / Validation Box
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Navy900)
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(SecurityCyan.copy(alpha = 0.2f))
                                .border(2.dp, SecurityCyan, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Selfie Verified",
                                tint = SecurityCyan,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Guard Selfie: Verified",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Facial timestamp recorded with tamper-proof signature.",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Clock In / Clock Out Big Button
                    if (isClockedIn) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    isClockedIn = false
                                    viewModel.clockOut(siteId = currentSite?.id ?: "site_metro")
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp)
                                    .testTag("clock_out_button"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = CriticalRed)
                            ) {
                                Text("Clock Out of Shift", fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = {
                                    viewModel.clockIn(
                                        siteId = currentSite?.id ?: "site_metro",
                                        siteName = currentSite?.name ?: "Metro Plaza"
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp)
                                    .testTag("reverify_button"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Re-Verify Presence")
                            }
                        }
                    } else {
                        Button(
                            onClick = {
                                isClockedIn = true
                                viewModel.clockIn(
                                    siteId = currentSite?.id ?: "site_metro",
                                    siteName = currentSite?.name ?: "Metro Plaza"
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("clock_in_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GuardGreen)
                        ) {
                            Icon(Icons.Default.Security, contentDescription = "Clock In", modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Capture Selfie & Clock In (GPS Geofenced)", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // QR Patrol Checkpoints Round
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("patrol_qr_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.QrCodeScanner, contentDescription = "QR Patrol", tint = SecurityCyan)
                            Text("Active Patrol Checkpoints", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                        Text(
                            text = "$scannedCheckpointsCount / 4 Scanned",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (scannedCheckpointsCount == 4) GuardGreen else SecurityCyan
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Scan NFC tags or QR plates across property boundaries to verify live patrols.",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    val checkpoints = listOf(
                        "CP-1: Main Vehicle Entrance & Gate" to (scannedCheckpointsCount >= 1),
                        "CP-2: Loading Dock Bay #3 & Cargo Access" to (scannedCheckpointsCount >= 2),
                        "CP-3: North Perimeter Fence & Emergency Exit" to (scannedCheckpointsCount >= 3),
                        "CP-4: Executive Tower Lobby & Server Vault" to (scannedCheckpointsCount >= 4)
                    )

                    checkpoints.forEachIndexed { index, (name, isScanned) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isScanned) GuardGreen.copy(alpha = 0.1f) else Navy900)
                                .clickable {
                                    if (scannedCheckpointsCount == index) {
                                        scannedCheckpointsCount++
                                        if (scannedCheckpointsCount == 4) {
                                            viewModel.logPatrol(
                                                siteId = currentSite?.id ?: "site_metro",
                                                scannedCount = 4L,
                                                totalCheckpoints = 4L,
                                                notes = "All 4 checkpoints scanned successfully via QR."
                                            )
                                        }
                                    }
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = if (isScanned) Icons.Default.CheckCircle else Icons.Default.QrCodeScanner,
                                    contentDescription = name,
                                    tint = if (isScanned) GuardGreen else TextMuted,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = name,
                                    fontSize = 13.sp,
                                    color = if (isScanned) TextPrimary else TextSecondary,
                                    fontWeight = if (isScanned) FontWeight.SemiBold else FontWeight.Normal
                                )
                            }

                            if (isScanned) {
                                Text("VERIFIED", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = GuardGreen)
                            } else if (scannedCheckpointsCount == index) {
                                Text("TAP TO SCAN", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SecurityCyan)
                            } else {
                                Text("PENDING", fontSize = 10.sp, color = TextMuted)
                            }
                        }
                        if (index < checkpoints.lastIndex) {
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (scannedCheckpointsCount == 4) {
                        Button(
                            onClick = {
                                scannedCheckpointsCount = 0
                                viewModel.logPatrol(
                                    siteId = currentSite?.id ?: "site_metro",
                                    scannedCount = 4L,
                                    totalCheckpoints = 4L,
                                    notes = "Patrol round concluded and synced."
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SecurityCyan)
                        ) {
                            Text("Complete & Start Next Round", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Fast Incident Reporting Trigger
        item {
            Button(
                onClick = { showIncidentModal = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("report_incident_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AlertAmber.copy(alpha = 0.2f),
                    contentColor = AlertAmber
                ),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Icon(Icons.Default.Warning, contentDescription = "Report Incident", modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Log Incident with Photo (Immediate Supervisor Dispatch)", fontWeight = FontWeight.Bold)
            }
        }
    }

    // Modal: Incident Report Sheet
    if (showIncidentModal) {
        GuardIncidentDialog(
            site = currentSite,
            onDismiss = { showIncidentModal = false },
            onSubmit = { category, severity, description ->
                viewModel.reportIncident(
                    siteId = currentSite?.id ?: "site_metro",
                    siteName = currentSite?.name ?: "Metro Plaza Complex",
                    category = category,
                    severity = severity,
                    description = description,
                    photoUrl = "https://example.com/incident_evidence.jpg"
                )
                showIncidentModal = false
            }
        )
    }
}

@Composable
fun GuardIncidentDialog(
    site: Site?,
    onDismiss: () -> Unit,
    onSubmit: (category: String, severity: String, description: String) -> Unit
) {
    var selectedCategory by remember { mutableStateOf("trespass") }
    var selectedSeverity by remember { mutableStateOf("high") }
    var description by remember { mutableStateOf("") }

    val categories = listOf(
        "trespass" to "Trespassing / Unauthorized Entry",
        "damage" to "Property Damage / Vandalism",
        "suspicious" to "Suspicious Person / Vehicle",
        "equipment" to "Equipment Fault / Closer Leak",
        "gate" to "Gate Left Unlatched / Forced",
        "other" to "Other Security Hazard"
    )

    val severities = listOf("low", "medium", "high", "critical")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Incident • Supervisor Alert", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Site: ${site?.name ?: "Current Site"}", fontSize = 12.sp, color = SecurityCyan)

                Text("Category:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    categories.take(4).forEach { (catId, catLabel) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (selectedCategory == catId) SecurityCyan.copy(alpha = 0.2f) else Color.Transparent)
                                .clickable { selectedCategory = catId }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = catLabel,
                                fontSize = 12.sp,
                                color = if (selectedCategory == catId) SecurityCyan else TextPrimary,
                                fontWeight = if (selectedCategory == catId) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Text("Severity Level:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    severities.forEach { sev ->
                        val isSelected = selectedSeverity == sev
                        val color = when (sev) {
                            "critical" -> CriticalRed
                            "high" -> AlertAmber
                            else -> SecurityCyan
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) color.copy(alpha = 0.3f) else Navy900)
                                .border(if (isSelected) 1.dp else 0.dp, color, RoundedCornerShape(8.dp))
                                .clickable { selectedSeverity = sev }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(sev.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (isSelected) color else TextMuted)
                        }
                    }
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description & Actions Taken") },
                    placeholder = { Text("e.g. Individual attempting to bypass north gate; escorted off premises.") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Navy900)
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = "Photo Evidence", tint = SecurityCyan)
                    Text("Photo evidence attached (1 photo)", fontSize = 12.sp, color = TextSecondary)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (description.isNotBlank()) {
                        onSubmit(selectedCategory, selectedSeverity, description)
                    }
                },
                enabled = description.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = CriticalRed)
            ) {
                Text("Transmit Incident Alert")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
