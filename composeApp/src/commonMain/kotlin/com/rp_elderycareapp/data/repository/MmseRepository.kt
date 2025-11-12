package com.rp_elderycareapp.data.repository

import com.rp_elderycareapp.data.models.MmseQuestion

object MmseRepository {
    val questions = listOf(
        MmseQuestion("Q1", "What year is it?", "Orientation - Time", listOf("2025"), 1),
        MmseQuestion("Q2", "What season are we in right now?", "Orientation - Time", listOf("summer", "winter"), 1),
        MmseQuestion("Q3", "Please repeat after me: Apple, Table, Penny.", "Registration", listOf("apple", "table", "penny"), 3)
    )
}