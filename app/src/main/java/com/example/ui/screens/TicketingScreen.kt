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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
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
import com.example.data.model.Ticket
import com.example.data.model.TicketMessage
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
fun TicketingScreen(
    viewModel: GuardViewViewModel,
    modifier: Modifier = Modifier
) {
    val ticketsState by viewModel.ticketsState.collectAsState()
    val tickets = (ticketsState as? UiState.Success)?.data ?: emptyList()

    var selectedFilterTab by remember { mutableIntStateOf(0) }
    val filterTabs = listOf("All Tickets", "Open (SLA)", "In Progress", "Resolved")

    val filteredTickets = when (selectedFilterTab) {
        1 -> tickets.filter { it.status == "open" || it.status == "acknowledged" }
        2 -> tickets.filter { it.status == "in_progress" }
        3 -> tickets.filter { it.status == "resolved" || it.status == "closed" }
        else -> tickets
    }

    var selectedTicketForDetail by remember { mutableStateOf<Ticket?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Header summary
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .testTag("sla_summary_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "COMPLAINT TICKETING & SLA DISPATCH",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SecurityCyan,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "30-Min First Response • 4-Hr Resolution Guarantee",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Active Tickets", fontSize = 11.sp, color = TextMuted)
                            Text("${tickets.count { it.status != "resolved" && it.status != "closed" }}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AlertAmber)
                        }
                        Column {
                            Text("Avg Response", fontSize = 11.sp, color = TextMuted)
                            Text("14 mins", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = GuardGreen)
                        }
                        Column {
                            Text("SLA Compliance", fontSize = 11.sp, color = TextMuted)
                            Text("98.4%", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = GuardGreen)
                        }
                    }
                }
            }

            // Filter Tabs
            ScrollableTabRow(
                selectedTabIndex = selectedFilterTab,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = SecurityCyan,
                edgePadding = 16.dp
            ) {
                filterTabs.forEachIndexed { index, tabTitle ->
                    Tab(
                        selected = selectedFilterTab == index,
                        onClick = { selectedFilterTab = index },
                        text = {
                            Text(
                                text = tabTitle,
                                fontWeight = if (selectedFilterTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            // Ticket List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (filteredTickets.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No complaints found in this category.",
                                fontSize = 14.sp,
                                color = TextMuted
                            )
                        }
                    }
                } else {
                    items(filteredTickets) { ticket ->
                        TicketItemCard(
                            ticket = ticket,
                            onClick = { selectedTicketForDetail = ticket }
                        )
                    }
                }
                item { Spacer(modifier = Modifier.height(72.dp)) }
            }
        }

        // Floating Action Button to create ticket
        FloatingActionButton(
            onClick = { showCreateDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("create_ticket_fab"),
            containerColor = SecurityCyan,
            contentColor = Navy900
        ) {
            Icon(Icons.Default.Add, contentDescription = "Create Ticket")
        }
    }

    // Detail Dialog
    if (selectedTicketForDetail != null) {
        TicketDetailDialog(
            ticket = selectedTicketForDetail!!,
            viewModel = viewModel,
            onDismiss = { selectedTicketForDetail = null }
        )
    }

    // Create Dialog
    if (showCreateDialog) {
        CreateTicketDialog(
            viewModel = viewModel,
            onDismiss = { showCreateDialog = false }
        )
    }
}

