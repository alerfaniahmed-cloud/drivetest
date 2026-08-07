package com.ahmed.drivetest.model

data class Question(
    val id: String,
    val category: String,
    val questionText: String,
    val imageAsset: String?,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String
)

data class QuestionProgress(
    val questionId: String,
    var timesCorrect: Int = 0,
    var timesWrong: Int = 0,
    var lastSeenTimestamp: Long = 0L,
    var nextDueTimestamp: Long = 0L
) {
    fun priorityWeight(): Int {
        val wrongWeight = timesWrong * 3
        val correctWeight = timesCorrect
        return (wrongWeight - correctWeight).coerceAtLeast(1)
    }
}
