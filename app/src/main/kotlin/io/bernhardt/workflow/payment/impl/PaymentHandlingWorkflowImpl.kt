package io.bernhardt.workflow.payment.impl

import io.bernhardt.workflow.payment.*
import io.bernhardt.workflow.payment.creditcard.CreditCardPaymentFailure
import io.bernhardt.workflow.payment.creditcard.CreditCardPaymentSuccess
import io.bernhardt.workflow.payment.creditcard.CreditCardProcessingWorkflow
import io.temporal.workflow.Workflow
import io.temporal.activity.ActivityOptions
import java.time.Duration

class PaymentHandlingWorkflowImpl: PaymentHandlingWorkflow {

    private val options = ActivityOptions.newBuilder()
            .setStartToCloseTimeout(Duration.ofSeconds(5))
            .build()

    private val paymentHandling: PaymentHandlingActivities = Workflow.newActivityStub(PaymentHandlingActivities::class.java, options)

    override fun handlePayment(orderId: OrderId, amount: Int, merchantId: MerchantId, userId: UserId): PaymentResult {
        val paymentConfiguration = paymentHandling.retrieveConfiguration(merchantId, userId)

        paymentConfiguration?.let { config ->
            when(val method = config.userConfiguration.paymentMethod) {
                is CreditCard -> {
                    val creditCardFlow = Workflow.newChildWorkflowStub(CreditCardProcessingWorkflow::class.java)

                    // call the child workflow synchronously
                    // this could be done asynchronously as well, but we don't really have anything else to do in the meanwhile
                    val result = creditCardFlow.processPayment(orderId, amount, config.merchantConfiguration, userId, method)

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
}

