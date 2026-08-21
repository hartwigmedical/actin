package com.hartwig.actin.clinical.serialization

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.JsonNode
import com.hartwig.actin.datamodel.clinical.treatment.Treatment
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentClass

object TreatmentDeserializer : JsonDeserializer<Treatment?>() {

    override fun deserialize(parser: JsonParser, context: DeserializationContext): Treatment? {
        val node = parser.codec.readTree<JsonNode>(parser)
        if (node.isNull) return null
        return try {
            val target = node.get("treatmentClass")?.takeUnless { it.isNull }?.asText()
                ?.let { TreatmentClass.valueOf(it).treatmentClass as Class<*> }
            parser.codec.treeToValue(node, target) as Treatment
        } catch (e: Exception) {
            throw JsonMappingException.from(parser, "Failed to deserialize Treatment: $node", e)
        }
    }

    override fun getNullValue(context: DeserializationContext): Treatment? = null
}
