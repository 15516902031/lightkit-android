# LightKit Android

独立维护的 Android 基础库 Monorepo，包含：

| 模块 | Maven `artifactId` | 说明 |
|------|-------------------|------|
| **retrofit-kernel** | `retrofit-kernel` | Retrofit 网络管道、`Result` 解析、401/Toast、`callResultWithState` |
| **compose-ui-kit** | `compose-ui-kit` | Jetpack Compose 通用 UI 组件 |

## 环境要求

- JDK 17+
- `compileSdk` 36 / `minSdk` 24
- Kotlin 2.2.x

## 本地构建

```bash
./gradlew :retrofit-kernel:assembleRelease :compose-ui-kit:assembleRelease
./gradlew :compose-ui-kit:testDebugUnitTest
```

## 发布到 JitPack

### Gitee（推荐）

1. 推送本仓库到 Gitee：`https://gitee.com/<用户>/lightkit-android`
2. 打版本 tag 并推送：

```bash
git tag 1.0.0
git push gitee main
git push gitee 1.0.0
```

3. 在 [JitPack](https://jitpack.io) 触发构建：`https://jitpack.io/#gitee.com/<用户>/lightkit-android/1.0.0`

4. 业务 App 依赖：

```kotlin
maven { url = uri("https://jitpack.io") }

implementation("com.gitee.<用户>.lightkit-android:retrofit-kernel:1.0.0")
implementation("com.gitee.<用户>.lightkit-android:compose-ui-kit:1.0.0")
```

### GitHub（可选）

坐标改为 `com.github.<用户>.lightkit-android:<模块名>:<tag>`，JitPack 地址：`https://jitpack.io/#<用户>/lightkit-android/<tag>`

## Maven Local（本机调试）

```bash
./gradlew :retrofit-kernel:publishToMavenLocal :compose-ui-kit:publishToMavenLocal
```

```kotlin
mavenLocal()
implementation("com.lightkit:retrofit-kernel:1.0.0")
implementation("com.lightkit:compose-ui-kit:1.0.0")
```

## 版本升级

在 `retrofit-kernel/build.gradle.kts` 与 `compose-ui-kit/build.gradle.kts` 中同步修改 `version`，提交后打新 tag 即可。

## 文档

- [LightKit 新手指南](docs/lightkit-getting-started.md)
- [retrofit-kernel/README.md](retrofit-kernel/README.md)
- [compose-ui-kit/README.md](compose-ui-kit/README.md)
