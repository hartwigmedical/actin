package com.hartwig.actin.molecular.datamodel

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import com.hartwig.actin.clinical.datamodel.PriorMolecularTest
import com.hartwig.actin.molecular.datamodel.panel.archer.ArcherPanel
import com.hartwig.actin.molecular.datamodel.panel.generic.GenericPanel
import java.time.LocalDate

const val ARCHER_FP_LUNG_TARGET = "Archer FP Lung Target"
const val AVL_PANEL = "AvL Panel"
const val FREE_TEXT_PANEL = "Freetext"

interface MolecularTest {
    val type: ExperimentType
    val date: LocalDate?
}

data class IHCMolecularTest(
    val test: PriorMolecularTest
) : MolecularTest {
    override val type = ExperimentType.IHC
    override val date = test.measureDate
}

data class OtherPriorMolecularTest(
    val test: PriorMolecularTest
) : MolecularTest {
    override val type = ExperimentType.OTHER
    override val date = test.measureDate
}

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
        return when (val type = jsonObject.get("type").asString) {
            ExperimentType.WHOLE_GENOME.toString() -> gson.fromJson(jsonObject, MolecularRecord::class.java)
            ExperimentType.TARGETED.toString() -> gson.fromJson(jsonObject, MolecularRecord::class.java)
            ExperimentType.IHC.toString() -> gson.fromJson(jsonObject, IHCMolecularTest::class.java)
            ExperimentType.ARCHER.toString() -> gson.fromJson(jsonObject, ArcherPanel::class.java)
            ExperimentType.GENERIC_PANEL.toString() -> gson.fromJson(jsonObject, GenericPanel::class.java)
            ExperimentType.OTHER.toString() -> gson.fromJson(jsonObject, OtherPriorMolecularTest::class.java)
            else -> throw IllegalArgumentException("Unknown molecular test type: $type")
        }
    }
}