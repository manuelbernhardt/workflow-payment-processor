package io.bernhardt.workflow.payment.creditcard

import com.fasterxml.jackson.annotation.JsonTypeInfo
import io.bernhardt.workflow.payment.*
import io.temporal.workflow.WorkflowInterface
import io.temporal.workflow.WorkflowMethod
import javax.money.MonetaryAmount

@WorkflowInterface
interface CreditCardProcessingWorkflow {

    @WorkflowMethod
    fun processPayment(orderId: OrderId, amount: MonetaryAmount, merchantConfiguration: MerchantConfiguration, userId: UserId, card: CreditCard): CreditCardPaymentResult

}

@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include= JsonTypeInfo.As.PROPERTY, property="@class")
sealed interface CreditCardPaymentResult
data class CreditCardPaymentSuccess(val transactionId: TransactionId): CreditCardPaymentResult
data class CreditCardPaymentFailure(val reason: String): CreditCardPaymentResult