package com.hartwig.actin.util.json

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import com.hartwig.actin.datamodel.clinical.treatment.Drug
import com.hartwig.actin.datamodel.clinical.treatment.DrugType
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import java.lang.reflect.Type

class DrugDeserializer : JsonDeserializer<Drug> {

    override fun deserialize(json: JsonElement, type: Type, context: JsonDeserializationContext): Drug {
        val obj = json.asJsonObject
        val synonymsElement = obj.get("synonyms")
        val synonyms: Set<String> = if (synonymsElement != null && !synonymsElement.isJsonNull) {
            context.deserialize(synonymsElement, object : TypeToken<Set<String>>() {}.type)
        } else emptySet()
        return Drug(
            name = obj.get("name").asString,
            synonyms = synonyms,
            drugTypes = context.deserialize(obj.get("drugTypes"), object : TypeToken<Set<DrugType>>() {}.type),
            category = context.deserialize(obj.get("category"), TreatmentCategory::class.java),
            displayOverride = obj.get("displayOverride")?.takeUnless { it.isJsonNull }?.asString
        )
    }
}
