package com.example.steppermodule.domain

import io.reactivex.Observable
import io.reactivex.ObservableTransformer

interface StepperService {
    sealed class Action {
        data class ChangeCount(val currentCount: Int, val changeCount: Int) : Action()
    }

    sealed class Result {
        data class NewCount(val newCount: Int) : Result()
    }
}

class StepperServiceImpl : StepperService {
    //todo(): inject
    //these are like use-cases
    private val changeCountAction: ChangeCountActionProcessor = ChangeCountActionProcessor()

    fun state() = ObservableTransformer<StepperService.Action, StepperService.Result> { actions ->
        actions.publish { shared ->
            Observable.mergeArray(
                //different use-cases I can include like below
                /*shared.ofType(StepperService.Action.ChangeCount::class.java).compose(changeCountAction),
                shared.ofType(StepperService.Action.DoSomething1::class.java).compose(doSomething1),
                shared.ofType(StepperService.Action.DoSomething2::class.java).compose(doSomething2),
                shared.ofType(StepperService.Action.DoSomething2::class.java).compose(doSomething3),*/

                //for now, I have just one use-case
                shared.ofType(StepperService.Action.ChangeCount::class.java)
                    .compose(changeCountAction)
            )
        }
    }
}