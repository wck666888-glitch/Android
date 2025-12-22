# TV IR Remote Simulator - 快速开始指南

## 📋 项目概览

**目标**: 开发 Android TV 红外遥控器模拟应用  
**评分**: 基本需求(50分) + 扩展功能(30分) + 代码质量(20分) = 100分  
**技术栈**: Kotlin + Android SDK + Material Design 3

## 🎯 核心功能

### 基本需求 ✅ (50分)
1. ✅ 遥控器 UI 布局 (数字/方向/功能键)
2. ✅ IR 信号发射 (NEC 协议, Header=0x8890)
3. ✅ TV 正常响应
4. ✅ Git 提交规范

### 扩展需求 🚀 (30分)
5. 📝 配置编辑功能 (导入/导出)
6. 🌐 远程服务器同步

### 代码质量 ⭐ (20分)
7. 🏗️ MVVM 架构
8. 📖 代码规范
9. 🎨 用户体验

## 🛠️ 技术架构

```
┌─────────────────────────────────────────┐
│         UI Layer (Activity/Fragment)     │
│  RemoteControlActivity, ConfigEditor     │
└──────────────────┬──────────────────────┘
                   │
┌──────────────────▼──────────────────────┐
│       ViewModel Layer (Business Logic)   │
│  RemoteControlViewModel, ConfigViewModel │
└──────────────────┬──────────────────────┘
                   │
┌──────────────────▼──────────────────────┐
│      Repository Layer (Data Management)  │
│        ConfigRepository                  │
└──────┬───────────────────────────┬───────┘
       │                           │
┌──────▼──────┐            ┌──────▼──────┐
│ IR Module   │            │  Network    │
│  IRManager  │            │   Retrofit  │
│ NECEmitter  │            │             │
└─────────────┘            └─────────────┘
```

## 📁 项目结构

```
TV IR Remote Simulator/
├── .agent/
│   └── workflows/
│       └── project-rules.md        # 📌 项目开发规范
├── app/
│   ├── src/main/
│   │   ├── java/com/cvte/irremote/
│   │   │   ├── ui/                 # Activity, Fragment
│   │   │   ├── viewmodel/          # ViewModel
│   │   │   ├── model/
│   │   │   │   ├── entity/         # IRKey, IRConfig
│   │   │   │   └── repository/     # ConfigRepository
│   │   │   ├── ir/                 # IRManager, NECEmitter
│   │   │   ├── network/            # API Service
│   │   │   └── utils/              # Logger, Preferences
│   │   ├── res/
│   │   │   ├── layout/             # XML 布局文件
│   │   │   └── values/             # strings, colors
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
├── server/                         # 配置服务器 (Node.js)
│   ├── server.js
│   └── configs/                    # JSON 配置文件
├── README.md
└── API.md
```

## 🔑 关键按键码值 (部分)

| 按键 | KeyCode | 说明 |
|------|---------|------|
| KEY_POWER | 0x0001 | 电源开关 |
| KEY_0 ~ KEY 9 | 0x0010, 0x0000, 0x0002... | 数字键 |
| KEY_MENU | 0x0046 | 菜单 |
| KEY_BACK | 0x004A | 返回 |
| KEY_ENTER | 0x0057 | 确认 |
| KEY_UP | 0x0056 | 方向上 |
| KEY_DOWN | 0x0050 | 方向下 |
| KEY_LEFT | 0x0047 | 方向左 |
| KEY_RIGHT | 0x004B | 方向右 |
| KEY_VOLUMEUP | 0x0044 | 音量+ |
| KEY_VOLUMEDOWN | 0x0045 | 音量- |

**完整码值表**: 见项目文档

## 🚀 开发步骤

### 第一步: 创建 Android 项目
```bash
# 使用 Android Studio 创建新项目
# - 模板: Empty Activity
# - 语言: Kotlin
# - 最低 SDK: API 21 (Android 5.0)
# - 包名: com.cvte.irremote
```

### 第二步: 配置依赖
```kotlin
// build.gradle.kts (app)
dependencies {
    // Material Design
    implementation("com.google.android.material:material:1.11.0")
    
    // Gson
    implementation("com.google.code.gson:gson:2.10.1")
    
    // Retrofit (扩展需求)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    
    // ViewModel & LiveData
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
}
```

