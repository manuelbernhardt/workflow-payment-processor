package io.bernhardt.workflow.payment.impl

import io.bernhardt.workflow.payment.creditcard.*
import io.bernhardt.workflow.payment.*
import io.temporal.activity.ActivityOptions
import io.temporal.workflow.Workflow
import io.temporal.activity.LocalActivityOptions
import org.slf4j.LoggerFactory
import java.time.Duration

class PaymentHandlingWorkflowImpl(useLocalActivities: Boolean = false): PaymentHandlingWorkflow {

    private val logger = LoggerFactory.getLogger(PaymentHandlingWorkflowImpl::class.java)

    private val localOptions = LocalActivityOptions.newBuilder()
            .setStartToCloseTimeout(Duration.ofSeconds(5))
            .build()

    private val options = ActivityOptions.newBuilder()
            .setStartToCloseTimeout(Duration.ofSeconds(5))
            .build()

    private val paymentHandling: PaymentHandlingActivities = if (useLocalActivities) {
        Workflow.newLocalActivityStub(PaymentHandlingActivities::class.java, localOptions)
    } else {
        Workflow.newActivityStub(PaymentHandlingActivities::class.java, options)
    }
    private val creditCard: CreditCardProcessingActivity = if(useLocalActivities) {
        Workflow.newLocalActivityStub(CreditCardProcessingActivity::class.java, localOptions)
    } else {
        Workflow.newActivityStub(CreditCardProcessingActivity::class.java, options)
    }

    override fun handlePayment(orderId: OrderId, amount: Int, merchantId: MerchantId, userId: UserId): PaymentResult {
        logger.info("Handling new payment for $orderId")
        val paymentConfiguration = paymentHandling.retrieveConfiguration(merchantId, userId)

        paymentConfiguration?.let { config ->
            when(val method = config.userConfiguration.paymentMethod) {
                is CreditCard -> {
                    val result = processCreditCardPayment(orderId, amount, config.merchantConfiguration, userId, method)
                    logger.info("Credit Card processing result $result")

                    return when(result) {
                        is CreditCardPaymentSuccess -> {
                            // notice how all the state we need here is in scope, even though the execution
                            // could have been paused in between
                            paymentHandling.dispatchForSettlement(result.transactionId, merchantId, userId, amount)
                            PaymentSuccess(result.transactionId)
                        }
                        is CreditCardPaymentFailure ->
                            PaymentFailure("Credit card payment failure: ${result.reason}")
                    }
                }
            }
        }

        return PaymentFailure("Configuration error")
    }

    private fun processCreditCardPayment(orderId: OrderId, amount: Int, merchantConfiguration: MerchantConfiguration, userId: UserId, card: CreditCard): CreditCardPaymentResult {
        val details = creditCard.retrieveCreditCardDetails(card.id)

        if (details != null) {
            // authorization and capture below are idempotent operations
            // if the workflow execution were to crash at any point in time, calling these operations again would yield the same results
            // (including stable identifiers in case of success)
            return when (val authorization = creditCard.authorize(details.id, amount, orderId)) {
                is AuthorizationSuccess -> {
                    when (val capture = creditCard.capture(authorization.id, orderId)) {
                        is CaptureSuccess ->
                            // keep capture as an internal detail of credit card payments
                            CreditCardPaymentSuccess(TransactionId(capture.captureId.id))
                        is CaptureFailure ->
                            CreditCardPaymentFailure("Capture failed: ${capture.reason}")
                    }
                }
                is AuthorizationFailure ->
                    CreditCardPaymentFailure("Authorization failed: ${authorization.reason}")
            }
        } else {
            return CreditCardPaymentFailure("Credit card not found in storage")
        }
    }

}

