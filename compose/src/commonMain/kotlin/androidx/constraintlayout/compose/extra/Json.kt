@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package androidx.constraintlayout.compose.extra

import kotlinx.serialization.json.JsonArrayBuilder
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.add
import kotlinx.serialization.json.put

internal class JSONObject {
    val jsonObjectBuilder = JsonObjectBuilder()

    fun put(key: String, value: String): JSONObject {
        jsonObjectBuilder.put(key = key, value = value)
        return this
    }

    fun put(key: String, value: Number): JSONObject {
        jsonObjectBuilder.put(key = key, value = value)
        return this
    }

    fun put(key: String, value: Boolean): JSONObject {
        jsonObjectBuilder.put(key = key, value = value)
        return this
    }

    fun put(key: String, element: JSONArray): JSONObject {
        jsonObjectBuilder.put(key = key, element = element.jsonArrayBuilder.build())
        return this
    }

    fun put(key: String, element: JSONObject): JSONObject {
        jsonObjectBuilder.put(key = key, element = element.jsonObjectBuilder.build())
        return this
    }

    fun put(key: String, value: Any): JSONObject {
        jsonObjectBuilder.put(key = key, value = value.toString())
        return this
    }

    override fun toString(): String = jsonObjectBuilder.build().toString()
}

internal class JSONArray {
    val jsonArrayBuilder = JsonArrayBuilder()

    fun put(element: JSONObject): JSONArray {
        jsonArrayBuilder.add(element.jsonObjectBuilder.build())
        return this
    }

    fun put(value: String): JSONArray {
        jsonArrayBuilder.add(value)
        return this
    }

    override fun toString(): String = jsonArrayBuilder.build().toString()
}
