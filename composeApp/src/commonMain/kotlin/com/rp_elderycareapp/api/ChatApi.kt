package com.rp_elderycareapp.api

import com.rp_elderycareapp.ApiConfig
import com.rp_elderycareapp.getApiBaseUrl
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
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
