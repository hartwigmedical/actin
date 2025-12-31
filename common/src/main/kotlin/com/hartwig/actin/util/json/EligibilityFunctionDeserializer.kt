package com.hartwig.actin.util.json

import com.google.gson.JsonArray
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.hartwig.actin.datamodel.trial.EligibilityFunction
import com.hartwig.actin.datamodel.trial.FunctionParameter
import com.hartwig.actin.datamodel.trial.Parameter
import java.lang.reflect.Type

class EligibilityFunctionDeserializer : JsonDeserializer<EligibilityFunction> {

    override fun deserialize(jsonElement: JsonElement, type: Type, context: JsonDeserializationContext): EligibilityFunction {
        return toEligibilityFunction(jsonElement.asJsonObject)
    }

    private fun toEligibilityFunction(function: JsonObject): EligibilityFunction {
        return EligibilityFunction(
            rule = function.get("ruleName").asString,
            parameters = toParameters(function.getAsJsonArray("parameters"))
        )
    }

    private fun toParameters(parameterArray: JsonArray): List<Parameter<*>> {
        return parameterArray.mapNotNull { element ->
            val type = Parameter.Type.valueOf(element.asJsonObject.get("type").asString)
            when {
                element.isJsonObject -> {
                    FunctionParameter(toEligibilityFunction(element.asJsonObject))
                }

                element.isJsonPrimitive -> {
                    type.create(element.asJsonPrimitive.asString)
                }

                else -> null
            }
        }
    }
}