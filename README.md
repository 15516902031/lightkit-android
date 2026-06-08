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

## 发布到 JitPack（GitHub）

1. 推送本仓库到 GitHub（示例：`https://github.com/<用户>/lightkit-android`）
2. 打版本 tag：

```bash
git tag 1.0.0
git push origin 1.0.0
```

3. 在 [JitPack](https://jitpack.io) 触发构建：`https://jitpack.io/#<用户>/lightkit-android/1.0.0`

4. 业务 App 依赖：

```kotlin
// settings.gradle.kts → repositories
maven { url = uri("https://jitpack.io") }

// app/build.gradle.kts
implementation("com.github.<用户>.lightkit-android:retrofit-kernel:1.0.0")
implementation("com.github.<用户>.lightkit-android:compose-ui-kit:1.0.0")
```

> JitPack 多模块坐标格式：`com.github.<GitHub用户>.<仓库名>:<模块名>:<tag>`

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
