---
description: TV IR Remote Simulator 项目开发规范
---

# TV IR Remote Simulator - 项目自定义规则

## 1. 项目架构要求

### 1.1 架构模式
- **必须采用** MVVM (Model-View-ViewModel) 架构模式
- 清晰分离业务逻辑、UI 和数据层
- 使用 Android Architecture Components (ViewModel, LiveData)

### 1.2 模块划分
项目应分为以下核心模块:

```
app/
├── ui/              # UI 层 - Activity, Fragment, Adapter
├── viewmodel/       # ViewModel 层 - 业务逻辑
├── model/           # 数据模型
│   ├── entity/      # 数据实体类
│   └── repository/  # 数据仓库
├── ir/              # IR 核心功能模块
│   ├── IRManager    # IR 发射管理器
│   ├── IREncoder    # IR 编码器
│   └── IRConfig     # IR 配置管理
├── network/         # 网络模块 (扩展需求)
└── utils/           # 工具类
```

## 2. Git 提交规范

### 2.1 提交格式 (强制执行)
```
[关键字] 英文简单描述

[what] 详细说明修改什么问题
[why] 详细说明问题产生原因和(或)为什么做这个修改
[how] 详细说明是怎么修改的
```

### 2.2 关键字说明
- **bugfix**: 针对 BUG 的修改和完善
- **feature**: 功能和需求的修改和完善
- **config**: 客户配置和参数的修改
- **merge**: 合并其他分支
- **revert**: 还原提交

### 2.3 提交原则
- **小步快跑**: 每个小改动都进行一次提交
- **单一职责**: 一次提交只做一件事
- **描述清晰**: 详细说明 what/why/how

示例:
```
[feature] Add IR emission functionality

[what] 实现 IR 信号发射功能
[why] 完成基本需求中的 IR 码值发射要求
[how] 使用 ConsumerIrManager API 实现 NEC 协议编码和发射
```

## 3. IR 协议实现规范

### 3.1 协议参数
基于项目要求的 IR 配置:
- **Protocol**: 0x01 (NEC 协议)
- **Header**: 0x8890
- **数据格式**: Header (16bit) + KeyCode (16bit)

### 3.2 编码实现
```java
// IR 发射频率: 38kHz
// NEC 协议时序:
// - Header: 9ms + 4.5ms
// - Bit 0: 560μs + 560μs
// - Bit 1: 560μs + 1680μs
```

## 4. 代码规范

### 4.1 命名规范
- **类名**: 大驼峰 (PascalCase) - `IRManager`, `RemoteControlActivity`
- **方法/变量**: 小驼峰 (camelCase) - `emitIRSignal()`, `keyCodeMap`
- **常量**: 全大写下划线 (UPPER_SNAKE_CASE) - `IR_FREQUENCY`, `PROTOCOL_NEC`
- **资源文件**: 小写下划线 - `activity_remote_control.xml`, `btn_power.xml`

### 4.2 注释要求
- **类注释**: 说明类的职责和用途
- **方法注释**: 复杂方法必须注释参数、返回值、异常
- **关键逻辑**: 添加行内注释解释算法和业务逻辑

### 4.3 代码质量
- **DRY 原则**: 避免重复代码
- **单一职责**: 每个类/方法只做一件事
- **异常处理**: 必须捕获并处理可能的异常
- **资源释放**: 及时释放系统资源

## 5. UI/UX 设计规范

### 5.1 遥控器布局
- 参考 CVTE 工厂遥控器设计
- 按键分组清晰: 数字区、方向区、功能区
- 支持横屏/竖屏适配

### 5.2 用户反馈
- **按键按下**: 视觉反馈 (高亮/阴影)
- **发射成功**: Toast 提示
- **发射失败**: 错误提示 + 原因说明
- **配置变更**: SnackBar 通知

### 5.3 交互体验
- 按键响应时间 < 100ms
- 流畅的动画效果
- 清晰的操作指引

## 6. 扩展性设计要求

### 6.1 配置管理
- **配置文件格式**: JSON/XML
- **支持多配置**: 可切换不同 IR 遥控配置
- **导入导出**: 支持本地文件和远程服务器

### 6.2 接口设计
```java
// IR 发射器接口 - 支持扩展不同协议
interface IIREmitter {
    boolean emit(int keyCode);
    boolean isSupported();
}

// 配置加载器接口 - 支持多种配置源
interface IConfigLoader {
    IRConfig loadConfig();
    boolean saveConfig(IRConfig config);
}
```

## 7. 测试要求

### 7.1 单元测试
- IR 编码逻辑测试
- 配置解析测试
- 数据转换测试

### 7.2 集成测试
- 真机 TV 响应测试
- 所有按键功能测试
- 异常场景测试

## 8. 文档要求

### 8.1 必须提供
- README.md: 项目介绍、环境配置、运行说明
- API.md: 接口文档 (扩展需求)
- CHANGELOG.md: 版本变更记录

### 8.2 代码内文档
- 复杂算法的实现说明
- 关键业务逻辑的流程图
- IR 协议的技术细节

## 9. 评分优化建议

### 9.1 基本需求 (50分)
- ✅ UI 美观、布局合理
- ✅ IR 发射稳定可靠
- ✅ TV 响应正常
- ✅ Git 提交规范

### 9.2 扩展需求 (30分)
- ✅ 配置编辑功能完善
- ✅ 远程服务器实现完整
- ✅ 错误处理健壮

### 9.3 代码质量 (20分)
- ✅ 架构清晰、解耦良好
- ✅ 代码规范、可读性强
- ✅ 异常处理完善
- ✅ 用户体验优秀

## 10. 技术栈建议

- **语言**: Java / Kotlin (推荐 Kotlin)
- **最低 SDK**: API 21 (Android 5.0) - 支持 IR API
- **目标 SDK**: API 34 (Android 14)
- **依赖库**:
  - Gson / Moshi (JSON 解析)
  - Retrofit (网络请求 - 扩展需求)
  - Material Design Components (UI)
  - JUnit / Espresso (测试)
