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
        return toEligibilityFunction(jsonElement.asJsonObject, context)
    }

    private fun toEligibilityFunction(function: JsonObject, context: JsonDeserializationContext): EligibilityFunction {
        val rule = function.get("rule").asString
        return EligibilityFunction(
            rule = rule,
            parameters = toParameters(function.getAsJsonArray("parameters"), rule, context)
        )
    }

    private fun toParameters(parameterArray: JsonArray, rule: String, context: JsonDeserializationContext): List<Parameter<*>> {
        val expectedTypes = runCatching { EligibilityRule.valueOf(rule).input }.getOrNull()
        return parameterArray.mapIndexedNotNull { index, element ->
            when {
                element.isJsonObject -> parseParameterObject(element.asJsonObject, context)
                element.isJsonPrimitive -> {
                    val type = expectedTypes?.getOrNull(index) ?: Parameter.Type.STRING
                    parseParameterValue(type, element.asJsonPrimitive.asString)
                }

                else -> null
            }
        }
    }

    private fun parseParameterObject(element: JsonObject, context: JsonDeserializationContext): Parameter<*>? {
        if (!element.has("type")) {
            return if (element.has("rule") || element.has("ruleName")) {
                FunctionParameter(toEligibilityFunction(element, context))
            } else {
                null
            }
        }

        val type = Parameter.Type.valueOf(element.get("type").asString)
        if (type == Parameter.Type.FUNCTION) {
            val function = element.getAsJsonObject("value")
            return FunctionParameter(toEligibilityFunction(function, context))
        }

        val value = element.get("value")
        return when (type) {
            Parameter.Type.TREATMENT -> {
                if (value == null || value.isJsonNull || value.isJsonPrimitive) {
                    return parseParameterValue(type, parameterValueAsString(value))
                }
                TreatmentParameter(context.deserialize(value, Treatment::class.java))
            }

            Parameter.Type.SYSTEMIC_TREATMENT -> {
                if (value == null || value.isJsonNull || value.isJsonPrimitive) {
                    return parseParameterValue(type, parameterValueAsString(value))
                }
                SystemicTreatmentParameter(context.deserialize(value, Treatment::class.java))
            }

            Parameter.Type.DRUG -> {
                if (value == null || value.isJsonNull || value.isJsonPrimitive) {
                    return parseParameterValue(type, parameterValueAsString(value))
                }
                DrugParameter(context.deserialize(value, Drug::class.java))
            }

            Parameter.Type.MANY_TREATMENTS -> {
                if (value == null || value.isJsonNull || value.isJsonPrimitive) {
                    return parseParameterValue(type, parameterValueAsString(value))
                }
                ManyTreatmentsParameter(parseTreatments(value, context))
            }

            Parameter.Type.MANY_DRUGS -> {
                if (value == null || value.isJsonNull || value.isJsonPrimitive) {
                    return parseParameterValue(type, parameterValueAsString(value))
                }
                ManyDrugsParameter(parseDrugs(value, context).toSet())
            }

            else -> parseParameterValue(type, parameterValueAsString(value))
        }
    }

    private fun serializeParameters(parameters: List<Parameter<*>>, context: JsonSerializationContext): JsonArray {
        val array = JsonArray()
        parameters.forEach { param ->
            val parameter = JsonObject()
            parameter.addProperty("type", param.type.name)
            if (param is FunctionParameter) {
                parameter.add("value", context.serialize(param.value))
            } else {
                val valueElement = when (param.type) {
                    Parameter.Type.TREATMENT,
                    Parameter.Type.SYSTEMIC_TREATMENT,
                    Parameter.Type.DRUG,
                    Parameter.Type.MANY_TREATMENTS,
                    Parameter.Type.MANY_DRUGS -> context.serialize(param.value)

                    else -> JsonPrimitive(parameterValueToString(param))
                }
                parameter.add("value", valueElement)
            }
            array.add(parameter)
        }
        return array
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

    private fun parameterValueAsString(value: JsonElement?): String {
        if (value == null || value.isJsonNull) {
            return ""
        }
        return when {
            value.isJsonPrimitive -> value.asJsonPrimitive.asString
            value.isJsonArray -> value.asJsonArray.joinToString(";") { it.asString }
            else -> value.toString()
        }
    }

    private fun parseTreatments(value: JsonElement?, context: JsonDeserializationContext): List<Treatment> {
        if (value == null || value.isJsonNull) {
            return emptyList()
        }
        return if (value.isJsonArray) {
            value.asJsonArray.mapNotNull { context.deserialize<Treatment>(it, Treatment::class.java) }
        } else {
            listOf(context.deserialize(value, Treatment::class.java))
        }
    }

    private fun parseDrugs(value: JsonElement?, context: JsonDeserializationContext): List<Drug> {
        if (value == null || value.isJsonNull) {
            return emptyList()
        }
        return if (value.isJsonArray) {
            value.asJsonArray.mapNotNull { context.deserialize<Drug>(it, Drug::class.java) }
        } else {
            listOf(context.deserialize(value, Drug::class.java))
        }
    }

    private fun parseParameterValue(type: Parameter.Type, value: String): Parameter<*> {
        return type.create(value)
    }
}
