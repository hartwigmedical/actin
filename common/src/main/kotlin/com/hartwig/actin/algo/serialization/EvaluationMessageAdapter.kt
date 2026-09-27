package com.hartwig.actin.algo.serialization

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.hartwig.actin.datamodel.algo.EvaluationMessage
import com.hartwig.actin.datamodel.algo.StaticMessage

object EvaluationMessageDeserializer : JsonDeserializer<EvaluationMessage>() {

    override fun deserialize(parser: JsonParser, context: DeserializationContext): EvaluationMessage {
        return parser.codec.readValue(parser, StaticMessage::class.java)
    }
}

object EvaluationMessageSerializer : JsonSerializer<EvaluationMessage>() {

    override fun serialize(value: EvaluationMessage, gen: JsonGenerator, serializers: SerializerProvider) {
        gen.writeStartObject()
        gen.writeStringField("message", value.combineBy())
        gen.writeEndObject()
    }
}
