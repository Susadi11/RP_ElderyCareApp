package com.rp_elderycareapp.api

import com.rp_elderycareapp.getApiBaseUrl
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.forms.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class MMSEFinalizeResponse(
    val total_score: Float,
    val ml_risk_label: String,
    val avg_ml_probability: Float
)

@Serializable
data class MmseAssessment(
    val _id: String,
    val user_id: String,
    val assessment_type: String,
    val assessment_date: String,
    val total_score: Float,
    val status: String,
    val completed_at: String? = null
)

@Serializable
data class MmseAssessmentResponse(
    val user_id: String,
    val total_assessments: Int,
    val assessments: List<MmseAssessment>
)

class MmseApi {
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

    suspend fun getUserMmseAssessments(userId: String): Result<MmseAssessmentResponse> {
        return try {
            val url = "$baseUrl/api/mmse/user/$userId"
            
            println("--- FETCHING MMSE ASSESSMENTS ---")
            println("URL: $url")
            println("User ID: $userId")
            println("---------------------------------")

            val response = httpClient.get(url)

            if (response.status.isSuccess()) {
                val body = response.body<MmseAssessmentResponse>()
                Result.success(body)
            } else {
                val errorBody = response.body<String>()
                Result.failure(Exception("Fetch assessments failed: ${response.status} - $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun startMmse(userId: String): Result<String> {
        return try {
            val url = "$baseUrl/api/mmse/start"
            
            // Log for debugging
            println("--- STARTING MMSE ---")
            println("URL: $url")
            println("User ID: $userId")
            println("---------------------")

            val response = httpClient.post(url) {
                parameter("user_id", userId)
            }

            if (response.status.isSuccess()) {
                val body = response.body<Map<String, String>>()
                val assessmentId = body["assessment_id"] ?: return Result.failure(Exception("Assessment ID not found in response"))
                Result.success(assessmentId)
            } else {
                val errorBody = response.body<String>()
                Result.failure(Exception("Start MMSE failed: ${response.status} - $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun submitMmse(
        assessmentId: String,
        userId: String,
        questionType: String,
        caregiverIsCorrect: Boolean?,
        audioBytes: ByteArray,
        fileName: String
    ): Result<Unit> {
        return try {
            val url = "$baseUrl/api/mmse/submit"
            
            // Log the data to be sent to Logcat
            println("--- MMSE SUBMISSION ---")
            println("URL: $url")
            println("Assessment ID: $assessmentId")
            println("User ID: $userId")
            println("Question Type: $questionType")
            println("Caregiver is Correct: $caregiverIsCorrect")
            println("File: $fileName (${audioBytes.size} bytes)")
            println("-----------------------")

            val response = httpClient.submitFormWithBinaryData(
                url = url,
                formData = formData {
                    append("assessment_id", assessmentId)
                    append("user_id", userId)
                    append("question_type", questionType)
                    if (caregiverIsCorrect != null) {
                        append("caregiver_is_correct", caregiverIsCorrect.toString())
                    }
                    append("file", audioBytes, Headers.build {
                        append(HttpHeaders.ContentType, "audio/wav")
                        append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
                    })
                }
            )

            if (response.status.isSuccess()) {
                Result.success(Unit)
            } else {
                val errorBody = response.body<String>()
                Result.failure(Exception("Upload failed: ${response.status} - $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun finalizeMmse(assessmentId: String, userId: String): Result<MMSEFinalizeResponse> {
        return try {
            val url = "$baseUrl/api/mmse/finalize"

            println("--- FINALIZING MMSE ---")
            println("URL: $url")
            println("Assessment ID: $assessmentId")
            println("User ID: $userId")
            println("-----------------------")

            val response = httpClient.post(url) {
                parameter("assessment_id", assessmentId)
                parameter("user_id", userId)
            }

            if (response.status.isSuccess()) {
                val body = response.body<MMSEFinalizeResponse>()
                Result.success(body)
            } else {
                val errorBody = response.body<String>()
                Result.failure(Exception("Finalize MMSE failed: ${response.status} - $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun close() {
        httpClient.close()
    }
}
