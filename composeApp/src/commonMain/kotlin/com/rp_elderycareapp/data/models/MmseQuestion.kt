package com.rp_elderycareapp.data.models

data class MmseQuestion(
    val id: String,
    val prompt: String,
    val section: String,
    val correctAnswers: List<String>,
    val points: Int
)