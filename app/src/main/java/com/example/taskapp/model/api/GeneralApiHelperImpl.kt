package com.example.taskapp.model.api

import com.example.taskapp.model.data_model.QuestionsModel
import retrofit2.Response

class GeneralApiHelperImpl(private val apiService: ApiService) : ApiHelper {

    override suspend fun questionsAsync(): Response<QuestionsModel> {
        return apiService.getQuestions()
    }

}