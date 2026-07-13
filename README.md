# UniversalTVRemote / 万能遥控器

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![API](https://img.shields.io/badge/API-21%2B-brightgreen.svg)](app/build.gradle.kts)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.22-blue.svg)](build.gradle.kts)
[![Compose](https://img.shields.io/badge/Compose-BOM%202024.01- purple)](app/build.gradle.kts)

A universal remote control Android application that uses your phone's built-in IR blaster to control TVs and air conditioners. Built with Kotlin and Jetpack Compose.

一款利用手机内置红外发射器控制电视和空调的 Android 万能遥控器应用。使用 Kotlin + Jetpack Compose 构建。

---

## Features / 功能

- **IR Blaster Control** — Sends IR signals via Android `ConsumerIrManager` hardware API
- **TV Remote** — Full-featured TV remote with power, volume, channel, D-pad, number pad, and function keys
- **AC Remote** — Air conditioner remote with temperature adjustment, mode switching (cool/heat/fan/dry/auto), and fan speed control
- **Multi-Protocol Support** — NEC, Sony SIRC, Samsung, RC5, RC6 and more
- **Brand Database** — Built-in JSON code library covering major TV and AC brands (Chinese & international)
- **Brand Search** — Search and filter brands by name
- **Frequency Calibration** — Scan through standard frequencies to find the right carrier frequency for your device
- **Save Remotes** — Save configured remotes for quick access
- **Material You** — Modern Material 3 design with dynamic theming

---

- **红外发射** — 通过 Android `ConsumerIrManager` 硬件 API 发射红外信号
- **电视遥控器** — 包含电源、音量、频道、方向键、数字键盘及功能键的完整电视遥控界面
- **空调遥控器** — 支持温度调节、模式切换（制冷/制热/送风/除湿/自动）和风速控制
- **多协议支持** — 支持 NEC、Sony SIRC、Samsung、RC5、RC6 等红外协议
- **内置码库** — 内置 JSON 红外码库，涵盖国内外主流电视和空调品牌
- **品牌搜索** — 支持按品牌名称搜索和筛选
- **频率校准** — 自动扫描标准载波频率，适配非标准设备
- **保存遥控器** — 已配置的遥控器可保存到本地，方便快速调用
- **Material You** — 现代化 Material 3 设计，支持动态主题

## Screenshots / 截图

*(Screenshots to be added)*

## Tech Stack / 技术栈

| Layer | Technology |
|---|---|
| Language | Kotlin 1.9.22 |
| UI | Jetpack Compose (Material 3, Compose BOM 2024.01) |
| Navigation | Navigation Compose 2.7.6 |
| Architecture | MVVM (ViewModel + StateFlow) |
| IR Hardware | Android ConsumerIrManager |
| JSON | Gson 2.10.1 |
| Min SDK | 21 (Android 5.0) |
| Target SDK | 34 (Android 14) |
| Build | Gradle 8.x + AGP 8.2.2 |

## Project Structure / 项目结构

```
app/
├── src/main/
│   ├── java/com/tvremote/ir/
│   │   ├── MainActivity.kt          # Entry point / 入口
│   │   ├── Data/
│   │   │   ├── BrandRepository.kt    # Brand data access / 品牌数据访问
│   │   │   ├── IRCodeLoader.kt       # IR code JSON loader / 红外码库加载
│   │   │   └── SavedRemoteManager.kt # Saved remotes persistence / 本地持久化
│   │   ├── IR/
│   │   │   ├── IRBlaster.kt          # IR emitter wrapper / 红外发射封装
│   │   │   ├── IRProtocols.kt        # Protocol signal generation / 协议信号生成
│   │   │   └── FrequencyScanner.kt   # Frequency calibration engine / 频率校准引擎
│   │   ├── Model/
│   │   │   ├── Brand.kt              # Brand entity / 品牌实体
│   │   │   ├── DeviceType.kt         # Device types / 设备类型枚举
│   │   │   ├── IRCommand.kt          # IR key command / 红外按键指令
│   │   │   ├── ProtocolConfig.kt     # Protocol configuration / 协议配置
│   │   │   ├── RemoteControl.kt      # Remote control model / 遥控器模型
│   │   │   ├── SavedRemote.kt        # Saved remote entity / 已保存遥控器
│   │   │   └── ScanResult.kt         # Calibration result / 校准结果
│   │   └── UI/
│   │       ├── MainScreen.kt         # Home screen / 首页
│   │       ├── BrandSelectScreen.kt  # Brand selection / 品牌选择
│   │       ├── CalibrationScreen.kt  # Frequency calibration / 频率校准
│   │       ├── RemoteScreen.kt       # TV remote control / 电视遥控器
│   │       ├── AcRemoteScreen.kt     # AC remote control / 空调遥控器
│   │       ├── SaveRemoteDialog.kt   # Save remote dialog / 保存对话框
│   │       ├── navigation/NavGraph.kt # Navigation graph / 导航图
│   │       ├── theme/                # Theme definitions / 主题定义
│   │       └── viewmodels/           # ViewModels
│   ├── assets/
│   │   └── ir_codes.json             # IR code database / 红外码库
│   ├── res/                          # Resources / 资源文件
│   └── AndroidManifest.xml
├── build.gradle.kts
└── proguard-rules.pro
```

## Getting Started / 快速开始

### Prerequisites / 前置要求

- Android Studio Hedgehog (2023.1.1) or later
- JDK 17
- An Android device **with built-in IR blaster** / 一部**带红外发射器**的 Android 手机

> Note: Most Xiaomi, Huawei, Samsung (Galaxy S/Note series), Honor, OPPO, vivo, and OnePlus devices include IR blasters. Check your device specifications.
>
> 注意：多数小米、华为、三星 Galaxy S/Note 系列、荣耀、OPPO、vivo 和一加手机均配有红外发射器，请查阅设备规格确认。

### Build & Install / 编译安装

```bash
# Clone the repository / 克隆仓库
git clone https://github.com/yourusername/UniversalTVRemote.git

# Open with Android Studio / 用 Android Studio 打开
# Sync Gradle and run on your device / 同步 Gradle 并运行到设备

# Or build APK directly / 或直接编译 APK
./gradlew assembleDebug
```

The debug APK will be at `app/build/outputs/apk/debug/app-debug.apk`.

## Compatibility / 兼容性

- **IR Hardware**: Devices with `android.hardware.consumerir` feature (required: `false` in manifest, app handles missing IR gracefully)
- **Android**: API 21+ (Android 5.0 Lollipop and above)
- **IR Protocols**: NEC (most common), Sony SIRC (12/15/20-bit), Samsung, RC5, RC6

## How It Works / 工作原理

1. Select device type (TV or AC) / 选择设备类型
2. Choose or search your brand / 选择或搜索品牌
3. Calibrate by selecting the correct protocol variant / 校准协议
4. Start controlling your device! / 开始控制设备

For devices not in the database, use the **Frequency Scan** feature to find the correct carrier frequency, then manually test against known command codes.

对于码库中未收录的设备，可使用**频率扫描**功能找到正确的载波频率，然后手动测试指令码。

## License / 开源协议

[MIT](LICENSE)

Copyright (c) 2024 UniversalTVRemote

## Disclaimer / 免责声明

This project is for educational and personal use only. The IR code database is compiled from publicly available sources. Device compatibility and signal accuracy may vary. The authors are not responsible for any damage caused by the use of this software.

本项目仅供学习和个人使用。红外码库来源于公开资料整理。设备兼容性和信号准确性因设备而异。作者不对使用本软件造成的任何损失承担责任。
