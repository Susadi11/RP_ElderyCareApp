package com.rp_elderycareapp.api

import com.rp_elderycareapp.getApiBaseUrl
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class GameApi {
    private val baseUrl = getApiBaseUrl()

    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                prettyPrint = true
            })
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 3000 // 3 seconds timeout
            connectTimeoutMillis = 2000 // 2 seconds to connect
            socketTimeoutMillis = 3000  // 3 seconds for socket
        }
    }

    suspend fun submitCalibration(
        request: CalibrationRequest
    ): Result<CalibrationResponse> {
        return try {
            val response: CalibrationResponse = httpClient.post("$baseUrl/game/calibration") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()
            Result.success(response)
        } catch (e: Exception) {
            println("GameApi: Error submitting calibration: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun getMotorBaseline(
        userId: String
    ): Result<MotorBaselineResponse> {
        return try {
            val response: MotorBaselineResponse = httpClient.get("$baseUrl/game/motor-baseline/$userId").body()
            Result.success(response)
        } catch (e: Exception) {
            println("GameApi: Error getting motor baseline: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun submitSession(
        request: GameSessionRequest
    ): Result<GameSessionResponse> {
        return try {
            val response: GameSessionResponse = httpClient.post("$baseUrl/game/session") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()
            Result.success(response)
        } catch (e: Exception) {
            println("GameApi: Error submitting session: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun getSessionHistory(
        userId: String
    ): Result<SessionHistoryResponse> {
        return try {
            val response: SessionHistoryResponse = httpClient.get("$baseUrl/game/history/$userId").body()
            Result.success(response)
        } catch (e: Exception) {
            println("GameApi: Error getting session history: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun getUserStats(
        userId: String
    ): Result<UserStatsResponse> {
        return try {
            val response: UserStatsResponse = httpClient.get("$baseUrl/game/stats/$userId").body()
            Result.success(response)
        } catch (e: Exception) {
            println("GameApi: Error getting user stats: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun deleteSession(
        sessionId: String
    ): Result<Map<String, String>> {
        return try {
            val response: Map<String, String> = httpClient.delete("$baseUrl/game/session/$sessionId").body()
            Result.success(response)
        } catch (e: Exception) {
            println("GameApi: Error deleting session: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    fun close() {
        httpClient.close()
    }
}