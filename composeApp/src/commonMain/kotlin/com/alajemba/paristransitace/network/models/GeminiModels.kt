package com.alajemba.paristransitace.network.models

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

/*
* curl https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent \
  -H "x-goog-api-key: $GEMINI_API_KEY" \
  -H 'Content-Type: application/json' \
  -X POST \
  -d '{
    "contents": [
      {
        "role": "user",
        "parts": [
          {
            "text": "Hello"
          }
        ]
      },
      {
        "role": "model",
        "parts": [
          {
            "text": "Great to meet you. What would you like to know?"
          }
        ]
      },
      {
        "role": "user",
        "parts": [
          {
            "text": "I have two dogs in my house. How many paws are in my house?"
          }
        ]
      }
    ]
  }'
* */

/*
{
    "candidates": [
    {
        "content": {
        "parts": [
        {
            "functionCall": {
            "name": "show_help",
            "args": {}
        },
            "thoughtSignature": "CpYCAXLI2nzbOGkF5XkEV4NxpLv+iRoZOY0XveUO3cbBPF6YOS8cwSmUF6GCb0c2PpbG1KQtFJNtajGPI/Z5PwvIcVE3bpfil0nbsukHvtoNOFPE7rQmfLNLxLGU3LEaPz6mS1068NreKgHlw1HmnJenmMx6g+RvnBaECWvXMMs7DDnmWoYxe8kq2Z6rpEFeYd2JzI61E/fv50VBjdmo0XXmUjj1yQ2voaSJD/gD8Cq/8JAjomlMS9clegONsuVEUwwudMpHS9UUXAeKc5Uy7vMOGIfOI85rrOm+0/DE0SYC5MW6xBJNEf53zuB1BfasLLAzCt2/bXJJqwJYXZas2W1Roor8OlU6tNRcNu6M3ZWhqTWu4DjSoso="
        }
        ],
        "role": "model"
    },
        "finishReason": "STOP",
        "index": 0,
        "finishMessage": "Model generated function call(s)."
    }
    ],
    "usageMetadata": {
    "promptTokenCount": 451,
    "candidatesTokenCount": 10,
    "totalTokenCount": 514,
    "promptTokensDetails": [
    {
        "modality": "TEXT",
        "tokenCount": 451
    }
    ],
    "thoughtsTokenCount": 53
},
    "modelVersion": "gemini-2.5-flash",
    "responseId": "AQRPadfIDLCdkdUPv82l2Qw"
}*/
