# LightKit 新手指南

面向**第一次使用** `retrofit-kernel` 与 `compose-ui-kit` 的 Android 项目。按顺序完成本文即可跑通「网络请求 + Compose 通用 UI」最小闭环。

---

## 这是什么？

| 模块 | 坐标 / 路径 | 作用 |
|------|-------------|------|
| **retrofit-kernel** | `:retrofit-kernel` / `com.lightkit:retrofit-kernel` | Retrofit 网络管道：`Result` 解析、401/Toast、`callResultWithState` |
| **compose-ui-kit** | `:compose-ui-kit` / `com.lightkit:compose-ui-kit` | 通用 Compose UI：列表刷新、顶栏、按钮、相册选择器等 |

两者可单独使用；推荐一起接入。  
**业务 API 接口、DTO、页面逻辑仍写在 App 里**，库只提供基础设施。

```text
┌─────────────────────────────────────────────────────────┐
│  你的 App（Activity / ViewModel / ApiService / 业务 UI） │
└───────────────┬─────────────────────┬───────────────────┘
                │                     │
        retrofit-kernel         compose-ui-kit
        （网络 + VM 状态）         （Compose 组件 + 主题）
```

---

## 环境要求

| 项 | 建议版本 |
|----|----------|
| JDK | 17+ |
| `compileSdk` | 36 |
| `minSdk` | 24+ |
| Kotlin | 2.2.x |
| Compose BOM | 与宿主 App 一致（kit 使用 `api(platform(compose-bom))` 传递） |
| Hilt | 可选；库本身不强制 |

---

## 接入方式（二选一）

### 方式 A：同仓库子模块（本 monorepo / 拷贝模块）

适合当前工程联调、改库即生效。

**1. `settings.gradle.kts`**

```kotlin
include(":retrofit-kernel")
include(":compose-ui-kit")
```

**2. App 模块 `build.gradle.kts`**

```kotlin
dependencies {
    implementation(project(":retrofit-kernel"))
    implementation(project(":compose-ui-kit"))
}
```

### 方式 B：远程依赖（JitPack / Maven）

适合**新开独立仓库**、不把两个 module 拷进工程时。

**JitPack（示例，需仓库已打 tag 并配置 `jitpack.yml`）：**

```kotlin
// settings.gradle.kts → dependencyResolutionManagement.repositories
maven { url = uri("https://jitpack.io") }

// app/build.gradle.kts
implementation("com.github.<GitHub用户>.<仓库名>:retrofit-kernel:1.0.0")
implementation("com.github.<GitHub用户>.<仓库名>:compose-ui-kit:1.2.1")
```

**Maven 本地发布（本仓库已配置 `maven-publish`）：**

```bash
./gradlew :retrofit-kernel:publishToMavenLocal
./gradlew :compose-ui-kit:publishToMavenLocal
```

```kotlin
// settings.gradle.kts
mavenLocal()

// app/build.gradle.kts
implementation("com.lightkit:retrofit-kernel:1.0.0")
implementation("com.lightkit:compose-ui-kit:1.2.1")
```

> 远程坐标以你实际发布为准；`compose-ui-kit` 当前 `group = com.lightkit`、`version = 1.2.1`；`retrofit-kernel` 仍为 `1.0.0`。

---

## 第一步：配置网络（retrofit-kernel）

### 1.1 实现 `NetworkCallbacks`

库通过该接口把 **Token、错误文案、401 登出、业务 Toast** 交给 App，库内不写死业务。

```kotlin
import android.content.Context
import com.lightkit.network.callback.NetworkCallbacks

class AppNetworkCallbacks(
    private val context: Context,
) : NetworkCallbacks {

  override fun authTokenOrNull(): String? {
      // 从 DataStore / MMKV 读取登录 token；无 token 返回 null
      return TokenStore.getToken()
  }

  override fun resolveErrorMessage(code: Int, rawMessage: String?): String {
      // 业务 code（HTTP 200 但 body.code != 0）的展示文案
      return rawMessage?.takeIf { it.isNotBlank() }
          ?: context.getString(R.string.error_unknown)
  }

  override fun onUnauthorized(message: String) {
      // 401 或业务未登录：清 token、发事件、跳登录页
      TokenStore.clear()
      // EventBus / SharedFlow 通知 UI
  }

  override fun onBusinessError(message: String) {
      // 业务失败（如 code != 0）：Toast / Snackbar
      // Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
  }

  override fun httpStatusMessageOrNull(httpCode: Int): String? {
      // 可选：HTTP 层错误文案（404、502 等）
      return when (httpCode) {
          404 -> "资源不存在"
          else -> null
      }
  }
}
```

