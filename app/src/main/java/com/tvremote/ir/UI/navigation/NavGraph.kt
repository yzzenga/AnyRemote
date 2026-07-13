package com.tvremote.ir.UI.navigation

import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.tvremote.ir.Model.DeviceType
import com.tvremote.ir.UI.*
import com.tvremote.ir.UI.viewmodels.RemoteViewModel

object Routes {
    const val HOME = "home"
    const val BRAND_SELECT = "brand_select"
    const val REMOTE = "remote/{brandId}/{protocolId}"
    const val AC_REMOTE = "ac_remote/{brandId}/{protocolId}"
    const val CALIBRATION = "calibration/{brandId}"
    const val FREQUENCY_SCAN = "frequency_scan"

    fun remote(brandId: String, protocolId: String) = "remote/$brandId/$protocolId"
    fun acRemote(brandId: String, protocolId: String) = "ac_remote/$brandId/$protocolId"
    fun calibration(brandId: String) = "calibration/$brandId"
}

@Composable
fun NavGraph(
    navController: NavHostController,
    remoteViewModel: RemoteViewModel
) {
    NavHost(navController = navController, startDestination = Routes.HOME) {
        // ===== 主页 =====
        composable(Routes.HOME) {
            MainScreen(
                onSelectBrand = { brandId ->
                    navController.navigate(Routes.calibration(brandId))
                },
                onViewAllBrands = {
                    navController.navigate(Routes.BRAND_SELECT)
                },
                onSavedRemote = { savedRemote ->
                    // 从已保存直接打开遥控器，跳过校准
                    remoteViewModel.loadFromSaved(savedRemote)
                    if (savedRemote.deviceType == DeviceType.TV) {
                        navController.navigate(Routes.remote(savedRemote.brandId, savedRemote.protocolId))
                    } else {
                        navController.navigate(Routes.acRemote(savedRemote.brandId, savedRemote.protocolId))
                    }
                },
                onFrequencyScan = {
                    navController.navigate(Routes.FREQUENCY_SCAN)
                }
            )
        }

        // ===== 品牌选择（全部品牌） =====
        composable(Routes.BRAND_SELECT) {
            BrandSelectScreen(
                onBrandSelected = { brandId ->
                    navController.navigate(Routes.calibration(brandId))
                },
                onBack = { navController.popBackStack(); Unit }
            )
        }

        // ===== 校准（TV和AC通用） =====
        composable(
            route = Routes.CALIBRATION,
            arguments = listOf(
                navArgument("brandId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val brandId = backStackEntry.arguments?.getString("brandId") ?: ""

            if (brandId.isBlank()) {
                // 空brandId = 频率扫描校准（未知品牌）
                CalibrationScreen(
                    brandId = "",
                    viewModel = remoteViewModel,
                    onProtocolSelected = { _ -> },
                    onBack = { navController.popBackStack(); Unit }
                )
            } else {
                CalibrationScreen(
                    brandId = brandId,
                    viewModel = remoteViewModel,
                    onProtocolSelected = { protocolId ->
                        val brand = remoteViewModel.currentBrand.value
                        if (brand?.deviceType == DeviceType.AC) {
                            navController.navigate(Routes.acRemote(brandId, protocolId))
                        } else {
                            navController.navigate(Routes.remote(brandId, protocolId))
                        }
                    },
                    onBack = { navController.popBackStack(); Unit }
                )
            }
        }

        // ===== 电视遥控器 =====
        composable(
            route = Routes.REMOTE,
            arguments = listOf(
                navArgument("brandId") { type = NavType.StringType },
                navArgument("protocolId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val brandId = backStackEntry.arguments?.getString("brandId") ?: ""
            val protocolId = backStackEntry.arguments?.getString("protocolId") ?: ""

            // 保存对话框
            var showSaveDialog by remember { mutableStateOf(false) }
            val currentBrand by remoteViewModel.currentBrand.collectAsState()
            val defaultSaveName = remember(brandId, currentBrand) {
                currentBrand?.name?.let { "${it}遥控器" } ?: "我的遥控器"
            }

            RemoteScreen(
                brandId = brandId,
                protocolId = protocolId,
                viewModel = remoteViewModel,
                onSaveRequest = { showSaveDialog = true },
                onBack = { navController.popBackStack(); Unit }
            )

            if (showSaveDialog) {
                SaveRemoteDialog(
                    defaultName = defaultSaveName,
                    onDismiss = { showSaveDialog = false },
                    onConfirm = { name ->
                        remoteViewModel.saveCurrentRemote(name)
                        showSaveDialog = false
                    }
                )
            }
        }

        // ===== 空调遥控器 =====
        composable(
            route = Routes.AC_REMOTE,
            arguments = listOf(
                navArgument("brandId") { type = NavType.StringType },
                navArgument("protocolId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val brandId = backStackEntry.arguments?.getString("brandId") ?: ""
            val protocolId = backStackEntry.arguments?.getString("protocolId") ?: ""

            var showSaveDialog by remember { mutableStateOf(false) }
            val currentBrand by remoteViewModel.currentBrand.collectAsState()
            val defaultSaveName = remember(brandId, currentBrand) {
                currentBrand?.name?.let { "${it}空调遥控器" } ?: "我的空调遥控器"
            }

            AcRemoteScreen(
                brandId = brandId,
                protocolId = protocolId,
                viewModel = remoteViewModel,
                onSave = { showSaveDialog = true },
                onBack = { navController.popBackStack(); Unit }
            )

            if (showSaveDialog) {
                SaveRemoteDialog(
                    defaultName = defaultSaveName,
                    onDismiss = { showSaveDialog = false },
                    onConfirm = { name ->
                        remoteViewModel.saveCurrentRemote(name)
                        showSaveDialog = false
                    }
                )
            }
        }

        // ===== 频率扫描（无品牌） =====
        composable(Routes.FREQUENCY_SCAN) {
            CalibrationScreen(
                brandId = "",
                viewModel = remoteViewModel,
                onProtocolSelected = { _ -> },
                onBack = { navController.popBackStack(); Unit }
            )
        }
    }
}
