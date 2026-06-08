package com.lightkit.network.call

data class JsonCallOptions(
    val successCodes: Set<Int> = ApiCallDefaults.SUCCESS_CODES,
    val notifyGlobalError: Boolean = true,
    val logTag: String? = null,
    /** 业务成功且 `data == null` 时仍返回 [Result.success]（如「未创建家族」）。 */
    val allowNullData: Boolean = false,
)

object ApiCallDefaults {
    val SUCCESS_CODES: Set<Int> = setOf(1, 200)
}
