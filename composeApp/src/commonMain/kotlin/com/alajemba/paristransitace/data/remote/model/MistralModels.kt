package com.alajemba.paristransitace.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class MistralRequest(
    val model: String,
    val messages: List<MistralMessage>,
    val tools: List<MistralTool>? = null,
    @SerialName("tool_choice")
    val toolChoice: String? = "auto",
    @SerialName("parallel_tool_calls")
    val parallelToolCalls: Boolean? = false,
    val temperature: Double? = 0.7,
    @SerialName("max_tokens")
    val maxTokens: Int? = null
)

@Serializable
data class MistralMessage(
    val role: String,
    val content: String? = null,
    val name: String? = null,
    @SerialName("tool_calls")
    val toolCalls: List<MistralToolCall>? = null,
    @SerialName("tool_call_id")
    val toolCallId: String? = null
)

@Serializable
data class MistralTool(
    val type: String = "function",
    val function: MistralFunction
)

@Serializable
data class MistralFunction(
    val name: String,
    val description: String,
    val parameters: MistralFunctionParameters
)

@Serializable
data class MistralFunctionParameters(
    val type: String = "object",
    val properties: Map<String, JsonElement>,
    val required: List<String>
)

@Serializable
data class MistralToolCall(
    val id: String,
    val type: String = "function",
    val function: MistralFunctionCall
)

@Serializable
data class MistralFunctionCall(
    val name: String,
    val arguments: String // JSON string of arguments
)

@Serializable
data class MistralResponse(
    val id: String? = null,
    val model: String? = null,
    val choices: List<MistralChoice>? = null,
    val usage: MistralUsage? = null,
    val error: MistralError? = null
)

@Serializable
data class MistralChoice(
    val index: Int,
    val message: MistralMessage,
    @SerialName("finish_reason")
    val finishReason: String? = null
)

@Serializable
data class MistralUsage(
    @SerialName("prompt_tokens")
    val promptTokens: Int,
    @SerialName("completion_tokens")
    val completionTokens: Int,
    @SerialName("total_tokens")
    val totalTokens: Int
)

@Serializable
data class MistralError(
    val message: String? = null,
    val type: String? = null,
    val code: String? = null
)

