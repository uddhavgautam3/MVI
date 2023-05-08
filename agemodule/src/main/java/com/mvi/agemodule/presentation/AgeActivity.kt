package com.mvi.agemodule.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.ui.Modifier
import com.mvi.agemodule.ui.theme.MVITheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AgeActivity : ComponentActivity() {

    private val AgeViewModel by viewModels<AgeViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MVITheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    AgeScreen(AgeViewModel)
                }
            }
        }
    }
}