### 第三步: 实现 IR 核心模块
1. 创建 `NECIREmitter.kt` - NEC 协议编码器
2. 创建 `IRManager.kt` - IR 管理器单例
3. 实现信号发射逻辑

### 第四步: 设计 UI 布局
1. 设计 `activity_remote_control.xml`
2. 实现 `RemoteControlActivity.kt`
3. 绑定按键点击事件

### 第五步: 实现配置管理
1. 创建数据模型 `IRKey`, `IRConfig`
2. 实现 `ConfigRepository`
3. 开发配置编辑界面

### 第六步: 远程同步 (扩展)
1. 实现 Node.js 配置服务器
2. 创建 Retrofit API 接口
3. 实现后台同步逻辑

### 第七步: 测试与优化
1. 单元测试
2. 真机 TV 测试
3. UI/UX 优化

## ✅ Git 提交规范

### 提交模板
```
[关键字] 英文简单描述

[what] 详细说明修改什么问题
[why] 详细说明问题产生原因和(或)为什么做这个修改
[how] 详细说明是怎么修改的
```

### 示例
```
[feature] Implement NEC IR emitter

[what] 实现 NEC 协议的 IR 信号发射功能
[why] 完成基本需求中的红外信号发射要求,支持与 CVTE TV 通信
[how] 使用 ConsumerIrManager API,根据 NEC 协议时序编码 Header 和 KeyCode
```

### 关键字
- `bugfix` - Bug 修复
- `feature` - 新功能
- `config` - 配置修改
- `merge` - 分支合并
- `revert` - 还原提交

## 📊 IR 协议详解

### NEC 协议参数
- **载波频率**: 38kHz
- **Protocol**: 0x01
- **Header**: 0x8890
- **数据格式**: [Header 16bit] + [KeyCode 16bit]

### 时序编码
```
Header: 
  - Leading Pulse: 9ms (高电平)
  - Space: 4.5ms (低电平)

Data Bit:
  - Bit 0: 560μs + 560μs
  - Bit 1: 560μs + 1680μs
```

### 发射流程
```kotlin
// 1. 检测设备支持
val hasIR = packageManager.hasSystemFeature(PackageManager.FEATURE_CONSUMER_IR)

// 2. 获取 IR Manager
val irManager = getSystemService(Context.CONSUMER_IR_SERVICE) as ConsumerIrManager

// 3. 编码信号
val pattern = encodeNEC(header = 0x8890, keyCode = 0x0001)

// 4. 发射
irManager.transmit(38000, pattern)  // 38kHz
```

## 🎨 UI 设计要点

### 布局原则
- **分组清晰**: 数字区、方向区、功能区
- **Material Design**: 使用 MaterialButton
- **触摸反馈**: Ripple 效果
- **适配**: 横屏/竖屏响应式布局

### 颜色主题
```xml
<!-- colors.xml -->
<color name="primary">#1976D2</color>
<color name="on_primary">#FFFFFF</color>
<color name="surface">#F5F5F5</color>
<color name="button_power">#E53935</color>
```

## 🧪 测试检查清单

### 基本功能
- [ ] 所有按键可点击且有视觉反馈
- [ ] IR 信号成功发射 (检测设备支持)
- [ ] TV 响应正确 (电源、音量、频道等)

### 扩展功能
- [ ] 配置新建/编辑/删除/切换
- [ ] 配置导入导出 JSON
- [ ] 远程配置拉取成功

### 代码质量
- [ ] 架构清晰 (MVVM)
- [ ] 无编译警告
- [ ] 异常处理完善
- [ ] 注释清晰

## 📚 参考资源

### Android IR API
- [ConsumerIrManager 文档](https://developer.android.com/reference/android/hardware/ConsumerIrManager)

### NEC 协议
- [NEC IR Protocol Specification](https://techdocs.altium.com/display/FPGA/NEC+Infrared+Transmission+Protocol)

### Material Design
- [Material Design 3](https://m3.material.io/)

## 🤝 获取帮助

遇到问题时可以:
1. 查看 `.agent/workflows/project-rules.md` - 项目规范
2. 查看 `implementation_plan.md` - 详细实施计划
3. 参考 NEC 协议开源实现
4. 使用 Android Studio Logcat 调试

---

**准备好了吗? 让我们开始构建这个项目! 🚀**
