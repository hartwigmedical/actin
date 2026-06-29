package com.hartwig.actin.trial.serialization

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import com.hartwig.actin.datamodel.clinical.treatment.Drug
import com.hartwig.actin.datamodel.clinical.treatment.Treatment
import com.hartwig.actin.datamodel.trial.DrugParameter
import com.hartwig.actin.datamodel.trial.EligibilityFunction
import com.hartwig.actin.datamodel.trial.FunctionParameter
import com.hartwig.actin.datamodel.trial.ManyDrugsParameter
import com.hartwig.actin.datamodel.trial.ManyTreatmentsParameter
import com.hartwig.actin.datamodel.trial.Parameter
import com.hartwig.actin.datamodel.trial.SystemicTreatmentParameter
import com.hartwig.actin.datamodel.trial.TreatmentCategoryOrType
import com.hartwig.actin.datamodel.trial.TreatmentParameter
import com.hartwig.actin.trial.input.EligibilityRule

private val STRUCTURED_PARAMETER_TYPES: Set<Parameter.Type> = setOf(
    Parameter.Type.TREATMENT,
    Parameter.Type.SYSTEMIC_TREATMENT,
    Parameter.Type.DRUG,
    Parameter.Type.MANY_TREATMENTS,
    Parameter.Type.MANY_DRUGS
)

private fun Parameter.Type.isStructured(): Boolean = this in STRUCTURED_PARAMETER_TYPES

object EligibilityFunctionDeserializer : JsonDeserializer<EligibilityFunction>() {

    override fun deserialize(parser: JsonParser, context: DeserializationContext): EligibilityFunction {
        val node: JsonNode = parser.codec.readTree(parser)
        return toEligibilityFunction(node, parser)
    }

    private fun toEligibilityFunction(node: JsonNode, parser: JsonParser): EligibilityFunction {
        val rule = node.get("rule")?.takeUnless { it.isNull }?.asText()
            ?: throw JsonMappingException.from(parser, "Missing 'rule' field in eligibility function: $node")
        val parameters = node.get("parameters") as? ArrayNode ?: JsonNodeFactory.instance.arrayNode()
        return EligibilityFunction(rule = rule, parameters = toParameters(parameters, rule, parser))
    }

    private fun toParameters(parameterArray: ArrayNode, rule: String, parser: JsonParser): List<Parameter<*>> {
        val expectedTypes = runCatching { EligibilityRule.valueOf(rule).input }.getOrNull()
        return parameterArray.mapIndexedNotNull { index, element ->
            when {
                element.isObject -> parseParameterObject(element as ObjectNode, parser)
                element.isValueNode && !element.isNull -> {
                    val type = expectedTypes?.getOrNull(index) ?: Parameter.Type.STRING
                    type.create(element.asText())
                }

                else -> null
            }
        }
    }

    private fun parseParameterObject(element: ObjectNode, parser: JsonParser): Parameter<*>? {
        if (!element.has("type")) {
            return if (element.has("rule") || element.has("ruleName")) {
                FunctionParameter(toEligibilityFunction(element, parser))
            } else {
                null
            }
        }

        val type = Parameter.Type.valueOf(element.get("type").asText())
        if (type == Parameter.Type.FUNCTION) {
            val function = element.get("value") as ObjectNode
            return FunctionParameter(toEligibilityFunction(function, parser))
        }

        val value: JsonNode? = element.get("value")
        if (!type.isStructured() || value == null || value.isNull || value.isValueNode) {
            return type.create(parameterValueAsString(value))
        }
        return parseStructuredParameter(type, value, parser)
    }

    private fun parseStructuredParameter(type: Parameter.Type, value: JsonNode, parser: JsonParser): Parameter<*> {
        return when (type) {
            Parameter.Type.TREATMENT -> TreatmentParameter(parser.codec.treeToValue(value, Treatment::class.java))
            Parameter.Type.SYSTEMIC_TREATMENT -> SystemicTreatmentParameter(parser.codec.treeToValue(value, Treatment::class.java))
            Parameter.Type.DRUG -> DrugParameter(parser.codec.treeToValue(value, Drug::class.java))
            Parameter.Type.MANY_TREATMENTS -> ManyTreatmentsParameter(parseList(value, Treatment::class.java, parser))
            Parameter.Type.MANY_DRUGS -> ManyDrugsParameter(parseList(value, Drug::class.java, parser).toSet())
            else -> error("parseStructuredParameter called for non-structured type $type")
        }
    }

    private fun <T : Any> parseList(value: JsonNode, target: Class<T>, parser: JsonParser): List<T> {
        return if (value.isArray) {
            value.map { parser.codec.treeToValue(it, target) }
        } else {
            listOf(parser.codec.treeToValue(value, target))
        }
    }

    private fun parameterValueAsString(value: JsonNode?): String {
        if (value == null || value.isNull) return ""
        return when {
            value.isValueNode -> value.asText()
            value.isArray -> value.joinToString(";") { it.asText() }
            else -> value.toString()
        }
    }
}

object EligibilityFunctionSerializer : JsonSerializer<EligibilityFunction>() {

    override fun serialize(value: EligibilityFunction, gen: JsonGenerator, serializers: SerializerProvider) {
        gen.writeStartObject()
        gen.writeStringField("rule", value.rule)
        gen.writeFieldName("parameters")
        gen.writeStartArray()
        value.parameters.forEach { param ->
            gen.writeStartObject()
            gen.writeStringField("type", param.type.name)
            gen.writeFieldName("value")
            when {
                param is FunctionParameter -> serializers.defaultSerializeValue(param.value, gen)
                param.type.isStructured() -> serializers.defaultSerializeValue(param.value, gen)
                else -> gen.writeString(parameterValueToString(param))
            }
            gen.writeEndObject()
        }
        gen.writeEndArray()
        gen.writeEndObject()
    }

    private fun parameterValueToString(param: Parameter<*>): String {
        return when (val value = param.value) {
            is Iterable<*> -> value.joinToString(";") { valueToString(it) }
            is Array<*> -> value.joinToString(";") { valueToString(it) }
            else -> valueToString(value)
        }
    }

    private fun valueToString(value: Any?): String {
        return when (value) {
            null -> ""
            is Drug -> value.name
            is Treatment -> value.name
            is TreatmentCategoryOrType -> value.category.name
            is Enum<*> -> value.name
            else -> value.toString()
        }
    }
}
