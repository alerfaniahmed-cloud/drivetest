package com.ahmed.drivetest.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import com.ahmed.drivetest.model.Question
import com.ahmed.drivetest.model.QuestionProgress

class QuestionRepository(private val context: Context) {

    private val prefsName = "drivetest_progress"
    private var cachedQuestions: List<Question>? = null

    fun loadAllQuestions(): List<Question> {
        cachedQuestions?.let { return it }

        val jsonText = context.assets.open("questions.json")
            .bufferedReader(Charsets.UTF_8)
            .use { it.readText() }

        val root = JSONObject(jsonText)
        val arr: JSONArray = root.getJSONArray("questions")

        val list = mutableListOf<Question>()
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            val optionsArr = obj.getJSONArray("options")
            val options = mutableListOf<String>()
            for (j in 0 until optionsArr.length()) {
                options.add(optionsArr.getString(j))
            }
            list.add(
                Question(
                    id = obj.getString("id"),
                    category = obj.getString("category"),
                    questionText = obj.getString("questionText"),
                    imageAsset = if (obj.isNull("imageAsset")) null else obj.getString("imageAsset"),
                    options = options,
                    correctIndex = obj.getInt("correctIndex"),
                    explanation = obj.getString("explanation")
                )
            )
        }
        cachedQuestions = list
        return list
    }

    fun getProgress(questionId: String): QuestionProgress {
        val prefs = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
        val correct = prefs.getInt("${questionId}_correct", 0)
        val wrong = prefs.getInt("${questionId}_wrong", 0)
        val lastSeen = prefs.getLong("${questionId}_lastSeen", 0L)
        val nextDue = prefs.getLong("${questionId}_nextDue", 0L)
        return QuestionProgress(questionId, correct, wrong, lastSeen, nextDue)
    }

    fun recordAnswer(questionId: String, wasCorrect: Boolean) {
        val prefs = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        val progress = getProgress(questionId)

        if (wasCorrect) {
            progress.timesCorrect++
        } else {
            progress.timesWrong++
        }
        progress.lastSeenTimestamp = System.currentTimeMillis()

        val intervalMillis = when {
            wasCorrect && progress.timesCorrect >= 3 -> 3 * 24 * 60 * 60 * 1000L
            wasCorrect -> 12 * 60 * 60 * 1000L
            else -> 0L
        }
        progress.nextDueTimestamp = System.currentTimeMillis() + intervalMillis

        editor.putInt("${questionId}_correct", progress.timesCorrect)
        editor.putInt("${questionId}_wrong", progress.timesWrong)
        editor.putLong("${questionId}_lastSeen", progress.lastSeenTimestamp)
        editor.putLong("${questionId}_nextDue", progress.nextDueTimestamp)
        editor.apply()
    }

    fun getTrainingQueue(): List<Question> {
        val all = loadAllQuestions()
        val now = System.currentTimeMillis()

        return all
            .map { q -> Pair(q, getProgress(q.id)) }
            .filter { (_, progress) -> progress.nextDueTimestamp <= now }
            .sortedByDescending { (_, progress) -> progress.priorityWeight() }
            .map { (q, _) -> q }
            .ifEmpty { all.shuffled() }
    }

    fun getStats(): Triple<Int, Int, Int> {
        val all = loadAllQuestions()
        var correct = 0
        var wrong = 0
        for (q in all) {
            val p = getProgress(q.id)
            if (p.timesCorrect > 0) correct++
            if (p.timesWrong > 0 && p.timesCorrect == 0) wrong++
        }
        return Triple(correct, wrong, all.size)
    }
}
