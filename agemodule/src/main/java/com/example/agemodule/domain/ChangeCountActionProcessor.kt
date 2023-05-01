package com.example.agemodule.domain

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer

class ChangeCountActionProcessor :
    ObservableTransformer<AgeService.Action.ChangeCount, AgeService.Result> {

    override fun apply(upstream: Observable<AgeService.Action.ChangeCount>): ObservableSource<AgeService.Result> {
        return upstream.flatMap { action ->

            val newCount = action.currentCount + action.changeCount

            if (newCount >= 0) {
                Observable.just(AgeService.Result.NewCount(newCount))
            } else {
                Observable.just(AgeService.Result.Error)
            }
        }
    }

}
