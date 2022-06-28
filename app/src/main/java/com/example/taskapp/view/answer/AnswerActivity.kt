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
import kotlinx.coroutines.flow.collect
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

        //Avoid emitting data when device rotation
        if(savedInstanceState == null){
            handleReceivedData()
        }

        observeViewModel()

    }

    //Receive Data from Intent and update the ViewModel
    private fun handleReceivedData() {
        viewModel.updateQuestions(intent.getParcelableExtra("Response"))
    }

    //Result of the Intent came from ViewModel
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

    //Update the UI async
    private fun updateUi(questionsStack: Stack<QuestionsModel.Question>) {
        lifecycleScope.launch {
            async {
                viewModel.updateRunningQuestion(questionsStack.peek())
            }
            async {
                viewModel.runningQuestion.collectLatest { runningQuestion ->
                    binding.run {
                        tvQuestionPoints.text = "${runningQuestion?.score} Points"
                        tvQuestion.text = runningQuestion?.question
                        ivQuestion.loadImage(
                            this@AnswerActivity,
                            runningQuestion?.questionImageUrl ?: ""
                        )
                        updateRecycler(questionsStack, runningQuestion)
                    }
                }
            }
            async {
                viewModel.currentScore.collectLatest {
                    binding.tvCurrentScore.text = "Current Score: $it"
                }
            }
        }

        viewModel.currentQuestion.combine(viewModel.questions) { current, questions ->
            binding.run {

                pbProgress.max = questions?.questions?.size ?: 0
                pbProgress.progress = current

                tvCurrentQuestion.text =
                    "Question $current / ${questions?.questions?.size}"
            }
        }.launchIn(lifecycleScope)
    }

    //Handle the recyclerview
    private fun ActivityAnswerBinding.updateRecycler(
        questions: Stack<QuestionsModel.Question>,
        runningQuestion: QuestionsModel.Question?
    ) {
        val answersList =
            viewModel.extractAnswers(runningQuestion?.answers, runningQuestion?.correctAnswer)
        val answerAdapter = AnswersAdapter(answersList) {
            questions.pop()
            if (it.isCorrect)
                viewModel.updateScore(runningQuestion?.score)
            if (questions.empty()) {
                saveAndExit()
            } else {
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

    //Save the result and return to main screen
    private fun saveAndExit() {
        lifecycleScope.launch {
            UserPreferencesRepository(this@AnswerActivity).run {
                    getUserScore.combine(viewModel.currentScore) { userScore, currentScore ->
                        if (currentScore > userScore.toInt()) {
                            setUserScore(currentScore.toString())
                        }
                    }.launchIn(lifecycleScope)
            }

            delay(2000)

            Intent(this@AnswerActivity, MainActivity::class.java).also {
                startActivity(it)
                finish()
            }
        }
    }
}