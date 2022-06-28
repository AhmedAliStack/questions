package com.example.taskapp.model.api

import com.example.taskapp.model.data_model.QuestionsModel
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @GET("quiz.json")
    suspend fun getQuestions(): Response<QuestionsModel>

}