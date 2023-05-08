package com.mvi.agemodule.domain.di

import com.mvi.agemodule.domain.AgeService
import com.mvi.agemodule.domain.AgeServiceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
abstract class AgeServiceModule {
    @Binds
    abstract fun bindAgeService(ageServiceImpl: AgeServiceImpl): AgeService
}