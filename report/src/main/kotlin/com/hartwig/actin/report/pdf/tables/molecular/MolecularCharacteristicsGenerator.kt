package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.molecular.datamodel.MolecularRecord
import com.hartwig.actin.molecular.datamodel.pharmaco.PharmacoEntry
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Formats
import com.hartwig.actin.report.pdf.util.Styles
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Table
import java.lang.Boolean
import java.util.*
import java.util.List
import java.util.function.Consumer
import java.util.function.Function
import kotlin.Double
import kotlin.Float
import kotlin.String

class MolecularCharacteristicsGenerator(private val molecular: MolecularRecord, private val width: Float) : TableGenerator {
    override fun title(): String {
        return "General"
    }

    override fun contents(): Table {
        val colWidth = width / 10
        val table = Tables.createFixedWidthCols(colWidth, colWidth, colWidth, colWidth, colWidth, colWidth, colWidth * 2, colWidth * 2)
        listOf("Purity", "Sufficient Quality", "TML Status", "TMB Status", "MS Stability", "HR Status", "DPYD", "UGT1A1").forEach(
            Consumer { title: String -> table.addHeaderCell(Cells.createHeader(title)) })
        val characteristics = molecular.characteristics()
        List.of(
            createPurityCell(characteristics.purity()),
            Cells.createContentYesNo(Formats.yesNoUnknown(molecular.hasSufficientQualityAndPurity())),
            createTMLStatusCell(),
            createTMBStatusCell(),
            createMSStabilityCell(),
            createHRStatusCell(),
            Cells.createContent(createPeachSummaryForGene(molecular.pharmaco(), "DPYD")),
            Cells.createContent(createPeachSummaryForGene(molecular.pharmaco(), "UGT1A1"))
        ).forEach(
            Consumer { cell: Cell? -> table.addCell(cell) })
        return table
    }

    private fun createPurityCell(purity: Double?): Cell {
        if (!molecular.containsTumorCells()) {
            return Cells.createContentWarn("None")
        }
        if (purity == null) {
            return Cells.createContentWarn(Formats.VALUE_UNKNOWN)
        }
        val purityString = Formats.percentage(purity)
        return if (purity < 0.2) {
            Cells.createContentWarn(purityString)
        } else {
            Cells.createContent(purityString)
        }
    }

    fun createTMLAndTMBStatusString(): String {
        val hasHighTumorMutationalLoad = molecular.characteristics().hasHighTumorMutationalLoad()
        val tumorMutationalLoad = molecular.characteristics().tumorMutationalLoad()
        val TMLString = if (tumorMutationalLoad == null || hasHighTumorMutationalLoad == null) Formats.VALUE_UNKNOWN else String.format(
            "TML %s (%d)",
            if (hasHighTumorMutationalLoad) "high" else "low",
            tumorMutationalLoad
        )
        val hasHighTumorMutationalBurden = molecular.characteristics().hasHighTumorMutationalBurden()
        val tumorMutationalBurden = molecular.characteristics().tumorMutationalBurden()
        val TMBString = if (tumorMutationalBurden == null || hasHighTumorMutationalBurden == null) Formats.VALUE_UNKNOWN else String.format(
            "TMB %s (%s)",
            if (hasHighTumorMutationalBurden) "high" else "low",
            Formats.singleDigitNumber(tumorMutationalBurden)
        )
        return String.format("%s / %s", TMLString, TMBString)
    }

    fun createTMLStatusStringOption(): Optional<String> {
        val hasHighTumorMutationalLoad = molecular.characteristics().hasHighTumorMutationalLoad()
        val tumorMutationalLoad = molecular.characteristics().tumorMutationalLoad()
        return if (hasHighTumorMutationalLoad == null || tumorMutationalLoad == null) {
            Optional.empty()
        } else Optional.of(
            String.format(
                "%s (%d)",
                if (hasHighTumorMutationalLoad) "High" else "Low",
                tumorMutationalLoad
            )
        )
    }

