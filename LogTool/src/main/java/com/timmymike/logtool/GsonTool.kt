package com.timmymike.logtool

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import org.json.JSONArray
import org.json.JSONObject

/**
 * Gson 的格式互轉
 *
 * */

/**
 * toData，通用型 轉換方法
 * 使用範例：
 * "{...}".toData<SampleData>
 * or
 * "[{...},{...},...]".toData<List<Record>>
 * */

inline fun <reified T> String.toData(): T? {
    return kotlin.runCatching {
        when {
            startsWith("{") -> Gson().fromJson(this, T::class.java)
            startsWith("[") -> Gson().fromJson(this@toData, object : TypeToken<T>() {}.type)
            else -> null
        }
    }.onFailure { loge("轉譯錯誤，錯誤資訊=>", it) }.getOrNull()
}

/**
 * toDataBeanList，不用再將List型別傳入，只需傳入List內的物件即可。
 * 使用範例：
 * data.toDataBeanList<Record>()
 */

inline fun <reified T> String.toDataBeanList(): List<T>? {
    return if (this.isJson()) Gson().fromJson(this, object : TypeToken<List<T>>() {}.type)
    else null
}

inline fun <reified T>  String.toDataBean(): T? {
    return if (this.isJson()) Gson().fromJson(this,  T::class.java)
    else null
}

/**
 *  直接將物件印出的鍊式表達式：
 *
 * */
fun <T : Any> T.forLogv(preString: String = "", tagName: String = logDefaultTag ?: "for Logv"): T = apply { logv(tagName, "${preString}${this@apply}") }
fun <T : Any> T.forLogd(preString: String = "", tagName: String = logDefaultTag ?: "for Logd"): T = apply { logd(tagName, "${preString}${this@apply}") }
fun <T : Any> T.forLogi(preString: String = "", tagName: String = logDefaultTag ?: "for Logi"): T = apply { logi(tagName, "${preString}${this@apply}") }
fun <T : Any> T.forLogw(preString: String = "", tagName: String = logDefaultTag ?: "for Logw"): T = apply { logw(tagName, "${preString}${this@apply}") }
fun <T : Any> T.forLoge(preString: String = "", tagName: String = logDefaultTag ?: "for Loge"): T = apply { loge(tagName, "${preString}${this@apply}") }

/**
 *  直接將物件轉為Json後印出的鍊式表達式：
 *
 * */
fun <T : Any> T.forJsonAndLogv(preString: String = "", tagName: String = logDefaultTag ?: "for Json And Logv"): String = this.toJson().apply { logv(tagName, "${preString}${this@apply}") }
fun <T : Any> T.forJsonAndLogd(preString: String = "", tagName: String = logDefaultTag ?: "for Json And Logd"): String = this.toJson().apply { logd(tagName, "${preString}${this@apply}") }
fun <T : Any> T.forJsonAndLogi(preString: String = "", tagName: String = logDefaultTag ?: "for Json And Logi"): String = this.toJson().apply { logi(tagName, "${preString}${this@apply}") }
fun <T : Any> T.forJsonAndLogw(preString: String = "", tagName: String = logDefaultTag ?: "for Json And Logw"): String = this.toJson().apply { logw(tagName, "${preString}${this@apply}") }
fun <T : Any> T.forJsonAndLoge(preString: String = "", tagName: String = logDefaultTag ?: "for Json And Loge"): String = this.toJson().apply { loge(tagName, "${preString}${this@apply}") }

fun <T : Any> T.toJson(): String {
    return GsonBuilder().disableHtmlEscaping().create().toJson(this) ?: ""
}

fun String?.isJson(): Boolean {
    if (this.isNullOrEmpty()) {
        loge("json is null or empty:: ${this.toString()}")
        return false
    }
    return runCatching { JSONObject(this) }.getOrNull() != null // 是JSONObject
            ||
            runCatching { JSONArray(this) }.getOrNull() != null // 是JSONArray

}
