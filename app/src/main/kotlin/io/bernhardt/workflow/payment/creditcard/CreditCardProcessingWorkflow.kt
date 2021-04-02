package io.bernhardt.workflow.payment.creditcard

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import io.bernhardt.workflow.payment.*
import io.temporal.workflow.WorkflowInterface
import io.temporal.workflow.WorkflowMethod

@WorkflowInterface
interface CreditCardProcessingWorkflow {

    @WorkflowMethod
    fun processPayment(orderId: OrderId, amount: Int, merchantConfiguration: MerchantConfiguration, userId: UserId, card: CreditCard): CreditCardPaymentResult

}

@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include= JsonTypeInfo.As.PROPERTY, property="type")
@JsonSubTypes(
        JsonSubTypes.Type(value = CreditCardPaymentSuccess::class, name = "CreditCardPaymentSuccess"),
        JsonSubTypes.Type(value = CreditCardPaymentFailure::class, name = "CreditCardPaymentFailure")
)
sealed interface CreditCardPaymentResult
data class CreditCardPaymentSuccess(val transactionId: TransactionId): CreditCardPaymentResult
data class CreditCardPaymentFailure(val reason: String): CreditCardPaymentResult