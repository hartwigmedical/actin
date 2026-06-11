package com.hartwig.actin.clinical.serialization

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.JsonNode
import com.hartwig.actin.datamodel.clinical.WhoStatus
import com.hartwig.actin.datamodel.clinical.WhoStatusPrecision
import java.time.LocalDate

object WhoStatusDeserializer : JsonDeserializer<WhoStatus>() {

    override fun deserialize(parser: JsonParser, context: DeserializationContext): WhoStatus {
        val node: JsonNode = parser.codec.readTree(parser)
        return try {
            val date = parser.codec.treeToValue(node.get("date"), LocalDate::class.java)
            val status = node.get("status").asInt()
            val precision = node.get("precision")
                ?.takeUnless { it.isNull }
                ?.let { WhoStatusPrecision.valueOf(it.asText()) }
                ?: WhoStatusPrecision.EXACT
            WhoStatus(date = date, status = status, precision = precision)
        } catch (e: Exception) {
            throw JsonMappingException.from(parser, "Failed to deserialize WHO status: $node", e)
        }
    }
}
