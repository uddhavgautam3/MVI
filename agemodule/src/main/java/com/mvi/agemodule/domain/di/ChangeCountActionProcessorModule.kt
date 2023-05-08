package com.mvi.agemodule.domain.di

import com.mvi.agemodule.domain.ChangeCountActionProcessor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class ChangeCountActionProcessorModule {
    @Provides
    @Singleton
    fun provideChangeCountActionProcessor() = ChangeCountActionProcessor()
}