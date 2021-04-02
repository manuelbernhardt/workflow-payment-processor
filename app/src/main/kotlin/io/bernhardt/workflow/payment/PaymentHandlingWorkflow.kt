package io.bernhardt.workflow.payment

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import io.temporal.workflow.WorkflowInterface
import io.temporal.workflow.WorkflowMethod

@WorkflowInterface
interface PaymentHandlingWorkflow {

    @WorkflowMethod
    fun handlePayment(orderId: OrderId, amount: Int, merchantId: MerchantId, userId: UserId): PaymentResult

}

@JsonTypeInfo(use= JsonTypeInfo.Id.NAME, include= JsonTypeInfo.As.PROPERTY, property="type")
@JsonSubTypes(
        JsonSubTypes.Type(value = PaymentSuccess::class, name = "PaymentSuccess"),
        JsonSubTypes.Type(value = PaymentFailure::class, name = "PaymentFailure")
)
sealed interface PaymentResult
data class PaymentSuccess(val transactionId: TransactionId): PaymentResult
data class PaymentFailure(val reason: String): PaymentResult

data class OrderId(val id: String)
data class MerchantId(val id: String)
data class UserId(val id: String)
data class BankIdentifier(val id: String)
data class TransactionId(val id: String)
