package com.alajemba.paristransitace.network.models

import kotlinx.serialization.Serializable

@Serializable
data class GeminiRequest (
    val systemInstruction: Parts,
    val contents: List<Content>
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
    val text: String
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