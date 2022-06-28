package com.example.taskapp.view.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskapp.intent.main_intent.IntentMain
import com.example.taskapp.intent.main_intent.StateMain
import com.example.taskapp.model.repo.GeneralRepo
import com.google.gson.JsonParser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val generalRepo: GeneralRepo) : ViewModel() {

    var intentMain = Channel<IntentMain>()
    private val _state = MutableStateFlow<StateMain>(StateMain.Idle)
    val state: StateFlow<StateMain> get() = _state

    init {
        handleIntent()
    }

    private fun handleIntent() {
        viewModelScope.launch {
            intentMain.consumeEach {
                when (it) {
                    is IntentMain.FetchQuestions -> {
                        fetchQuestions()
                    }
                }
            }
        }
    }

    private fun fetchQuestions() {
        viewModelScope.launch(Dispatchers.IO) {
            _state.value = StateMain.Loading
            try {
                val response = generalRepo.questions()
                if (response.code() == 200)
                    response.body()?.let {
                        _state.value = StateMain.Questions(
                            it
                        )
                    }
                else {
                    val msg = JsonParser().parse(
                        response.errorBody()?.string()
                    ).asJsonObject.get("message").asString
                    _state.value = StateMain.Error(msg)
                }
            } catch (e: Exception) {
                if (e is CancellationException) {
                    throw CancellationException()
                }
                withContext(Dispatchers.Main) {
                    _state.value = StateMain.Error(e.localizedMessage)
                }
            }
        }
    }
}