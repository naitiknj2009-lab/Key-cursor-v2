package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.manager.PermissionManager
import com.example.manager.SettingsManager
import com.example.model.CursorSettings
import com.example.service.CursorAccessibilityService
import com.example.ui.components.InteractiveSandbox
import com.example.ui.components.ShortcutsReferenceCard
import com.example.ui.components.StatusDiagnosticsCard
import com.example.ui.components.TroubleshootingCard
import com.example.ui.components.TuningSliders
import com.example.ui.theme.CanvasCardDark
import com.example.ui.theme.CanvasDeepDark
import com.example.ui.theme.CanvasSurfaceDark
import com.example.ui.theme.CyberAmber
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

class MainActivity : ComponentActivity() {

    private lateinit var settingsManager: SettingsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        settingsManager = SettingsManager.getInstance(this)

        setContent {
            MyApplicationTheme {
                KeyCursorApp(settingsManager = settingsManager)
            }
        }
    }
}

@Composable
fun KeyCursorApp(settingsManager: SettingsManager) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val settings by settingsManager.settingsFlow.collectAsStateWithLifecycle()
    val isServiceActive by CursorAccessibilityService.isServiceActiveFlow.collectAsStateWithLifecycle()

    var isKeyboardConnected by remember { mutableStateOf(PermissionManager.isPhysicalKeyboardConnected(context)) }
    var connectedKeyboards by remember { mutableStateOf(PermissionManager.getConnectedKeyboardNames(context)) }
    var selectedTab by remember { mutableIntStateOf(0) }

    // Re-check hardware state whenever app resumes
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isKeyboardConnected = PermissionManager.isPhysicalKeyboardConnected(context)
                connectedKeyboards = PermissionManager.getConnectedKeyboardNames(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(CanvasDeepDark),
        bottomBar = {
            KeyCursorBottomNav(
                selectedTab = selectedTab,
                onTabSelect = { selectedTab = it }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            CanvasSurfaceDark,
                            CanvasDeepDark,
                            Color(0xFF03050A)
                        ),
                        radius = 1800f
                    )
                )
                .padding(innerPadding)
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val isTabletWide = maxWidth >= 840.dp

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .padding(horizontal = if (isTabletWide) 32.dp else 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Top Branding Bar
                    TopHeaderBar(
                        settings = settings,
                        isServiceActive = isServiceActive
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Tab Content
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .widthIn(max = if (isTabletWide) 1100.dp else 650.dp)
                    ) {
                        when (selectedTab) {
                            0 -> DashboardTab(
                                settings = settings,
                                isServiceActive = isServiceActive,
                                isKeyboardConnected = isKeyboardConnected,
                                connectedKeyboards = connectedKeyboards,
                                isTabletWide = isTabletWide,
                                onToggleMaster = { settingsManager.setEnabled(it) },
                                onOpenAccessibilitySettings = { PermissionManager.openAccessibilitySettings(context) }
                            )

                            1 -> TuningTab(
                                settings = settings,
                                onSpeedChange = { settingsManager.setMovementSpeed(it) },
                                onAccelerationChange = { settingsManager.setAcceleration(it) },
                                onFastMultChange = { settingsManager.setFastMultiplier(it) },
                                onCursorSizeChange = { settingsManager.setCursorSizeDp(it) },
                                onColorSelect = { settingsManager.setCursorColorIndex(it) },
                                onClickFeedbackToggle = { settingsManager.setClickFeedback(it) },
                                onHapticToggle = { settingsManager.setHapticFeedback(it) },
                                onTrailToggle = { settingsManager.setTrailEnabled(it) }
                            )

                            2 -> HelpGuideTab()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TopHeaderBar(
    settings: CursorSettings,
    isServiceActive: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(NeonCyan.copy(alpha = 0.25f), ElectricViolet.copy(alpha = 0.25f))
                        )
                    )
                    .border(1.dp, NeonCyan.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Navigation,
                    contentDescription = "KeyCursor Icon",
                    tint = NeonCyan,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "KeyCursor",
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(ElectricViolet.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "TAB M10 READY",
                            color = ElectricViolet,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
                Text(
                    text = "Physical Keyboard Mouse Controller",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
        }

        // Live Status Pill
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF0F172A))
                .border(
                    1.dp,
                    if (isServiceActive && settings.enabled) EmeraldGlow.copy(alpha = 0.6f) else CyberAmber.copy(alpha = 0.6f),
                    RoundedCornerShape(20.dp)
                )
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (isServiceActive && settings.enabled) EmeraldGlow else CyberAmber)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isServiceActive && settings.enabled) "ACTIVE" else "STANDBY",
                    color = if (isServiceActive && settings.enabled) EmeraldGlow else CyberAmber,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