### 1.2 创建 Retrofit

```kotlin
import com.lightkit.network.retrofit.RetrofitFactory
import com.lightkit.network.retrofit.RetrofitFactoryConfig

object NetworkClient {
    const val BASE_URL = "https://api.example.com/"  // 必须以 / 结尾

    fun createRetrofit(context: Context, callbacks: NetworkCallbacks): Retrofit {
        return RetrofitFactory.create(
            callbacks = callbacks,
            config = RetrofitFactoryConfig(
                baseUrl = BASE_URL,
                debugLogging = BuildConfig.DEBUG,
            ),
        )
    }
}
```

### 1.3 声明接口并调用

**推荐：强类型 Response**

```kotlin
import com.lightkit.network.api.ApiResponseWrapper
import com.lightkit.network.call.ApiCall

data class UserDto(val id: String, val name: String)

data class UserResponse(
    override val code: Int?,
    override val msg: String?,
    override val data: UserDto?,
) : ApiResponseWrapper<UserDto>

interface ApiService {
    @GET("user/info")
    suspend fun getUser(): retrofit2.Response<UserResponse>
}

class UserRepository(
    private val api: ApiService,
    private val apiCall: ApiCall,
) {
    suspend fun loadUser(): Result<UserDto> =
        apiCall.call { api.getUser() }.map { it }  // 已是 DTO，可再 map 到 Domain
}
```

`ApiCall` 构造：`ApiCall(networkCallbacks)`（传入与 Retrofit 相同的 `NetworkCallbacks` 实例）。

### 1.4 ViewModel 里统一 loading / error

```kotlin
import com.lightkit.network.ui.LoadingErrorState
import com.lightkit.network.ui.callResultWithState
import kotlinx.coroutines.flow.MutableStateFlow

data class UserUiState(
    val userName: String = "",
    override val loading: Boolean = false,
    override val errorMessage: String? = null,
) : LoadingErrorState

class UserViewModel(private val repo: UserRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(UserUiState())
    val uiState = _uiState.asStateFlow()

    fun load() {
        viewModelScope.launch {
            callResultWithState(
                uiState = _uiState,
                updateState = { state, loading, error ->
                    state.copy(loading = loading, errorMessage = error)
                },
                apiRequest = { repo.loadUser() },
                onSuccess = { dto ->
                    _uiState.update { it.copy(userName = dto.name) }
                },
            )
        }
    }
}
```

**注意：** 请直接 `import com.lightkit.network.ui.*`，不要复制 App 里的 deprecated 封装。

更多细节（分页 `callRows`、`callJson` 过渡、拦截器扩展）见 [network-framework.md](./network-framework.md) 与 [retrofit-kernel/README.md](../retrofit-kernel/README.md)。

---

## 第二步：配置 UI（compose-ui-kit）

### 2.1 在应用根部注入主题（必做）

多数组件通过 `LocalUiKitTheme` 读取颜色与文案；**未包裹主题会使用库内默认值**。

```kotlin
import androidx.compose.runtime.Composable
import com.lightkit.compose.ui.ProvideUiKitTheme
import com.lightkit.compose.ui.UiKitTheme

@Composable
fun AppUiKitTheme(content: @Composable () -> Unit) {
    ProvideUiKitTheme(
        theme = UiKitTheme(
            primaryGradientStart = Color(0xFFFF97EF),
            primaryGradientEnd = Color(0xFFFF65E8),
            loadMoreIndicatorColor = Color(0xFF36C10B),
            // … 按设计稿覆盖，未赋值的项用库内默认
        ),
        content = content,
    )
}
```

在 `MainActivity` / `AppRoot`：

```kotlin
setContent {
    AppUiKitTheme {
        // NavHost、业务 Theme 可套在内层
        YourApp()
    }
}
```

本仓库参考实现：`app/.../theme/HourLightUiKitTheme.kt`（从 `strings.xml` 映射 `ImagePickerStrings` 等）。

### 2.2 使用组件（直接 import 库包名）

```kotlin
import com.lightkit.compose.ui.CommonTopBar
import com.lightkit.compose.ui.RefreshLoadList
import com.lightkit.compose.ui.RefreshLoadState
import com.lightkit.compose.ui.SearchClearInputField
import com.lightkit.compose.ui.SpacerV
import com.lightkit.compose.ui.clickableWithEffect

@Composable
fun DemoScreen() {
    Column {
        CommonTopBar(title = "标题", onBack = { /* … */ })
        SearchClearInputField(
            value = keyword,
            onValueChange = { keyword = it },
            placeholder = "搜索",
            showSearchIcon = true,
        )
        // …
    }
}
```

