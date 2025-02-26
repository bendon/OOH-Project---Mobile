package com.edgetech.bbscout.data.data.remote.bbscout_api.model

data class FileResponse(
    val createdAt: Long? = null,
    val fileExtension: String? = null,
    val fileName: String? = null,
    val fileSize: Long? = null,
    val fileType: String? = null,
    val fileUrl: String? = null,
    val id: String? = null,
    val organizationId: String? = null,
    val updatedAt: Long? = null,
    val uploadedById: String? = null
) : ApiResponse