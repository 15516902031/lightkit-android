package com.lightkit.network.json

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject

/**
 * 从 `{ code, msg, data }` 或兼容结构中取出业务 payload。
 */
object JsonResponseParser {

    fun unwrapData(body: JsonObject): JsonElement? {
        val data = body.get("data")
        if (data != null && !data.isJsonNull) return data
        if (body.has("rows") || body.has("list") || body.has("records")) return body
        return null
    }

    fun extractArray(data: JsonElement?): JsonArray {
        if (data == null || data.isJsonNull) return JsonArray()
        if (data.isJsonArray) return data.asJsonArray
        if (data.isJsonObject) {
            val obj = data.asJsonObject
            return obj.get("rows")?.takeIf { it.isJsonArray }?.asJsonArray
                ?: obj.get("list")?.takeIf { it.isJsonArray }?.asJsonArray
                ?: obj.get("records")?.takeIf { it.isJsonArray }?.asJsonArray
                ?: JsonArray()
        }
        return JsonArray()
    }

    fun extractTotal(data: JsonElement?, fallbackSize: Int = 0): Int {
        if (data == null || !data.isJsonObject) return fallbackSize
        return data.asJsonObject.intOrNull("total", "count") ?: fallbackSize
    }

    /** 与 [extractArray] 相同，返回 Kotlin List（便于 mapNotNull）。 */
    fun extractArrayElements(data: JsonElement?): List<JsonElement> =
        extractArray(data).toList()
}
