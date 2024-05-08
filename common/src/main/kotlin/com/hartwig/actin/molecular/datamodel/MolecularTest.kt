package com.hartwig.actin.molecular.datamodel

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import com.hartwig.actin.clinical.datamodel.PriorMolecularTest
import com.hartwig.actin.molecular.datamodel.panel.archer.ArcherFusion
import com.hartwig.actin.molecular.datamodel.panel.archer.ArcherPanel
import com.hartwig.actin.molecular.datamodel.panel.archer.ArcherSkippedExons
import com.hartwig.actin.molecular.datamodel.panel.archer.ArcherVariant
import com.hartwig.actin.molecular.datamodel.panel.generic.GenericExonDeletion
import com.hartwig.actin.molecular.datamodel.panel.generic.GenericFusion
import com.hartwig.actin.molecular.datamodel.panel.generic.GenericPanel
import com.hartwig.actin.molecular.datamodel.panel.generic.GenericPanelType
import com.hartwig.actin.molecular.datamodel.panel.generic.GenericVariant
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
const val FREE_TEXT_PANEL = "Freetext"

class MolecularTestFactory {
    companion object {
        fun classify(result: PriorMolecularTest): ExperimentType {
            return when (result.test) {
                ARCHER_FP_LUNG_TARGET -> ExperimentType.ARCHER
                AVL_PANEL -> ExperimentType.GENERIC_PANEL
                FREE_TEXT_PANEL -> ExperimentType.GENERIC_PANEL
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

private val FUSION_REGEX = Regex("([A-Za-z0-9 ]+)( fusie aangetoond)")
private val EXON_SKIP_REGEX = Regex("([A-Za-z0-9 ]+)( exon )([0-9]+(-[0-9]+)?)( skipping aangetoond)")
private const val NO_FUSIONS = "GEEN fusie(s) aangetoond"
private const val NO_MUTATION = "GEEN mutaties aangetoond"

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
                    val resultsWithItemAndMeasure = results.filter { it.item != null && it.measure != null }
                    val variants = resultsWithItemAndMeasure
                        .filter { it.measure!!.startsWith("c.") }
                        .map {
                            ArcherVariant(it.item!!, it.measure!!) to it
                        }
                    val fusions = resultsWithItemAndMeasure
                        .mapNotNull {
                            FUSION_REGEX.find(it.measure!!)?.let { matchResult -> ArcherFusion(matchResult.groupValues[1]) to it }
                        }
                    val exonSkips = resultsWithItemAndMeasure
                        .mapNotNull {
                            EXON_SKIP_REGEX.find(it.measure!!)?.let { matchResult ->
                                val (start, end) = parseRange(matchResult.groupValues[3])
                                ArcherSkippedExons(matchResult.groupValues[1], start, end) to it
                            }
                        }
                    checkForUnknownResults(results, variants, fusions, exonSkips)
                    ArcherMolecularTest(
                        date = date,
                        result = ArcherPanel(variants.map { it.first }, fusions.map { it.first }, exonSkips.map { it.first })
                    )
                }
        }

        private fun checkForUnknownResults(
            results: List<PriorMolecularTest>,
            variants: List<Pair<ArcherVariant, PriorMolecularTest>>,
            fusions: List<Pair<ArcherFusion, PriorMolecularTest>>,
            exonSkips: List<Pair<ArcherSkippedExons, PriorMolecularTest>>
        ) {
            val relevantResults = results.filter { it.measure != NO_FUSIONS && it.measure != NO_MUTATION }.toSet()
            val processedResults = (variants + fusions + exonSkips).map { it.second }.toSet()
            val unknownResults = relevantResults - processedResults
            if (unknownResults.isNotEmpty()) {
                throw IllegalArgumentException("Unknown results in Archer: ${unknownResults.map { "${it.item} ${it.measure}" }}")
            }
        }

        private fun parseRange(range: String): Pair<Int, Int> {
            val parts = range.split("-")
            return parts[0].toInt() to parts[parts.size - 1].toInt()
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
            return results.groupBy { it.test }
                .flatMap { (test, results) -> groupedByTestDate(results, classify(test)) }
        }

        private fun groupedByTestDate(results: List<PriorMolecularTest>, type: GenericPanelType): List<GenericPanelMolecularTest> {
            return results
                .groupBy { it.measureDate }
                .map { (date, results) ->
                    val usableResults = results.filterNot { result -> isKnownIgnorableRecord(result, type) }
                    val (fusionRecords, nonFusionRecords) = usableResults.partition { it.item?.contains("::") ?: false }
                    val fusions = fusionRecords.mapNotNull { it.item?.let { item -> GenericFusion.parseFusion(item) } }

                    val (exonDeletionRecords, variantRecords) = nonFusionRecords.partition { it.measure?.endsWith(" del") ?: false }
                    val variants = variantRecords.map { record -> GenericVariant.parseVariant(record) }
                    val exonDeletions = exonDeletionRecords.map { record -> GenericExonDeletion.parse(record) }

                    GenericPanelMolecularTest(date = date, result = GenericPanel(type, variants, fusions, exonDeletions))
                }
        }

        private fun isKnownIgnorableRecord(result: PriorMolecularTest, type: GenericPanelType): Boolean {
            return when (type) {
                GenericPanelType.AVL -> result.measure == "GEEN mutaties aangetoond met behulp van het AVL Panel"
                else -> false
            }
        }

        private fun classify(type: String?): GenericPanelType {
            return when (type) {
                AVL_PANEL -> GenericPanelType.AVL
                FREE_TEXT_PANEL -> GenericPanelType.FREE_TEXT
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