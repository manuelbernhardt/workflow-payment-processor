package io.bernhardt.workflow.payment.creditcard.impl

import io.bernhardt.workflow.payment.CreditCardId
import io.bernhardt.workflow.payment.creditcard.CreditCardDetails
import io.bernhardt.workflow.payment.creditcard.CreditCardStorage

class MemoryCreditCardStorage: CreditCardStorage {

    private val storage = mutableMapOf<CreditCardId, CreditCardDetails>()

    override fun storeCreditCard(id: CreditCardId, details: CreditCardDetails) {
        storage[id] = details
    }

    override fun retrieveCreditCardDetails(id: CreditCardId): CreditCardDetails? {
        return storage[id]
    }
}