# 业务工程迁移指南

## 已在本仓库完成

- 新增模块 `:retrofit-kernel`（`com.lightkit:retrofit-kernel`）
- `ApiCall` / `ApiResponseWrapper` 等迁至库，app 通过 `NetworkAliases.kt` 保持旧 import
- `HourNetworkCallbacks` 对接 `AuthTokenStore` / `GlobalApiErrorEvents`
- `NetworkClient` 使用 `RetrofitFactory`
- **样板**：`SocialRepository` 已删除私有 `callJson`，统一 `apiCall.callJson`

## 其余 Repository 迁移步骤（每个文件约 10～20 分钟）

1. 删除文件底部的 `callJson` / `unwrapJsonApiData` / `extractArray` 私有方法  
2. 增加 import：

```kotlin
import com.lightkit.network.call.JsonCallOptions
import com.lightkit.network.json.JsonResponseParser
import com.lightkit.network.json.intOrZero // 如需要
```

3. 替换调用：

| 旧 | 新 |
|----|-----|
| `callJson { api.xxx() }` | `apiCall.callJson { api.xxx() }` |
| `callJsonSuccessCodes(0,1,200) { ... }` | `apiCall.callJson(JsonCallOptions(successCodes = setOf(0,1,200))) { ... }` |
| `callJsonWithoutGlobalNotify { ... }` | `apiCall.callJson(JsonCallOptions(notifyGlobalError = false)) { ... }` |
| `extractArray(data)` | `JsonResponseParser.extractArray(data)` |

4. 新接口优先补 `XxxResponse : ApiResponseWrapper<XxxDto>`，不再新增 `Response<JsonObject>`。

## 强类型全量迁移

`callJson` 收拢完成后，下一步按模块将 JsonObject 改为 Gson DTO + `apiCall.call`。  
**完整阶段计划、任务清单与工时估算**见 [docs/converter-migration-plan.md](../docs/converter-migration-plan.md)。

## 独立仓库发布

本模块已位于独立仓库 [lightkit-android](../README.md)。JitPack 坐标示例：

```kotlin
implementation("com.github.<用户>.lightkit-android:retrofit-kernel:1.0.0")
```

业务工程本地联调可在 `local.properties` 设置 `lightkit.localPath=../lightkit-android`。
