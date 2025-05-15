package com.hartwig.actin.util.json

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import com.hartwig.actin.datamodel.molecular.ExperimentType
import com.hartwig.actin.datamodel.molecular.PanelRecord
import com.hartwig.actin.datamodel.molecular.KnownPanelSpecification
import com.hartwig.actin.datamodel.molecular.characteristics.MolecularCharacteristics
import com.hartwig.actin.datamodel.molecular.driver.Drivers
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
            specification = gson.fromJson(jsonObject.getAsJsonObject("specification"), KnownPanelSpecification::class.java),
            testTypeDisplay = testType,
            experimentType = experimentType,
            date = gson.fromJson(jsonObject.get("date"), LocalDate::class.java),
            drivers = gson.fromJson(jsonObject.get("drivers"), Drivers::class.java),
            characteristics = gson.fromJson(jsonObject.get("characteristics"), MolecularCharacteristics::class.java),
            evidenceSource = jsonObject.get("evidenceSource").asString,
            hasSufficientPurity = true,
            hasSufficientQuality = true
        )
    }
}