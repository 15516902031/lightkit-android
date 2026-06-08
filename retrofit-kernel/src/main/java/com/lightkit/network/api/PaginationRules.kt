package com.lightkit.network.api

/**
 * 分页「是否还有下一页」的统一判定。
 *
 * **约定**：接口字段 `total` 表示**符合条件的记录总条数**，不是总页数。
 * 是否还有下一页：`loadedCount < total`（且本页 [rows] 非空）。
 *
 * 服务端可能返回 `total > 0` 但 `rows` 为空（过滤、越界等），
 * 仅看 total 会导致无限上拉，必须同时结合本页 [currentPageSize]。
 */
object PaginationRules {

    /**
     * @param loadedCount 当前已加载的记录总条数（合并去重后）
     * @param total 服务端返回的**记录总条数**（非总页数）
     * @param currentPageSize 本页 `rows` 实际返回条数
     * @param pageSize 请求参数 `pageSize`；当 [total] 无效时用于估算是否可能还有下一页
     */
    fun hasMore(
        loadedCount: Int,
        total: Int,
        currentPageSize: Int,
        pageSize: Int = 0,
    ): Boolean {
        if (currentPageSize <= 0) return false
        return if (total > 0) {
            loadedCount < total
        } else {
            pageSize > 0 && currentPageSize >= pageSize
        }
    }
}

fun ApiRowsPage<*>.hasMorePages(loadedCount: Int, pageSize: Int = 0): Boolean =
    PaginationRules.hasMore(
        loadedCount = loadedCount,
        total = total,
        currentPageSize = rows.size,
        pageSize = pageSize,
    )
