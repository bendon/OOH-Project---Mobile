package com.edgetech.bbscout.data.data.remote.gen_ai.data_model.bbscout

import com.edgetech.bbscout.data.data.remote.bbscout_api.model.ApiResponse

data class OwnerInformation(
    val owner_address: String? = null,
    val owner_email: List<String>? = null,
    val owner_name: String? = null,
    val owner_phone: List<Long>? = null,
    val owner_website: String? = null
) : ApiResponse