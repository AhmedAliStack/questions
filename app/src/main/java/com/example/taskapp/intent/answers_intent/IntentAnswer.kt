package com.example.taskapp.intent.answers_intent

import com.example.taskapp.model.data_model.QuestionsModel

sealed class IntentAnswer {
    data class ConvertData(val questions: QuestionsModel) : IntentAnswer()
}