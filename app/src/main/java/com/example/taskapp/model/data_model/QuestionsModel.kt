package com.example.taskapp.model.data_model

import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import android.os.Parcelable

@Parcelize
data class QuestionsModel(
    @SerializedName("questions")
    val questions: List<Question?>? = listOf()
) : Parcelable {
    @Parcelize
    data class Question(
        @SerializedName("answers")
        val answers: Answers? = Answers(),
        @SerializedName("correctAnswer")
        val correctAnswer: String? = "",
        @SerializedName("question")
        val question: String? = "",
        @SerializedName("questionImageUrl")
        val questionImageUrl: String? = "",
        @SerializedName("score")
        val score: Int? = 0
    ) : Parcelable {
        @Parcelize
        data class Answers(
            @SerializedName("A")
            val a: String? = "",
            @SerializedName("B")
            val b: String? = "",
            @SerializedName("C")
            val c: String? = "",
            @SerializedName("D")
            val d: String? = ""
        ) : Parcelable
    }
}