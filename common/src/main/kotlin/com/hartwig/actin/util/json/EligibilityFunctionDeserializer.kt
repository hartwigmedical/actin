package com.hartwig.actin.util.json

import com.google.gson.JsonArray
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.hartwig.actin.datamodel.trial.EligibilityFunction
import com.hartwig.actin.datamodel.trial.EligibilityRule
import java.lang.reflect.Type

class EligibilityFunctionDeserializer : JsonDeserializer<EligibilityFunction> {

    override fun deserialize(jsonElement: JsonElement, type: Type, context: JsonDeserializationContext): EligibilityFunction {
        return toEligibilityFunction(jsonElement.asJsonObject)
    }

    private fun toEligibilityFunction(function: JsonObject): EligibilityFunction {
        return EligibilityFunction(
            rule = EligibilityRule.valueOf(function.get("rule").asString),
            parameters = toParameters(function.getAsJsonArray("parameters"))
        )
    }

    private fun toParameters(parameterArray: JsonArray): List<Any> {
        return parameterArray.mapNotNull { element ->
            when {
                element.isJsonObject -> {
                    toEligibilityFunction(element.asJsonObject)
                }

                element.isJsonPrimitive -> {
                    element.asJsonPrimitive.asString
                }

                else -> null
            }
        }
    }
}