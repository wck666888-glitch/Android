# IR 远程配置同步功能使用教程

本教程详细介绍了如何设置和使用 "实时拉取远端服务器 IR 配置" 功能。

---

## 1. 系统架构概览

该功能采用 **Client-Server** 架构：
- **服务端 (Server)**: 一个轻量级的 Node.js/Express 服务，存储并提供 IR 配置 JSON 文件。
- **客户端 (App)**: Android 应用通过 Retrofit 请求接口，将配置下载并应用到本地存储。

---

## 2. 服务端部署指南

在同步之前，您需要先启动伴随的后端服务。

### 2.1 环境准备
确保您的电脑已安装 [Node.js](https://nodejs.org/) (建议版本 14.x 或更高)。

### 2.2 启动步骤
1. 打开终端（或命令提示符），进入项目的服务端目录：
   ```bash
   cd "d:/TV IR Remote Simulator/server"
   ```
2. 安装依赖：
   ```bash
   npm install
   ```
3. 启动服务器：
   ```bash
   npm start
   ```
4. **验证启动**：
   看到控制台输出 `IR Config Server is running! on port 3000` 即表示成功。

### 2.3 管理远端配置
- 服务器的配置文件存储在 `server/configs/` 目录下（JSON 格式）。
- 您可以直接在该目录下放入新的 `.json` 文件，或者通过 Postman 调用 `POST /api/configs` 接口上传。

---

## 3. App 端操作指南

### 3.1 网络配置 (物理机测试必读)
- **模拟器测试**：App 默认指向 `http://10.0.2.2:3000`，无需修改。
- **物理机测试**：
  1. 确保手机与电脑在同一 Wi-Fi 下。
  2. 修改 `app/src/main/java/com/cvte/irremote/network/RetrofitClient.kt` 中的 `BASE_URL` 为您电脑的局域网 IP（例如 `http://192.168.1.5:3000/`）。
  3. 重新编译并运行。

### 3.2 执行同步
1. 打开 **TV IR Remote Simulator** 应用。
2. 点击标题栏右上角的 **设置 (齿轮)** 按钮。
3. 选择 **"同步配置" (Sync Config)**。
4. **观察状态**：
   - 底部状态提示会显示 `"正在同步配置..."`。
   - 同步成功后，提示将变为 `"配置同步成功"`。

### 3.3 切换并查看新配置
1. 点击右上角菜单，选择 **"配置设定"**。
2. 在弹出的列表中，您将看到刚才从服务器拉取的所有新配置名称。
3. 点击目标配置即可完成实时切换。

---

---

## 4. 高级 API 使用 (POST 与 DELETE)

由于浏览器默认只支持 GET 请求，测试 POST 和 DELETE 建议使用 **Postman**、**Insomnia** 或 **cURL**。

### 4.1 上传新配置 (POST)
**URL**: `http://172.19.234.87:3000/api/configs`
**Method**: `POST`
**Headers**: `Content-Type: application/json`
**Body (JSON)**:
```json
{
  "id": "new_tv_config",
  "name": "卧室电视遥控器",
  "protocol": 1,
  "header": 36900,
  "keys": [
    {
      "key_name": "KEY_POWER",
      "key_code": 1,
      "display_name": "开关",
      "category": "function"
    }
  ]
}
```
**cURL 命令示例**:
```bash
curl -X POST http://172.19.234.87:3000/api/configs \
     -H "Content-Type: application/json" \
     -d '{"id":"new_tv_config","name":"卧室电视遥控器","protocol":1,"header":36900,"keys":[]}'
```

---

### 4.2 删除配置 (DELETE)
**URL**: `http://172.19.234.87:3000/api/configs/:id`
**Method**: `DELETE`

**cURL 命令示例** (删除演示配置):
```bash
curl -X DELETE http://172.19.234.87:3000/api/configs/remote_demo_config
```

---

## 5. 常见问题排查 (FAQ)

| 问题描述 | 可能原因 | 解决方法 |
| :--- | :--- | :--- |
| 同步时提示 "请检查网络" | 服务器未启动 | 确保执行了 `npm start` 且端口 3000 未被占用。 |
| 同步时无响应 | IP 地址不通 | 若使用真机，请检查局域网连接及电脑防火墙设置。 |
| 同步后配置列表没更新 | 缓存未刷新 | 尝试关闭并重新打开 "配置设定" 对话框，或重启应用。 |

---

## 5. 开发者建议
- **自动化**：可以通过配置 `WorkManager` 实现定时自动同步，目前应用采用手动触发方式以节省电量。
- **安全性**：生产环境下建议在 `RetrofitClient` 中加入鉴权拦截器。

---
*制作：Antigravity AI 团队*
