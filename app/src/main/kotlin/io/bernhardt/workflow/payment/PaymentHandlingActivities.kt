package io.bernhardt.workflow.payment

import io.temporal.activity.ActivityInterface
import io.temporal.activity.ActivityMethod


@ActivityInterface
interface PaymentHandlingActivity {

    @ActivityMethod
    fun retrieveConfiguration(merchantId: MerchantId, userId: UserId): PaymentConfiguration?

    @ActivityMethod
    fun dispatchForSettlement(transactionId: TransactionId, merchantId: MerchantId, userId: UserId, amount: Int)
}

data class PaymentConfiguration(val merchantConfiguration: MerchantConfiguration, val userConfiguration: UserConfiguration)
