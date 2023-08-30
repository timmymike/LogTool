package com.timmymike.logtool

import kotlinx.coroutines.runBlocking


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
fun calculateTimeStep(stepTime: Long, preString: String = logDefaultTag ?: "CalculateTime LOG"): Long {
    return System.currentTimeMillis().apply {
        if (this - stepTime != 0L)
            loge(logDefaultTag ?: "calculateTimeStep LOG", "於[${preString}]，與上一階段相差時間是${this - stepTime}毫秒")
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
