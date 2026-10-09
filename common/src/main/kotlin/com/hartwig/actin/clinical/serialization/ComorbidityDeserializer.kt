package com.hartwig.actin.clinical.serialization

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.JsonNode
import com.hartwig.actin.datamodel.clinical.Comorbidity
import com.hartwig.actin.datamodel.clinical.ComorbidityClass

object ComorbidityDeserializer : JsonDeserializer<Comorbidity>() {

    override fun deserialize(parser: JsonParser, context: DeserializationContext): Comorbidity {
        val node: JsonNode = parser.codec.readTree(parser)
        return try {
            val className = node.get("comorbidityClass").asText()
            val target = ComorbidityClass.valueOf(className).comorbidityClass as Class<*>
            parser.codec.treeToValue(node, target) as Comorbidity
        } catch (e: Exception) {
            throw JsonMappingException.from(parser, "Failed to deserialize Comorbidity: $node", e)
        }
    }
}
