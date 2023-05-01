package com.example.steppermodule.domain

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer

class ChangeCountActionProcessor :
    ObservableTransformer<StepperService.Action.ChangeCount, StepperService.Result> {

    override fun apply(upstream: Observable<StepperService.Action.ChangeCount>): ObservableSource<StepperService.Result> {
        return upstream.flatMap { action ->
            val newCount = action.currentCount + action.changeCount
            Observable.just(
                StepperService.Result.NewCount(newCount)
            )
        }
    }

}