### 2.3 下拉刷新列表（最常见）

```kotlin
import com.lightkit.compose.ui.RefreshLoadList
import com.lightkit.compose.ui.RefreshLoadState

@Composable
fun UserListScreen(items: List<User>, loadState: RefreshLoadState, …) {
    RefreshLoadList(
        items = items,
        loadState = loadState,
        onRefresh = { viewModel.refresh() },
        onLoadMore = { viewModel.loadMore() },
        onRetryLoadMore = { viewModel.loadMore() },
    ) { user ->
        UserRow(user)
    }
}
```

ViewModel 侧维护 `RefreshLoadState`（`Idle` / `Refreshing` / `LoadingMore` / `NoMoreData` / `Error`），与分页接口配合。

### 2.4 相册选择器 ImagePicker（需额外配置）

ImagePicker 涉及**相机、相册权限、FileProvider**，除主题外还需：

**① `AndroidManifest.xml` 权限（按 targetSdk 调整）**

```xml
<uses-permission android:name="android.permission.CAMERA" />
<!-- Android 13+ -->
<uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
<!-- 更低版本 -->
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"
    android:maxSdkVersion="32" />
```

**② FileProvider（与 `hostConfig.fileProviderAuthority` 一致）**

```xml
<provider
    android:name="androidx.core.content.FileProvider"
    android:authorities="${applicationId}.fileprovider"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/file_paths" />
</provider>
```

`res/xml/file_paths.xml`：

```xml
<paths>
    <cache-path name="camera_cache" path="." />
</paths>
```

**③ 代码**

```kotlin
import android.net.Uri
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.lightkit.compose.ui.ImagePicker
import com.lightkit.compose.ui.ImagePickerConfig
import com.lightkit.compose.ui.ImagePickerHostConfig
import com.lightkit.compose.ui.MediaType
import com.lightkit.compose.ui.SelectionMode

@Composable
fun PickAvatarScreen(onPicked: (Uri) -> Unit) {
    val context = LocalContext.current

    ImagePicker(
        config = ImagePickerConfig(
            mediaType = MediaType.Image,
            selectionMode = SelectionMode.Single,
            showEmbeddedPicker = false,
        ),
        hostConfig = ImagePickerHostConfig(
            fileProviderAuthority = "${context.packageName}.fileprovider",
            showMessage = { msg ->
                // 权限拒绝等提示，可用 Toast
            },
        ),
        onMediaSelected = { uris -> uris.firstOrNull()?.let(onPicked) },
    ) { showPicker ->
        Button(onClick = showPicker) {
            Text("选择图片")
        }
    }
}
```

多选示例：

```kotlin
ImagePickerConfig(
    mediaType = MediaType.Image,
    selectionMode = SelectionMode.Multiple,
    maxSelectionCount = 9,
)
```

需要分别暴露「打开面板 / 直接相机 / 直接相册」时，使用 `ImagePickerWithActions`。

---

## 第三步：最小可运行检查清单

按顺序打勾，任意一步失败请先回到对应小节：

- [ ] `settings.gradle.kts` 已 include 两个 module（或远程依赖可解析）
- [ ] App `build.gradle.kts` 已 `implementation` 两个库
- [ ] 已实现 `NetworkCallbacks` 且 Retrofit `baseUrl` 以 `/` 结尾
- [ ] 至少一个接口通过 `ApiCall.call { }` 能返回 `Result`
- [ ] ViewModel 使用 `com.lightkit.network.ui.callResultWithState`
- [ ] `setContent { }` 外层已包 `ProvideUiKitTheme { }`
- [ ] 至少一个页面使用了库组件（如 `CommonTopBar` / `RefreshLoadList`）
- [ ] 若用 ImagePicker：Manifest 权限 + FileProvider + `ImagePickerHostConfig` 三项齐全

---

## API 速查（compose-ui-kit）

