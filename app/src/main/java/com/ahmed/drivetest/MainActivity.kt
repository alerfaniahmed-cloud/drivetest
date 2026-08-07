package com.ahmed.drivetest

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.ahmed.drivetest.data.QuestionRepository
import com.ahmed.drivetest.ui.TrainingActivity

class MainActivity : AppCompatActivity() {

    private lateinit var repository: QuestionRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        repository = QuestionRepository(this)

        updateStats()

        findViewById<Button>(R.id.btnStartTraining).setOnClickListener {
            startActivity(Intent(this, TrainingActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        updateStats()
    }

    private fun updateStats() {
        val (correct, wrong, total) = repository.getStats()
        findViewById<TextView>(R.id.tvCorrectCount).text = correct.toString()
        findViewById<TextView>(R.id.tvWrongCount).text = wrong.toString()
        findViewById<TextView>(R.id.tvTotalCount).text = total.toString()
    }
}
