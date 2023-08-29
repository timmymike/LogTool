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
        // 集合方法使用範例
        sampleForCollectionLog()

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

        // 數字格式化範例
        sampleForDigitFormat()

    }

    private fun sampleForDigitFormat() {
        "000".let{10.format(it).forLoge("${it}格式化輸出為=>")}
        "0000".let{20L.format(it).forLoge("${it}格式化輸出為=>")}
        "00.##".let{ 5.789923f.format(it).forLoge("${it}格式化輸出為=>")}
    }


    private fun sampleForCollectionLog() {
        listOf(1, 2, 3).logdAll("測試列表印出=>")
    }

    private fun sampleForGsonTools(data: String) {
        // =====字串轉類別=====

        // 轉換為 物件
        val transferData: SampleData? = data.toDataBean<SampleData>()

        // 處理或印出
        loge("專用型 物件測試轉譯結果=>${transferData}")

        // 轉換為 List 物件
        transferData?.records?.toJson()?.toDataBeanList<Record>()?.forLoge("專用型 列表測試轉譯結果=>")

        // 轉換為物件、List物件通用型：toData方法使用範例：
        val transferData2: SampleData? = data.toData<SampleData>()
        val recordsList: List<Record>? = transferData2?.records?.toJson()?.toData<List<Record>>()

        // 處理或印出
        transferData2?.forLoge("通用型 物件測試轉譯結果=>")
        recordsList?.getOrNull(0)?.item2?.forLoge("通用型 列表測試轉譯結果第1個item2=>")

        // =====類別轉字串=====
        transferData?.toJson()?.forLoge("鍊式方法 轉換為Json字串並印出的結果=>")
        transferData?.forJsonAndLoge("單一方法 轉換為Json字串並印出的結果=>")

        loge("sampleForGsonTools", "使用Gson將Json字串與類別互轉 範例執行完成")
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
            loge("我即將開始做了某件事")
            delay(1000L) // 模擬做某件事
            loge("某件事已經完成了")
        }

        loge("sampleForCalculateTimeInterval", "內容型計時方法示範完成")
    }

    private fun sampleForLogMultipleLine(): String? = runBlocking {
        return@runBlocking withContext(Dispatchers.Default) {
            withContext(Dispatchers.Default) {
                // 讀取檔案(一大串Json)後印出範例。
                kotlin.runCatching {
                    getDataFromAssets("test_to_print.json").apply {
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