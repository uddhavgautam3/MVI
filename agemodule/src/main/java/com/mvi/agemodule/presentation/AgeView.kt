package com.mvi.agemodule.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject


interface AgeView {

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
fun AgeScreen(AgeViewModel: AgeViewModel) {

    //observing state
    val state by AgeViewModel.state().observeAsState(AgeViewModel.defaultViewState)

    //create a publishSubject that can emit actions
    val publishSubject = PublishSubject.create<AgeView.Action>()


    //tell viewmodel to start listening whatever gets emitted from publishSubject
    AgeViewModel.registerActions(
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
        AgeRender(state, publishSubject)
    }
}

@Composable
fun AgeRender(state: AgeView.State, publishSubject: PublishSubject<AgeView.Action>) {
    when (state) {
        is AgeView.State.Loading -> {
            LoadingState()
        }
        is AgeView.State.AfterLoading -> {
            AfterLoadingState()
        }
        is AgeView.State.Error -> {
            ErrorState(state.error)
        }
        is AgeView.State.Loaded -> {
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
fun LoadedState(newCount: String, publishSubject: PublishSubject<AgeView.Action>) {
    Text("Your Age")
    Text(text = newCount)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Button(onClick = {
            publishSubject.onNext(AgeView.Action.DecreaseCount(newCount))
        }) {
            Text(text = "-")
        }
        Button(onClick = {
            publishSubject.onNext(AgeView.Action.IncreaseCount(newCount))
        }) {
            Text(text = "+")
        }
    }

}

@Composable
fun AfterLoadingState() {
    Text("After Loading")
}
