package io.bernhardt.workflow.payment.creditcard

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import io.bernhardt.workflow.payment.*
import io.temporal.activity.ActivityInterface
import io.temporal.activity.ActivityMethod

@ActivityInterface
interface CreditCardProcessingActivity {

    /**
     * Retrieves the credit card details from secure storage
     */
    @ActivityMethod
    fun retrieveCreditCardDetails(id: CreditCardId): CreditCardDetails?

    /**
     * Authorizes a credit card payment with the issuer bank
     */
    @ActivityMethod
    fun authorize(id: CreditCardId, amount: Int, orderId: OrderId): AuthorizationResult

    /**
     * Captures a credit card payment with the issuer bank
     */
    @ActivityMethod
    fun capture(authorizationId: AuthorizationId, orderId: OrderId): CaptureResult

}

data class CreditCardDetails(val id: CreditCardId, val userId: UserId, val last4Digits: String, val issuerBank: BankIdentifier, val issuerCardId: IssuerCardId)

data class IssuerCardId(val id: String)
data class AuthorizationId(val id: String)

@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include=JsonTypeInfo.As.PROPERTY, property="type")
@JsonSubTypes(
        JsonSubTypes.Type(value = AuthorizationSuccess::class, name = "AuthorizationSuccess"),
        JsonSubTypes.Type(value = AuthorizationFailure::class, name = "AuthorizationFailure")
)
sealed interface AuthorizationResult
data class AuthorizationSuccess(val id: AuthorizationId): AuthorizationResult
data class AuthorizationFailure(val reason: String): AuthorizationResult

@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include=JsonTypeInfo.As.PROPERTY, property="type")
@JsonSubTypes(
        JsonSubTypes.Type(value = CaptureSuccess::class, name = "CaptureSuccess"),
        JsonSubTypes.Type(value = CaptureFailure::class, name = "CaptureFailure")
)
sealed interface CaptureResult
data class CaptureSuccess(val captureId: CaptureId): CaptureResult
data class CaptureFailure(val reason: String): CaptureResult
data class CaptureId(val id: String)