@Composable
fun TicketItemCard(
    ticket: Ticket,
    onClick: () -> Unit
) {
    val statusColor = when (ticket.status.lowercase()) {
        "open" -> CriticalRed
        "acknowledged" -> AlertAmber
        "in_progress" -> SecurityCyan
        "resolved", "closed" -> GuardGreen
        else -> TextSecondary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("ticket_card_${ticket.id}"),
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
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "#${ticket.id.takeLast(6).uppercase()}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted
                    )
                    Text("•")
                    Text(
                        text = ticket.siteName.ifEmpty { "Metro Plaza" },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SecurityCyan
                    )
                }

                Text(
                    text = ticket.status.replace("_", " ").uppercase(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = statusColor,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(statusColor.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = ticket.category.replace("_", " ").replaceFirstChar { it.uppercase() },
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = ticket.description,
                fontSize = 13.sp,
                color = TextSecondary,
                maxLines = 2
            )

            // Auto-evidence banner if present
            if (ticket.autoEvidenceText.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(Navy900)
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.Link, contentDescription = "Auto Evidence", tint = GuardGreen, modifier = Modifier.size(14.dp))
                    Text(
                        text = "Auto-evidence: ${ticket.autoEvidenceText}",
                        fontSize = 11.sp,
                        color = GuardGreen,
                        maxLines = 1
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 10.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Default.Timer, contentDescription = "SLA Target", tint = AlertAmber, modifier = Modifier.size(14.dp))
                    Text(
                        text = "First Response: < 30m SLA",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }

                Text(
                    text = "Supervisor: ${ticket.supervisorName.ifEmpty { "Capt. Ramos" }}",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
fun TicketDetailDialog(
    ticket: Ticket,
    viewModel: GuardViewViewModel,
    onDismiss: () -> Unit
) {
    var messageText by remember { mutableStateOf("") }
    var rating by remember { mutableIntStateOf(ticket.satisfactionRating.toInt()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ticket #${ticket.id.takeLast(6).uppercase()}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = ticket.status.uppercase(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = SecurityCyan
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Category: ${ticket.category.replace('_', ' ').uppercase()}", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Text("Description:", fontSize = 11.sp, color = TextMuted)
                Text(ticket.description, fontSize = 13.sp)

                if (ticket.autoEvidenceText.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Navy900)
                            .padding(10.dp)
                    ) {
                        Column {
                            Text("Verified Shift Data Attached:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GuardGreen)
                            Text(ticket.autoEvidenceText, fontSize = 12.sp, color = TextPrimary)
                        }
                    }
                }

                HorizontalDivider()

                // Status Update Controls (Admin / Supervisor action)
                Text("Update Ticket Status:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val statusList = listOf("in_progress", "resolved", "closed")
                    statusList.forEach { s ->
                        OutlinedButton(
                            onClick = { viewModel.updateTicketStatus(ticket.id, s) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(s.replace("_", " ").take(6), fontSize = 10.sp)
                        }
                    }
                }

                // Satisfaction Rating (1-5 stars)
                if (ticket.status == "resolved" || ticket.status == "closed") {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Client Satisfaction Rating:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        (1..5).forEach { star ->
                            IconButton(
                                onClick = {
                                    rating = star
                                    viewModel.updateTicketStatus(ticket.id, ticket.status, star.toLong())
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = if (star <= rating) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = "$star stars",
                                    tint = AlertAmber
                                )
                            }
                        }
                    }
                }

                HorizontalDivider()

                // Quick Message / Thread Note
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    label = { Text("Send reply or note to client") },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                if (messageText.isNotBlank()) {
                                    viewModel.sendTicketMessage(ticket.id, "Capt. David Ramos", "supervisor", messageText)
                                    messageText = ""
                                }
                            }
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Send", tint = SecurityCyan)
                        }
                    }
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Composable
fun CreateTicketDialog(
    viewModel: GuardViewViewModel,
    onDismiss: () -> Unit
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
        title = { Text("Create Complaint Ticket", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Select Category:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    categories.forEach { (catId, catLabel) ->
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
                    label = { Text("Description") },
                    placeholder = { Text("Detail the incident or observation...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                if (selectedCategory == "guard_absent" || selectedCategory == "late") {
                    Text(
                        text = "ℹ️ Verified Shift Timeline will automatically attach as evidence.",
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
                        viewModel.createTicket(
                            category = selectedCategory,
                            description = descriptionText,
                            autoEvidenceText = "Auto-attached shift status: Marcus Vance assigned (06:00 - 18:00 UTC)."
                        )
                        onDismiss()
                    }
                },
                enabled = descriptionText.isNotBlank()
            ) {
                Text("File Ticket")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
