package com.rp_elderycareapp.viewmodel

import androidx.compose.runtime.mutableStateOf
import com.rp_elderycareapp.api.MmseApi
import com.rp_elderycareapp.api.MmseAssessment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MmseViewModel {
    private val mmseApi = MmseApi()
    
    val isLoading = mutableStateOf(false)
    val mmseScores = mutableStateOf<List<MmseAssessment>>(emptyList())
    val errorMessage = mutableStateOf<String?>(null)

    // Bento Grid Insight States
    val latestScore = mutableStateOf<Int?>(null)
    val trendStatus = mutableStateOf("Stable") // Improving, Stable, Declining
    val completionRate = mutableStateOf(0) // Percentage of consistency
    val lastAssessmentDate = mutableStateOf<String?>(null)

    fun fetchMmseAssessments(userId: String) {
        CoroutineScope(Dispatchers.Main).launch {
            isLoading.value = true
            errorMessage.value = null
            
            try {
                val result = withContext(Dispatchers.Default) {
                    mmseApi.getUserMmseAssessments(userId)
                }
                
                result.onSuccess { response ->
                    val completed = response.assessments
                        .filter { it.status == "completed" }
                        .sortedByDescending { it.assessment_date }
                    
                    mmseScores.value = completed
                    
                    // Calculate Insights
                    if (completed.isNotEmpty()) {
                        latestScore.value = completed.first().total_score.toInt()
                        lastAssessmentDate.value = completed.first().assessment_date
                        
                        // Calculate Trend (Comparing last 2)
                        if (completed.size >= 2) {
                            val current = completed[0].total_score
                            val previous = completed[1].total_score
                            trendStatus.value = when {
                                current > previous -> "Improving"
                                current < previous -> "Declining"
                                else -> "Stable"
                            }
                        }
                        
                        // Calculate Completion Rate (Mock logic based on frequency)
                        completionRate.value = ((completed.size.toFloat() / 15f) * 100).toInt().coerceAtMost(100)
                    }
                }.onFailure { error ->
                    errorMessage.value = error.message ?: "Failed to fetch assessments"
                    println("MmseViewModel: Error fetching assessments: ${error.message}")
                }
            } catch (e: Exception) {
                errorMessage.value = e.message ?: "An unexpected error occurred"
                println("MmseViewModel: Unexpected error: ${e.message}")
            } finally {
                isLoading.value = false
            }
        }
    }
}
