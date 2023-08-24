package com.timmymike.logtool

import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.Collection

object LogOption {
    // 控制列印log日誌的每行字數
    var LOG_MAX_LENGTH = 3000

    // 每一個Log檔案可以容許的最大大小(KB)(預設為1MB)
    var MAX_LOG_FILE_SIZE = 1024

    // 裝置剩餘空間小於這個容量以後，不寫入檔案(KB)(預留可存20個最大檔案。)
    var WRITE_LOG_FREE_SPACE = 20 * MAX_LOG_FILE_SIZE

    // call這麼多次才寫入檔案一次 //以節省效能。
    var COLLECT_LOG_SIZE = 1000
}

/** 一般過多字元換行印Log方法*/

fun logv(msg: String?) {
    logv(logDefaultTag ?: "Log", msg)
}

fun logv(tagName: String, msg: String?) {
    logMsgMultiLine(msg, tagName, LogLevelType.Verbose)
}

fun logd(msg: String?) {
    logd(logDefaultTag ?: "Log", msg)
}

fun logd(tagName: String, msg: String?) {
    logMsgMultiLine(msg, tagName, LogLevelType.Debug)
}

fun logi(msg: String?) {
    logi(logDefaultTag ?: "Log", msg)
}

fun logi(tagName: String, msg: String?) {
    logMsgMultiLine(msg, tagName, LogLevelType.Info)
}

fun logw(msg: String?) {
    logw(logDefaultTag ?: "Log", msg)
}

fun logw(tagName: String, msg: String?) {
    logMsgMultiLine(msg, tagName, LogLevelType.Warning)
}

fun loge(msg: String?) {
    loge(logDefaultTag ?: "Log", msg)
}

fun loge(tagName: String, msg: String?) {
    logMsgMultiLine(msg, tagName, LogLevelType.Error)
}

fun loge(msg: String?, throwable: Throwable) {
    Log.e(logDefaultTag ?: "Log", msg, throwable)
}

/** 寫入檔案的Log方法*/

fun logWtf(filePath: File, msg: String, writeType: WriteType = WriteType.Default) {
    logWtf(filePath, logDefaultTag ?: "Log", msg, writeType)
}

fun logWtf(filePath: File, tagName: String, msg: String, writeType: WriteType = WriteType.Default) {
    writeToFile(filePath, "${getNowTimeFormat()} $tagName <Thread name:${Thread.currentThread().name}>: $msg $END_LINE", writeType)
}

/** 集合的Log (Collection和 Map)*/

fun <T : Any> Collection<T>.logvAll(preString: String = "", tagName: String = logDefaultTag ?: "") = this.forEach { logv(tagName, "${preString}${it}") }
fun <T : Any> Collection<T>.logdAll(preString: String = "", tagName: String = logDefaultTag ?: "") = this.forEach { logd(tagName, "${preString}${it}") }
fun <T : Any> Collection<T>.logiAll(preString: String = "", tagName: String = logDefaultTag ?: "") = this.forEach { logi(tagName, "${preString}${it}") }
fun <T : Any> Collection<T>.logwAll(preString: String = "", tagName: String = logDefaultTag ?: "") = this.forEach { logw(tagName, "${preString}${it}") }
fun <T : Any> Collection<T>.logeAll(preString: String = "", tagName: String = logDefaultTag ?: "") = this.forEach { loge(tagName, "${preString}${it}") }

fun <K : Any, V : Any> Map<K, V>.logvKeyAll(preString: String = "", tagName: String = logDefaultTag ?: "") = this.keys.forEach { logv(tagName, "${preString}${it}") }
fun <K : Any, V : Any> Map<K, V>.logdKeyAll(preString: String = "", tagName: String = logDefaultTag ?: "") = this.keys.forEach { logd(tagName, "${preString}${it}") }
fun <K : Any, V : Any> Map<K, V>.logiKeyAll(preString: String = "", tagName: String = logDefaultTag ?: "") = this.keys.forEach { logi(tagName, "${preString}${it}") }
fun <K : Any, V : Any> Map<K, V>.logwKeyAll(preString: String = "", tagName: String = logDefaultTag ?: "") = this.keys.forEach { logw(tagName, "${preString}${it}") }
fun <K : Any, V : Any> Map<K, V>.logeKeyAll(preString: String = "", tagName: String = logDefaultTag ?: "") = this.keys.forEach { loge(tagName, "${preString}${it}") }

