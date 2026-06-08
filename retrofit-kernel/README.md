# retrofit-kernel

基于 **Retrofit + OkHttp + Gson + 协程** 的 Android 网络内核：统一 `{ code, msg, data }` 解析为 `Result<T>`，可注入鉴权与错误处理。

> **第一次接入？** 请从 **[LightKit 新手指南](../docs/lightkit-getting-started.md)** 开始（`NetworkCallbacks` → `RetrofitFactory` → `ApiCall` → `callResultWithState`）。

---

## 快速开始（4 步）

### 1. 依赖

```kotlin
implementation(project(":retrofit-kernel"))
```

### 2. 实现 `NetworkCallbacks`

```kotlin
class AppNetworkCallbacks(private val context: Context) : NetworkCallbacks {
    override fun authTokenOrNull(): String? = TokenStore.getToken()
    override fun resolveErrorMessage(code: Int, rawMessage: String?) = rawMessage ?: "请求失败"
    override fun onUnauthorized(message: String) { TokenStore.clear() /* 跳登录 */ }
    override fun onBusinessError(message: String) { /* Toast */ }
}
```

字段说明见 [新手指南 · 1.1](../docs/lightkit-getting-started.md#11-实现-networkcallbacks)。

### 3. 创建 Retrofit 与 ApiCall

```kotlin
val callbacks = AppNetworkCallbacks(context)
val retrofit = RetrofitFactory.create(
    callbacks = callbacks,
    config = RetrofitFactoryConfig(
        baseUrl = "https://api.example.com/",
        debugLogging = BuildConfig.DEBUG,
    ),
)
val api = retrofit.create(ApiService::class.java)
val apiCall = ApiCall(callbacks)
```

### 4. ViewModel

```kotlin
import com.lightkit.network.ui.LoadingErrorState
import com.lightkit.network.ui.callResultWithState

callResultWithState(
    uiState = _uiState,
    updateState = { s, loading, error -> s.copy(loading = loading, errorMessage = error) },
    apiRequest = { repository.load() },
    onSuccess = { data -> /* … */ },
)
```

---

## 特性

- 统一业务体解析 → `Result<T>`
- 强类型 `ApiResponseWrapper`、分页 `callRows`
- 遗留 `JsonObject`：`callJson` + `JsonResponseParser`（过渡用）
- 可注入 `NetworkCallbacks`（鉴权、文案、登出）
- OkHttp：`AuthHeaderInterceptor`、`HttpStatusMessageInterceptor`
- `RetrofitFactory` 支持自定义拦截器 / Converter / 日志
- ViewModel：`callResultWithState`

---

## 依赖方式

| 方式 | 说明 |
|------|------|
| 本地模块 | `implementation(project(":retrofit-kernel"))` |
| Maven Local | `com.lightkit:retrofit-kernel:1.0.0` |
| JitPack | 见 [新手指南 · 方式 B](../docs/lightkit-getting-started.md#方式-b远程依赖jitpack--maven) |

---

## 扩展 OkHttp / Retrofit

```kotlin
RetrofitFactory.create(
    callbacks = callbacks,
    config = RetrofitFactoryConfig(
        baseUrl = "https://api.example.com/",
        interceptors = InterceptorConfig(
            applicationInterceptorsAfterBuiltIn = listOf(myInterceptor),
        ),
        logging = LoggingConfig(
            enabled = BuildConfig.DEBUG,
            customInterceptor = chuckerInterceptor, // 可选
        ),
        converterFactories = listOf(/* 自定义 Converter */),
        useDefaultGsonConverter = false, // 关闭默认 Gson 时需自备 Factory
    ),
)
```

| 扩展点 | 说明 |
|--------|------|
| `interceptors.networkInterceptors` | `addNetworkInterceptor` |
| `interceptors.applicationInterceptorsBeforeBuiltIn` | 内置鉴权之前 |
| `interceptors.applicationInterceptorsAfterBuiltIn` | 内置鉴权之后 |
| `logging.customInterceptor` | 替换 `HttpLoggingInterceptor` |
| `converterFactories` | 自定义序列化 |
| `enableBuiltInAuthInterceptor` | 关闭后自行鉴权 |

---

## 接口声明示例

**推荐（强类型）：**

```kotlin
data class UserResponse(
    override val code: Int?,
    override val msg: String?,
    override val data: UserDto?,
) : ApiResponseWrapper<UserDto>

@GET("user/info")
suspend fun getUser(): Response<UserResponse>
```

```kotlin
apiCall.call { api.getUser() }.map { it.toDomain() }
```

**过渡（JsonObject）：**

```kotlin
apiCall.callJson { api.legacyEndpoint() }
    .map { JsonResponseParser.extractArray(it) }
```

分页、`total`/`rows` 约定见 [network-framework.md](../docs/network-framework.md)。

---

## 与 compose-ui-kit

UI 层列表刷新使用 `compose-ui-kit` 的 `RefreshLoadState` + `RefreshLoadList`；状态字段与 `LoadingErrorState` 对齐。  
联合接入见 [LightKit 新手指南](../docs/lightkit-getting-started.md)。

---

## 与 Net 的差异

| 项目 | Net | retrofit-kernel |
|------|-----|-----------------|
| UI 自动绑定列表/缺省页 | 有 | 无（交给 Compose/ViewModel） |
| 学习成本 | DSL + 全家桶 | 仅网络管道 |
| 依赖 | 较重 | Retrofit 生态 |

---

## 相关文档

- [LightKit 新手指南](../docs/lightkit-getting-started.md) — **推荐入口**
- [network-framework.md](../docs/network-framework.md) — 分页、数据流、迁移说明
- [compose-ui-kit/README.md](../compose-ui-kit/README.md)

---

## 发布

`com.lightkit:retrofit-kernel:1.0.0`

```bash
./gradlew :retrofit-kernel:publishToMavenLocal
```

## 许可证

与宿主工程约定一致（默认 Apache-2.0）。
