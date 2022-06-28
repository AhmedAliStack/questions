package com.example.taskapp.model.repo

import com.example.taskapp.model.api.ApiHelper

class GeneralRepo(private val apiHelper: ApiHelper) {
    suspend fun questions() = apiHelper.questionsAsync()
}