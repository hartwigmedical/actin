package com.hartwig.actin.util.json

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import com.hartwig.actin.molecular.datamodel.Drivers
import com.hartwig.actin.molecular.datamodel.ExperimentType
import com.hartwig.actin.molecular.datamodel.MolecularCharacteristics
import com.hartwig.actin.molecular.datamodel.panel.McgiExtraction
import com.hartwig.actin.molecular.datamodel.panel.PanelExtraction
import com.hartwig.actin.molecular.datamodel.panel.PanelRecord
import com.hartwig.actin.molecular.datamodel.panel.archer.ArcherPanelExtraction
import com.hartwig.actin.molecular.datamodel.panel.generic.GenericPanelExtraction
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

    override fun read(input: JsonReader): PanelRecord? {
        val jsonObject = JsonParser.parseReader(input).asJsonObject
        val experimentType = ExperimentType.valueOf(jsonObject.get("type").asString)

        val panelExtraction: PanelExtraction = when (experimentType) {
            ExperimentType.ARCHER -> gson.fromJson(jsonObject.get("panelExtraction"), ArcherPanelExtraction::class.java)
            ExperimentType.GENERIC_PANEL -> gson.fromJson(jsonObject.get("panelExtraction"), GenericPanelExtraction::class.java)
            else -> gson.fromJson(jsonObject.get("panelExtraction"), McgiExtraction::class.java)
        }

        return PanelRecord(
            panelExtraction = panelExtraction,
            type = experimentType,
            date = gson.fromJson(jsonObject.get("date"), LocalDate::class.java),
            drivers = gson.fromJson(jsonObject.get("drivers"), Drivers::class.java),
            characteristics = gson.fromJson(jsonObject.get("characteristics"), MolecularCharacteristics::class.java),
            evidenceSource = jsonObject.get("evidenceSource").asString
        )
    }
}