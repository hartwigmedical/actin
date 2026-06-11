package com.hartwig.actin.clinical.serialization

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.JsonNode
import com.hartwig.actin.datamodel.clinical.treatment.Drug
import com.hartwig.actin.datamodel.clinical.treatment.DrugType
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory

object DrugDeserializer : JsonDeserializer<Drug>() {

    private val STRING_SET_TYPE = object : TypeReference<Set<String>>() {}
    private val DRUG_TYPE_SET_TYPE = object : TypeReference<Set<DrugType>>() {}

    override fun deserialize(parser: JsonParser, context: DeserializationContext): Drug {
        val node: JsonNode = parser.codec.readTree(parser)
        return Drug(
            name = requiredField(node, "name", parser).asText(),
            synonyms = readTree(parser, node.get("synonyms"), STRING_SET_TYPE) ?: emptySet(),
            drugTypes = readTree(parser, requiredField(node, "drugTypes", parser), DRUG_TYPE_SET_TYPE)!!,
            category = parser.codec.treeToValue(requiredField(node, "category", parser), TreatmentCategory::class.java),
            displayOverride = node.get("displayOverride")?.takeUnless { it.isNull }?.asText()
        )
    }

    private fun requiredField(node: JsonNode, field: String, parser: JsonParser): JsonNode {
        return node.get(field)?.takeUnless { it.isNull }
            ?: throw JsonMappingException.from(parser, "Missing required field '$field' in drug: $node")
    }

    private fun <T> readTree(parser: JsonParser, node: JsonNode?, type: TypeReference<T>): T? {
        if (node == null || node.isNull) return null
        return parser.codec.readValue(parser.codec.treeAsTokens(node), type)
    }
}
