package com.example.agemodule.domain

import io.reactivex.Observable
import io.reactivex.ObservableTransformer

interface AgeService {
    sealed class Action {
        data class ChangeCount(val currentCount: Int, val changeCount: Int) : Action()
    }

    sealed class Result {
        data class NewCount(val newCount: Int) : Result()
        object Error : Result()
    }
}

class AgeServiceImpl : AgeService {
    //todo(): inject
    //these are like use-cases
    private val changeCountAction: ChangeCountActionProcessor = ChangeCountActionProcessor()

    fun state() = ObservableTransformer<AgeService.Action, AgeService.Result> { actions ->
        actions.publish { shared ->
            Observable.mergeArray(
                //different use-cases I can include like below
                /*shared.ofType(AgeService.Action.ChangeCount::class.java).compose(changeCountAction),
                shared.ofType(AgeService.Action.DoSomething1::class.java).compose(doSomething1),
                shared.ofType(AgeService.Action.DoSomething2::class.java).compose(doSomething2),
                shared.ofType(AgeService.Action.DoSomething2::class.java).compose(doSomething3),*/

                //for now, I have just one use-case
                shared.ofType(AgeService.Action.ChangeCount::class.java)
                    .compose(changeCountAction)
            )
        }
    }
}