package com.example.steppermodule.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.toLiveData
import com.example.steppermodule.presentation.StepperView.State
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.PublishSubject


class StepperViewModel : ViewModel() {

    private val statesObservable: Observable<State> by lazy { composeFlow() }

    //LiveDataReactiveStreams.fromPublisher(publisher) changed to publisher.toLiveData() in androidx.lifecycle:lifecycle-runtime-ktx:2.6.1
    private val stateFlowable: Flowable<State> = statesObservable.toFlowable(
        BackpressureStrategy.BUFFER)

    fun state(): LiveData<State> = stateFlowable.toLiveData()

    val defaultViewState: State = State.Loading(25)

    private val reducer: BiFunction<State, State, State>
        get() = BiFunction { previousState: State, result: State ->
            val transition = when (result) {
                is State.Loading -> {
                    State.Loading(25)
                }
                is State.AfterLoading -> {
                    State.AfterLoading
                }
                is State.Error -> {
                    State.Error("Error Occurred!")
                }
                is State.Loaded -> {
                    State.Loaded(5)
                }
            }
            transition
        }

    private fun composeFlow(): Observable<State> {
        val intentsSubject: PublishSubject<State> = PublishSubject.create()

        return intentsSubject
            .scan(defaultViewState, reducer)
            .distinctUntilChanged()
            .replay(1)
            .autoConnect(0)
    }

}