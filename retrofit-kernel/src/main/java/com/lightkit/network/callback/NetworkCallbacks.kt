package com.lightkit.network.callback

/**
 * 宿主 App 注入的网络侧回调（鉴权、文案、全局 Toast、登出清理）。
 */
interface NetworkCallbacks {

    /** HTTP 或业务 code 对应的用户可见文案。 */
    fun resolveErrorMessage(code: Int, rawMessage: String?): String

    /** 业务 code=401：清理本地会话并通知 UI 跳转登录。 */
    fun onUnauthorized(message: String)

    /** 非 401 的业务/网络错误：可选全局 Toast 等。 */
    fun onBusinessError(message: String)

    /**
     * 可选：重写 HTTP 状态码对应的 OkHttp [okhttp3.Response.message]。
     * 返回 null 表示不替换。
     */
    fun httpStatusMessageOrNull(httpCode: Int): String? = null

    /** 请求头 Bearer Token；未登录返回 null。 */
    fun authTokenOrNull(): String? = null
}
