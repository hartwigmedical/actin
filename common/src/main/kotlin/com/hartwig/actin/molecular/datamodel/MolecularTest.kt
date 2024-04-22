package com.hartwig.actin.molecular.datamodel

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import com.hartwig.actin.clinical.datamodel.PriorMolecularTest
import com.hartwig.actin.molecular.datamodel.panel.archer.ArcherPanel
import com.hartwig.actin.molecular.datamodel.panel.archer.ArcherVariant
import com.hartwig.actin.molecular.datamodel.panel.generic.GenericFusion
import com.hartwig.actin.molecular.datamodel.panel.generic.GenericPanel
import com.hartwig.actin.molecular.datamodel.panel.generic.GenericPanelType
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

const val ARCHER_FP_LUNG_TARGET = "Archer FP Lung Target"
const val AVL_PANEL = "AvL Panel"

class MolecularTestFactory {
    companion object {
        fun classify(result: PriorMolecularTest): ExperimentType {
            return when (result.test) {
                ARCHER_FP_LUNG_TARGET -> ExperimentType.ARCHER
                AVL_PANEL -> ExperimentType.GENERIC_PANEL
                "Freetext" -> ExperimentType.GENERIC_PANEL
                "IHC" -> ExperimentType.IHC
                "" -> if (result.item == "PD-L1") ExperimentType.IHC else ExperimentType.OTHER
                else -> ExperimentType.OTHER
            }
        }

        fun fromPriorMolecular(tests: List<PriorMolecularTest>): List<MolecularTest<out Any>> {
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

    companion object {
        fun fromPriorMolecularTest(result: PriorMolecularTest): IHCMolecularTest {
            return IHCMolecularTest(date = null, result)
        }
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

    companion object {
        fun fromPriorMolecularTests(results: List<PriorMolecularTest>): List<ArcherMolecularTest> {
            return results.filter { it.test == ARCHER_FP_LUNG_TARGET }
                .groupBy { it.measureDate }
                .map { (date, results) ->
                    val variants = results.mapNotNull { result ->
                        result.item?.let { item ->
                            ArcherVariant(
                                gene = item, hgvsCodingImpact = result.measure
                                    ?: throw IllegalArgumentException("Expected measure with hgvs variant but was null for item $item")
                            )
                        }
                    }

                    // TODO (kz): we haven't seen an example of fusions in the data yet,
                    //  figure out how they are represented and add them here when we do
                    ArcherMolecularTest(
                        date = date,
                        result = ArcherPanel(variants, fusions = emptyList())
                    )
                }
        }
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

    companion object {
        fun fromPriorMolecularTest(results: List<PriorMolecularTest>): List<GenericPanelMolecularTest> {
            return results.groupBy { it.test }.flatMap { (test, results) -> fromTestGroup(results, classify(test)) }
        }

        private fun fromTestGroup(results: List<PriorMolecularTest>, type: GenericPanelType): List<GenericPanelMolecularTest> {
            return results.groupBy { it.measureDate }
                .map { (date, results) ->
                    val fusion = results.mapNotNull { it.item?.let { item -> parseFusion(item) } }
                    GenericPanelMolecularTest(date = date, result = GenericPanel(GenericPanelType.AVL, fusion))
                }
        }

        fun parseFusion(text: String): GenericFusion {
            val parts = text.trim().split("::")
            if (parts.size != 2) {
                throw IllegalArgumentException("Expected two parts in fusion but got ${parts.size} for $text")
            }

            return GenericFusion(parts[0], parts[1])
        }

        private fun classify(type: String?): GenericPanelType {
            return when (type) {
                "AvL Panel" -> GenericPanelType.AVL
                "Freetext" -> GenericPanelType.FREE_TEXT
                else -> throw IllegalArgumentException("Unknown generic panel type: $type")
            }
        }
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

    companion object {
        fun fromPriorMolecularTest(result: PriorMolecularTest): OtherPriorMolecularTest {
            return OtherPriorMolecularTest(date = null, result)
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