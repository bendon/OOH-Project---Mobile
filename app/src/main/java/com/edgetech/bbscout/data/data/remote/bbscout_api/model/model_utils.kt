package com.edgetech.bbscout.data.data.remote.bbscout_api.model


interface ApiResponse


class ApiResponsePage<T: ApiResponse>(
    val data: List<T>? = null,
    val page: Int? = null,
    val total: Int? = null,
    val page_size: Int? = null,
    val total_pages: Int? = null
): ApiResponse