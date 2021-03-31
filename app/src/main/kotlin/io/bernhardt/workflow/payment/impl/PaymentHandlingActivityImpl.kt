package io.bernhardt.workflow.payment.impl

import io.bernhardt.workflow.payment.*
import javax.money.MonetaryAmount

class PaymentHandlingActivityImpl(private val configurationService: ConfigurationService): PaymentHandlingActivity {

    override fun retrieveConfiguration(merchantId: MerchantId, userId: UserId): PaymentConfiguration {
        val merchantConfiguration = configurationService.retrieveMerchantConfiguration(merchantId)
        val userConfiguration = configurationService.retrieveUserConfiguration(userId)

        requireNotNull(merchantConfiguration) { "Merchant Configuration not found" }
        requireNotNull(userConfiguration) { "User Configuration not found "}

        return PaymentConfiguration(merchantConfiguration, userConfiguration)
    }

    override fun dispatchForSettlement(transactionId: TransactionId, merchantId: MerchantId, userId: UserId, amount: MonetaryAmount) {
        // this would usually go to a message bus of sorts
        println("Settlement: transactionId:$transactionId amount:$amount merchantId:$merchantId userId:$userId")
    }
}