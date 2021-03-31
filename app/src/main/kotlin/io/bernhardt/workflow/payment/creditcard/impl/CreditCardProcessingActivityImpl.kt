package io.bernhardt.workflow.payment.creditcard.impl

import io.bernhardt.workflow.payment.CreditCardId
import io.bernhardt.workflow.payment.OrderId
import io.bernhardt.workflow.payment.creditcard.*
import java.time.Duration
import javax.money.Monetary
import javax.money.MonetaryAmount

class CreditCardProcessingActivityImpl(private val secureStorage: CreditCardStorage): CreditCardProcessingActivity {

    private val maxAmount = Monetary.getDefaultAmountFactory().setCurrency("USD").setNumber(100).create()

    private val issuerBankClient: IssuerBankClient = RandomLatencyIssuerBankClient(maxAmount, Duration.ofMillis(200), Duration.ofMillis(3000))

    override fun retrieveCreditCardDetails(id: CreditCardId): CreditCardDetails? {
        return secureStorage.retrieveCreditCardDetails(id)
    }

    override fun authorize(id: CreditCardId, amount: MonetaryAmount, orderId: OrderId): AuthorizationResult {
        return issuerBankClient.authorize(id, amount, orderId)
    }

    override fun capture(authorizationId: AuthorizationId): CaptureResult {
        return issuerBankClient.capture(authorizationId)
    }
}