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

    fun fetchMmseAssessments(userId: String) {
        CoroutineScope(Dispatchers.Main).launch {
            isLoading.value = true
            errorMessage.value = null
            
            try {
                val result = withContext(Dispatchers.Default) {
                    mmseApi.getUserMmseAssessments(userId)
                }
                
                result.onSuccess { response ->
                    // Filter for completed assessments and sort by date, newest first
                    mmseScores.value = response.assessments
                        .filter { it.status == "completed" }
                        .sortedByDescending { it.assessment_date }
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
