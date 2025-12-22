# TV IR Remote Simulator

## 项目概述

CVTE TV嵌入式实习项目 - TV红外遥控器模拟器 Android 应用。

本应用通过手机红外发射器模拟电视遥控器，支持 CVTE 工厂遥控器协议，可以控制支持 NEC 协议的电视设备。

## 功能特性

### 基本功能 ✅
- 📱 **遥控器UI界面** - 完整的遥控器按键布局
  - 数字键 (0-9)
  - 方向键 (上下左右 + 确认)
  - 功能键 (电源、菜单、返回、主页)
  - 音量/频道控制
  - 颜色快捷键 (红绿黄蓝)
- 📡 **IR信号发射** - 支持 NEC 协议红外信号发射
- ⚙️ **CVTE工厂遥控配置** - 预置完整的工厂遥控器码值

### 扩展功能 ✅
- ✏️ **配置编辑** - 支持自定义 IR 码值配置
  - 新增/修改/删除按键
  - 导入/导出 JSON 配置
  - 支持多套配置切换
- 🌐 **远程同步** - 从服务器拉取配置
  - RESTful API 服务器
  - 配置按需更新

## 技术架构

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

## 项目结构

```
TV IR Remote Simulator/
├── app/
│   ├── src/main/
│   │   ├── java/com/cvte/irremote/
│   │   │   ├── ui/                 # Activity, Adapter
│   │   │   ├── viewmodel/          # ViewModel
│   │   │   ├── model/
│   │   │   │   ├── entity/         # 数据模型
│   │   │   │   └── repository/     # 数据仓库
│   │   │   ├── ir/                 # IR核心模块
│   │   │   ├── network/            # 网络模块
│   │   │   └── utils/              # 工具类
│   │   ├── res/                    # 资源文件
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
├── server/                         # 配置服务器
│   ├── server.js
│   ├── package.json
│   └── configs/                    # 配置文件
├── .agent/workflows/
│   └── project-rules.md            # 开发规范
├── README.md
├── QUICKSTART.md
└── API.md
```

## 开发环境

### 前置要求
- Android Studio Hedgehog (2023.1.1) 或更高版本
- JDK 17
- Android SDK 34
- 支持 IR 发射的 Android 设备 (可选，用于真机测试)

### 构建项目

1. **克隆代码**
```bash
git clone <repository-url>
cd "TV IR Remote Simulator"
```

2. **使用 Android Studio 打开项目**
```
File -> Open -> 选择项目目录
```

3. **同步 Gradle**
```
等待 Gradle 同步完成
```

4. **运行应用**
```
选择设备 -> Run 'app'
```

### 运行配置服务器 (扩展功能)

```bash
cd server
npm install
npm start
```

服务器默认运行在 `http://localhost:3000`

## IR 协议说明

### NEC 协议参数
- **载波频率**: 38kHz
- **Protocol**: 0x01
- **Header**: 0x8890
- **数据格式**: `[Header 16bit] + [KeyCode 16bit]`

### 时序编码
```
Header: 
  - Leading Pulse: 9ms 高电平
  - Space: 4.5ms 低电平

Data Bit:
  - Bit 0: 560μs 高电平 + 560μs 低电平
  - Bit 1: 560μs 高电平 + 1680μs 低电平
```

## Git 提交规范

本项目执行严格的 Git Commit Log 规范：

```
[关键字] 英文简单描述

[what] 详细说明修改什么问题
[why] 详细说明问题产生原因和(或)为什么做这个修改
[how] 详细说明是怎么修改的
```

### 关键字
- **bugfix**: Bug 修复
- **feature**: 新功能
- **config**: 配置修改
- **merge**: 分支合并
- **revert**: 还原提交

### 提交示例
```
[feature] Implement NEC IR emitter

[what] 实现 NEC 协议的 IR 信号发射功能
[why] 完成基本需求中的红外信号发射要求
[how] 使用 ConsumerIrManager API，根据 NEC 协议时序编码并发射
```

## 测试

### 设备兼容性
应用需要设备支持红外发射功能 (`android.hardware.consumerir`)。

支持 IR 的常见设备:
- 小米系列 (Mi / Redmi)
- 华为系列
- 荣耀系列
- 部分三星机型

### 功能测试
1. 安装应用到支持 IR 的 Android 设备
2. 对准 CVTE TV 的红外接收器
3. 点击遥控器按键测试响应

## 许可证

本项目仅用于 CVTE 嵌入式实习项目学习目的。

## 联系方式

如有问题，请联系项目负责人。
