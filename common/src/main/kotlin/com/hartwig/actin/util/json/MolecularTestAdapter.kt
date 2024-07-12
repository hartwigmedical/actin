package com.hartwig.actin.util.json

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import com.hartwig.actin.molecular.datamodel.ExperimentType
import com.hartwig.actin.molecular.datamodel.IHCMolecularTest
import com.hartwig.actin.molecular.datamodel.MolecularRecord
import com.hartwig.actin.molecular.datamodel.MolecularTest
import com.hartwig.actin.molecular.datamodel.OtherPriorMolecularTest

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
        return when (ExperimentType.valueOf(jsonObject.get("type").asString)) {
            ExperimentType.WHOLE_GENOME -> gson.fromJson(jsonObject, MolecularRecord::class.java)
            ExperimentType.TARGETED -> gson.fromJson(jsonObject, MolecularRecord::class.java)
            ExperimentType.IHC -> gson.fromJson(jsonObject, IHCMolecularTest::class.java)
            ExperimentType.ARCHER -> PanelRecordAdapter(gson).fromJsonTree(jsonObject)
            ExperimentType.GENERIC_PANEL -> PanelRecordAdapter(gson).fromJsonTree(jsonObject)
            ExperimentType.OTHER -> gson.fromJson(jsonObject, OtherPriorMolecularTest::class.java)
            ExperimentType.LIQUID_CDX -> PanelRecordAdapter(gson).fromJsonTree(jsonObject)
            ExperimentType.FOUNDATION_CDX -> PanelRecordAdapter(gson).fromJsonTree(jsonObject)
        }
    }
}