# compose-ui-kit

Jetpack Compose 通用 UI 库，与 [`retrofit-kernel`](../retrofit-kernel/README.md) 配套，可独立发布。

> **第一次接入？** 请从 **[LightKit 新手指南](../docs/lightkit-getting-started.md)** 开始（Gradle → 主题 → 组件 → ImagePicker 清单）。

---

## 快速开始（3 步）

### 1. 依赖

```kotlin
// settings.gradle.kts
include(":compose-ui-kit")

// app/build.gradle.kts
implementation(project(":compose-ui-kit"))
```

远程依赖见新手指南 [方式 B](../docs/lightkit-getting-started.md#接入方式二选一)。

### 2. 根部主题（必做）

```kotlin
import com.lightkit.compose.ui.ProvideUiKitTheme
import com.lightkit.compose.ui.UiKitTheme

setContent {
    ProvideUiKitTheme(theme = UiKitTheme(/* 品牌色、文案 */)) {
        YourApp()
    }
}
```

### 3. 使用组件

```kotlin
import com.lightkit.compose.ui.CommonTopBar
import com.lightkit.compose.ui.RefreshLoadList
import com.lightkit.compose.ui.ClearableInputField
```

---

## 模块内容

| 分类 | API |
|------|-----|
| 主题 | `UiKitTheme`、`ProvideUiKitTheme` |
| 间距/点击 | `SpacerH/V`、`clickableWithEffect` |
| 键盘 | `keyboardBottomInsetsPadding` |
| 滚轮/日期 | `WheelPickerRow`、`DateWheelPickerBottomSheet`、`parseIsoDate`… |
| 文本 | `compactTextStyle` |
| 按钮 | `PrimaryButton`（主 CTA + loading）、`GradientPillButton`、`ButtonBox`、`PrimaryGradientButtonTextBox`… |
| 图片 | `AppIcon`、`AppImage`、`AppAsyncImage`（Coil） |
| 输入 | `ClearableInputField`（宽高/背景/圆角走 `modifier`，不强制全宽） |
| 设置/弹窗 | `SettingsSwitchRow`、`ReminderConfirmDialog`、`LockedOverlay` |
| 列表 | `RefreshLoadList`、`RefreshLoadGrid`、`RefreshLoadState` |
| Tab | `TabPagerList`、`TabPagerRefreshList`、`EmptyState` |
| 顶栏 | `CommonTopBar` |
| 二级页基类 | `PageScaffold`、`PageUiState`、`resolvePageUiState`、`PageEmpty` / `PageError` / `PageLoading`；主题 `UiKitTheme.pageScaffold`；可选 `LocalPageEmptyContent` / `LocalPageErrorContent` |
| 相册 | `ImagePicker`、`ImagePickerWithActions`、`ImagePickerHostConfig` |

---

## ImagePicker 必读

除主题外，还需 **Manifest 权限**、**FileProvider**、**`ImagePickerHostConfig`**。  
完整步骤见 [新手指南 · 2.4 相册选择器](../docs/lightkit-getting-started.md#24-相册选择器-imagepicker需额外配置)。

---

## 与 retrofit-kernel 协作

ViewModel 请使用网络库提供的：

- `com.lightkit.network.ui.LoadingErrorState`
- `com.lightkit.network.ui.callResultWithState`

列表页：`RefreshLoadState` + `RefreshLoadList` + `callResultWithState`。

---

## 测试

```bash
./gradlew :compose-ui-kit:testDebugUnitTest
```

`ImagePickerLogicTest`：权限列表、多选切换、初始选中等（JUnit4 + Robolectric）。

---

## 发布

| 字段 | 值 |
|------|-----|
| `groupId` | `com.lightkit` |
| `artifactId` | `compose-ui-kit` |
| `version` | `1.4.0` |

```bash
./gradlew :compose-ui-kit:publishToMavenLocal
```

---

## 相关文档

- [LightKit 新手指南](../docs/lightkit-getting-started.md) — **推荐入口**
- [retrofit-kernel/README.md](../retrofit-kernel/README.md)
- [network-framework.md](../docs/network-framework.md)
