package com.javhualde.nospoilerapk.di

import android.content.Context
import com.javhualde.nospoilerapk.data.billing.BillingService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BillingModule {

    @Provides
    @Singleton
    fun provideBillingService(
        @ApplicationContext context: Context
    ): BillingService {
        return BillingService(context)
    }
} 