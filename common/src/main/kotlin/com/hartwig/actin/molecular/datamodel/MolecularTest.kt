package com.hartwig.actin.molecular.datamodel

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import com.hartwig.actin.clinical.datamodel.PriorMolecularTest
import com.hartwig.actin.molecular.datamodel.archer.ArcherPanel
import com.hartwig.actin.molecular.datamodel.archer.ArcherVariant
import com.hartwig.actin.molecular.datamodel.panel.GenericPanel
import com.hartwig.actin.molecular.datamodel.panel.GenericPanelType
import java.time.LocalDate

interface MolecularTest<T> {
    val type: ExperimentType
    val date: LocalDate?
    val result: T
}

class MolecularTestFactory {
    companion object {
        fun classify(result: PriorMolecularTest): ExperimentType {
            return when (result.test) {
                "Archer FP Lung Target" -> ExperimentType.ARCHER
                "IHC" -> ExperimentType.IHC
                "" -> if (result.item == "PD-L1") ExperimentType.IHC else ExperimentType.OTHER
                "AvL Panel" -> ExperimentType.GENERIC_PANEL
                else -> ExperimentType.OTHER
            }
        }

        fun fromPriorMolecular(tests: List<PriorMolecularTest>): List<MolecularTest<*>> {
            return tests.groupBy { classify(it) }
                .flatMap { (type, results) ->
                    when (type) {
                        ExperimentType.IHC -> results.map { IHCMolecularTest.fromPriorMolecularTest(it) }
                        ExperimentType.ARCHER -> ArcherMolecularTest.fromPriorMolecularTests(results)
                        ExperimentType.GENERIC_PANEL -> GenericPanelMolecularTest.fromPriorMolecularTest(results)
                        else -> results.map { OtherPriorMolecularTest.fromPriorMolecularTest(it) }
                    }
                }
        }
    }
}

data class WGSMolecularTest(
    override val type: ExperimentType,
    override val date: LocalDate?,
    override val result: MolecularRecord
) : MolecularTest<MolecularRecord> {

    companion object {
        fun fromMolecularRecord(result: MolecularRecord): WGSMolecularTest {
            return WGSMolecularTest(result.type, result.date, result)
        }
    }
}

data class IHCMolecularTest(
    override val type: ExperimentType,
    override val date: LocalDate?,
    override val result: PriorMolecularTest
) : MolecularTest<PriorMolecularTest> {

    companion object {
        fun fromPriorMolecularTest(result: PriorMolecularTest): IHCMolecularTest {
            return IHCMolecularTest(ExperimentType.IHC, date = null, result)
        }
    }
}

data class ArcherMolecularTest(
    override val type: ExperimentType,
    override val date: LocalDate?,
    override val result: ArcherPanel
) : MolecularTest<ArcherPanel> {

    companion object {
        fun fromPriorMolecularTests(results: List<PriorMolecularTest>): List<ArcherMolecularTest> {
            return results.filter { it.test == "Archer FP Lung Target" }
                .groupBy { it.measureDate }
                .map { (date, results) ->
                    val variants = results.mapNotNull { result ->
                        result.item?.let { item ->
                            ArcherVariant(gene = item, hgvsCodingImpact = result.measure
                                ?: throw IllegalArgumentException("Expected measure with hgvs variant but was null for item $item"))
                        }
                    }

                    // TODO (kz): we haven't seen an example of fusions in the data yet,
                    //  figure out how they are represented and add them here when we do
                    ArcherMolecularTest(ExperimentType.ARCHER, date = date,
                        result = ArcherPanel(date, variants, fusions = emptyList()))
                }
        }
    }
}

data class GenericPanelMolecularTest(
    override val type: ExperimentType,
    override val date: LocalDate?,
    override val result: GenericPanel
) : MolecularTest<GenericPanel> {

    companion object {
        fun fromPriorMolecularTest(results: List<PriorMolecularTest>): List<GenericPanelMolecularTest> {
            return results.filter { it.test == "AvL Panel" }
                .groupBy { it.measureDate }
                .map { (date, results) ->
                    GenericPanelMolecularTest(ExperimentType.GENERIC_PANEL, date = date, result = GenericPanel(GenericPanelType.AVL, date))
                }
        }
    }
}

data class OtherPriorMolecularTest(
    override val type: ExperimentType,
    override val date: LocalDate?,
    override val result: PriorMolecularTest
) : MolecularTest<PriorMolecularTest> {

    companion object {
        fun fromPriorMolecularTest(result: PriorMolecularTest): OtherPriorMolecularTest {
            return OtherPriorMolecularTest(ExperimentType.OTHER, date = null, result)
        }
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
        val type = jsonObject.get("type").asString

        return when (type) {
            ExperimentType.WHOLE_GENOME.toString() -> gson.fromJson(jsonObject, WGSMolecularTest::class.java)
            ExperimentType.TARGETED.toString() -> gson.fromJson(jsonObject, WGSMolecularTest::class.java)
            ExperimentType.IHC.toString() -> gson.fromJson(jsonObject, IHCMolecularTest::class.java)
            ExperimentType.ARCHER.toString() -> gson.fromJson(jsonObject, ArcherMolecularTest::class.java)
            ExperimentType.OTHER.toString() -> gson.fromJson(jsonObject, OtherPriorMolecularTest::class.java)
            else -> throw IllegalArgumentException("Unknown molecular test type: $type")
        }
    }
}