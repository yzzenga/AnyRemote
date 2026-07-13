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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.sp
import com.tvremote.ir.Model.ProtocolConfig

/**
 * 遥控器主界面 - 模拟电视遥控器
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemoteScreen(
    brandId: String,
    protocolId: String,
    viewModel: com.tvremote.ir.UI.viewmodels.RemoteViewModel,
    onBack: () -> Unit,
    onSaveRequest: () -> Unit = {}
) {
    val currentBrand by viewModel.currentBrand.collectAsState()
    val currentProtocol by viewModel.currentProtocol.collectAsState()
    val irAvailable by viewModel.irAvailable.collectAsState()
    val irStatusMessage by viewModel.irStatusMessage.collectAsState()
    val protocols by viewModel.protocols.collectAsState()
    val calibrated by viewModel.calibrated.collectAsState()
    val view = LocalView.current

    LaunchedEffect(brandId) {
        // 如果已经加载过（来自已保存遥控器），则跳过
        val existingBrand = viewModel.currentBrand.value
        if (existingBrand == null || existingBrand.id != brandId) {
            viewModel.loadBrand(brandId)
        }
        // 自动选中协议
        val existingProtocol = viewModel.currentProtocol.value
        if (existingProtocol == null || existingProtocol.id != protocolId) {
            val proto = viewModel.protocols.value.find { it.id == protocolId }
            if (proto != null) {
                viewModel.selectProtocol(proto)
            } else {
                viewModel.protocols.value.firstOrNull()?.let {
                    viewModel.selectProtocol(it)
                }
            }
        }
    }

    // 更新协议
    LaunchedEffect(protocolId, protocols) {
        val proto = protocols.find { it.id == protocolId }
            ?: protocols.firstOrNull()
        if (proto != null) {
            viewModel.selectProtocol(proto)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(currentBrand?.name ?: "遥控器")
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
                    // 保存按钮
                    IconButton(onClick = {
                        view.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP)
                        onSaveRequest()
                    }) {
                        Icon(Icons.Default.Save, contentDescription = "保存")
                    }
                    // 校准状态
                    if (calibrated) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "已校准",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (!irAvailable) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    irStatusMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 协议选择器
            if (protocols.size > 1) {
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        onClick = {
                            view.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP)
                            expanded = true
                        }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "代码组: ${currentProtocol?.description ?: "请选择"}",
                                modifier = Modifier.weight(1f)
                            )
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    }
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        protocols.forEach { proto ->
                            DropdownMenuItem(
                                text = { Text("${proto.description} (${proto.frequency / 1000}KHz)") },
                                onClick = {
                                    view.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP)
                                    viewModel.selectProtocol(proto)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // 发送提示
            Text(
                "点击按钮发送红外信号",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))

            // 遥控器按钮区域
            RemoteControlPanel(
                onKeyPress = { viewModel.sendKey(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 手动频率微调
            ManualFrequencyControl(
                currentFrequency = viewModel.manualFrequency.collectAsState().value,
                onFrequencyChange = { viewModel.setManualFrequency(it) },
                onSendPower = { viewModel.sendPowerAtFrequency(viewModel.manualFrequency.value) }
            )
        }
    }
}

@Composable
fun RemoteControlPanel(onKeyPress: (String) -> Unit) {
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 第一行：电源 + 静音 + 信源
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                RemoteButton("电源", Icons.Default.PowerSettingsNew, Color(0xFFE53935)) {
                    onKeyPress("power")
                }
                RemoteButton("静音", Icons.Default.VolumeOff, Color(0xFFFF6F00)) {
                    onKeyPress("mute")
                }
                RemoteButton("信源", Icons.Default.Input, Color(0xFF1565C0)) {
                    onKeyPress("input")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 第二行：音量 + 频道
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 音量侧
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    SmallRemoteButton(Icons.Default.Add) { onKeyPress("vol_up") }
                    Text("音量", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(vertical = 4.dp))
                    SmallRemoteButton(Icons.Default.Remove) { onKeyPress("vol_down") }
                }

                // 方向键
                DPadControl(
                    onUp = { onKeyPress("up") },
                    onDown = { onKeyPress("down") },
                    onLeft = { onKeyPress("left") },
                    onRight = { onKeyPress("right") },
                    onOk = { onKeyPress("ok") }
                )

                // 频道侧
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    SmallRemoteButton(Icons.Default.Add) { onKeyPress("ch_up") }
                    Text("频道", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(vertical = 4.dp))
                    SmallRemoteButton(Icons.Default.Remove) { onKeyPress("ch_down") }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 第三行：导航键
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                RemoteButton("菜单", Icons.Default.Menu, Color(0xFF0F3460)) {
                    onKeyPress("menu")
                }
                RemoteButton("首页", Icons.Default.Home, Color(0xFF0F3460)) {
                    onKeyPress("home")
                }
                RemoteButton("返回", Icons.Default.ArrowBack, Color(0xFF0F3460)) {
                    onKeyPress("back")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 数字键盘
            Text(
                "数字键",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))

            val numbers = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("", "0", "")
            )

            numbers.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    row.forEach { num ->
                        if (num.isNotEmpty()) {
                            NumberButton(num) { onKeyPress(num) }
                        } else {
                            Spacer(modifier = Modifier.size(64.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

@Composable
fun RemoteButton(
    label: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    val view = LocalView.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = {
            view.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP)
            onClick()
        })
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.2f))
                .border(1.dp, color.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun SmallRemoteButton(icon: ImageVector, onClick: () -> Unit) {
    val view = LocalView.current
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = {
                view.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP)
                onClick()
            }),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun DPadControl(
    onUp: () -> Unit,
    onDown: () -> Unit,
    onLeft: () -> Unit,
    onRight: () -> Unit,
    onOk: () -> Unit
) {
    val view = LocalView.current
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        SmallRemoteButton(Icons.Default.KeyboardArrowUp, onClick = onUp)
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SmallRemoteButton(Icons.Default.KeyboardArrowLeft, onClick = onLeft)
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable(onClick = {
                        view.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP)
                        onOk()
                    }),
                contentAlignment = Alignment.Center
            ) {
                Text("OK", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            SmallRemoteButton(Icons.Default.KeyboardArrowRight, onClick = onRight)
        }
        SmallRemoteButton(Icons.Default.KeyboardArrowDown, onClick = onDown)
    }
}

@Composable
fun NumberButton(number: String, onClick: () -> Unit) {
    val view = LocalView.current
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = {
                view.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP)
                onClick()
            }),
        contentAlignment = Alignment.Center
    ) {
        Text(
            number,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun ManualFrequencyControl(
    currentFrequency: Int,
    onFrequencyChange: (Int) -> Unit,
    onSendPower: () -> Unit
) {
    val view = LocalView.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "频率微调",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    view.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP)
                    onFrequencyChange(currentFrequency - 500)
                }) {
                    Icon(Icons.Default.RemoveCircleOutline, contentDescription = "-500Hz")
                }

                Text(
                    "${currentFrequency / 1000}.${(currentFrequency % 1000) / 100}KHz",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                IconButton(onClick = {
                    view.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP)
                    onFrequencyChange(currentFrequency + 500)
                }) {
                    Icon(Icons.Default.AddCircleOutline, contentDescription = "+500Hz")
                }

                Spacer(modifier = Modifier.width(8.dp))

                FilledTonalButton(onClick = {
                    view.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP)
                    onSendPower()
                }) {
                    Text("测试")
                }
            }
        }
    }
}
