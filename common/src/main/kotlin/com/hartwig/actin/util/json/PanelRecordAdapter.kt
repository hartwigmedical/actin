package com.hartwig.actin.util.json

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import com.hartwig.actin.molecular.datamodel.Drivers
import com.hartwig.actin.molecular.datamodel.ExperimentType
import com.hartwig.actin.molecular.datamodel.MolecularCharacteristics
import com.hartwig.actin.molecular.datamodel.PanelRecord
import java.time.LocalDate

class PanelRecordAdapter(private val gson: Gson) : TypeAdapter<PanelRecord>() {

    override fun write(out: JsonWriter, value: PanelRecord?) {
        if (value == null) {
            out.nullValue()
            return
        }

        val jsonObject = gson.toJsonTree(value).asJsonObject
        gson.toJson(jsonObject, out)
    }

    override fun read(input: JsonReader): PanelRecord {
        val jsonObject = JsonParser.parseReader(input).asJsonObject
        val experimentType = ExperimentType.valueOf(jsonObject.get("experimentType").asString)
        val testTypeJson = jsonObject.get("testTypeDisplay")
        val testType = if (testTypeJson.isJsonNull) null else testTypeJson.asString

        return PanelRecord(
            testedGenes = jsonObject.getAsJsonArray("testedGenes").map { it.asString }.toSet(),
            testTypeDisplay = testType,
            experimentType = experimentType,
            date = gson.fromJson(jsonObject.get("date"), LocalDate::class.java),
            drivers = gson.fromJson(jsonObject.get("drivers"), Drivers::class.java),
            characteristics = gson.fromJson(jsonObject.get("characteristics"), MolecularCharacteristics::class.java),
            evidenceSource = jsonObject.get("evidenceSource").asString
        )
    }
}