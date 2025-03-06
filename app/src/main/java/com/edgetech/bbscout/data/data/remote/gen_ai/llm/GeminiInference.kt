package com.edgetech.bbscout.data.data.remote.gen_ai.llm

import android.graphics.Bitmap
import com.edgetech.bbscout.BuildConfig
import com.edgetech.bbscout.components.utils.log
import com.edgetech.bbscout.data.data.remote.gen_ai.data_model.GenAIResponse
import com.edgetech.bbscout.data.utils.SimpleResource
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content

class GeminiInference : FulltextAndImageInference {
    override suspend fun getCampaignInfo(
        image: Bitmap?,
        text: String?
    ): SimpleResource<GenAIResponse> {
        val generativeModel = GenerativeModel(
            modelName = "gemini-2.0-flash-lite-preview-02-05",
            apiKey = ""  //BuildConfig.GEMINI_API_KEY
        )

        val prompt = """
You are given an image of an advertising campaign.
Analyze the image and provide the following information in JSON format:
{
  "brand": "",
  "campaignTheme": "",
  "slogan": "",
  "contact":""
}
Important: Respond with ONLY the JSON object and no additional text.
"""

        try {
            val inputContent = content() {
                if (image != null)
                    image(image)
                if (text != null)
                    text(prompt)
            }

            val response = generativeModel.generateContent(inputContent)
            println("the response is ${response.text}")

            val result = GenAIResponse.fromJson(response.text?.substringAfter("json")?.substringBefore("```") ?: "{}")
            return SimpleResource.Success(result)
        } catch (e: Exception) {
            e.printStackTrace()
            return SimpleResource.Error(e.message ?: "Unknown Error")
        }

    }
}