fun <K : Any, V : Any> Map<K, V>.logvValueAll(preString: String = "", tagName: String = logDefaultTag ?: "") = this.values.forEach { logv(tagName, "${preString}${it}") }
fun <K : Any, V : Any> Map<K, V>.logdValueAll(preString: String = "", tagName: String = logDefaultTag ?: "") = this.values.forEach { logd(tagName, "${preString}${it}") }
fun <K : Any, V : Any> Map<K, V>.logiValueAll(preString: String = "", tagName: String = logDefaultTag ?: "") = this.values.forEach { logi(tagName, "${preString}${it}") }
fun <K : Any, V : Any> Map<K, V>.logwValueAll(preString: String = "", tagName: String = logDefaultTag ?: "") = this.values.forEach { logw(tagName, "${preString}${it}") }
fun <K : Any, V : Any> Map<K, V>.logeValueAll(preString: String = "", tagName: String = logDefaultTag ?: "") = this.values.forEach { loge(tagName, "${preString}${it}") }

/**
 * 找到是哪裡呼叫到這裡的(呼叫路徑追蹤方法)
 * 使用範例：
 * Exception("標題TAG").trace("到底是哪裡去Call的")
 * */
fun Throwable.trace(preString: String = "", TAG: String = logDefaultTag ?: "TRACE LOG") {
    try {
        throw this
    } catch (th: Throwable) {
        loge(TAG, "=======${th.localizedMessage}=======")
        th.stackTrace.forEach {
            loge(TAG, "${preString}${it}")
        }
    }

}

/**
 * 執行多久計時工具(階段型)
 * 使用範例：
val timeList = mutableListOf(System.currentTimeMillis())  // 開始計時

delay(1000) // 實際上處理了一些事情

timeList.add(calculateTimeStep(timeList[0], "第一步驟"))

delay(2000) // 實際上又處理了一些事情

timeList.add(calculateTimeStep(timeList[1], "第二步驟"))

delay(3000) // 實際上再處理了一些事情

timeList.add(calculateTimeStep(timeList[2], "第三步驟"))

loge("sampleForCalculateTimeStep", "階段型計時方法示範完成")
 * */
fun calculateTimeStep(stepTime: Long, tagName: String = logDefaultTag ?: "CalculateTime LOG"): Long {
    return System.currentTimeMillis().apply {
        if (this - stepTime != 0L)
            loge(logDefaultTag ?: "calculateTimeStep LOG", "於[${tagName}]，與上一階段相差時間是${this - stepTime}毫秒")
    }
}

/**
 * 執行多久計時工具(內容型)
 * 使用範例：
calculateTimeInterval("某件事的計時") {

loge("我即將開始做了某件事")

delay(1000L) // 模擬做某件事

loge("某件事已經完成了")

}
 * */

fun calculateTimeInterval(tagName: String = logDefaultTag ?: "CalculateTime LOG", function: suspend () -> Unit) = runBlocking {
    val startTime = System.currentTimeMillis()
    loge(tagName, "計時開始。")
    function.invoke()
    loge(tagName, "花費時間共計${System.currentTimeMillis() - startTime}毫秒。")
}

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

fun <T> String.toDataBeanList(): List<T>? {
    return if (this.isJson()) Gson().fromJson(this, object : TypeToken<List<T>>() {}.type)
    else null
}

fun <T> String.toDataBean(classOfT: Class<T>?): T? {
    return if (this.isJson()) Gson().fromJson(this, classOfT)
    else null
}

/**
 *  直接將物件印出的鍊式表達式：
 *
 * */
fun <T : Any> T.forLogv(preString: String = "", tagName: String = logDefaultTag ?: "for Logv"): T = apply { loge(tagName, "${preString}${this@apply}") }
fun <T : Any> T.forLogd(preString: String = "", tagName: String = logDefaultTag ?: "for Logd"): T = apply { loge(tagName, "${preString}${this@apply}") }
fun <T : Any> T.forLogi(preString: String = "", tagName: String = logDefaultTag ?: "for Logi"): T = apply { loge(tagName, "${preString}${this@apply}") }
fun <T : Any> T.forLogw(preString: String = "", tagName: String = logDefaultTag ?: "for Logw"): T = apply { loge(tagName, "${preString}${this@apply}") }
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

private enum class LogLevelType { Verbose, Debug, Info, Warning, Error }

@get:JvmSynthetic
private val logDefaultTag: String?
    get() = Throwable().stackTrace.getOrNull(2)?.let(::createStackElementTag)

private const val MAX_TAG_LENGTH = 23

private val ANONYMOUS_CLASS = Pattern.compile("\\$\\d+")

private fun createStackElementTag(element: StackTraceElement): String {
    var tag = element.className.substringAfterLast('.')
    val m = ANONYMOUS_CLASS.matcher(tag)
    if (m.find()) {
        tag = m.replaceAll("")
    }
    // Tag length limit was removed in API 26.
    return if (tag.length <= MAX_TAG_LENGTH || Build.VERSION.SDK_INT >= 26) {
        tag
    } else {
        tag.substring(0, MAX_TAG_LENGTH)
    }
}


