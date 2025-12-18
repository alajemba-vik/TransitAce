package com.alajemba.paristransitace.network

import com.alajemba.paristransitace.BuildConfig
import com.alajemba.paristransitace.network.models.ApiResponse
import com.alajemba.paristransitace.network.models.Content
import com.alajemba.paristransitace.network.models.GeminiRequest
import com.alajemba.paristransitace.network.models.GeminiResponse
import com.alajemba.paristransitace.network.models.Part
import com.alajemba.paristransitace.network.models.Parts
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.header
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
            systemInstruction = Parts(
                parts = listOf(Part(SYSTEM_PROMPT))
            ),
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
                header("x-goog-api-key", apiKey)
                setBody(requestBody)
            }.body<GeminiResponse>()

            val reply = response.candidates?.first()?.content?.parts?.first()?.text ?: ""

            return ApiResponse(data = reply)

        } catch (e: Exception) {
            return ApiResponse(errorMessage = e.message ?: "Connection error ${e.message}")
        }

    }
}

private const val SYSTEM_PROMPT = """
        You are Sophie, a 68-year-old Parisian grandmother working at a transit help desk.
        
        Your Personality:
        - You are impatient, sarcastic, and bluntly honest.
        - You use French slang like "Bof", "Putain", "Merde", "Oulala".
        - You vaguely despise tourists but grudgingly help them.
        - You frequently mention that the user is wasting your time.
        
        Context:
        - The user is a broke student in Paris.
        - The currency is Euros (€).
        - A fine is exactly €60.
        
        Goal: Answer the user's question, but make them feel slightly stupid for asking it. Keep it short (under 50 words).
    """