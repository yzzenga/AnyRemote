package com.tvremote.ir.UI

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.sp
import com.tvremote.ir.IR.FrequencyScanner
import com.tvremote.ir.Model.ProtocolConfig

/**
 * 品牌校准界面 - 协议选择 + 频率测试
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalibrationScreen(
    brandId: String,
    viewModel: com.tvremote.ir.UI.viewmodels.RemoteViewModel,
    onProtocolSelected: (String) -> Unit,
    onBack: () -> Unit
) {
    val scanning by viewModel.scanning.collectAsState()
    val scanProgress by viewModel.scanProgress.collectAsState()
    val protocols by viewModel.protocols.collectAsState()
    val currentBrand by viewModel.currentBrand.collectAsState()
    val irAvailable by viewModel.irAvailable.collectAsState()
    val irStatusMessage by viewModel.irStatusMessage.collectAsState()
    val calibrated by viewModel.calibrated.collectAsState()
    val manualFrequency by viewModel.manualFrequency.collectAsState()

    val view = LocalView.current

    LaunchedEffect(brandId) {
        if (brandId.isNotBlank()) {
            viewModel.loadBrand(brandId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (brandId.isBlank()) "频率扫描校准" else "${currentBrand?.name ?: ""} 校准")
                },
                navigationIcon = {
                    IconButton(onClick = {
                        view.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP)
                        onBack()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // IR 状态提示
            if (!irAvailable) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(irStatusMessage, color = MaterialTheme.colorScheme.error)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // 校准指南
            if (!scanning && !calibrated) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "使用说明",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "1. 将手机红外发射口对准电视\n" +
                            "2. 选择或搜索电视品牌\n" +
                            "3. 逐个测试推荐的代码组\n" +
                            "4. 电视有反应后点击确认\n" +
                            "5. 如果是未知品牌，使用频率扫描模式",
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 24.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // 扫描进度
            if (scanning) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(scanProgress.message, style = MaterialTheme.typography.bodyLarge)
                        Spacer(modifier = Modifier.height(12.dp))
                        @Suppress("DEPRECATION")
                        LinearProgressIndicator(
                            progress = scanProgress.progress,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "${(scanProgress.progress * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            // 校准成功
            if (calibrated) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "校准完成！",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // 协议列表（代码组测试）
            if (protocols.isNotEmpty() && !scanning) {
                Text(
                    "可用的遥控代码组",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(8.dp))

                protocols.forEach { config ->
                    ProtocolTestCard(
                        config = config,
                        onTest = {
                            viewModel.selectProtocol(config)
                            viewModel.sendKey("power")
                        },
                        onSelect = {
                            viewModel.selectProtocol(config)
                            onProtocolSelected(config.id)
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // 频率手动调节
            if (!scanning) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "手动频率调节",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "当前频率",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "${manualFrequency / 1000}KHz (${manualFrequency}Hz)",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = {
                                    view.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP)
                                    viewModel.adjustFrequency(-1000)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) { Text("-1KHz") }

                            Button(
                                onClick = {
                                    view.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP)
                                    viewModel.sendPowerAtFrequency(manualFrequency)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) { Text("测试电源") }

                            Button(
                                onClick = {
                                    view.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP)
                                    viewModel.adjustFrequency(1000)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) { Text("+1KHz") }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                view.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP)
                                viewModel.cycleFrequency()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            )
                        ) { Text("切换到下一常用频率") }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 操作按钮组
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            view.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP)
                            viewModel.startFrequencyScan()
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !scanning && irAvailable
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("自动频率扫描")
                    }

                    Button(
                        onClick = {
                            view.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP)
                            viewModel.startAutoScan()
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !scanning && irAvailable
                    ) {
                        Icon(Icons.Default.Tune, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("全自动校准")
                    }
                }

                if (!calibrated && protocols.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            view.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP)
                            viewModel.confirmCalibration()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("确认 - 电视已有反应！")
                    }
                }
            }
        }
    }
}

@Composable
fun ProtocolTestCard(
    config: ProtocolConfig,
    onTest: () -> Unit,
    onSelect: () -> Unit
) {
    val view = LocalView.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    config.description.ifEmpty { config.protocol },
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    "${config.protocol} | ${config.frequency / 1000}KHz | 地址码: ${config.deviceCode}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            FilledTonalButton(onClick = {
                view.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP)
                onTest()
            }, modifier = Modifier.padding(end = 4.dp)) {
                Text("测试")
            }
            Button(onClick = {
                view.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP)
                onSelect()
            }) {
                Text("选择")
            }
        }
    }
}
