package com.ahmed.drivetest.ui

import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.ahmed.drivetest.R
import com.ahmed.drivetest.data.QuestionRepository
import com.ahmed.drivetest.model.Question

class TrainingActivity : AppCompatActivity() {

    private lateinit var repository: QuestionRepository
    private var queue: List<Question> = emptyList()
    private var currentIndex = 0
    private var answered = false

    private lateinit var tvProgress: TextView
    private lateinit var tvQuestion: TextView
    private lateinit var tvExplanation: TextView
    private lateinit var ivSign: ImageView
    private lateinit var btnNext: Button
    private val optionButtons = mutableListOf<Button>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_training)

        repository = QuestionRepository(this)
        queue = repository.getTrainingQueue()

        tvProgress = findViewById(R.id.tvProgress)
        tvQuestion = findViewById(R.id.tvQuestion)
        tvExplanation = findViewById(R.id.tvExplanation)
        ivSign = findViewById(R.id.ivSign)
        btnNext = findViewById(R.id.btnNext)

        optionButtons.add(findViewById(R.id.btnOptionA))
        optionButtons.add(findViewById(R.id.btnOptionB))
        optionButtons.add(findViewById(R.id.btnOptionC))

        btnNext.setOnClickListener { goToNextQuestion() }

        showCurrentQuestion()
    }

    private fun showCurrentQuestion() {
        if (currentIndex >= queue.size) {
            tvQuestion.text = "أحسنت! أنهيت كل الأسئلة المتاحة في هذه الجلسة 🎉"
            tvExplanation.text = ""
            ivSign.visibility = android.view.View.GONE
            optionButtons.forEach { it.visibility = android.view.View.GONE }
            btnNext.visibility = android.view.View.GONE
            return
        }

        answered = false
        tvExplanation.text = ""
        btnNext.visibility = android.view.View.GONE

        val q = queue[currentIndex]
        tvProgress.text = "${currentIndex + 1}/${queue.size}"
        tvQuestion.text = q.questionText

        if (q.imageAsset != null) {
            val resId = resources.getIdentifier(q.imageAsset, "drawable", packageName)
            if (resId != 0) {
                ivSign.setImageResource(resId)
                ivSign.visibility = android.view.View.VISIBLE
            } else {
                ivSign.visibility = android.view.View.GONE
            }
        } else {
            ivSign.visibility = android.view.View.GONE
        }

        for (i in optionButtons.indices) {
            val btn = optionButtons[i]
            btn.visibility = android.view.View.VISIBLE
            btn.setBackgroundColor(Color.parseColor("#FFFFFF"))
            if (i < q.options.size) {
                btn.text = q.options[i]
                btn.setOnClickListener { onOptionSelected(i) }
            }
        }
    }

    private fun onOptionSelected(selectedIndex: Int) {
        if (answered) return
        answered = true

        val q = queue[currentIndex]
        val isCorrect = selectedIndex == q.correctIndex

        optionButtons[q.correctIndex].setBackgroundColor(Color.parseColor("#A8E6CF"))
        if (!isCorrect) {
            optionButtons[selectedIndex].setBackgroundColor(Color.parseColor("#FFB3B3"))
        }

        tvExplanation.text = q.explanation
        btnNext.visibility = android.view.View.VISIBLE

        repository.recordAnswer(q.id, isCorrect)
    }

    private fun goToNextQuestion() {
        currentIndex++
        showCurrentQuestion()
    }
}