/**印多行文字(例如Json)的時候，可以強迫AndroidStudio全部印出的方法：*/
private fun logMsgMultiLine(msg: String?, tagName: String, type: LogLevelType) {
    msg?.let { content ->
        val strLength = content.length
        var start = 0
        var end = LogOption.LOG_MAX_LENGTH
        val totalLine = (strLength / LogOption.LOG_MAX_LENGTH) + 2
        (0..totalLine).forEach {
            if (strLength > end) {
                printLog(tagName, content, start, end, type)
                start = end
                end += LogOption.LOG_MAX_LENGTH
            } else {
                printLog(tagName, content, start, strLength, type)
                return
            }
        }
    }
}

private fun printLog(tagName: String, msg: String, start: Int, end: Int, type: LogLevelType) {
    when (type) {
        LogLevelType.Verbose -> Log.v(tagName, msg.substring(start, end))
        LogLevelType.Debug -> Log.d(tagName, msg.substring(start, end))
        LogLevelType.Info -> Log.i(tagName, msg.substring(start, end))
        LogLevelType.Warning -> Log.w(tagName, msg.substring(start, end))
        LogLevelType.Error -> Log.e(tagName, msg.substring(start, end))
    }
}


private const val END_LINE = "\n"

enum class WriteType {
    Collect,// call這麼 LogOption.COLLECT_LOG_SIZE 次才寫入檔案一次 //以節省效能。
    Single,  // 每call一次寫入檔案一次 //預設使用Single
    Default;  // 預設值，若沒有傳入、且Map中沒有該檔案才會預設使用Single。
}

private data class WrapFile(
    var storeFile: File,
    val writeType: WriteType = WriteType.Single,
    var catchMsg: MutableList<String> = mutableListOf(),
)

private val fileMap by lazy { mutableMapOf<String, WrapFile>() }

private fun getNowTimeFormat(): String = SimpleDateFormat("yyyy-MM-dd_HH:mm:ss", Locale.getDefault()).format(Date())

private fun getInternalFreeSpaceInKB() =
    StatFs(Environment.getDataDirectory().absolutePath).let {
        (it.blockSizeLong * it.availableBlocksLong) / (1024)
    }


private class WriteTypeException(msg: String) : Exception(msg)

private fun writeToFile(filePath: File, msg: String, writeType: WriteType) {

    if (getInternalFreeSpaceInKB() < LogOption.WRITE_LOG_FREE_SPACE) return

    try {
        var wrapFile = fileMap[filePath.absolutePath]

        if (wrapFile == null || (wrapFile.storeFile.length() + wrapFile.catchMsg.sumOf { it.length } + msg.length > LogOption.MAX_LOG_FILE_SIZE * 1024)) {
            fileMap[filePath.absolutePath] = (wrapFile?.copy(storeFile = createStoreFile(filePath)) ?: // 找得到新的，但是容量超過，要換一個storeFile。
            WrapFile( // 找不到舊的，新增一個新的
                storeFile = createStoreFile(filePath),
                writeType = if (writeType == WriteType.Default) WriteType.Single else writeType
            )).apply {
                wrapFile = this
            }
        }

        if ((wrapFile?.writeType != writeType) && writeType != WriteType.Default) {
            throw WriteTypeException(" ${filePath.name} 第一次寫入的Type和此次不同！第一次為${wrapFile?.writeType}，此次為${writeType}")
        }

        wrapFile?.catchMsg?.apply {
            add(msg)
            takeIf { it.size >= (LogOption.COLLECT_LOG_SIZE) || wrapFile?.writeType == WriteType.Single }
                ?.joinToString("")
                ?.let { wrapFile?.storeFile?.appendText(it) }
                ?.also { clear() }
        }
    } catch (e: IOException) {
        loge("Exception", "檔案寫入失敗，錯誤訊息： ${e.message}")
        e.printStackTrace()
    }
}

fun writeRemainingLogOnExit() {
    fileMap.filter { it.value.catchMsg.isNotEmpty() }.forEach {
        it.value.storeFile.appendText(it.value.catchMsg.joinToString(""))
    }
}

private fun createStoreFile(filePath: File) =
    File(filePath.parentFile, getStoreFileName(filePath)).apply {
        parentFile?.mkdirs()
        createNewFile()
    }


/**由於要自動分檔名去儲存，因此需要加上時間戳記，那傳進來的檔案名稱比如說是
 * log.txt
 * 要回傳 「log_2023-03-09 11:15:15.txt」
 * */
private fun getStoreFileName(filePath: File) = filePath.name.let {
    it.substring(0, it.lastIndexOf(".")) + // 檔案名稱
            "_${getNowTimeFormat()}" + // 時間戳記
            it.substring(it.lastIndexOf("."), it.length) // 副檔名
}
