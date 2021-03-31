package io.bernhardt.workflow.payment.creditcard

import io.bernhardt.workflow.payment.CreditCardId

interface CreditCardStorage {

    fun storeCreditCard(id: CreditCardId, details: CreditCardDetails)

    fun retrieveCreditCardDetails(id: CreditCardId): CreditCardDetails?

}