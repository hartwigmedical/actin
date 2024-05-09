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

interface MolecularTest<T> {
    val type: ExperimentType
    val date: LocalDate?
    val result: T
    fun accept(molecularTestVisitor: MolecularTestVisitor)
}

interface MolecularTestVisitor {
    fun visit(test: WGSMolecularTest) {}
    fun visit(test: IHCMolecularTest) {}
    fun visit(test: ArcherMolecularTest) {}
    fun visit(test: GenericPanelMolecularTest) {}
    fun visit(test: OtherPriorMolecularTest) {}
}

data class WGSMolecularTest(
    override val type: ExperimentType,
    override val date: LocalDate?,
    override val result: MolecularRecord
) : MolecularTest<MolecularRecord> {

    override fun accept(molecularTestVisitor: MolecularTestVisitor) {
        molecularTestVisitor.visit(this)
    }

    companion object {
        fun fromMolecularRecord(result: MolecularRecord): WGSMolecularTest {
            return WGSMolecularTest(result.type, result.date, result)
        }
    }
}

data class IHCMolecularTest(
    override val date: LocalDate? = null,
    override val result: PriorMolecularTest
) : MolecularTest<PriorMolecularTest> {

    override val type = ExperimentType.IHC

    override fun accept(molecularTestVisitor: MolecularTestVisitor) {
        molecularTestVisitor.visit(this)
    }
}

data class ArcherMolecularTest(
    override val date: LocalDate? = null,
    override val result: ArcherPanel
) : MolecularTest<ArcherPanel> {

    override val type = ExperimentType.ARCHER

    override fun accept(molecularTestVisitor: MolecularTestVisitor) {
        molecularTestVisitor.visit(this)
    }
}

data class GenericPanelMolecularTest(
    override val date: LocalDate? = null,
    override val result: GenericPanel
) : MolecularTest<GenericPanel> {

    override val type = ExperimentType.GENERIC_PANEL

    override fun accept(molecularTestVisitor: MolecularTestVisitor) {
        molecularTestVisitor.visit(this)
    }
}

data class OtherPriorMolecularTest(
    override val date: LocalDate? = null,
    override val result: PriorMolecularTest
) : MolecularTest<PriorMolecularTest> {

    override val type = ExperimentType.OTHER

    override fun accept(molecularTestVisitor: MolecularTestVisitor) {
        molecularTestVisitor.visit(this)
    }
}

class MolecularTestAdapter(private val gson: Gson) : TypeAdapter<MolecularTest<*>>() {

    override fun write(out: JsonWriter, value: MolecularTest<*>?) {
        if (value == null) {
            out.nullValue()
            return
        }

        val jsonObject = gson.toJsonTree(value).asJsonObject
        gson.toJson(jsonObject, out)
    }

    override fun read(input: JsonReader): MolecularTest<*>? {
        val jsonObject = JsonParser.parseReader(input).asJsonObject
        return when (val type = jsonObject.get("type").asString) {
            ExperimentType.WHOLE_GENOME.toString() -> gson.fromJson(jsonObject, WGSMolecularTest::class.java)
            ExperimentType.TARGETED.toString() -> gson.fromJson(jsonObject, WGSMolecularTest::class.java)
            ExperimentType.IHC.toString() -> gson.fromJson(jsonObject, IHCMolecularTest::class.java)
            ExperimentType.ARCHER.toString() -> gson.fromJson(jsonObject, ArcherMolecularTest::class.java)
            ExperimentType.GENERIC_PANEL.toString() -> gson.fromJson(jsonObject, GenericPanelMolecularTest::class.java)
            ExperimentType.OTHER.toString() -> gson.fromJson(jsonObject, OtherPriorMolecularTest::class.java)
            else -> throw IllegalArgumentException("Unknown molecular test type: $type")
        }
    }
}