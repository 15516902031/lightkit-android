package com.lightkit.network.call

/**
 * 常用 Json 请求选项预设，供各 Repository 复用。
 */
object JsonCallPresets {

    /** code 1 / 200，失败走 [NetworkCallbacks.onBusinessError] */
    val standard: JsonCallOptions = JsonCallOptions()

    /** 兼容 code=0 的老接口 */
    val legacy: JsonCallOptions = JsonCallOptions(successCodes = setOf(0, 1, 200))

    /** 不弹全局错误（由 ViewModel 自行处理） */
    val silent: JsonCallOptions = JsonCallOptions(notifyGlobalError = false)

    /** code=0 且不弹全局错误 */
    val silentLegacy: JsonCallOptions = JsonCallOptions(
        successCodes = setOf(0, 1, 200),
        notifyGlobalError = false,
    )

    /** 同 [silentLegacy]，且允许 `data == null` 视为成功（无家族/无申请等）。 */
    val silentLegacyNullable: JsonCallOptions = silentLegacy.copy(allowNullData = true)

    /** 分页/列表 GET：兼容 code=0，不 Toast 服务端「查询成功」等文案 */
    val pageQuery: JsonCallOptions = silentLegacy

    fun legacyTagged(logTag: String): JsonCallOptions = legacy.copy(logTag = logTag)
}
