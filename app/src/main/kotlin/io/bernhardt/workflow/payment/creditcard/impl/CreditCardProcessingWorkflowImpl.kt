package io.bernhardt.workflow.payment.creditcard.impl

import io.bernhardt.workflow.payment.*
import io.bernhardt.workflow.payment.creditcard.*
import io.temporal.activity.ActivityOptions
import io.temporal.common.RetryOptions
import io.temporal.workflow.Workflow
import java.time.Duration
import javax.money.MonetaryAmount

class CreditCardProcessingWorkflowImpl : CreditCardProcessingWorkflow {

    // configure retry options in line with the systems we're calling
    // in this example, given the SLA with the acquiring bank we can assume that 2 seconds
    // is the maximum we should be willing to wait
    private val retryOptions = RetryOptions.newBuilder()
            .setInitialInterval(Duration.ofMillis(150))
            .setMaximumInterval(Duration.ofMillis(2000))
            .setBackoffCoefficient(1.2)
            .build()

    private val options = ActivityOptions.newBuilder()
            .setStartToCloseTimeout(Duration.ofSeconds(1))
            .setRetryOptions(retryOptions)
            .build()

    private val creditCard: CreditCardProcessingActivity = Workflow.newActivityStub(CreditCardProcessingActivity::class.java, options)

    override fun processPayment(orderId: OrderId, amount: MonetaryAmount, merchantConfiguration: MerchantConfiguration, userId: UserId, card: CreditCard): CreditCardPaymentResult {
        val details = creditCard.retrieveCreditCardDetails(card.id)

        if (details != null) {
            // authorization and capture below are idempotent operations
            // if the workflow execution were to crash at any point in time, calling these operations again would yield the same results
            // (including stable identifiers in case of success)
            return when (val authorization = creditCard.authorize(details.id, amount, orderId)) {
                is AuthorizationSuccess -> {
                    when (val capture = creditCard.capture(authorization.id)) {
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