package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.molecular.datamodel.MolecularRecord
import com.hartwig.actin.molecular.datamodel.hasSufficientQualityAndPurity
import com.hartwig.actin.molecular.datamodel.pharmaco.PharmacoEntry
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Formats
import com.hartwig.actin.report.pdf.util.Styles
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Table
import java.util.function.Consumer

class MolecularCharacteristicsGenerator(private val molecular: MolecularRecord, private val width: Float) : TableGenerator {
    override fun title(): String {
        return "General"
    }

    override fun contents(): Table {
        val colWidth = width / 10
        val table = Tables.createFixedWidthCols(colWidth, colWidth, colWidth, colWidth, colWidth, colWidth, colWidth * 2, colWidth * 2)

        listOf("Purity", "TML Status", "TMB Status", "MS Stability", "HR Status", "DPYD", "UGT1A1").forEach(
            Consumer { title: String -> table.addHeaderCell(Cells.createHeader(title)) })

        listOf(
            createPurityCell(molecular.characteristics.purity),
            createTMLStatusCell(),
            createTMBStatusCell(),
            createMSStabilityCell(),
            createHRStatusCell(),
            createPeachSummaryForGeneCell(molecular.pharmaco, "DPYD"),
            createPeachSummaryForGeneCell(molecular.pharmaco, "UGT1A1")
        ).forEach { table.addCell(it) }

        return table
    }

    private fun createPurityCell(purity: Double?): Cell {
        if (!molecular.hasSufficientQuality) {
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

    private fun createTMLStatusString(): String? {
        val hasHighTumorMutationalLoad = molecular.characteristics.hasHighTumorMutationalLoad
        val tumorMutationalLoad = molecular.characteristics.tumorMutationalLoad
        return if (hasHighTumorMutationalLoad == null || tumorMutationalLoad == null) {
            null
        } else {
            String.format("%s (%d)", if (hasHighTumorMutationalLoad) "High" else "Low", tumorMutationalLoad)
        }
    }

    private fun createTMLStatusCell(): Cell {
        return createCellForCharacteristic(createTMLStatusString(), molecular.characteristics.hasHighTumorMutationalLoad)
    }

    private fun createTMBStatusCell(): Cell {
        if (!molecular.hasSufficientQuality) {
            return Cells.createContentWarn(Formats.VALUE_NOT_AVAILABLE)
        }
        val hasHighTumorMutationalBurden = molecular.characteristics.hasHighTumorMutationalBurden
        val tumorMutationalBurden = molecular.characteristics.tumorMutationalBurden
        if (hasHighTumorMutationalBurden == null || tumorMutationalBurden == null) {
            return Cells.createContentWarn(Formats.VALUE_UNKNOWN)
        }
        val interpretation = if (hasHighTumorMutationalBurden) "High" else "Low"
        val value = interpretation + " (" + Formats.singleDigitNumber(tumorMutationalBurden) + ")"
        val cell = if (hasSufficientQualityAndPurity(molecular)) Cells.createContent(value) else Cells.createContentWarn(value)
        if (hasHighTumorMutationalBurden) {
            cell.addStyle(Styles.tableHighlightStyle())
        }
        return cell
    }

    fun createMSStabilityString(): String? {
        return molecular.characteristics.isMicrosatelliteUnstable?.let { unstable -> if (unstable) "Unstable" else "Stable" }
    }

    private fun createMSStabilityCell(): Cell {
        return createCellForCharacteristic(createMSStabilityString(), molecular.characteristics.isMicrosatelliteUnstable)
    }

    fun createHRStatusString(): String? {
        val homologousRepairScore = molecular.characteristics.homologousRepairScore
        molecular.characteristics.isHomologousRepairDeficient ?: return null
        val statusInterpretation = if (molecular.characteristics.isHomologousRepairDeficient!!) "Deficient" else "Proficient"
        val scoreInterpretation = homologousRepairScore?.let { " (${Formats.twoDigitNumber(it)})" } ?: ""
        return statusInterpretation + scoreInterpretation
    }

    private fun createHRStatusCell(): Cell {
        return createCellForCharacteristic(createHRStatusString(), molecular.characteristics.isHomologousRepairDeficient)
    }

    private fun createCellForCharacteristic(summaryString: String?, shouldHighlight: Boolean?): Cell {
        return if (!molecular.hasSufficientQuality) {
            Cells.createContentWarn(Formats.VALUE_NOT_AVAILABLE)
        } else {
            summaryString?.let { value: String ->
                val cell = if (hasSufficientQualityAndPurity(molecular)) Cells.createContent(value) else Cells.createContentWarn(value)
                if (true == shouldHighlight) {
                    cell.addStyle(Styles.tableHighlightStyle())
                }
                cell
            } ?: Cells.createContentWarn(Formats.VALUE_UNKNOWN)
        }
    }

    private fun createPeachSummaryForGeneCell(pharmaco: Set<PharmacoEntry>, gene: String): Cell {
        return if (molecular.isContaminated) {
            Cells.createContentWarn(Formats.VALUE_NOT_AVAILABLE)
        } else {
            val pharmacoEntry = findPharmacoEntry(pharmaco, gene) ?: return Cells.createContentWarn(Formats.VALUE_UNKNOWN)
            Cells.createContent(pharmacoEntry.haplotypes.joinToString(", ") { "${it.name} (${it.function})" })
        }
    }

    companion object {
        private fun findPharmacoEntry(pharmaco: Set<PharmacoEntry>, geneToFind: String): PharmacoEntry? {
            return pharmaco.find { it.gene == geneToFind }
        }
    }
}