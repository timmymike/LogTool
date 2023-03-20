package com.timmymike.sample

import android.os.Bundle
import android.os.Environment
import androidx.appcompat.app.AppCompatActivity
import com.timmymike.logtool.*
import kotlinx.coroutines.*
import java.io.File

class LogSampleActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        logSample()
    }

    private fun logSample() {

        // 計時方法內容型使用範例：
        sampleForCalculateTimeInterval()

        // 計時方法階段型使用範例：
        sampleForCalculateTimeStep()

        // Log寫入檔案範例
        sampleForLogWriteToFile()

        // 讀取檔案寫入多行Log範例：
        sampleForLogMultipleLine()

        // Gson的toJson和資料類別互轉
        sampleForGsonTools(getDataFromAssets("test_to_print_short.json") ?: return)

    }

    private fun sampleForGsonTools(data: String) {
        loge("轉換為DataBean的內容是=>${data.toDataBean(SampleData::class.java)}")
        loge("此內容轉為Json是=>${data.toDataBean(SampleData::class.java)?.toJson()}")

        loge("sampleForGsonTools", "Gson方法範例執行完成")
    }

    private fun sampleForLogWriteToFile() = CoroutineScope(Dispatchers.Default).launch {

//        LogOption.COLLECT_LOG_SIZE = 5000 // 透過此方法去設定每次收集這麼多次的訊息以後再寫入
        getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)?.absolutePath?.let { environmentPath ->
            logWtf(File("$environmentPath/ERROR_LOG_FOLDER/error_log.txt"), "msgTest in first times", WriteType.Single)

            logWtf(File("$environmentPath/ERROR_LOG_FOLDER/error_log.txt"), "msgTest in second times")
            (1..10300).forEach {
                // 寫入Log檔案範例：
                logWtf(File("$environmentPath/LOG_FOLDER/log.txt"), "msgTest in ${+it} times", WriteType.Collect)
            }

            logWtf(File("$environmentPath/LOG_FOLDER/log.txt"), "msgTest in penultimate times") // 第一次指定WriteType以後，後續的WriteType可以不用再傳入。

            // 以下範例將造成 WriteTypeException // 因為檔名相同，寫入的type必須一樣。 //所有的Collect Type 共用一組 collectTimes
//        logWtf(File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)?.absolutePath + "/LOG_FOLDER/" + "log.txt"), "msgTest in last times", WriteType.Single)

        }
        loge("sampleForLogWriteToFile", "檔案範例寫入完成")
    }

    // 計時方法階段型使用範例：
    private fun sampleForCalculateTimeStep() = CoroutineScope(Dispatchers.Default).launch {

        val timeList = mutableListOf(System.currentTimeMillis())  // 開始計時

        delay(1000) // 實際上處理了一些事情

        timeList.add(calculateTimeStep(timeList[0], "第一步驟"))

        delay(2000) // 實際上又處理了一些事情

        timeList.add(calculateTimeStep(timeList[1], "第二步驟"))

        delay(3000) // 實際上再處理了一些事情

        timeList.add(calculateTimeStep(timeList[2], "第三步驟"))

        loge("sampleForCalculateTimeStep", "階段型計時方法示範完成")
    }

    // 計時方法內容型使用範例：
    private fun sampleForCalculateTimeInterval() = CoroutineScope(Dispatchers.Default).launch {
        calculateTimeInterval("某件事的計時") {
            loge("我做了某件事")
            delay(1000L)
            loge("某件事已經完成了")
        }

        loge("sampleForCalculateTimeInterval", "內容型計時方法示範完成")
    }

    private fun sampleForLogMultipleLine(): String? = runBlocking {
        return@runBlocking withContext(Dispatchers.Default) {
            withContext(Dispatchers.Default) {
                // 讀取檔案(一大串Json)後印出範例。
                kotlin.runCatching {
                    assets.open("test_to_print.json").bufferedReader().use { it.readText() }
                        .apply {
                            loge(this)
                            loge("sampleForLogMultipleLine", "多行Log方法示範完成")
                        }
                }.onFailure { e -> loge("讀取錯誤！原因：${e.message}", e) }.getOrNull()
            }
        }
    }

    private fun getDataFromAssets(fileName: String): String? = runBlocking {
        return@runBlocking withContext(Dispatchers.Default) {
            withContext(Dispatchers.Default) {
                kotlin.runCatching {
                    assets.open(fileName).bufferedReader().use { it.readText() }
                }.onFailure { e -> loge("讀取錯誤！原因：${e.message}", e) }.getOrNull()
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        writeRemainingLogOnExit()
    }
}