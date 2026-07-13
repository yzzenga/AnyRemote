package com.tvremote.ir.UI

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tvremote.ir.Model.Brand
import com.tvremote.ir.Model.DeviceType
import com.tvremote.ir.Model.SavedRemote
import com.tvremote.ir.UI.theme.TVRemoteTheme
import com.tvremote.ir.UI.viewmodels.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onSelectBrand: (String) -> Unit,       // 品牌ID -> 校准
    onViewAllBrands: () -> Unit,           // 查看全部品牌 -> 品牌列表
    onSavedRemote: (SavedRemote) -> Unit,  // 一键打开已保存遥控器
    onFrequencyScan: () -> Unit,
    viewModel: MainViewModel = viewModel()
) {
    val currentDeviceType by viewModel.currentDeviceType.collectAsState()
    val hotBrands by viewModel.hotBrands.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val savedRemotes by viewModel.savedRemotes.collectAsState()
    val view = LocalView.current

    TVRemoteTheme {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text("万能遥控器", fontWeight = FontWeight.Bold)
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
                    .padding(horizontal = 16.dp)
            ) {
                // 设备类型 Tab
                DeviceTypeTab(
                    selectedType = currentDeviceType,
                    onTypeSelected = {
                        viewModel.switchDeviceType(it)
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 搜索栏
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.search(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            if (currentDeviceType == DeviceType.TV) "搜索电视品牌..."
                            else "搜索空调品牌..."
                        )
                    },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (isSearching && searchResults.isNotEmpty()) {
                    // 搜索结果
                    Text(
                        "搜索结果",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    BrandGrid(brands = searchResults) { brand ->
                        onSelectBrand(brand.id)
                    }
                } else if (isSearching) {
                    Box(
                        modifier = Modifier.height(200.dp).fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("未找到匹配品牌", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    // 已保存设备
                    if (savedRemotes.isNotEmpty()) {
                        SavedRemotesSection(
                            remotes = savedRemotes,
                            onRemoteClick = { onSavedRemote(it) },
                            onDelete = { viewModel.deleteSavedRemote(it.id) }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Divider()
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // 热门品牌
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "热门${currentDeviceType.label}品牌",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    BrandGrid(brands = hotBrands) { brand ->
                        view.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP)
                        onSelectBrand(brand.id)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 所有品牌入口和频率扫描
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        OutlinedButton(
                            onClick = {
                                view.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP)
                                onViewAllBrands()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.List, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("查看全部${currentDeviceType.label}品牌")
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        OutlinedButton(
                            onClick = {
                                view.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP)
                                onFrequencyScan()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Tune, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("频率扫描校准")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeviceTypeTab(
    selectedType: DeviceType,
    onTypeSelected: (DeviceType) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        DeviceType.entries.forEach { type ->
            FilterChip(
                selected = selectedType == type,
                onClick = { onTypeSelected(type) },
                label = {
                    Text(
                        type.label,
                        fontWeight = if (selectedType == type) FontWeight.Bold else FontWeight.Normal
                    )
                },
                leadingIcon = {
                    Icon(
                        if (type == DeviceType.TV) Icons.Default.Tv
                        else Icons.Default.AcUnit,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun BrandGrid(
    brands: List<Brand>,
    onClick: (Brand) -> Unit
) {
    // 最多显示2行（6个）
    val displayBrands = if (brands.size > 6) brands.take(6) else brands
    val rows = (displayBrands.size + 2) / 3
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.height((rows * 66).dp)
    ) {
        items(displayBrands) { brand ->
            BrandCard(brand = brand, onClick = { onClick(brand) })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BrandCard(brand: Brand, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                brand.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                brand.nameEn,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SavedRemotesSection(
    remotes: List<SavedRemote>,
    onRemoteClick: (SavedRemote) -> Unit,
    onDelete: (SavedRemote) -> Unit
) {
    Text(
        "已保存的设备",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface
    )
    Spacer(modifier = Modifier.height(8.dp))

    remotes.forEach { remote ->
        Card(
            onClick = { onRemoteClick(remote) },
            modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    if (remote.deviceType == DeviceType.TV) Icons.Default.Tv
                    else Icons.Default.AcUnit,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        remote.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        "${remote.brandName} · ${remote.frequency / 1000}KHz",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = { onDelete(remote) }) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "删除",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