    private fun createTMLStatusCell(): Cell {
        return if (!molecular.containsTumorCells()) {
            Cells.createContentWarn(Formats.VALUE_NOT_AVAILABLE)
        } else createTMLStatusStringOption().map { value: String ->
            val cell = if (molecular.hasSufficientQualityAndPurity()) Cells.createContent(value) else Cells.createContentWarn(value)
            if (Boolean.TRUE == molecular.characteristics().hasHighTumorMutationalLoad()) {
                cell.addStyle(Styles.tableHighlightStyle())
            }
            cell
        }.orElse(Cells.createContentWarn(Formats.VALUE_UNKNOWN))
    }

    private fun createTMBStatusCell(): Cell {
        if (!molecular.containsTumorCells()) {
            return Cells.createContentWarn(Formats.VALUE_NOT_AVAILABLE)
        }
        val hasHighTumorMutationalBurden = molecular.characteristics().hasHighTumorMutationalBurden()
        val tumorMutationalBurden = molecular.characteristics().tumorMutationalBurden()
        if (hasHighTumorMutationalBurden == null || tumorMutationalBurden == null) {
            return Cells.createContentWarn(Formats.VALUE_UNKNOWN)
        }
        val interpretation = if (hasHighTumorMutationalBurden) "High" else "Low"
        val value = interpretation + " (" + Formats.singleDigitNumber(tumorMutationalBurden) + ")"
        val cell = if (molecular.hasSufficientQualityAndPurity()) Cells.createContent(value) else Cells.createContentWarn(value)
        if (hasHighTumorMutationalBurden) {
            cell.addStyle(Styles.tableHighlightStyle())
        }
        return cell
    }

    fun createMSStabilityStringOption(): Optional<String?> {
        val isMicrosatelliteUnstable = molecular.characteristics().isMicrosatelliteUnstable ?: return Optional.empty()
        return Optional.of(if (isMicrosatelliteUnstable) "Unstable" else "Stable")
    }

    private fun createMSStabilityCell(): Cell {
        return if (!molecular.containsTumorCells()) {
            Cells.createContentWarn(Formats.VALUE_NOT_AVAILABLE)
        } else createMSStabilityStringOption().map(Function { value: String ->
            val cell = if (molecular.hasSufficientQualityAndPurity()) Cells.createContent(value) else Cells.createContentWarn(value)
            if (Boolean.TRUE == molecular.characteristics().isMicrosatelliteUnstable) {
                cell.addStyle(Styles.tableHighlightStyle())
            }
            cell
        }).orElse(Cells.createContentWarn(Formats.VALUE_UNKNOWN))
    }

    fun createHRStatusStringOption(): Optional<String?> {
        val isHomologousRepairDeficient = molecular.characteristics().isHomologousRepairDeficient
            ?: return Optional.empty()
        return Optional.of(if (isHomologousRepairDeficient) "Deficient" else "Proficient")
    }

    private fun createHRStatusCell(): Cell {
        return if (!molecular.containsTumorCells()) {
            Cells.createContentWarn(Formats.VALUE_NOT_AVAILABLE)
        } else createHRStatusStringOption().map(Function { value: String ->
            val cell = if (molecular.hasSufficientQualityAndPurity()) Cells.createContent(value) else Cells.createContentWarn(value)
            if (Boolean.TRUE == molecular.characteristics().isHomologousRepairDeficient) {
                cell.addStyle(Styles.tableHighlightStyle())
            }
            cell
        }).orElse(Cells.createContentWarn(Formats.VALUE_UNKNOWN))
    }

    companion object {
        private fun createPeachSummaryForGene(pharmaco: Set<PharmacoEntry>, gene: String): String {
            val pharmacoEntry = findPharmacoEntry(pharmaco, gene) ?: return Formats.VALUE_UNKNOWN
            val joiner = Formats.commaJoiner()
            for (haplotype in pharmacoEntry.haplotypes()) {
                joiner.add(haplotype.name() + " (" + haplotype.function() + ")")
            }
            return joiner.toString()
        }

        private fun findPharmacoEntry(pharmaco: Set<PharmacoEntry>, geneToFind: String): PharmacoEntry? {
            for (entry in pharmaco) {
                if (entry.gene() == geneToFind) {
                    return entry
                }
            }
            return null
        }
    }
}