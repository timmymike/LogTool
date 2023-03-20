package com.timmymike.sample

import com.google.gson.annotations.SerializedName

data class SampleData(
    @SerializedName("__extras")
    val extras: Extras,
    @SerializedName("fields")
    val fields: List<Field>,
    @SerializedName("include_total")
    val includeTotal: Boolean,
    @SerializedName("limit")
    val limit: String,
//    @SerializedName("_links") //用不到
//    val links: Links,
//    @SerializedName("offset")
//    val offset: String,
    @SerializedName("records")
    val records: List<Record>,
    @SerializedName("resource_format")
    val resourceFormat: String,
    @SerializedName("resource_id")
    val resourceId: String,
    @SerializedName("total")
    val total: String
)

data class Extras(
    @SerializedName("api_key")
    val apiKey: String
)

data class Field(
    @SerializedName("id")
    val id: String,
    @SerializedName("info")
    val info: Info,
    @SerializedName("type")
    val type: String
)

//data class Links(
//    @SerializedName("next")
//    val next: String,
//    @SerializedName("start")
//    val start: String
//)

data class Record(
    @SerializedName("item1")
    val item1: String,
    @SerializedName("item2")
    val item2: String,
    @SerializedName("value1")
    val value1: String,
    @SerializedName("value2")
    val value2: String,
    @SerializedName("value3")
    val value3: String,
    @SerializedName("value4")
    val value4: String,
    @SerializedName("value5")
    val value5: String,
    @SerializedName("value6")
    val value6: String,
    @SerializedName("value7")
    val value7: String
) {
    fun getValueByField(propertyName: String) = this::class.members.find { it.name == propertyName }.let {
        if (it is kotlin.reflect.KProperty<*>) {
            it.getter.call(this).toString()
        } else {
            "null"
        }
    }
}

data class Info(
    @SerializedName("label")
    val label: String
)
