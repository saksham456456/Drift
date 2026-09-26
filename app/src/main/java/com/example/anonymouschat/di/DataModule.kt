package com.example.anonymouschat.di

import com.example.anonymouschat.data.DataRepository
import com.example.anonymouschat.data.DefaultDataRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {
    @Binds
    abstract fun bindDataRepository(
        defaultDataRepository: DefaultDataRepository
    ): DataRepository
}
