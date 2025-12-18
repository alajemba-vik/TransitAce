package com.alajemba.paristransitace.network.models

data class ApiResponse<T>(
    val data: T? = null,
    val errorMessage: String? = null,
    val errorCode: Int? = null
) {
    val hasError: Boolean get() = errorMessage != null || errorCode != null
}