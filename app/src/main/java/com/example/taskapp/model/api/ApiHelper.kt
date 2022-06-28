package com.example.taskapp.model.api

import com.example.taskapp.model.data_model.QuestionsModel
import retrofit2.Response

interface ApiHelper {
    suspend fun questionsAsync(): Response<QuestionsModel>
}