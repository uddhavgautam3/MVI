package com.example.steppermodule.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.ui.Modifier
import com.example.steppermodule.presentation.theme.MVITheme

class StepperActivity : ComponentActivity() {
    private lateinit var stepperViewModel: StepperViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        stepperViewModel = StepperViewModel()

        setContent {
            MVITheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    StepperScreen(stepperViewModel)
                }
            }
        }
    }
}