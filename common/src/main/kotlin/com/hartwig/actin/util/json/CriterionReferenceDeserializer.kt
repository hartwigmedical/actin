package com.hartwig.actin.util.json

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.hartwig.actin.trial.datamodel.CriterionReference
import com.hartwig.actin.util.json.Json.string
import java.lang.reflect.Type

class CriterionReferenceDeserializer : JsonDeserializer<CriterionReference> {

    override fun deserialize(jsonElement: JsonElement, type: Type, context: JsonDeserializationContext): CriterionReference {
        val obj = jsonElement.asJsonObject
        return CriterionReference(id = string(obj, "id"), text = fromJsonReferenceText(string(obj, "text")))
    }

    private fun fromJsonReferenceText(text: String): String {
        return text.replace(JSON_REFERENCE_TEXT_LINE_BREAK, JAVA_REFERENCE_TEXT_LINE_BREAK)
    }

    companion object {
        private const val JSON_REFERENCE_TEXT_LINE_BREAK: String = "<enter>"
        private const val JAVA_REFERENCE_TEXT_LINE_BREAK: String = "\n"

        fun toJsonReferenceText(text: String): String {
            return text.replace(JAVA_REFERENCE_TEXT_LINE_BREAK, JSON_REFERENCE_TEXT_LINE_BREAK)
        }
    }
}