package com.example.taskapp.intent.main_intent

import com.example.taskapp.model.data_model.QuestionsModel

sealed class StateMain {
    object Idle : StateMain()
    object Loading : StateMain()
    data class Questions(val questions: QuestionsModel) : StateMain()
    data class Error(val error: String?) : StateMain()
}