private fun DashboardTab(
    settings: CursorSettings,
    isServiceActive: Boolean,
    isKeyboardConnected: Boolean,
    connectedKeyboards: List<String>,
    isTabletWide: Boolean,
    onToggleMaster: (Boolean) -> Unit,
    onOpenAccessibilitySettings: () -> Unit
) {
    val scrollState = rememberScrollState()

    if (isTabletWide) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                StatusDiagnosticsCard(
                    settings = settings,
                    isServiceActive = isServiceActive,
                    isKeyboardConnected = isKeyboardConnected,
                    connectedKeyboards = connectedKeyboards,
                    onToggleMaster = onToggleMaster,
                    onOpenAccessibilitySettings = onOpenAccessibilitySettings
                )

                ShortcutsReferenceCard()
            }

            Column(
                modifier = Modifier
                    .weight(1.1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                InteractiveSandbox(
                    settings = settings,
                    isServiceActive = isServiceActive
                )
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            StatusDiagnosticsCard(
                settings = settings,
                isServiceActive = isServiceActive,
                isKeyboardConnected = isKeyboardConnected,
                connectedKeyboards = connectedKeyboards,
                onToggleMaster = onToggleMaster,
                onOpenAccessibilitySettings = onOpenAccessibilitySettings
            )

            InteractiveSandbox(
                settings = settings,
                isServiceActive = isServiceActive
            )

            ShortcutsReferenceCard()
        }
    }
}

@Composable
private fun TuningTab(
    settings: CursorSettings,
    onSpeedChange: (Float) -> Unit,
    onAccelerationChange: (Float) -> Unit,
    onFastMultChange: (Float) -> Unit,
    onCursorSizeChange: (Int) -> Unit,
    onColorSelect: (Int) -> Unit,
    onClickFeedbackToggle: (Boolean) -> Unit,
    onHapticToggle: (Boolean) -> Unit,
    onTrailToggle: (Boolean) -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = 16.dp)
    ) {
        TuningSliders(
            settings = settings,
            onSpeedChange = onSpeedChange,
            onAccelerationChange = onAccelerationChange,
            onFastMultChange = onFastMultChange,
            onCursorSizeChange = onCursorSizeChange,
            onColorSelect = onColorSelect,
            onClickFeedbackToggle = onClickFeedbackToggle,
            onHapticToggle = onHapticToggle,
            onTrailToggle = onTrailToggle
        )
    }
}

@Composable
private fun HelpGuideTab() {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        TroubleshootingCard()
        ShortcutsReferenceCard()
    }
}

@Composable
private fun KeyCursorBottomNav(
    selectedTab: Int,
    onTabSelect: (Int) -> Unit
) {
    NavigationBar(
        containerColor = CanvasCardDark.copy(alpha = 0.95f),
        tonalElevation = 8.dp,
        modifier = Modifier.testTag("bottom_navigation_bar")
    ) {
        NavigationBarItem(
            selected = selectedTab == 0,
            onClick = { onTabSelect(0) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Navigation,
                    contentDescription = "Dashboard"
                )
            },
            label = { Text("Dashboard") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = NeonCyan,
                selectedTextColor = NeonCyan,
                unselectedIconColor = TextMuted,
                unselectedTextColor = TextMuted,
                indicatorColor = NeonCyan.copy(alpha = 0.15f)
            )
        )

        NavigationBarItem(
            selected = selectedTab == 1,
            onClick = { onTabSelect(1) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = "Settings"
                )
            },
            label = { Text("Tuning") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = NeonCyan,
                selectedTextColor = NeonCyan,
                unselectedIconColor = TextMuted,
                unselectedTextColor = TextMuted,
                indicatorColor = NeonCyan.copy(alpha = 0.15f)
            )
        )

        NavigationBarItem(
            selected = selectedTab == 2,
            onClick = { onTabSelect(2) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Guide"
                )
            },
            label = { Text("Help & Guide") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = NeonCyan,
                selectedTextColor = NeonCyan,
                unselectedIconColor = TextMuted,
                unselectedTextColor = TextMuted,
                indicatorColor = NeonCyan.copy(alpha = 0.15f)
            )
        )
    }
}
