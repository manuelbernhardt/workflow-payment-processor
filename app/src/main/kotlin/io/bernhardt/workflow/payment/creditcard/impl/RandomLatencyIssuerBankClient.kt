package io.bernhardt.workflow.payment.creditcard.impl

import io.bernhardt.workflow.payment.CreditCardId
import io.bernhardt.workflow.payment.OrderId
import io.bernhardt.workflow.payment.creditcard.*
import java.time.Duration
import java.util.*
import kotlin.math.abs

/**
 * Mock client that authorizes expenses until a given amount has been reached.
 * Results are returned within a given latency boundary.
 */
class RandomLatencyIssuerBankClient(private val spendingLimit: Int, private val min: Duration, private val max: Duration): IssuerBankClient {

    private val expenses = mutableMapOf<CreditCardId, Int>()

    override fun authorize(id: CreditCardId, amount: Int, orderId: OrderId): AuthorizationResult {
        if(!expenses.containsKey(id)) {
            expenses[id] = spendingLimit
        }

        return if(expenses[id]!! > amount) {
            expenses[id] = expenses[id]!! - amount
            if (max.toMillis() > 0) {
                waitRandom()
            }
            val authorizationId = AuthorizationId("auth-${abs((orderId.id + id.id).hashCode())}")
            AuthorizationSuccess(authorizationId)
        } else {
            AuthorizationFailure("Amount too high")
        }

    }

    override fun capture(id: AuthorizationId, orderId: OrderId): CaptureResult {
        if (max.toMillis() > 0) {
            waitRandom()
        }
        val captureId = CaptureId("capture-${abs((orderId.id + id.id).hashCode())}")
        return CaptureSuccess(captureId)
    }

    private fun waitRandom() {
        val r = Random()
        val duration = Duration.ofMillis(r.nextInt((max.toMillis() - min.toMillis()).toInt()) + min.toMillis())
        Thread.sleep(duration.toMillis())
    }
}