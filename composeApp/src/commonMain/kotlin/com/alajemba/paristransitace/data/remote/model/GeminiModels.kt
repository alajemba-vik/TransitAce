package com.alajemba.paristransitace.data.remote.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class GeminiRequest (
    val systemInstruction: Parts,
    val contents: List<Content>,
    val tools: List<Tool>? = null
)

@Serializable
data class Tool(
    val functionDeclarations: List<FunctionDeclaration>
)

@Serializable
data class FunctionDeclaration(
    val name: String,
    val description: String,
    val parameters: FunctionParameters
) {
    companion object{
        const val DECL_GET_ALL_STORYLINES = "get_all_storylines"
        const val DECL_LOAD_STORYLINE = "load_new_storyline"
        const val DECL_RESTART_GAME = "reset_game"
        const val DECL_SHOW_HELP = "show_help"
    }
}

@Serializable
data class FunctionParameters(
    val type: String = "OBJECT",
    val properties: Map<String, JsonElement>,
    val required: List<String>
) {
    companion object {
        const val PARAM_SCENARIO_ID = "scenario_id"
    }
}

@Serializable
data class FunctionCall(
    val name: String,
    val args: Map<String, String>
)

@Serializable
data class GeminiResponse (
    val candidates: List<Candidate>? = null
)

@Serializable
data class Candidate (
    val content: Content,
    val finishReason: String? = null
)

@Serializable
data class Content (
    val role: String,
    val parts: List<Part>
)

@Serializable
data class Part (
    val text: String? = null,
    val functionCall: FunctionCall? = null
)

@Serializable
data class Parts(
    val parts: List<Part>
)
