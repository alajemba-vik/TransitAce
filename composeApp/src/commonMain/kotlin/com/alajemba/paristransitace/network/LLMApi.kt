package com.alajemba.paristransitace.network

import com.alajemba.paristransitace.BuildConfig
import com.alajemba.paristransitace.network.models.ApiResponse
import com.alajemba.paristransitace.network.models.Content
import com.alajemba.paristransitace.network.models.GeminiRequest
import com.alajemba.paristransitace.network.models.GeminiResponse
import com.alajemba.paristransitace.network.models.Part
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class LLMApi(
    private val httpClient: HttpClient
) {

    private val ENDPOINT = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent"
    private val apiKey = BuildConfig.GEMINI_API_KEY

    suspend fun sendChatMessage(message: String): ApiResponse<String> {
        val requestBody = GeminiRequest(
            contents = listOf(
                Content(
                    parts = listOf(Part(message)),
                    role = "user"
                )
            )
        )

        try {
            val response = httpClient.post {
                url(ENDPOINT)
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }.body<GeminiResponse>()

            val reply = response.candidates?.first()?.content?.parts?.first()?.text ?: ""

            return ApiResponse(data = reply)

        } catch (e: Exception) {
            return ApiResponse(errorMessage = e.message ?: "Connection error ${e.message}")
        }

    }
}