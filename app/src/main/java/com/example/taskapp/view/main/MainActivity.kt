package com.example.taskapp.view.main

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.example.taskapp.R
import com.example.taskapp.databinding.ActivityMainBinding
import com.example.taskapp.intent.main_intent.IntentMain
import com.example.taskapp.intent.main_intent.StateMain
import com.example.taskapp.utils.UserPreferencesRepository
import com.example.taskapp.view.answer.AnswerActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        observeViewModel()

        handleActions()

        getUserScore()

    }

    private fun observeViewModel() {
        lifecycleScope.launchWhenStarted {
            viewModel.state.collectLatest {
                when(it){
                    is StateMain.Error -> {
                        handleLoading(isLoading = false)
                        Toast.makeText(this@MainActivity,it.error,Toast.LENGTH_LONG).show()
                    }
                    StateMain.Idle -> {

                    }
                    StateMain.Loading -> {
                        handleLoading(isLoading = true)
                    }
                    is StateMain.Questions -> {
                        handleLoading(isLoading = false)
                        Intent(this@MainActivity,AnswerActivity::class.java).putExtra("Response",it.questions).also { intent ->
                            startActivity(intent)
                        }
                    }
                }
            }
        }
    }

    private fun handleActions() {
        binding.run {
            btStart.setOnClickListener {
                startViewModel()
            }
        }
    }

    private fun startViewModel() {
        lifecycleScope.launch {
            viewModel.intentMain.send(IntentMain.FetchQuestions)
        }
    }

    private fun getUserScore() {
        lifecycleScope.launchWhenStarted {
            UserPreferencesRepository(this@MainActivity).getUserScore.collectLatest {
                Log.d("UserCurrentScore",it)
                binding.tvScoreValue.text = it
            }
        }
    }

    private fun handleLoading(isLoading:Boolean) {
        binding.run {
            pbLoading.isVisible = isLoading
            btStart.isVisible = !isLoading
        }
    }
}