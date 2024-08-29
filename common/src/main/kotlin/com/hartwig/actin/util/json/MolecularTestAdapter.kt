package com.hartwig.actin.util.json

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import com.hartwig.actin.molecular.datamodel.ExperimentType
import com.hartwig.actin.molecular.datamodel.MolecularRecord
import com.hartwig.actin.molecular.datamodel.MolecularTest

class MolecularTestAdapter(private val gson: Gson) : TypeAdapter<MolecularTest>() {

    override fun write(out: JsonWriter, value: MolecularTest?) {
        if (value == null) {
            out.nullValue()
            return
        }

        val jsonObject = gson.toJsonTree(value).asJsonObject
        gson.toJson(jsonObject, out)
    }

    override fun read(input: JsonReader): MolecularTest? {
        val jsonObject = JsonParser.parseReader(input).asJsonObject
        return when (ExperimentType.valueOf(jsonObject.get("experimentType").asString)) {
            ExperimentType.HARTWIG_WHOLE_GENOME -> gson.fromJson(jsonObject, MolecularRecord::class.java)
            ExperimentType.HARTWIG_TARGETED -> gson.fromJson(jsonObject, MolecularRecord::class.java)
            ExperimentType.PANEL -> PanelRecordAdapter(gson).fromJsonTree(jsonObject)
        }
    }
}