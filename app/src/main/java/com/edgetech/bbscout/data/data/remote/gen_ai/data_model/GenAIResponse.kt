package com.edgetech.bbscout.data.data.remote.gen_ai.data_model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json


@Serializable
data class GenAIResponse(
    val brand: String? = null,
    val campaignTheme: String? = null,
    val slogan: String? = null,
    val contact: String? = null,
    ){
    companion object {
        fun fromJson(json: String): GenAIResponse {
            val cleanedJson = json
                .replace("\"\"\"", "")
                .trim()
                .replace("\\n", "")

            return Json.decodeFromString(cleanedJson)
        }

    }
}