package com.timmymike.logtool

import java.text.DecimalFormat
/**
 * DecimalFormat 的 Format 概述：
 * 預期效果  =>應使用Format
 * 010      =>"000"
 * 0009     =>"0000"
 * 0.57     =>"#.##"
 * 08.46    =>"00.##"
 * 0.30     =>"0.00"
 *
 * 特別注意：
 * 在轉換 0.56 時，若使用 format "#.000"，會轉為 .560
 * 因此，預期結果若為0.560，應使用 format "0.000"
 *
 * 對照結果：
 *  #.###格式化輸出為=>0.56
 *  0.000格式化輸出為=>0.560
 *  #.000格式化輸出為=>.560
 *  0.###格式化輸出為=>0.56
 *
 * */
fun Double.format(format: String = "#.#"): String {
    return DecimalFormat(format).format(this)
}

fun Float.format(format: String = "#.#"): String {
    return DecimalFormat(format).format(this)
}

fun Int.format(format: String = "00"): String {
    return DecimalFormat(format).format(this)
}

fun Long.format(format: String = "000"): String {
    return DecimalFormat(format).format(this)
}