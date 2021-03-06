package io.bernhardt.workflow.payment.converter

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.bernhardt.workflow.payment.*
import io.bernhardt.workflow.payment.creditcard.*
import io.temporal.api.common.v1.Payload
import io.temporal.common.converter.DataConverterException
import io.temporal.common.converter.JacksonJsonPayloadConverter
import java.lang.reflect.Type
import java.util.*

/**
 * Extend the JacksonJsonPayloadConverter to be able to implement [.getEncodingType]
 */
class ExtendedJacksonJsonPayloadConverter : JacksonJsonPayloadConverter() {
    private val converter: JacksonJsonPayloadConverter

    init {
        val mapper = ObjectMapper()
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
                CreditCardPaymentFailure::class.java
        )

        converter = JacksonJsonPayloadConverter(mapper)
    }

    @Throws(DataConverterException::class)
    override fun toData(value: Any): Optional<Payload> {
        return converter.toData(value)
    }

    @Throws(DataConverterException::class)
    override fun <T> fromData(content: Payload, valueClass: Class<T>, valueType: Type): T {
        return converter.fromData(content, valueClass, valueType)
    }

}