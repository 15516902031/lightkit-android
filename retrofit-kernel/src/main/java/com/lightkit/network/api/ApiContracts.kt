package com.lightkit.network.api

/**
 * 通用 `{ code, msg, data }` 业务包装。
 */
interface ApiResponseWrapper<T> {
    val code: Int?
    val msg: String?
    val data: T?
}

/** 无 data 的成功响应（仅 code/msg）。 */
interface NoDataResponse : ApiResponseWrapper<Unit> {
    override val data: Unit?
        get() = null
}

/**
 * 顶层分页：`{ code, msg, total, rows }`。
 *
 * [total]：符合条件的**记录总条数**（全表/全结果集条数），**不是**总页数。
 */
interface ApiRowsResponse<T> {
    val code: Int?
    val msg: String?
    /** 记录总条数，非总页数。 */
    val total: Int?
    val rows: List<T>?
}

data class ApiRowsPage<T>(
    /** 记录总条数，非总页数。 */
    val total: Int,
    val rows: List<T>,
)
