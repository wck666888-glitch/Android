# IR Config Server API Documentation

## 概述

IR Config Server 提供 RESTful API 用于管理和分发 IR 遥控器配置。

**基础URL**: `http://<server-ip>:3000`

## 接口列表

### 1. 获取配置列表

获取所有可用配置的元数据。

**请求**
```http
GET /api/configs
```

**响应**
```json
[
  {
    "id": "cvte_factory",
    "name": "CVTE工厂遥控器",
    "description": "CVTE TV工厂测试遥控器配置",
    "version": "1.0",
    "updated_at": 1703232000000
  }
]
```

---

### 2. 获取指定配置

根据配置ID获取完整配置详情。

**请求**
```http
GET /api/configs/{id}
```

**参数**
| 参数 | 类型 | 说明 |
|------|------|------|
| id | string | 配置唯一标识符 |

**响应**
```json
{
  "id": "cvte_factory",
  "name": "CVTE工厂遥控器",
  "protocol": 1,
  "header": 34960,
  "is_default": true,
  "keys": [
    {
      "key_name": "KEY_POWER",
      "key_code": 1,
      "display_name": "电源",
      "category": "function"
    }
  ]
}
```

**错误响应**
```json
{
  "error": "Config not found"
}
```

---

### 3. 获取默认配置

获取标记为默认的配置。

**请求**
```http
GET /api/configs/default
```

**响应**

返回完整的配置对象，格式同"获取指定配置"。

---

### 4. 上传/更新配置

上传新配置或更新已有配置。

**请求**
```http
POST /api/configs
Content-Type: application/json
```

**请求体**
```json
{
  "id": "my_config",
  "name": "我的遥控器",
  "protocol": 1,
  "header": 34960,
  "keys": [
    {
      "key_name": "KEY_POWER",
      "key_code": 1,
      "display_name": "电源",
      "category": "function"
    }
  ]
}
```

**响应**
```json
{
  "success": true,
  "id": "my_config"
}
```

---

### 5. 删除配置

删除指定的配置。

**请求**
```http
DELETE /api/configs/{id}
```

**参数**
| 参数 | 类型 | 说明 |
|------|------|------|
| id | string | 配置唯一标识符 |

**响应**
```json
{
  "success": true
}
```

---

### 6. 健康检查

检查服务器运行状态。

**请求**
```http
GET /health
```

**响应**
```json
{
  "status": "ok",
  "timestamp": 1703232000000
}
```

---

## 数据模型

### IRConfig (配置)

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | string | ✅ | 配置唯一标识符 |
| name | string | ✅ | 配置名称 |
| protocol | number | ✅ | 协议类型 (1=NEC) |
| header | number | ✅ | 协议头码值 |
| keys | IRKey[] | ✅ | 按键列表 |
| is_default | boolean | ❌ | 是否为默认配置 |
| description | string | ❌ | 配置描述 |
| version | string | ❌ | 版本号 |
| created_at | number | ❌ | 创建时间戳 |
| updated_at | number | ❌ | 更新时间戳 |

### IRKey (按键)

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| key_name | string | ✅ | 按键标识符 (如 KEY_POWER) |
| key_code | number | ✅ | IR码值 |
| display_name | string | ✅ | 显示名称 |
| category | string | ❌ | 分类 (number/direction/volume/channel/color/function/factory) |

---

## 错误码

| HTTP 状态码 | 说明 |
|------------|------|
| 200 | 请求成功 |
| 400 | 请求参数错误 |
| 404 | 资源未找到 |
| 500 | 服务器内部错误 |

---

## 使用示例

### cURL

**获取配置列表**
```bash
curl http://localhost:3000/api/configs
```

**获取指定配置**
```bash
curl http://localhost:3000/api/configs/cvte_factory
```

**上传配置**
```bash
curl -X POST http://localhost:3000/api/configs \
  -H "Content-Type: application/json" \
  -d '{"id":"test","name":"测试配置","protocol":1,"header":34960,"keys":[]}'
```

### Android (Retrofit)

```kotlin
interface RemoteConfigService {
    @GET("/api/configs")
    suspend fun getConfigList(): List<ConfigMetadata>
    
    @GET("/api/configs/{id}")
    suspend fun getConfig(@Path("id") id: String): IRConfig
}
```

---

## 部署说明

### 本地开发

```bash
cd server
npm install
npm start
```

### 生产部署

1. 准备服务器 (推荐 Node.js 18+)
2. 克隆代码并安装依赖
3. 使用 PM2 或 systemd 管理进程
4. 配置反向代理 (Nginx)
5. 更新 Android 应用中的服务器地址

### 环境变量

| 变量 | 默认值 | 说明 |
|------|--------|------|
| PORT | 3000 | 服务器端口 |
