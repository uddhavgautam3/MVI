package com.example.steppermodule.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.steppermodule.R
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject


interface StepperView {

    //view observes state
    sealed class State {
        object Loading : State()
        object AfterLoading : State()
        data class Error(val error: String) : State()
        data class Loaded(val newCount: String) : State()
    }

    //view emits actions
    sealed class Action {
        data class IncreaseCount(val currentCount: String) : Action()
        data class DecreaseCount(val currentCount: String) : Action()
    }

}

@Composable
fun StepperScreen(stepperViewModel: StepperViewModel) {

    //observing state
    val state by stepperViewModel.state().observeAsState(stepperViewModel.defaultViewState)

    //create a publishSubject that can emit actions
    val publishSubject = PublishSubject.create<StepperView.Action>()


    //tell viewmodel to start listening whatever gets emitted from publishSubject
    stepperViewModel.registerActions(
        Observable.merge(
            listOf(publishSubject)
        )
    )

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    )
    {
        StepperRender(state, publishSubject)
    }
}

@Composable
fun StepperRender(state: StepperView.State, publishSubject: PublishSubject<StepperView.Action>) {
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
            LoadedState(state.newCount, publishSubject)
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
fun LoadedState(newCount: String, publishSubject: PublishSubject<StepperView.Action>) {
    Text("Stepper Value")
    Text(text = newCount)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Button(onClick = {
            publishSubject.onNext(StepperView.Action.DecreaseCount(newCount))
        }) {
            Text(text = "-")
        }
        Button(onClick = {
            publishSubject.onNext(StepperView.Action.IncreaseCount(newCount))
        }) {
            Text(text = "+")
        }
    }

}

@Composable
fun AfterLoadingState() {
    Text(stringResource(id = R.string.stepperview_string_after_loading_state))
}
