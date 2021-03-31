package io.bernhardt.workflow.payment.creditcard.impl

import io.bernhardt.workflow.payment.CreditCardId
import io.bernhardt.workflow.payment.OrderId
import io.bernhardt.workflow.payment.creditcard.*
import java.time.Duration
import java.util.*
import javax.money.MonetaryAmount
import kotlin.math.abs

/**
 * Mock client that authorizes expenses until a given amount has been reached.
 * Results are returned within a given latency boundary.
 */
class RandomLatencyIssuerBankClient(private val maxExpenses: MonetaryAmount, private val min: Duration, private val max: Duration): IssuerBankClient {

    private val expenses = mutableMapOf<CreditCardId, MonetaryAmount>()

    override fun authorize(id: CreditCardId, amount: MonetaryAmount, orderId: OrderId): AuthorizationResult {
        if(!expenses.containsKey(id)) {
            expenses[id] = maxExpenses
        }

        return if(expenses[id]!!.isGreaterThan(amount)) {
            expenses[id] = expenses[id]!!.subtract(amount)
            waitRandom()
            val authorizationId = AuthorizationId("auth-${abs((orderId.id + id.id).hashCode())}")
            AuthorizationSuccess(authorizationId)
        } else {
            AuthorizationFailure("Amount too high")
        }

    }

    override fun capture(id: AuthorizationId): CaptureResult {
        waitRandom()
        val captureId = CaptureId("capture-${abs((id.id).hashCode())}")
        return CaptureSuccess(captureId)
    }

    private fun waitRandom() {
        val r = Random()
        val duration = Duration.ofMillis(r.nextInt((max.toMillis() - min.toMillis()).toInt()) + min.toMillis())
        Thread.sleep(duration.toMillis())
    }
}