package com.example.steppermodule.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.steppermodule.R


interface StepperView {

    sealed class State {
        object Loading : State()
        object AfterLoading : State()
        data class Error(val error: String) : State()
        data class Loaded(val newCount: Int) : State()
    }

}

@Composable
fun StepperScreen(stepperViewModel: StepperViewModel) {

    val state by stepperViewModel.state().observeAsState(stepperViewModel.defaultViewState)

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    )
    {
        StepperRender(state)
    }
}

@Composable
fun StepperRender(state: StepperView.State) {
    when (state) {
        is StepperView.State.Loading -> {
            LoadingState()
        }
        is StepperView.State.AfterLoading -> {
            AfterLoadingState()
        }
        is StepperView.State.Error -> {
            ErrorState(state.error)
        }
        is StepperView.State.Loaded -> {
            LoadedState()
        }
    }
}

@Composable
fun LoadingState() {
    CircularProgressIndicator()
}

@Composable
fun ErrorState(error: String) {
    Text("Error: $error")
}

@Composable
fun LoadedState() {
    Text("Loaded")
}

@Composable
fun AfterLoadingState() {
    Text(stringResource(id = R.string.stepperview_string_after_loading_state))
}
