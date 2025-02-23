package com.edgetech.bbscout.data.data.remote.gen_ai.llm

import android.graphics.Bitmap
import com.edgetech.bbscout.data.data.remote.gen_ai.data_model.GenAIResponse
import com.edgetech.bbscout.data.utils.SimpleResource

interface FulltextAndImageInference {

    suspend fun getCampaignInfo(image: Bitmap?, text: String?): SimpleResource<GenAIResponse>

}