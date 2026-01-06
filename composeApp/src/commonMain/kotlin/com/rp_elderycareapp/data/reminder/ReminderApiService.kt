package com.rp_elderycareapp.data.reminder

import com.rp_elderycareapp.getApiBaseUrl
import com.rp_elderycareapp.ApiConfig
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json



class ReminderApiService {
    
    // Using base URL from Constants.kt - automatically configured for emulator/physical device
    private val baseUrl = getApiBaseUrl() + ApiConfig.Endpoints.REMINDERS
    
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                prettyPrint = true
            })
        }
    }
    
    // 1. Create a new reminder (structured)
    suspend fun createReminder(request: CreateReminderRequest): Result<Reminder> {
        return try {
            val response = client.post("$baseUrl/create") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            println("Response status: ${response.status}")
            
            // Check HTTP status first
            if (response.status.value !in 200..299) {
                val errorBody = try { response.body<String>() } catch (e: Exception) { "Unknown error" }
                println("HTTP Error ${response.status.value}: $errorBody")
                return Result.failure(Exception("Server error: ${response.status.value} - $errorBody"))
            }
            
            try {
                val apiResponse: ApiResponse<Reminder> = response.body()
                println("API Response: status=${apiResponse.status}, data=${apiResponse.data}, error=${apiResponse.error}")
                if (apiResponse.data != null) {
                    Result.success(apiResponse.data)
                } else {
                    Result.failure(Exception(apiResponse.error ?: "No data returned from server"))
                }
            } catch (parseError: Exception) {
                // Try parsing as direct Reminder object (in case backend doesn't wrap it)
                println("Failed to parse as ApiResponse, trying direct Reminder: ${parseError.message}")
                try {
                    val reminder: Reminder = response.body()
                    Result.success(reminder)
                } catch (e: Exception) {
                    println("Failed to parse as Reminder too: ${e.message}")
                    Result.failure(Exception("Failed to parse server response: ${e.message}"))
                }
            }
        } catch (e: Exception) {
            println("Create reminder error: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }
    
    // 2. Create reminder from natural language
    suspend fun createReminderFromNaturalLanguage(request: NaturalLanguageReminderRequest): Result<Reminder> {
        return try {
            val response = client.post("$baseUrl/natural-language") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            val apiResponse: ApiResponse<Reminder> = response.body()
            Result.success(apiResponse.data!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // 2b. Create reminder from audio file (voice recording)
    suspend fun createReminderFromAudio(
        audioFile: ByteArray,
        fileName: String,
        userId: String,
        priority: String? = null,
        caregiverIds: String? = null
    ): Result<AudioReminderResponse> {
        return try {
            val boundary = "----WebKitFormBoundary${System.currentTimeMillis()}"
            val response = client.post("$baseUrl/create-from-audio") {
                setBody(buildMultipartFormData(boundary, audioFile, fileName, userId, priority, caregiverIds))
                contentType(ContentType.parse("multipart/form-data; boundary=$boundary"))
            }
            
            println("Audio upload response status: ${response.status}")
            val audioResponse: AudioReminderResponse = response.body()
            println("Transcription: ${audioResponse.transcription}")
            Result.success(audioResponse)
        } catch (e: Exception) {
            println("Audio upload error: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }
    
    private fun buildMultipartFormData(
        boundary: String,
        audioFile: ByteArray,
        fileName: String,
        userId: String,
        priority: String?,
        caregiverIds: String?
    ): ByteArray {
        val builder = StringBuilder()
        val lineBreak = "\r\n"
        
        // Add user_id field
        builder.append("--$boundary$lineBreak")
        builder.append("Content-Disposition: form-data; name=\"user_id\"$lineBreak$lineBreak")
        builder.append("$userId$lineBreak")
        
        // Add priority if present
        if (priority != null) {
            builder.append("--$boundary$lineBreak")
            builder.append("Content-Disposition: form-data; name=\"priority\"$lineBreak$lineBreak")
            builder.append("$priority$lineBreak")
        }
        
        // Add caregiver_ids if present
        if (caregiverIds != null) {
            builder.append("--$boundary$lineBreak")
            builder.append("Content-Disposition: form-data; name=\"caregiver_ids\"$lineBreak$lineBreak")
            builder.append("$caregiverIds$lineBreak")
        }
        
        // Add file
        builder.append("--$boundary$lineBreak")
        builder.append("Content-Disposition: form-data; name=\"file\"; filename=\"$fileName\"$lineBreak")
        builder.append("Content-Type: audio/3gpp$lineBreak$lineBreak")
        
        val header = builder.toString().toByteArray(Charsets.UTF_8)
        val footer = "$lineBreak--$boundary--$lineBreak".toByteArray(Charsets.UTF_8)
        
        return header + audioFile + footer
    }
    
    // 3. Get all reminders for a user
    suspend fun getUserReminders(userId: String, statusFilter: String? = null): Result<List<Reminder>> {
        return try {
            val url = if (statusFilter != null) {
                "$baseUrl/user/$userId?status_filter=$statusFilter"
            } else {
                "$baseUrl/user/$userId"
            }
            println("Fetching reminders from: $url")
            val response = client.get(url)
            println("Response status: ${response.status}")
            
            // Get raw response text first for debugging
            val responseText: String = response.body()
            println("Raw response length: ${responseText.length} chars")
            println("First 200 chars: ${responseText.take(200)}")
            
            // Try to parse as ApiResponse wrapper first
            val json = Json { 
                ignoreUnknownKeys = true
                isLenient = true
                coerceInputValues = true
            }
            
            return try {
                // Try parsing as wrapped response: {"status": "success", "data": [...]} OR {"reminders": [...]}
                val apiResponse: ApiResponse<List<Reminder>> = json.decodeFromString(responseText)
                println("Parsed as ApiResponse: status=${apiResponse.status}")
                
                // Backend might use either "data" or "reminders" field!
                val remindersList = apiResponse.reminders ?: apiResponse.data
                
                if (remindersList != null && remindersList.isNotEmpty()) {
                    println("SUCCESS: Found ${remindersList.size} reminders")
                    Result.success(remindersList)
                } else if (apiResponse.status == "success") {
                    println("SUCCESS: Empty list")
                    Result.success(emptyList())
                } else {
                    println("Failed: ${apiResponse.error}")
                    Result.failure(Exception(apiResponse.error ?: "No data returned"))
                }
            } catch (e: Exception) {
                // If wrapped parsing fails, try parsing as direct list: [...]
                println("Wrapped parsing failed, trying direct list parsing: ${e.message}")
                try {
                    val reminderList: List<Reminder> = json.decodeFromString(responseText)
                    println("Parsed as direct list: ${reminderList.size} reminders")
                    Result.success(reminderList)
                } catch (e2: Exception) {
                    println("Direct list parsing also failed: ${e2.message}")
                    e2.printStackTrace()
                    Result.failure(Exception("Failed to parse response: ${e2.message}"))
                }
            }
        } catch (e: Exception) {
            println("Get reminders error: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }
    
    // 4. Update existing reminder
    suspend fun updateReminder(reminderId: String, request: CreateReminderRequest): Result<Reminder> {
        return try {
            val response = client.put("$baseUrl/update/$reminderId") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            val apiResponse: ApiResponse<Reminder> = response.body()
            Result.success(apiResponse.data!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // 5. Delete a reminder
    suspend fun deleteReminder(reminderId: String): Result<String> {
        return try {
            val response = client.delete("$baseUrl/delete/$reminderId")
            val apiResponse: ApiResponse<String> = response.body()
            Result.success(apiResponse.message ?: "Deleted successfully")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // 6. Snooze reminder
    suspend fun snoozeReminder(reminderId: String, delayMinutes: Int = 15): Result<SnoozeResult> {
        return try {
            val response = client.post("$baseUrl/snooze/$reminderId?delay_minutes=$delayMinutes")
            val apiResponse: ApiResponse<SnoozeResult> = response.body()
            Result.success(apiResponse.data!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // 7. Process user's response to reminder (AI analysis)
    suspend fun respondToReminder(request: ReminderResponseRequest): Result<ReminderResponseResult> {
        return try {
            val response = client.post("$baseUrl/respond") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            val result: ReminderResponseResult = response.body()
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // 8. Get behavior patterns
    suspend fun getBehaviorPattern(userId: String, days: Int = 30): Result<BehaviorPattern> {
        return try {
            val response = client.get("$baseUrl/behavior/$userId?days=$days")
            val apiResponse: ApiResponse<BehaviorPattern> = response.body()
            Result.success(apiResponse.data!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // 9. Get dashboard analytics
    suspend fun getDashboardAnalytics(userId: String, days: Int = 7): Result<DashboardData> {
        return try {
            val response = client.get("$baseUrl/analytics/dashboard/$userId?days=$days")
            val apiResponse: ApiResponse<DashboardData> = response.body()
            Result.success(apiResponse.data!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // 10. Get caregiver alerts
    suspend fun getCaregiverAlerts(caregiverId: String, activeOnly: Boolean = true): Result<List<CaregiverAlert>> {
        return try {
            val response = client.get("$baseUrl/caregiver/alerts/$caregiverId?active_only=$activeOnly")
            val apiResponse: ApiResponse<List<CaregiverAlert>> = response.body()
            Result.success(apiResponse.data ?: emptyList())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // 11. Acknowledge caregiver alert
    suspend fun acknowledgeAlert(alertId: String, caregiverId: String): Result<String> {
        return try {
            val response = client.post("$baseUrl/caregiver/alerts/$alertId/acknowledge?caregiver_id=$caregiverId")
            val apiResponse: ApiResponse<String> = response.body()
            Result.success(apiResponse.message ?: "Acknowledged")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // 12. Resolve caregiver alert
    suspend fun resolveAlert(alertId: String, caregiverId: String): Result<String> {
        return try {
            val response = client.post("$baseUrl/caregiver/alerts/$alertId/resolve?caregiver_id=$caregiverId")
            val apiResponse: ApiResponse<String> = response.body()
            Result.success(apiResponse.message ?: "Resolved")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // 13. Get weekly report
    suspend fun getWeeklyReport(userId: String, format: String = "json"): Result<WeeklyReportSummary> {
        return try {
            val response = client.get("$baseUrl/reports/weekly/$userId?format=$format")
            val apiResponse: ApiResponse<WeeklyReportSummary> = response.body()
            Result.success(apiResponse.data!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // 14. Get weekly report summary
    suspend fun getWeeklyReportSummary(userId: String): Result<WeeklyReportSummary> {
        return try {
            val response = client.get("$baseUrl/reports/weekly/$userId/summary")
            val apiResponse: ApiResponse<WeeklyReportSummary> = response.body()
            Result.success(apiResponse.data!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // 15. Complete reminder and create next occurrence for daily repeats
    suspend fun completeReminder(reminderId: String): Result<Reminder> {
        return try {
            println("Completing reminder: $reminderId")
            val response = client.post("$baseUrl/complete/$reminderId")
            println("Complete response status: ${response.status}")
            val apiResponse: ApiResponse<Reminder> = response.body()
            println("Complete parsed: next reminder = ${apiResponse.data?.id}")
            if (apiResponse.data != null) {
                Result.success(apiResponse.data)
            } else {
                Result.failure(Exception(apiResponse.error ?: "Failed to complete reminder"))
            }
        } catch (e: Exception) {
            println("Complete reminder error: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }
    
    // Check for due reminders (polling endpoint)
    suspend fun getDueReminders(userId: String, timeWindowMinutes: Int = 5): Result<DueNowResponse> {
        return try {
            val response: DueNowResponse = client.get("$baseUrl/user/$userId/due-now") {
                parameter("time_window_minutes", timeWindowMinutes)
            }.body()
            Result.success(response)
        } catch (e: Exception) {
            println("Get due reminders error: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }
    
    fun close() {
        client.close()
    }
}
