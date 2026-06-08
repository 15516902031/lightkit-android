package com.lightkit.network.json

import com.google.gson.JsonObject

fun JsonObject.str(vararg keys: String): String? =
    keys.firstNotNullOfOrNull { k ->
        val e = get(k) ?: return@firstNotNullOfOrNull null
        if (e.isJsonNull) null else runCatching { e.asString }.getOrNull()?.takeIf { it.isNotBlank() }
    }

fun JsonObject.intOrNull(vararg keys: String): Int? =
    keys.firstNotNullOfOrNull { k ->
        val e = get(k) ?: return@firstNotNullOfOrNull null
        if (e.isJsonNull) null else runCatching { e.asInt }.getOrNull()
    }

fun JsonObject.longOrNull(vararg keys: String): Long? =
    keys.firstNotNullOfOrNull { k ->
        val e = get(k) ?: return@firstNotNullOfOrNull null
        if (e.isJsonNull) null else runCatching { e.asLong }.getOrNull()
    }

fun JsonObject.boolOrNull(vararg keys: String): Boolean? =
    keys.firstNotNullOfOrNull { k ->
        val e = get(k) ?: return@firstNotNullOfOrNull null
        if (e.isJsonNull) null else runCatching { e.asBoolean }.getOrNull()
    }

fun JsonObject.doubleOrNull(vararg keys: String): Double? =
    keys.firstNotNullOfOrNull { k ->
        val e = get(k) ?: return@firstNotNullOfOrNull null
        if (e.isJsonNull) null else runCatching { e.asDouble }.getOrNull()
    }

fun JsonObject.idString(vararg keys: String): String? =
    keys.firstNotNullOfOrNull { k ->
        val e = get(k) ?: return@firstNotNullOfOrNull null
        if (e.isJsonNull) return@firstNotNullOfOrNull null
        runCatching {
            when {
                e.isJsonPrimitive && e.asJsonPrimitive.isString ->
                    e.asString.trim().takeIf { it.isNotEmpty() }
                e.isJsonPrimitive && e.asJsonPrimitive.isNumber ->
                    e.asNumber.toString()
                else -> null
            }
        }.getOrNull()
    }

fun JsonObject.intOrZero(vararg keys: String): Int = intOrNull(*keys) ?: 0

fun normalizeUrl(raw: String?): String? {
    val url = raw?.trim().takeIf { !it.isNullOrEmpty() } ?: return null
    return if (url.startsWith("http://") || url.startsWith("https://")) url else "https://$url"
}
