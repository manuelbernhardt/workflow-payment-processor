package io.bernhardt.workflow.payment.creditcard

import com.fasterxml.jackson.annotation.JsonTypeInfo
import io.bernhardt.workflow.payment.BankIdentifier
import io.bernhardt.workflow.payment.CreditCardId
import io.bernhardt.workflow.payment.OrderId
import io.bernhardt.workflow.payment.UserId
import io.temporal.activity.ActivityInterface
import io.temporal.activity.ActivityMethod
import javax.money.MonetaryAmount

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
    fun authorize(id: CreditCardId, amount: MonetaryAmount, orderId: OrderId): AuthorizationResult

    /**
     * Captures a credit card payment with the issuer bank
     */
    @ActivityMethod
    fun capture(authorizationId: AuthorizationId): CaptureResult

}

data class CreditCardDetails(val id: CreditCardId, val userId: UserId, val last4Digits: String, val issuerBank: BankIdentifier, val issuerCardId: IssuerCardId)

data class IssuerCardId(val id: String)
data class AuthorizationId(val id: String)

@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
sealed interface AuthorizationResult
data class AuthorizationSuccess(val id: AuthorizationId): AuthorizationResult
data class AuthorizationFailure(val reason: String): AuthorizationResult

@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
sealed interface CaptureResult
data class CaptureSuccess(val captureId: CaptureId): CaptureResult
data class CaptureFailure(val reason: String): CaptureResult
data class CaptureId(val id: String)