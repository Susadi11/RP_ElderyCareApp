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
            requestTimeoutMillis = 30000 // 30 seconds timeout (increased for development)
            connectTimeoutMillis = 10000 // 10 seconds to connect (increased)
            socketTimeoutMillis = 30000  // 30 seconds for socket
        }
    }

    suspend fun submitCalibration(
        request: CalibrationRequest
    ): Result<CalibrationResponse> {
        return try {
            println("GameApi: Attempting to submit calibration to: $baseUrl/game/calibration")
            val response: CalibrationResponse = httpClient.post("$baseUrl/game/calibration") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()
            println("GameApi: Calibration successful")
            Result.success(response)
        } catch (e: Exception) {
            println("GameApi: Error submitting calibration to $baseUrl/game/calibration")
            println("GameApi: Error details: ${e.message}")
            println("GameApi: Error type: ${e::class.simpleName}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun getMotorBaseline(
        userId: String
    ): Result<MotorBaselineResponse> {
        return try {
            println("GameApi: Attempting to get motor baseline from: $baseUrl/game/motor-baseline/$userId")
            val response: MotorBaselineResponse = httpClient.get("$baseUrl/game/motor-baseline/$userId").body()
            println("GameApi: Motor baseline retrieved successfully")
            Result.success(response)
        } catch (e: Exception) {
            println("GameApi: Error getting motor baseline from $baseUrl/game/motor-baseline/$userId")
            println("GameApi: Error details: ${e.message}")
            println("GameApi: Error type: ${e::class.simpleName}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun submitSession(
        request: GameSessionRequest
    ): Result<GameSessionResponse> {
        return try {
            println("GameApi: Attempting to submit session to: $baseUrl/game/session")
            val response: GameSessionResponse = httpClient.post("$baseUrl/game/session") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()
            println("GameApi: Session submitted successfully")
            Result.success(response)
        } catch (e: Exception) {
            println("GameApi: Error submitting session to $baseUrl/game/session")
            println("GameApi: Error details: ${e.message}")
            println("GameApi: Error type: ${e::class.simpleName}")
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