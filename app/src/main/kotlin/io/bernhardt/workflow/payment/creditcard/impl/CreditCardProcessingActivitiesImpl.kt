package io.bernhardt.workflow.payment.creditcard.impl

import io.bernhardt.workflow.payment.CreditCardId
import io.bernhardt.workflow.payment.OrderId
import io.bernhardt.workflow.payment.creditcard.*

class CreditCardProcessingActivitiesImpl(private val secureStorage: CreditCardStorage, private val bankClient: IssuerBankClient): CreditCardProcessingActivity {

    override fun retrieveCreditCardDetails(id: CreditCardId): CreditCardDetails? {
        return secureStorage.retrieveCreditCardDetails(id)
    }

    override fun authorize(id: CreditCardId, amount: Int, orderId: OrderId): AuthorizationResult {
        return bankClient.authorize(id, amount, orderId)
    }

    override fun capture(authorizationId: AuthorizationId, orderId: OrderId): CaptureResult {
        return bankClient.capture(authorizationId, orderId)
    }
}