package com.hartwig.actin.util.json

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import com.hartwig.actin.molecular.datamodel.Drivers
import com.hartwig.actin.molecular.datamodel.ExperimentType
import com.hartwig.actin.molecular.datamodel.MolecularCharacteristics
import com.hartwig.actin.molecular.datamodel.panel.PanelExtraction
import com.hartwig.actin.molecular.datamodel.panel.PanelRecord
import com.hartwig.actin.molecular.datamodel.panel.archer.ArcherPanelExtraction
import com.hartwig.actin.molecular.datamodel.panel.generic.GenericPanelExtraction
import com.hartwig.actin.molecular.datamodel.panel.mcgi.McgiExtraction
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
        val panelExtractionJson = jsonObject.get("panelExtraction")
        val panelExtractionClass = panelExtractionJson.asJsonObject.get("extractionClass").asString
        val panelExtraction: PanelExtraction = when (panelExtractionClass) {
            ArcherPanelExtraction::class.java.simpleName -> gson.fromJson(panelExtractionJson, ArcherPanelExtraction::class.java)
            McgiExtraction::class.java.simpleName -> gson.fromJson(panelExtractionJson, ArcherPanelExtraction::class.java)
            GenericPanelExtraction::class.java.simpleName -> gson.fromJson(panelExtractionJson, GenericPanelExtraction::class.java)
            else -> throw IllegalArgumentException("Unsupported panel extraction $panelExtractionClass")
        }

        return PanelRecord(
            panelExtraction = panelExtraction,
            testTypeDisplay = testType,
            experimentType = experimentType,
            date = gson.fromJson(jsonObject.get("date"), LocalDate::class.java),
            drivers = gson.fromJson(jsonObject.get("drivers"), Drivers::class.java),
            characteristics = gson.fromJson(jsonObject.get("characteristics"), MolecularCharacteristics::class.java),
            evidenceSource = jsonObject.get("evidenceSource").asString
        )
    }
}