package com.hartwig.actin.clinical.feed

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeParseException

class FlexibleLocalDateTimeDeserializer : JsonDeserializer<LocalDateTime>() {
    
    override fun deserialize(parser: JsonParser, context: DeserializationContext): LocalDateTime =
        try {
            LocalDateTime.parse(parser.text)
        } catch (_: DateTimeParseException) {
            val node = parser.readValueAsTree<JsonNode>()

            if (!node.isArray || node.size() < 3) {
                throw JsonParseException(parser, "Expected array with at least 3 elements [year, month, day]")
            }

            val (year, month, day) = node.take(3).map { it.asInt() }

            LocalDate.of(year, month, day).atStartOfDay()
        }
}

object FeedModelJsonUtil {

    val feedModelMapper: ObjectMapper = ObjectMapper()
        .registerModule(JavaTimeModule())
        .registerModule(SimpleModule().apply {
            addDeserializer(LocalDateTime::class.java, FlexibleLocalDateTimeDeserializer())
        })
        .registerModule(KotlinModule.Builder().build())
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
}
