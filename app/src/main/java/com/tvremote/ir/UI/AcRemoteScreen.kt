package com.tvremote.ir.UI

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tvremote.ir.Model.ProtocolConfig

// 空调模式列表
private val AC_MODES = listOf("auto", "cool", "dry", "fan", "heat")
private val AC_MODE_LABELS = mapOf(
    "auto" to "自动", "cool" to "制冷", "dry" to "除湿",
    "fan" to "送风", "heat" to "制热"
)

// 风扇速度
private val FAN_SPEEDS = listOf("auto", "low", "med", "high")
private val FAN_LABELS = mapOf("auto" to "自动", "low" to "低风", "med" to "中风", "high" to "高风")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AcRemoteScreen(
    brandId: String,
    protocolId: String,
    viewModel: com.tvremote.ir.UI.viewmodels.RemoteViewModel,
    onSave: () -> Unit,
    onBack: () -> Unit
) {
    val currentBrand by viewModel.currentBrand.collectAsState()
    val currentProtocol by viewModel.currentProtocol.collectAsState()
    val irAvailable by viewModel.irAvailable.collectAsState()
    val irStatusMessage by viewModel.irStatusMessage.collectAsState()
    val protocols by viewModel.protocols.collectAsState()
    val calibrated by viewModel.calibrated.collectAsState()
    val view = LocalView.current

    // 空调状态
    var currentMode by remember { mutableStateOf("cool") }
    var currentFan by remember { mutableStateOf("auto") }
    var swingOn by remember { mutableStateOf(false) }
    var temperature by remember { mutableStateOf(26) }

    LaunchedEffect(brandId) {
        val existingBrand = viewModel.currentBrand.value
        if (existingBrand == null || existingBrand.id != brandId) {
            viewModel.loadBrand(brandId)
        }
        val existingProtocol = viewModel.currentProtocol.value
        if (existingProtocol == null || existingProtocol.id != protocolId) {
            val proto = viewModel.protocols.value.find { it.id == protocolId }
                ?: viewModel.protocols.value.firstOrNull()
            if (proto != null) viewModel.selectProtocol(proto)
        }
    }

    LaunchedEffect(protocolId, protocols) {
        val existingProtocol = viewModel.currentProtocol.value
        if (existingProtocol == null || existingProtocol.id != protocolId) {
            val proto = protocols.find { it.id == protocolId } ?: protocols.firstOrNull()
            if (proto != null) viewModel.selectProtocol(proto)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(currentBrand?.name ?: "空调遥控器")
                        currentProtocol?.let {
                            Text(
                                "${it.description} | ${it.frequency / 1000}KHz",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        view.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP)
                        onBack()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        onSave()
                    }) {
                        Icon(Icons.Default.Save, contentDescription = "保存")
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // IR 状态
            if (!irAvailable) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        irStatusMessage,
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
                return@Column
            }

            // 温度显示
            TemperatureDisplay(temperature = temperature)

            Spacer(modifier = Modifier.height(16.dp))

            // 温度调节
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledIconButton(
                    onClick = {
                        view.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP)
                        if (temperature > 16) {
                            temperature--
                            viewModel.sendKey("temp_down")
                        }
                    },
                    modifier = Modifier.size(64.dp),
                    enabled = temperature > 16
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "降温", modifier = Modifier.size(32.dp))
                }

                Spacer(modifier = Modifier.width(32.dp))

                FilledIconButton(
                    onClick = {
                        view.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP)
                        if (temperature < 30) {
                            temperature++
                            viewModel.sendKey("temp_up")
                        }
                    },
                    modifier = Modifier.size(64.dp),
                    enabled = temperature < 30
                ) {
                    Icon(Icons.Default.Add, contentDescription = "升温", modifier = Modifier.size(32.dp))
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 模式选择行
            Text("模式", style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                AC_MODES.forEach { mode ->
                    AcModeButton(
                        label = AC_MODE_LABELS[mode] ?: mode,
                        selected = currentMode == mode,
                        onClick = {
                            view.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP)
                            currentMode = mode
                            viewModel.sendKey("mode_$mode")
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 风速选择行
            Text("风速", style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FAN_SPEEDS.forEach { speed ->
                    FilterChip(
                        selected = currentFan == speed,
                        onClick = {
                            view.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP)
                            currentFan = speed
                            viewModel.sendKey("fan_$speed")
                        },
                        label = { Text(FAN_LABELS[speed] ?: speed) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 摆风和电源
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // 摆风开关
                FilterChip(
                    selected = swingOn,
                    onClick = {
                        view.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP)
                        swingOn = !swingOn
                        viewModel.sendKey("swing")
                    },
                    leadingIcon = {
                        Icon(
                            if (swingOn) Icons.Default.PlayArrow else Icons.Default.Stop,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    label = { Text("摆风") }
                )

                // 电源按钮
                Button(
                    onClick = {
                        view.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP)
                        viewModel.sendKey("power")
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE53935)
                    ),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Icon(Icons.Default.PowerSettingsNew, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("电源", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 协议选择
            if (protocols.size > 1) {
                var expanded by remember { mutableStateOf(false) }
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { expanded = true }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("代码组: ${currentProtocol?.description ?: "请选择"}",
                            modifier = Modifier.weight(1f))
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    protocols.forEach { proto ->
                        DropdownMenuItem(
                            text = { Text("${proto.description} (${proto.frequency / 1000}KHz)") },
                            onClick = {
                                viewModel.selectProtocol(proto)
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 已校准状态
            if (calibrated) {
                AssistChip(
                    onClick = { },
                    label = { Text("已校准  ✓") },
                    leadingIcon = {
                        Icon(Icons.Default.CheckCircle, contentDescription = null,
                            modifier = Modifier.size(18.dp))
                    }
                )
            }
        }
    }
}

@Composable
private fun TemperatureDisplay(temperature: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "设定温度",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    "$temperature",
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "°C",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun AcModeButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val bgColor = if (selected) MaterialTheme.colorScheme.primaryContainer
    else MaterialTheme.colorScheme.surfaceVariant
    val contentColor = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
    else MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(8.dp)
            .then(if (selected) Modifier.border(
                1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp)
            ) else Modifier)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
            textAlign = TextAlign.Center
        )
    }
}
