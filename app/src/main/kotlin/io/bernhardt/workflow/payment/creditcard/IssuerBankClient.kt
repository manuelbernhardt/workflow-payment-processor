package io.bernhardt.workflow.payment.creditcard

import io.bernhardt.workflow.payment.CreditCardId
import io.bernhardt.workflow.payment.OrderId

interface IssuerBankClient {

    fun authorize(id: CreditCardId, amount: Int, orderId: OrderId): AuthorizationResult

    fun capture(id: AuthorizationId, orderId: OrderId): CaptureResult
}