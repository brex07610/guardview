package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GuardGreen
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy900
import com.example.ui.theme.Navy950
import com.example.ui.theme.SecurityCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.GuardViewRole
import com.example.ui.viewmodel.GuardViewViewModel

@Composable
fun MainFeatureScreen(
    viewModel: GuardViewViewModel,
    onSignOut: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val currentRole by viewModel.currentRole.collectAsState()
    val feedbackMessage by viewModel.userFeedback.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(feedbackMessage) {
        if (feedbackMessage != null) {
            snackbarHostState.showSnackbar(feedbackMessage!!)
            viewModel.clearFeedback()
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        containerColor = Navy950,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBarRoleHeader(
                currentRole = currentRole,
                onSelectRole = { viewModel.selectRole(it) },
                onSignOut = onSignOut
            )
        },
        bottomBar = {
            GuardViewBottomNav(
                currentRole = currentRole,
                onSelectRole = { viewModel.selectRole(it) }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentRole) {
                GuardViewRole.CLIENT -> {
                    ClientPortalScreen(
                        viewModel = viewModel,
                        onNavigateToTicketing = { viewModel.selectRole(GuardViewRole.ADMIN) }
                    )
                }
                GuardViewRole.GUARD -> {
                    GuardCheckinScreen(viewModel = viewModel)
                }
                GuardViewRole.ADMIN -> {
                    TicketingScreen(viewModel = viewModel)
                }
                GuardViewRole.WHATSAPP_BOT -> {
                    WhatsAppBotScreen(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun TopAppBarRoleHeader(
    currentRole: GuardViewRole,
    onSelectRole: (GuardViewRole) -> Unit,
    onSignOut: () -> Unit
) {
    Surface(
        color = Navy900,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(SecurityCyan.copy(alpha = 0.2f))
                            .border(1.5.dp, SecurityCyan, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "GuardView Logo",
                            tint = SecurityCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "GUARDVIEW",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp,
                                color = TextPrimary
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(GuardGreen.copy(alpha = 0.2f))
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "LIVE",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GuardGreen
                                )
                            }
                        }
                        Text(
                            text = currentRole.subtitle,
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }

                TextButton(
                    onClick = onSignOut,
                    modifier = Modifier.testTag("sign_out_button")
                ) {
                    Text("Exit", fontSize = 12.sp, color = TextMuted)
                }
            }
        }
    }
}

@Composable
fun GuardViewBottomNav(
    currentRole: GuardViewRole,
    onSelectRole: (GuardViewRole) -> Unit
) {
    NavigationBar(
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.navigationBars)
            .testTag("bottom_nav_bar"),
        containerColor = Navy900,
        contentColor = TextSecondary
    ) {
        val navItems = listOf(
            Triple(GuardViewRole.CLIENT, "Client", Icons.Default.Shield),
            Triple(GuardViewRole.GUARD, "Guard PWA", Icons.Default.Fingerprint),
            Triple(GuardViewRole.ADMIN, "Tickets", Icons.Default.ConfirmationNumber),
            Triple(GuardViewRole.WHATSAPP_BOT, "WhatsApp", Icons.Default.Chat)
        )

        navItems.forEach { (role, label, icon) ->
            val isSelected = currentRole == role
            NavigationBarItem(
                selected = isSelected,
                onClick = { onSelectRole(role) },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        modifier = Modifier.size(22.dp)
                    )
                },
                label = {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Navy950,
                    selectedTextColor = SecurityCyan,
                    indicatorColor = SecurityCyan,
                    unselectedIconColor = TextMuted,
                    unselectedTextColor = TextMuted
                )
            )
        }
    }
}
