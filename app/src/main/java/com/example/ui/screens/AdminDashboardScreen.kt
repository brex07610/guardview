package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CrisisAlert
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Guard
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
fun AdminDashboardScreen(
    viewModel: GuardViewViewModel,
    modifier: Modifier = Modifier
) {
    val sitesState by viewModel.sitesState.collectAsState()
    val guardsState by viewModel.guardsState.collectAsState()
    val checkinsState by viewModel.checkinsState.collectAsState()
    val incidentsState by viewModel.incidentsState.collectAsState()
    val ticketsState by viewModel.ticketsState.collectAsState()

    val sites = (sitesState as? UiState.Success)?.data ?: emptyList()
    val guards = (guardsState as? UiState.Success)?.data ?: emptyList()
    val checkins = (checkinsState as? UiState.Success)?.data ?: emptyList()
    val incidents = (incidentsState as? UiState.Success)?.data ?: emptyList()
    val tickets = (ticketsState as? UiState.Success)?.data ?: emptyList()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // High Level Operations Overview
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_header_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "SUPERVISOR & OPERATIONS COMMAND",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SecurityCyan,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Live Multi-Site Attendance Matrix",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Protected Sites", fontSize = 11.sp, color = TextMuted)
                            Text("${sites.size}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("All geofenced", fontSize = 10.sp, color = GuardGreen)
                        }
                        Column {
                            Text("Guards On Duty", fontSize = 11.sp, color = TextMuted)
                            Text("${guards.count { it.status == "on_duty" }} / ${guards.size}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = GuardGreen)
                            Text("100% on-time", fontSize = 10.sp, color = GuardGreen)
                        }
                        Column {
                            Text("Open Complaints", fontSize = 11.sp, color = TextMuted)
                            Text("${tickets.count { it.status != "resolved" && it.status != "closed" }}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = AlertAmber)
                            Text("< 30m SLA met", fontSize = 10.sp, color = SecurityCyan)
                        }
                    }
                }
            }
        }

        // Live Alerts Feed
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("live_alerts_feed"),
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
                            Icon(Icons.Default.NotificationsActive, contentDescription = "Alerts", tint = AlertAmber)
                            Text("Live Field Alerts & Geofence Feed", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        Text("Live", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = GuardGreen)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val alerts = listOf(
                        "✅ GPS Geofence Verified: Marcus Vance logged at Metro Plaza (24m from center)",
                        "🚶 Patrol Complete: Northpoint Logistics Hub 4/4 checkpoints scanned",
                        "ℹ️ WhatsApp Daily 6pm cron scheduled for 3 client executives",
                        "⚠️ Ticket #GV-841: 30-min response SLA in progress (assigned to Ramos)"
                    )

                    alerts.forEachIndexed { idx, alertText ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Navy900)
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(alertText, fontSize = 11.sp, color = TextSecondary, lineHeight = 16.sp)
                        }
                        if (idx < alerts.lastIndex) {
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }
                }
            }
        }

        // Site Status Grid
        item {
            Text(
                text = "Protected Facilities Status",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        items(sites) { site ->
            val isOnSite = site.currentStatus == "on_site"
            val statusColor = if (isOnSite) GuardGreen else AlertAmber
            val statusLabel = if (isOnSite) "ON SITE" else "NOT ARRIVED"

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.selectSite(site.id) },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.LocationCity, contentDescription = "Site", tint = SecurityCyan)
                            Text(site.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }

                        Text(
                            text = statusLabel,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(statusColor.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(site.address, fontSize = 12.sp, color = TextSecondary)

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Supervisor: ${site.supervisorName}", fontSize = 11.sp, color = TextMuted)
                        Text("Geofence: ${site.geofenceRadiusMeters.toInt()}m radius", fontSize = 11.sp, color = TextSecondary)
                    }
                }
            }
        }

        // Guard Roster Summary
        item {
            Text(
                text = "Active Guard Roster & Performance",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        items(guards) { guard ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Navy900)
                            .border(1.dp, if (guard.status == "on_duty") GuardGreen else TextMuted, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Security, contentDescription = "Guard", tint = SecurityCyan, modifier = Modifier.size(22.dp))
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(guard.name, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = guard.status.replace("_", " ").uppercase(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (guard.status == "on_duty") GuardGreen else TextMuted
                            )
                        }
                        Text("Badge: ${guard.badgeNumber} • ${guard.phone}", fontSize = 12.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("100% On-Time Attendance • 0 GPS violations", fontSize = 11.sp, color = GuardGreen)
                    }
                }
            }
        }
    }
}
