package com.rp_elderycareapp.data

/**
 * Complete MMSE (Mini-Mental State Examination) Data Structures
 * Based on Standardized MMSE (SMMSE) 2014 Guidelines
 */

// MMSE Question with scoring information
data class MmseQuestion(
    val id: Int,
    val question: String,
    val category: String,
    val maxPoints: Int = 1,
    val instructions: String = "",
    val timeLimit: Int = 10 ,// seconds
    val requiresCaregiverEvaluation: Boolean = false
)

// Complete MMSE Test Questions (Total: 30 points)
object MmseQuestions {
    val allQuestions = listOf(
        // ORIENTATION TO TIME (5 points total)
        MmseQuestion(
            id = 1,
            question = "What year is this?",
            category = "Year",
            maxPoints = 1,
            instructions = "Accept exact answer only",
            timeLimit = 10,
        ),
        MmseQuestion(
            id = 2,
            question = "What season is this?",
            category = "Season",
            maxPoints = 1,
            instructions = "Accept either last week of old season or first week of new season",
            timeLimit = 10,
            requiresCaregiverEvaluation = true
        ),
        MmseQuestion(
            id = 3,
            question = "What month is this?",
            category = "Month",
            maxPoints = 1,
            instructions = "Accept either first day of new month or last day of previous month",
            timeLimit = 10,
        ),
        MmseQuestion(
            id = 4,
            question = "What is today's date?",
            category = "Date",
            maxPoints = 1,
            instructions = "Accept previous or next date",
            timeLimit = 10,
        ),
        MmseQuestion(
            id = 5,
            question = "What day of the week is this?",
            category = "Day",
            maxPoints = 1,
            instructions = "Accept exact answer only",
            timeLimit = 10,
        ),

        // ORIENTATION TO PLACE (5 points total)
        MmseQuestion(
            id = 6,
            question = "What country are we in?",
            category = "Country",
            maxPoints = 1,
            instructions = "Accept exact answer only",
            timeLimit = 10,
            requiresCaregiverEvaluation = true
        ),
        MmseQuestion(
            id = 7,
            question = "What province are we in?",
            category = "Province",
            maxPoints = 1,
            instructions = "Accept exact answer only",
            timeLimit = 10,
            requiresCaregiverEvaluation = true
        ),
        MmseQuestion(
            id = 8,
            question = "What city or town are we in?",
            category = "City",
            maxPoints = 1,
            instructions = "Accept exact answer only",
            timeLimit = 10,
            requiresCaregiverEvaluation = true
        ),
        MmseQuestion(
            id = 9,
            question = "What is the name of this building?",
            category = "Building",
            maxPoints = 1,
            instructions = "In home: street address. In facility: building name",
            timeLimit = 10,
            requiresCaregiverEvaluation = true
        ),
        MmseQuestion(
            id = 10,
            question = "What floor of the building are we on?",
            category = "Floor",
            maxPoints = 1,
            instructions = "In home: what room. In facility: what floor",
            timeLimit = 10,
            requiresCaregiverEvaluation = true
        ),

        // REGISTRATION (3 points total)
        MmseQuestion(
            id = 11,
            question = "I am going to name three objects. When I am finished, I want you to repeat them. Remember what they are because I am going to ask you to name them again in a few minutes. Ball, Car, Man. Please repeat the three items.",
            category = "Registration",
            maxPoints = 3,
            instructions = "Score one point for each correct reply on first attempt. Repeat up to 5 times if needed.",
            timeLimit = 20
        ),

        // ATTENTION AND CALCULATION (5 points total)
        MmseQuestion(
            id = 12,
            question = "Spell the word WORLD. Now spell it backwards please.",
            category = "Attention",
            maxPoints = 5,
            instructions = "Score based on maximum non-crossing lines matching DLROW",
            timeLimit = 30
        ),

        // RECALL (3 points total)
        MmseQuestion(
            id = 13,
            question = "Now what were the three objects I asked you to remember?",
            category = "Recall",
            maxPoints = 3,
            instructions = "Score one point for each correct answer regardless of order",
            timeLimit = 10
        ),

        // NAMING (2 points total)
        MmseQuestion(
            id = 14,
            question = "What are these called?",
            category = "Naming",
            maxPoints = 2,
            instructions = "Identify both the wristwatch and the pencil.",
            timeLimit = 20
        ),

        // REPETITION (1 point total)
        MmseQuestion(
            id = 15,
            question = "I would like you to repeat a phrase after me: No ifs, ands or buts",
            category = "Repetition",
            maxPoints = 1,
            instructions = "Must be exact. 'No ifs or buts' scores 0",
            timeLimit = 10
        ),

        // READING (1 point total)
//        MmseQuestion(
//            id = 17,
//            question = "Read the words on this page and then do what it says: CLOSE YOUR EYES",
//            category = "Reading",
//            maxPoints = 1,
//            instructions = "Score 1 only if subject closes eyes. Does not need to read aloud.",
//            timeLimit = 10,
//            requiresCaregiverEvaluation = true
//        ),

        // WRITING (1 point total)
//        MmseQuestion(
//            id = 18,
//            question = "Write any complete sentence on the paper.",
//            category = "Writing",
//            maxPoints = 1,
//            instructions = "Sentence must make sense. Ignore spelling errors.",
//            timeLimit = 30,
//            requiresCaregiverEvaluation = true
//        ),

        // DRAWING (1 point total)
//        MmseQuestion(
//            id = 19,
//            question = "Copy this design please. (Two five-sided figures intersecting to make a four-sided figure)",
//            category = "Drawing",
//            maxPoints = 1,
//            instructions = "Must draw a four-sided figure between two five-sided figures",
//            timeLimit = 60,
//            requiresCaregiverEvaluation = true
//        ),

        // THREE-STAGE COMMAND (3 points total)
//        MmseQuestion(
//            id = 20,
//            question = "Take this paper in your right hand, fold the paper in half once with both hands, and put the paper down on the floor.",
//            category = "Command",
//            maxPoints = 3,
//            instructions = "Score 1 for each: takes paper correctly, folds in half, puts on floor",
//            timeLimit = 30,
//            requiresCaregiverEvaluation = true
//        )
    )
}

// MMSE Answer Record
data class MmseAnswer(
    val questionId: Int,
    val answer: String,
    val pointsEarned: Int,
    val maxPoints: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val timeTaken: Int = 0 // seconds
)

// MMSE Test Session
data class MmseTestSession(
    val sessionId: String,
    val userId: String,
    val startTime: Long,
    val endTime: Long? = null,
    val answers: List<MmseAnswer> = emptyList(),
    val totalScore: Int = 0,
    val maxScore: Int = 30,
    val isCompleted: Boolean = false
)

// MMSE Score Interpretation
enum class CognitiveStage(
    val scoreRange: IntRange,
    val description: String,
    val severity: String,
    val duration: String
) {
    NORMAL(26..30, "Could be normal", "Normal", "Varies"),
    MILD(20..25, "Mild cognitive impairment", "Early stage", "0-2 years"),
    MODERATE(10..19, "Moderate cognitive impairment", "Middle stage", "4-7 years"),
    SEVERE(0..9, "Severe cognitive impairment", "Late stage", "7-14 years");

    companion object {
        fun fromScore(score: Int): CognitiveStage {
            return values().first { score in it.scoreRange }
        }
    }
}


