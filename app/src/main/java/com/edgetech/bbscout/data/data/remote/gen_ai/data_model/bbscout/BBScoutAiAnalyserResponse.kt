package com.edgetech.bbscout.data.data.remote.gen_ai.data_model.bbscout

import com.edgetech.bbscout.data.data.remote.bbscout_api.model.ApiResponse

data class BBScoutAiAnalyserResponse(
    val additional_notes: String,
    val billboard_measurements: BillboardMeasurements? = null,
    val campaign_brand: String? = null,
    val campaign_description: String? = null,
    //val campaign_socials: List<Any>? = null,
    val contact_information: ContactInformation? = null,
    val location: String? = null,
    val other_details: List<OtherDetail>? = null,
    val percentage_accuracy: Double? = null,
    val site_url: List<String>? = null,
    val target_age: String? = null,
    val target_audience: String? = null,
    val target_gender: String? = null
): ApiResponse