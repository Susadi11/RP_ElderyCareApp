package com.rp_elderycareapp.api

import com.rp_elderycareapp.ApiConfig
import com.rp_elderycareapp.getApiBaseUrl
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class ChatApi {
    private val baseUrl = getApiBaseUrl()

    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                prettyPrint = true
            })
        }
    }

    suspend fun sendTextMessage(
        userId: String,
        message: String,
        sessionId: String? = null
    ): Result<ChatTextResponse> {
        return try {
            val url = baseUrl + ApiConfig.Endpoints.CHAT_TEXT
            println("ChatApi: Sending request to: $url")
            println("ChatApi: UserId: $userId, Message: $message")

            val request = ChatTextRequest(
                user_id = userId,
                message = message,
                session_id = sessionId
            )

            val response: ChatTextResponse = httpClient.post(url) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()

            println("ChatApi: Response received: ${response.response}")
            Result.success(response)
        } catch (e: Exception) {
            println("ChatApi: Error occurred: ${e::class.simpleName} - ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun sendVoiceMessage(
        userId: String,
        audioFilePath: String,
        sessionId: String? = null
    ): Result<VoiceResponse> {
        return try {
            val url = baseUrl + ApiConfig.Endpoints.CHAT_VOICE
            println("ChatApi: Sending voice request to: $url")
            println("ChatApi: UserId: $userId, Audio: $audioFilePath")

            // Read audio file first (suspend call)
            val audioBytes = readAudioFile(audioFilePath)

            val response: VoiceResponse = httpClient.submitFormWithBinaryData(
                url = url,
                formData = formData {
                    append("user_id", userId)
                    if (sessionId != null) {
                        append("session_id", sessionId)
                    }
                    append("max_tokens", "150")
                    append("temperature", "0.7")

                    // Append audio file bytes
                    append("file", audioBytes, Headers.build {
                        append(HttpHeaders.ContentType, "audio/wav")
                        append(HttpHeaders.ContentDisposition, "filename=\"audio.wav\"")
                    })
                }
            ).body()

            println("ChatApi: Voice response received: ${response.response}")
            println("ChatApi: Transcription: ${response.transcription}")
            Result.success(response)
        } catch (e: Exception) {
            println("ChatApi: Voice error occurred: ${e::class.simpleName} - ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun checkHealth(): Result<String> {
        return try {
            val response: String = httpClient.get(baseUrl + ApiConfig.Endpoints.CHAT_HEALTH).body()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun close() {
        httpClient.close()
    }
}

// Platform-specific function to read audio file
expect suspend fun readAudioFile(filePath: String): ByteArray
