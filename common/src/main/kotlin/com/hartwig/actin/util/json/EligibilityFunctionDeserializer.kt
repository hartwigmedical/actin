package com.hartwig.actin.util.json

import com.google.gson.JsonArray
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.hartwig.actin.datamodel.clinical.treatment.Drug
import com.hartwig.actin.datamodel.clinical.treatment.Treatment
import com.hartwig.actin.datamodel.trial.EligibilityFunction
import com.hartwig.actin.datamodel.trial.FunctionParameter
import com.hartwig.actin.datamodel.trial.Parameter
import com.hartwig.actin.datamodel.trial.TreatmentCategoryOrType
import com.hartwig.actin.trial.input.EligibilityRule
import java.lang.reflect.Type

class EligibilityFunctionDeserializer : JsonDeserializer<EligibilityFunction>, JsonSerializer<EligibilityFunction> {

    override fun serialize(
        src: EligibilityFunction,
        typeOfSrc: Type,
        context: JsonSerializationContext
    ): JsonElement {
        val function = JsonObject()
        function.addProperty("rule", src.rule)
        function.add("parameters", serializeParameters(src.parameters, context))
        return function
    }

    override fun deserialize(jsonElement: JsonElement, type: Type, context: JsonDeserializationContext): EligibilityFunction {
        return toEligibilityFunction(jsonElement.asJsonObject)
    }

    private fun toEligibilityFunction(function: JsonObject): EligibilityFunction {
        val rule = when {
            function.has("rule") -> function.get("rule").asString
            function.has("ruleName") -> function.get("ruleName").asString
            else -> throw IllegalArgumentException("Missing rule name for eligibility function")
        }
        return EligibilityFunction(
            rule = rule,
            parameters = toParameters(function.getAsJsonArray("parameters"), rule)
        )
    }

    private fun toParameters(parameterArray: JsonArray, rule: String): List<Parameter<*>> {
        val expectedTypes = runCatching { EligibilityRule.valueOf(rule).input }.getOrNull()
        return parameterArray.mapIndexedNotNull { index, element ->
            when {
                element.isJsonObject -> parseParameterObject(element.asJsonObject)
                element.isJsonPrimitive -> {
                    val type = expectedTypes?.getOrNull(index) ?: Parameter.Type.STRING
                    type.create(element.asJsonPrimitive.asString)
                }
                else -> null
            }
        }
    }

    private fun parseParameterObject(element: JsonObject): Parameter<*>? {
        if (!element.has("type")) {
            return if (element.has("rule") || element.has("ruleName")) {
                FunctionParameter(toEligibilityFunction(element))
            } else {
                null
            }
        }

        val type = Parameter.Type.valueOf(element.get("type").asString)
        if (type == Parameter.Type.FUNCTION) {
            val function = element.getAsJsonObject("value")
            return FunctionParameter(toEligibilityFunction(function))
        }

        val value = element.get("value") ?: return type.create("")
        val valueString = when {
            value.isJsonPrimitive -> value.asJsonPrimitive.asString
            value.isJsonArray -> value.asJsonArray.joinToString(";") { it.asString }
            else -> value.toString()
        }
        return type.create(valueString)
    }

    private fun serializeParameters(parameters: List<Parameter<*>>, context: JsonSerializationContext): JsonArray {
        val array = JsonArray()
        parameters.forEach { param ->
            val parameter = JsonObject()
            parameter.addProperty("type", param.type.name)
            if (param is FunctionParameter) {
                parameter.add("value", context.serialize(param.value))
            } else {
                parameter.addProperty("value", parameterValueToString(param))
            }
            array.add(parameter)
        }
        return array
    }

    private fun parameterValueToString(param: Parameter<*>): String {
        val value = param.value
        return when (value) {
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
            is TreatmentCategoryOrType -> {
                value.category?.name ?: value.type?.let { valueToString(it) }.orEmpty()
            }
            is Enum<*> -> value.name
            else -> value.toString()
        }
    }
}
