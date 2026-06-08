package com.lightkit.compose.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.launch

/**
 * 点击效果类型
 */
enum class ClickEffectType {
    /**
     * 缩放效果：按下时缩小，松开恢复
     */
    Scale,

    /**
     * 透明度效果：按下时变半透明
     */
    Alpha,

    /**
     * 组合效果：缩放 + 透明度
     */
    Combined,

    /**
     * 无效果（仅使用默认 ripple）
     */
    None
}

/**
 * 带自定义点击效果的点击修饰符
 * 修改为：点击后立即执行动画，动画结束后执行点击事件
 *
 * @param onClick 点击回调（动画结束后执行）
 * @param effectType 点击效果类型，默认为 Combined（缩放+透明度）
 * @param scaleValue 按下时的缩放比例，默认 0.95
 * @param alphaValue 按下时的透明度，默认 0.7
 * @param animationDuration 动画时长（毫秒），默认 100
 * @param enabled 是否启用点击
 */
@Composable
fun Modifier.clickableWithEffect(
    onClick: (() -> Unit)?,
    effectType: ClickEffectType = ClickEffectType.Combined,
    scaleValue: Float = 0.95f,
    alphaValue: Float = 0.7f,
    animationDuration: Int = 100,
    enabled: Boolean = true
): Modifier {
    val scope = rememberCoroutineScope()
    val scaleAnim = remember { Animatable(1f) }
    val alphaAnim = remember { Animatable(1f) }
    val currentOnClick = rememberUpdatedState(onClick)

    return this
        .graphicsLayer {
            when (effectType) {
                ClickEffectType.Scale -> {
                    scaleX = scaleAnim.value
                    scaleY = scaleAnim.value
                }
                ClickEffectType.Alpha -> {
                    alpha = alphaAnim.value
                }
                ClickEffectType.Combined -> {
                    scaleX = scaleAnim.value
                    scaleY = scaleAnim.value
                    alpha = alphaAnim.value
                }
                ClickEffectType.None -> {}
            }
        }
        .pointerInput(enabled) {
            if (!enabled) return@pointerInput
            detectTapGestures(
                onTap = { offset ->
                    // 点击后立即执行动画，动画结束后执行点击事件
                    scope.launch {
                        when (effectType) {
                            ClickEffectType.Scale -> {
                                // 缩小动画
                                scaleAnim.animateTo(
                                    targetValue = scaleValue,
                                    animationSpec = tween(durationMillis = animationDuration)
                                )
                                // 恢复动画
                                scaleAnim.animateTo(
                                    targetValue = 1f,
                                    animationSpec = tween(durationMillis = animationDuration)
                                )
                            }
                            ClickEffectType.Alpha -> {
                                // 变透明动画
                                alphaAnim.animateTo(
                                    targetValue = alphaValue,
                                    animationSpec = tween(durationMillis = animationDuration)
                                )
                                // 恢复动画
                                alphaAnim.animateTo(
                                    targetValue = 1f,
                                    animationSpec = tween(durationMillis = animationDuration)
                                )
                            }
                            ClickEffectType.Combined -> {
                                // 同时执行缩放和透明度动画
                                val scaleJob = launch {
                                    scaleAnim.animateTo(
                                        targetValue = scaleValue,
                                        animationSpec = tween(durationMillis = animationDuration)
                                    )
                                    scaleAnim.animateTo(
                                        targetValue = 1f,
                                        animationSpec = tween(durationMillis = animationDuration)
                                    )
                                }
                                val alphaJob = launch {
                                    alphaAnim.animateTo(
                                        targetValue = alphaValue,
                                        animationSpec = tween(durationMillis = animationDuration)
                                    )
                                    alphaAnim.animateTo(
                                        targetValue = 1f,
                                        animationSpec = tween(durationMillis = animationDuration)
                                    )
                                }
                                // 等待两个动画都完成
                                scaleJob.join()
                                alphaJob.join()
                            }
                            ClickEffectType.None -> {
                                // 无动画，直接执行点击事件
                            }
                        }
                        // 动画结束后执行点击事件
                        currentOnClick.value?.let { it() }
                    }
                }
            )
        }
}

/**
 * 带自定义 ripple 和点击效果的点击修饰符
 * 
 * 注意：使用 Material3 的默认 ripple 效果（不传 indication 参数）
 * Material3 会自动使用主题中的 ripple 颜色和样式
 * 
 * @param onClick 点击回调
 * @param effectType 额外的点击效果类型
 * @param scaleValue 按下时的缩放比例
 * @param alphaValue 按下时的透明度
 * @param interactionSource 交互源
 * @param enabled 是否启用点击
 */
@Composable
fun Modifier.clickableWithRippleAndEffect(
    onClick: () -> Unit,
    effectType: ClickEffectType = ClickEffectType.Scale,
    scaleValue: Float = 0.97f,
    alphaValue: Float = 0.85f,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    enabled: Boolean = true
): Modifier {
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed && (effectType == ClickEffectType.Scale || effectType == ClickEffectType.Combined)) {
            scaleValue
        } else {
            1f
        },
        animationSpec = tween(durationMillis = 120),
        label = "scale"
    )
    
    val alpha by animateFloatAsState(
        targetValue = if (isPressed && (effectType == ClickEffectType.Alpha || effectType == ClickEffectType.Combined)) {
            alphaValue
        } else {
            1f
        },
        animationSpec = tween(durationMillis = 120),
        label = "alpha"
    )
    
    return this
        .then(
            when (effectType) {
                ClickEffectType.Scale -> Modifier.scale(scale)
                ClickEffectType.Alpha -> Modifier.alpha(alpha)
                ClickEffectType.Combined -> Modifier.scale(scale).alpha(alpha)
                ClickEffectType.None -> Modifier
            }
        )
        .clickable(
            interactionSource = interactionSource,
            // 显式传 null，交由 Material 体系在上层提供默认 ripple（或你也可以传 rememberRipple()）
            indication = null,
            enabled = enabled,
            onClick = onClick
        )
}

/**
 * 可点击容器，内部内容会应用点击效果
 * 
 * @param onClick 点击回调
 * @param effectType 点击效果类型
 * @param scaleValue 按下时的缩放比例
 * @param alphaValue 按下时的透明度
 * @param modifier 修饰符
 * @param content 内容
 */
@Composable
fun ClickableBox(
    onClick: () -> Unit,
    effectType: ClickEffectType = ClickEffectType.Combined,
    scaleValue: Float = 0.95f,
    alphaValue: Float = 0.7f,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed && (effectType == ClickEffectType.Scale || effectType == ClickEffectType.Combined)) {
            scaleValue
        } else {
            1f
        },
        animationSpec = tween(durationMillis = 150),
        label = "scale"
    )
    
    val alpha by animateFloatAsState(
        targetValue = if (isPressed && (effectType == ClickEffectType.Alpha || effectType == ClickEffectType.Combined)) {
            alphaValue
        } else {
            1f
        },
        animationSpec = tween(durationMillis = 150),
        label = "alpha"
    )
    
    Box(
        modifier = modifier
            .then(
                when (effectType) {
                    ClickEffectType.Scale -> Modifier.scale(scale)
                    ClickEffectType.Alpha -> Modifier.alpha(alpha)
                    ClickEffectType.Combined -> Modifier.scale(scale).alpha(alpha)
                    ClickEffectType.None -> Modifier
                }
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        content = content
    )
}
