package io.bernhardt.workflow.payment.creditcard

import io.bernhardt.workflow.payment.CreditCardId
import io.bernhardt.workflow.payment.OrderId
import javax.money.MonetaryAmount

interface IssuerBankClient {

    fun authorize(id: CreditCardId, amount: MonetaryAmount, orderId: OrderId): AuthorizationResult

    fun capture(id: AuthorizationId): CaptureResult
}