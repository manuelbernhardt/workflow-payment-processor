package io.bernhardt.workflow.payment.impl

import io.bernhardt.workflow.payment.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class PaymentHandlingActivitiesImpl(private val configurationService: ConfigurationService): PaymentHandlingActivities {

    private val logger: Logger = LoggerFactory.getLogger(PaymentHandlingActivitiesImpl::class.java)

    override fun retrieveConfiguration(merchantId: MerchantId, userId: UserId): PaymentConfiguration {
        val merchantConfiguration = configurationService.retrieveMerchantConfiguration(merchantId)
        val userConfiguration = configurationService.retrieveUserConfiguration(userId)

        requireNotNull(merchantConfiguration) { "Merchant Configuration not found" }
        requireNotNull(userConfiguration) { "User Configuration not found "}

        return PaymentConfiguration(merchantConfiguration, userConfiguration)
    }

    override fun dispatchForSettlement(transactionId: TransactionId, merchantId: MerchantId, userId: UserId, amount: Int) {
        // this would usually go to a message bus of sorts
        logger.debug("Settlement: transactionId:$transactionId amount:$amount merchantId:$merchantId userId:$userId")
    }
}