| 你想做… | 用的 API |
|---------|----------|
| 统一间距 | `SpacerH`、`SpacerV` |
| 防连点 / 涟漪点击 | `Modifier.clickableWithEffect` |
| 键盘顶起留白 | `Modifier.keyboardBottomInsetsPadding()` |
| 紧凑文字样式 | `compactTextStyle` |
| 主色渐变按钮 | `PrimaryGradientButtonTextBox`、`GradientPillButton` |
| 顶栏返回 | `CommonTopBar`（返回图标可自绘 slot） |
| 二级页基类 | `PageScaffold` + `PageUiState` / `resolvePageUiState`；主题 `UiKitTheme.pageScaffold` |
| 页空/错/载 | `PageEmpty`、`PageError`、`PageLoading` / `PageBlockingLoading` |
| 搜索框 | `SearchClearInputField` |
| 设置页开关行 | `SettingsSwitchRow` |
| 确认弹窗 | `ReminderConfirmDialog` |
| 锁定蒙层 | `LockedOverlay` |
| Tab + ViewPager 列表 | `TabPagerList`、`TabPagerRefreshList` |
| 空状态 | `EmptyState` |
| 日期滚轮 | `DateWheelPickerBottomSheet`、`parseIsoDate`、`formatIsoDate` |
| 相册 | `ImagePicker`、`ImagePickerWithActions` |

完整列表见 [compose-ui-kit/README.md](../compose-ui-kit/README.md)。

---

## 建议放在 App 里、不要放进库的内容

| 类型 | 示例 | 原因 |
|------|------|------|
| 业务 API / DTO | `ApiService`、`UserDto` | 每个 App 不同 |
| 业务弹窗文案与流程 | 兑换成功、星钻不足 | 强业务 |
| 业务徽章 / 头像框 | 财富等级、官方标识 | 与设计资源绑定 |
| SVGA / 短视频播放器 | `AppSvgaPlayer` | 重依赖、非通用 UI |
| 简单 Toast 封装 | `AppToast` | 与 Compose 无关 |

本仓库 `app/.../core/ui/common/` 中部分文件是**薄封装**（保留 `com.example.hour.light` 包名兼容旧代码）；**新项目请直接 `import com.lightkit.compose.ui.*` 与 `com.lightkit.network.ui.*`**。

---

## 常见问题

### Q1：组件颜色不对 / 像默认主题

根 Composable 未包 `ProvideUiKitTheme`，或包在了具体页面内部而非 `setContent` 外层。请把主题放在 `MainActivity` 最外层。

### Q2：`callResultWithState` 找不到 / 与 `LoadingErrorState` 冲突

统一使用：

```kotlin
import com.lightkit.network.ui.LoadingErrorState
import com.lightkit.network.ui.callResultWithState
```

### Q3：`RefreshLoadState` 在 App 和 kit 各有一份？

kit 内为 `com.lightkit.compose.ui.RefreshLoadState`。新代码请直接用 kit 类型；旧工程可能保留 App 副本 + `toKit()` 转换，迁移后可删除 App 副本。

### Q4：ImagePicker 拍照后崩溃 / 无法分享 Uri

检查 `fileProviderAuthority` 是否与 Manifest 里 `android:authorities` 完全一致（一般为 `"${applicationId}.fileprovider"`），且 `file_paths.xml` 包含 `cache-path`。

### Q5：Coil 与 Compose 版本冲突

compose-ui-kit 的 Coil 已 exclude JetBrains Compose Multiplatform 产物。App 侧请使用 **AndroidX Compose BOM** 统一版本，勿混用 `org.jetbrains.compose.*`。

### Q6：只想用网络库，不用 UI 库？

可以只依赖 `retrofit-kernel`。反之亦可只依赖 `compose-ui-kit`（需自行处理 REST 或其它网络方案）。

---

## 进一步阅读

| 文档 | 内容 |
|------|------|
| [retrofit-kernel/README.md](../retrofit-kernel/README.md) | 网络库 API、扩展 OkHttp/Converter |
| [network-framework.md](./network-framework.md) | 分页约定、`callRows`、与 Net 对比 |
| [compose-ui-kit/README.md](../compose-ui-kit/README.md) | UI 组件清单、单测命令 |
| 本仓库 `app/.../core/network/NetworkClient.kt` | Retrofit 创建示例 |
| 本仓库 `app/.../theme/HourLightUiKitTheme.kt` | 主题映射示例 |
| 本仓库 `app/.../core/ui/common/ImagePicker.kt` | ImagePicker App 侧封装参考 |

---

## 版本与发布

| 库 | 默认版本 | 发布 |
|----|----------|------|
| retrofit-kernel | `1.0.0` | `com.lightkit:retrofit-kernel` / JitPack 子模块 |
| compose-ui-kit | `1.2.1` | `com.lightkit:compose-ui-kit` / JitPack 子模块 |

升级库版本后，建议执行：

```bash
./gradlew :compose-ui-kit:testDebugUnitTest
./gradlew :app:compileDebugKotlin
```

---

**文档维护说明：** 若库 API 有增删，请同步更新本文「API 速查」与两个模块的 README。
