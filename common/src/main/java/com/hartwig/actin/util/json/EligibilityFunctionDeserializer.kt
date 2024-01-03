package com.hartwig.actin.util.json

import com.google.gson.JsonArray
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.hartwig.actin.trial.datamodel.EligibilityFunction
import com.hartwig.actin.trial.datamodel.EligibilityRule
import java.lang.reflect.Type

class EligibilityFunctionDeserializer : JsonDeserializer<EligibilityFunction> {

    override fun deserialize(jsonElement: JsonElement, type: Type, context: JsonDeserializationContext): EligibilityFunction {
        return toEligibilityFunction(jsonElement.asJsonObject)
    }

    private fun toEligibilityFunction(function: JsonObject): EligibilityFunction {
        return EligibilityFunction(
            rule = EligibilityRule.valueOf(Json.string(function, "rule")),
            parameters = toParameters(Json.array(function, "parameters"))
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