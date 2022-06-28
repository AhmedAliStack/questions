package com.example.taskapp.intent.answers_intent

import com.example.taskapp.model.data_model.QuestionsModel
import java.util.*

sealed class StateAnswer {
    object Idle : StateAnswer()
    data class StackQuestions(val questions: Stack<QuestionsModel.Question>) : StateAnswer()
    data class Error(val error: String?) : StateAnswer()
}