package com.javhualde.nospoilerapk.ui.viewmodels

import android.app.Activity
import androidx.lifecycle.ViewModel
import com.javhualde.nospoilerapk.data.billing.BillingService
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SubscriptionViewModel @Inject constructor(
    private val billingService: BillingService
) : ViewModel() {

    val subscriptionStatus = billingService.subscriptionStatus

    suspend fun purchaseSubscription(activity: Activity, productId: String) {
        billingService.launchBillingFlow(activity, productId)
    }
} 