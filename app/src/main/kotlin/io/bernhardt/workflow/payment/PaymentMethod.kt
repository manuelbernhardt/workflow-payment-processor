package io.bernhardt.workflow.payment

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

/**
 * An abstract payment method. There can be many different ones (SEPA payment, Apple Pay, etc.) but
 * for this example we only cover Credit Card payments
 */
@JsonTypeInfo(use= JsonTypeInfo.Id.NAME, include= JsonTypeInfo.As.PROPERTY, property="type")
@JsonSubTypes(
        JsonSubTypes.Type(value = CreditCard::class, name = "CreditCard")
)
sealed interface PaymentMethod

data class CreditCard(val id: CreditCardId): PaymentMethod
data class CreditCardId(val id: String)
