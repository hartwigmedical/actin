package com.hartwig.actin.util.json

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import com.hartwig.actin.datamodel.clinical.treatment.DrugTreatment
import com.hartwig.actin.datamodel.clinical.treatment.OtherTreatment
import com.hartwig.actin.datamodel.clinical.treatment.Radiotherapy
import com.hartwig.actin.datamodel.clinical.treatment.Treatment

class TreatmentAdapter(private val gson: Gson) : TypeAdapter<Treatment>() {
    override fun write(out: JsonWriter, value: Treatment) {
        gson.toJson(gson.toJsonTree(value), out)
    }

    override fun read(input: JsonReader): Treatment {
        val jsonObject = JsonParser.parseReader(input).asJsonObject
        return when (val type = jsonObject.get("treatmentClass").asString) {
            "DRUG_TREATMENT" -> gson.fromJson(jsonObject, DrugTreatment::class.java)
            "OTHER_TREATMENT" -> gson.fromJson(jsonObject, OtherTreatment::class.java)
            "RADIOTHERAPY" -> gson.fromJson(jsonObject, Radiotherapy::class.java)
            else -> throw IllegalArgumentException("Unknown treatment type: $type")
        }
    }
}