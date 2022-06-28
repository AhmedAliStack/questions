package com.example.taskapp.view.answer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskapp.intent.answers_intent.IntentAnswer
import com.example.taskapp.intent.answers_intent.StateAnswer
import com.example.taskapp.model.data_model.CustomAnswerData
import com.example.taskapp.model.data_model.QuestionsModel
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.*
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList


@HiltViewModel
class AnswerViewModel @Inject constructor() : ViewModel() {
    var intentAnswer = Channel<IntentAnswer>()
    private val _state = MutableStateFlow<StateAnswer>(StateAnswer.Idle)
    val state: StateFlow<StateAnswer> get() = _state

    val currentScore = MutableStateFlow(0)
    val currentQuestion = MutableStateFlow(1)
    val questions = MutableStateFlow<QuestionsModel?>(null)
    val runningQuestion = MutableStateFlow<QuestionsModel.Question?>(null)

    init {
        handleIntent()
    }

    private fun handleIntent() {
        viewModelScope.launch {
            intentAnswer.consumeEach {
                when (it) {
                    is IntentAnswer.ConvertData -> {
                        convertDataToStack(it.questions)
                    }
                }
            }
        }
    }

    private fun convertDataToStack(questions: QuestionsModel) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val questionsStack = Stack<QuestionsModel.Question>()
                Collections.shuffle(questions.questions)
                questions.questions?.forEach {
                    questionsStack.push(it)
                }
                _state.value = StateAnswer.StackQuestions(questionsStack)
            } catch (e: Exception) {
                if (e is CancellationException) {
                    throw CancellationException()
                }
                withContext(Dispatchers.Main) {
                    _state.value = StateAnswer.Error(e.localizedMessage)
                }
            }
        }
    }

    fun extractAnswers(
        answers: QuestionsModel.Question.Answers?,
        correctAnswer: String?
    ): ArrayList<CustomAnswerData> {
        val resultArray = ArrayList<CustomAnswerData>()
        val jsonAnswers = JSONObject(Gson().toJson(answers))
        val iter: Iterator<String> = jsonAnswers.keys()
        while (iter.hasNext()) {
            val customAnswer = CustomAnswerData()
            val key = iter.next()
            try {
                customAnswer.isCorrect = correctAnswer == key
                val value: String = jsonAnswers.getString(key)
                customAnswer.answer = value
                if (value.isNotEmpty())
                    resultArray.add(customAnswer)
            } catch (e: JSONException) {
                throw e
            }
        }
        return resultArray
    }

    fun updateScore(score: Int?) {
        viewModelScope.launch {
            score?.let { currentScore.emit(currentScore.value + it) }
        }
    }

    fun updateCurrentQuestion() {
        viewModelScope.launch {
            currentQuestion.value = currentQuestion.value + 1
        }
    }

    fun updateQuestions(parcelableQuestions: QuestionsModel?) {
        viewModelScope.launch {
            async {
                parcelableQuestions?.let {
                    intentAnswer.send(IntentAnswer.ConvertData(parcelableQuestions))
                    questions.emit(parcelableQuestions)
                }
            }
        }
    }

    fun updateRunningQuestion(passedRunningQuestion: QuestionsModel.Question?) {
        viewModelScope.launch {
            passedRunningQuestion?.let {
                runningQuestion.emit(passedRunningQuestion)
            }
        }
    }
}