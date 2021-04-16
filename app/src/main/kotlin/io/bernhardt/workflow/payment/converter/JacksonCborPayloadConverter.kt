package io.bernhardt.workflow.payment.converter

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.google.protobuf.ByteString
import io.bernhardt.workflow.payment.*
import io.bernhardt.workflow.payment.creditcard.*
import io.temporal.api.common.v1.Payload
import io.temporal.common.converter.DataConverterException
import io.temporal.common.converter.PayloadConverter
import java.io.IOException
import java.lang.reflect.Type
import java.nio.charset.StandardCharsets
import java.util.*

/**
 * Payload converter using the binary CBOR format
 */
class JacksonCborPayloadConverter(private val encoding: String) : PayloadConverter {
    private val mapper: ObjectMapper

    private val encodedEncoding: ByteString = ByteString.copyFrom(encoding, StandardCharsets.UTF_8)

    init {
        mapper = CBORMapper()
        mapper.registerModule(KotlinModule())
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
        mapper.registerModule(JavaTimeModule())
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)

        // using this approach since the annotation-based approach fails with JMH
        mapper.registerSubtypes(
                PaymentSuccess::class.java,
                PaymentFailure::class.java,
                CreditCard::class.java,
                AuthorizationSuccess::class.java,
                AuthorizationFailure::class.java,
                CaptureSuccess::class.java,
                CaptureFailure::class.java,
                CreditCardPaymentSuccess::class.java,
                CreditCardPaymentFailure::class.java,
                CreditCardId::class.java,
                OrderId::class.java,
                MerchantId::class.java,
                UserId::class.java,
                BankIdentifier::class.java,
                TransactionId::class.java
        )
    }

    override fun getEncodingType(): String {
        return encoding
    }

    @Throws(DataConverterException::class)
    override fun toData(value: Any?): Optional<Payload> {
        return try {
            val serialized = mapper.writeValueAsBytes(value)
            Optional.of(
                Payload.newBuilder()
                    .putMetadata("encoding", encodedEncoding)
                    .setData(ByteString.copyFrom(serialized))
                    .build()
            )
        } catch (e: JsonProcessingException) {
            throw DataConverterException(e)
        }
    }

    @Throws(DataConverterException::class)
    override fun <T> fromData(content: Payload, valueClass: Class<T>?, valueType: Type?): T? {
        val data = content.data
        return if (data.isEmpty) {
            null
        } else try {
            val reference = mapper.typeFactory.constructType(valueType)
            mapper.readValue(content.data.toByteArray(), reference)
        } catch (e: IOException) {
            throw DataConverterException(e)
        }
    }
}