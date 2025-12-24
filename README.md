# TV IR Remote Simulator

> CVTE TV嵌入式实习项目 - Android 红外遥控器模拟器

[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com/)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-blue.svg)](https://kotlinlang.org/)
[![API](https://img.shields.io/badge/API-21%2B-brightgreen.svg)](https://android-arsenal.com/api?level=21)

## 📋 项目简介

本项目是一款 Android 红外遥控器模拟应用，通过手机红外发射器模拟 CVTE 工厂电视遥控器。应用采用 MVVM 架构，支持 NEC 协议红外信号发射，可以控制支持该协议的电视设备。

### 功能亮点

| 功能模块 | 描述 | 状态 |
|---------|------|------|
| 遥控器 UI | 完整的按键布局，包含数字/方向/功能/颜色/工厂测试键 | ✅ |
| IR 信号发射 | NEC 协议编码，38kHz 载波频率 | ✅ |
| 配置编辑 | 支持自定义 IR 码值，新增/修改/删除按键 | ✅ |
| 远程同步 | 从服务器拉取配置，RESTful API | ✅ |
| QR 扫码导入 | 扫描二维码快速导入配置 | ✅ |

---

## 🎯 需求实现情况

### 基本需求 (50分) ✅

| 需求项 | 实现情况 |
|-------|---------|
| UI 按键布局 | ✅ 完整实现数字键(0-9)、方向键、电源键、信源键、菜单键、返回键等 |
| IR 码值配置 | ✅ Protocol=0x01, Header=0x8890，按照 KIR_IRFAC_Keymap 完整实现 |
| 正确发射码值 | ✅ TV 正常响应，NEC 协议时序准确 |
| Git 提交规范 | ✅ 遵循 `[关键字] 描述` + `[what][why][how]` 格式 |

### 扩展需求 (30分) ✅

| 需求项 | 实现情况 |
|-------|---------|
| 配置编辑 | ✅ 配置编辑器支持新增/修改/删除按键，导入/导出 JSON |
| 远程服务器同步 | ✅ Node.js RESTful API 服务器，支持配置的增删改查 |

### 代码质量 (20分) ✅

| 评分项 | 实现情况 |
|-------|---------|
| 设计解耦/可扩展性 | ✅ MVVM 架构，接口抽象 (`IIREmitter`)，模块化设计 |
| 代码可靠/稳定性 | ✅ 异常处理完善，资源正确释放 |
| 编码规范/可读性 | ✅ Kotlin 命名规范，完整注释 |
| 人机交互体验 | ✅ Material Design，震动反馈，状态提示 |

---

## 🏗️ 技术架构

```
┌───────────────────────────────────────────────┐
│              UI Layer (Activity)               │
│   RemoteControlActivity │ ConfigEditorActivity │
└─────────────────────┬─────────────────────────┘
                      │
┌─────────────────────▼─────────────────────────┐
│            ViewModel Layer                     │
│  RemoteControlViewModel │ ConfigEditorViewModel│
└─────────────────────┬─────────────────────────┘
                      │
┌─────────────────────▼─────────────────────────┐
│           Repository Layer                     │
│              ConfigRepository                  │
└──────┬──────────────────────────────┬─────────┘
       │                              │
┌──────▼──────┐                ┌──────▼──────┐
│  IR Module  │                │   Network   │
│  IRManager  │                │   Retrofit  │
│ NECIREmitter│                │  API Client │
└─────────────┘                └─────────────┘
```

## 📁 项目结构

```
TV IR Remote Simulator/
├── app/src/main/java/com/cvte/irremote/
│   ├── ui/                     # UI 层
│   │   ├── RemoteControlActivity.kt    # 遥控器主界面
│   │   ├── ConfigEditorActivity.kt     # 配置编辑器
│   │   ├── ConfigListActivity.kt       # 配置列表
│   │   └── CustomScannerActivity.kt    # QR 扫码
│   ├── viewmodel/              # ViewModel 层
│   │   ├── RemoteControlViewModel.kt
│   │   └── ConfigEditorViewModel.kt
│   ├── model/
│   │   ├── entity/             # 数据实体
│   │   │   ├── IRConfig.kt     # IR 配置模型
│   │   │   ├── IRKey.kt        # 按键模型
│   │   │   └── EmitResult.kt   # 发射结果
│   │   └── repository/         # 数据仓库
│   │       └── ConfigRepository.kt
│   ├── ir/                     # IR 核心模块
│   │   ├── IIREmitter.kt       # 发射器接口
│   │   ├── IRManager.kt        # IR 管理器
│   │   └── NECIREmitter.kt     # NEC 协议实现
│   ├── network/                # 网络模块
│   │   ├── RetrofitClient.kt
│   │   ├── ConfigApiService.kt
│   │   └── NetworkManager.kt
│   └── utils/                  # 工具类
│       └── IRLogger.kt
├── server/                     # 配置同步服务器
│   ├── server.js               # Express 服务器
│   ├── package.json
│   └── configs/                # 预置配置
│       └── cvte_factory.json
├── .agent/workflows/
│   └── project-rules.md        # 开发规范
├── API.md                      # API 文档
├── QUICKSTART.md               # 快速开始
└── README.md
```

---

## 📡 IR 协议规格

### CVTE 工厂遥控器配置

```
Protocol = 0x01 (NEC)
Header   = 0x8890
```

### NEC 协议时序

| 参数 | 值 |
|-----|-----|
| 载波频率 | 38,000 Hz |
| Header 脉冲 | 9,000 μs |
| Header 间隔 | 4,500 μs |
| Bit 0 | 560 μs 脉冲 + 560 μs 间隔 |
| Bit 1 | 560 μs 脉冲 + 1,680 μs 间隔 |
| Stop bit | 560 μs 脉冲 |

### 数据格式

```
[Header High 8bit] + [Header Low 8bit] + [Command 8bit] + [Command Inverse 8bit]
```

### 部分按键码值对照表

| 按键 | 码值 | 按键 | 码值 |
|-----|------|-----|------|
| KEY_POWER | 0x0001 | KEY_MENU | 0x0046 |
| KEY_0 | 0x0010 | KEY_BACK | 0x004A |
| KEY_1 | 0x0002 | KEY_HOME | 0x004C |
| KEY_UP | 0x0056 | KEY_ENTER | 0x0057 |
| KEY_DOWN | 0x0050 | KEY_VOLUMEUP | 0x0044 |
| KEY_LEFT | 0x0047 | KEY_VOLUMEDOWN | 0x0045 |
| KEY_RIGHT | 0x004B | KEY_MUTE | 0x0011 |

完整码值表请参考 `server/configs/cvte_factory.json`

---

## 🚀 快速开始

### 环境要求

- Android Studio Hedgehog (2023.1.1) 或更高版本
- JDK 17
- Android SDK 34
- 支持 IR 发射的 Android 设备（小米、华为、荣耀等）

### 构建运行

```bash
# 1. 克隆项目
git clone <repository-url>
cd "TV IR Remote Simulator"

# 2. 用 Android Studio 打开项目

# 3. 同步 Gradle 依赖

# 4. 连接支持 IR 的设备，运行应用
```

### 启动配置服务器（可选）

```bash
cd server
npm install
npm start
# 服务器运行在 http://localhost:3000
```

---

## 📝 Git 提交规范

### 提交格式

```
[关键字] 英文简单描述

[what] 详细说明修改什么问题
[why] 详细说明问题产生原因和(或)为什么做这个修改
[how] 详细说明是怎么修改的
```

### 关键字

| 关键字 | 说明 |
|-------|------|
| feature | 功能和需求的修改和完善 |
| bugfix | 针对 BUG 的修改和完善 |
| config | 客户配置和参数的修改 |
| merge | 合并其他分支 |
| revert | 还原已提交修改项 |

### 示例

```
[feature] Implement NEC protocol IR emitter

[what] 实现 NEC 协议的 IR 信号发射功能
[why] 完成基本需求中的红外信号发射要求
[how] 使用 ConsumerIrManager API，根据 NEC 协议时序编码并发射信号
```

---

## 🧪 测试验证

### 兼容设备

应用需要设备支持 `android.hardware.consumerir` 特性：

- 小米系列 (Mi / Redmi)
- 华为系列
- 荣耀系列
- 部分三星机型

### 功能测试步骤

1. 安装应用到支持 IR 的 Android 设备
2. 将手机红外发射口对准电视红外接收器
3. 点击遥控器按键，验证电视响应
4. 测试配置编辑和远程同步功能

---

## 📚 相关文档

- [API.md](API.md) - 服务器 API 接口文档
- [QUICKSTART.md](QUICKSTART.md) - 详细快速开始指南
- [REMOTE_SYNC_TUTORIAL.md](REMOTE_SYNC_TUTORIAL.md) - 远程同步使用教程

---

## 📄 许可证

本项目仅用于 CVTE 嵌入式实习项目学习目的。

---

## 👤 联系方式

如有问题，请联系项目负责人。
