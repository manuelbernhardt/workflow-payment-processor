package io.bernhardt.workflow.payment.converter

import io.temporal.common.converter.*

class ExtendedConverter: DefaultDataConverter(
        NullPayloadConverter(),
        ByteArrayPayloadConverter(),
        ProtobufJsonPayloadConverter(),
        ExtendedJacksonJsonPayloadConverter()
)