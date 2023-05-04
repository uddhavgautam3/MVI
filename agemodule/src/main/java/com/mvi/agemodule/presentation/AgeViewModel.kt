package com.mvi.agemodule.presentation

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.toLiveData
import com.mvi.agemodule.domain.AgeService
import com.mvi.agemodule.domain.AgeServiceImpl
import com.mvi.agemodule.presentation.AgeView.State
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject


@HiltViewModel
class AgeViewModel @Inject constructor(
    @ApplicationContext application: Context,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val intentsSubject: PublishSubject<AgeView.Action> = PublishSubject.create()

    private val service = AgeServiceImpl()

    private val serviceState: ObservableTransformer<AgeService.Action, AgeService.Result>
        get() = service.state()

    //things start to change here in our compose flow
    fun registerActions(actions: Observable<AgeView.Action>) {
        actions.subscribe(intentsSubject)
    }

    //mapping of Services and Actions
    private fun mapServiceAction(action: AgeView.Action): AgeService.Action {
        return when (action) {
            is AgeView.Action.IncreaseCount -> {
                AgeService.Action.ChangeCount(
                    currentCount = action.currentCount.toInt(),
                    changeCount = 1
                )
            }
            is AgeView.Action.DecreaseCount -> {
                AgeService.Action.ChangeCount(
                    currentCount = action.currentCount.toInt(),
                    changeCount = -1
                )
            }
        }
    }

    //below two lines should be in order
    val defaultViewState: State = State.Loaded("25")
    private val statesObservable: Observable<State> by lazy { composeFlow() }

    //LiveDataReactiveStreams.fromPublisher(publisher) changed to publisher.toLiveData() in androidx.lifecycle:lifecycle-runtime-ktx:2.6.1
    private val stateFlowable: Flowable<State> = statesObservable.toFlowable(
        BackpressureStrategy.BUFFER
    )

    fun state(): LiveData<State> = stateFlowable.toLiveData()

    //BiFunction<Default, get Result, and mapped to State (i.e., respond with a State)
    private val reducer: BiFunction<State, AgeService.Result, State>
        get() = BiFunction { previousState: State, result: AgeService.Result ->
            val transition = when (result) {
                is AgeService.Result.NewCount -> {
                    State.Loaded(result.newCount.toString())
                }
                is AgeService.Result.Error -> {
                    State.Error("Error: Age can't be negative!")
                }
            }
            transition
        }

    private fun composeFlow(): Observable<State> {

        return intentsSubject
            .flatMap { viewAction ->
                val serviceAction = mapServiceAction(viewAction)
                return@flatMap return@flatMap if (serviceAction == null) {
                    Observable.empty()
                } else {
                    Observable.just(serviceAction)
                }
            }
            .compose(serviceState)
            .scan(defaultViewState, reducer)
            .distinctUntilChanged()
            .replay(1)
            .autoConnect(0)
    }

}