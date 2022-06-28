package com.example.taskapp.view.answer

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.taskapp.databinding.ActivityAnswerBinding
import com.example.taskapp.intent.answers_intent.IntentAnswer
import com.example.taskapp.intent.answers_intent.StateAnswer
import com.example.taskapp.model.data_model.QuestionsModel
import com.example.taskapp.utils.UserPreferencesRepository
import com.example.taskapp.utils.loadImage
import com.example.taskapp.view.answer.adapter.AnswersAdapter
import com.example.taskapp.view.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import java.util.*

@AndroidEntryPoint
class AnswerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAnswerBinding
    private val viewModel: AnswerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAnswerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if(savedInstanceState == null){
            handleReceivedData()
        }

        observeViewModel()

    }

    private fun handleReceivedData() {
        viewModel.updateQuestions(intent.getParcelableExtra("Response"))
    }

    private fun observeViewModel() {
        lifecycleScope.launchWhenStarted {
            viewModel.state.collectLatest {
                when (it) {
                    is StateAnswer.Error -> {
                        Toast.makeText(this@AnswerActivity, it.error, Toast.LENGTH_LONG).show()
                    }
                    StateAnswer.Idle -> {

                    }
                    is StateAnswer.StackQuestions -> {
                        updateUi(it.questions)
                    }
                }
            }
        }
    }

    private fun updateUi(questionsStack: Stack<QuestionsModel.Question>) {
        lifecycleScope.launch {
            launch {
                viewModel.updateRunningQuestion(questionsStack.peek())
            }
            launch {
                viewModel.runningQuestion.collectLatest { runningQuestion ->
                    binding.run {
                        tvQuestionPoints.text = "${runningQuestion?.score} Points"
                        tvQuestion.text = runningQuestion?.question
                        ivQuestion.loadImage(
                            this@AnswerActivity,
                            runningQuestion?.questionImageUrl ?: ""
                        )
                        lifecycleScope.launch {
                            viewModel.currentScore.collectLatest {
                                tvCurrentScore.text = "Current Score: $it"
                            }
                        }
                        updateRecycler(questionsStack, runningQuestion)
                    }
                }
            }
        }

        viewModel.currentQuestion.combine(viewModel.questions) { current, questions ->
            binding.tvCurrentQuestion.text =
                "Question $current / ${questions?.questions?.size}"
        }.launchIn(lifecycleScope)
    }

    private fun ActivityAnswerBinding.updateRecycler(
        questions: Stack<QuestionsModel.Question>,
        runningQuestion: QuestionsModel.Question?
    ) {
        val answersList =
            viewModel.extractAnswers(runningQuestion?.answers, runningQuestion?.correctAnswer)
        val answerAdapter = AnswersAdapter(answersList) {
            questions.pop()
            if (questions.empty()) {
                saveAndExit()
            } else {
                if (it.isCorrect)
                    viewModel.updateScore(runningQuestion?.score)

                lifecycleScope.launch {
                    delay(2000)
                    launch {
                        viewModel.updateCurrentQuestion()
                    }
                    updateUi(questions)
                }
            }
        }
        rvAnswers.run {
            adapter = answerAdapter
            layoutManager = LinearLayoutManager(this@AnswerActivity)
        }
        answerAdapter.notifyItemRangeChanged(0, answersList.size)
    }

    private fun saveAndExit() {
        lifecycleScope.launch {
            UserPreferencesRepository(this@AnswerActivity).run {
                async {
                    getUserScore.combine(viewModel.currentScore) { userScore, currentScore ->
                        if (currentScore > userScore.toInt()) {
                            setUserScore(currentScore.toString())
                        }
                    }
                }
            }

            delay(2000)

            Intent(this@AnswerActivity, MainActivity::class.java).also {
                startActivity(it)
                finish()
            }
        }
    }
}