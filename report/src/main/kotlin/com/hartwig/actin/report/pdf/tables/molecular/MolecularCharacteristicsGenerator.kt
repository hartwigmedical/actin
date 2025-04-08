package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.datamodel.molecular.MolecularRecord
import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.datamodel.molecular.pharmaco.PharmacoEntry
import com.hartwig.actin.datamodel.molecular.pharmaco.PharmacoGene
import com.hartwig.actin.report.interpretation.MolecularCharacteristicFormat
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Formats
import com.hartwig.actin.report.pdf.util.Styles
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Table
import java.util.function.Consumer

class MolecularCharacteristicsGenerator(private val molecular: MolecularTest, private val width: Float) : TableGenerator {

    private val wgsMolecular = molecular as? MolecularRecord

    override fun title(): String {
        return "General"
    }

    override fun contents(): Table {
        val colWidth = width / 10
        val table = Tables.createFixedWidthCols(colWidth, colWidth, colWidth, colWidth, colWidth, colWidth, colWidth * 2, colWidth * 2)

        listOf("Purity", "Ploidy", "TML Status", "TMB Status", "MS Stability", "HR Status", "DPYD", "UGT1A1").forEach(
            Consumer { title: String -> table.addHeaderCell(Cells.createHeader(title)) })
        listOfNotNull(
            createPurityCell(molecular.characteristics.purity),
            createPloidyCell(molecular.characteristics.ploidy),
            createTMLStatusCell(),
            createTMBStatusCell(),
            createMSStabilityCell(),
            createHRStatusCell(),
            wgsMolecular?.let { Cells.createContent(createPeachSummaryForGene(it.pharmaco, PharmacoGene.DPYD)) } ?: Cells.createEmpty(),
            wgsMolecular?.let { Cells.createContent(createPeachSummaryForGene(it.pharmaco, PharmacoGene.UGT1A1)) } ?: Cells.createEmpty()
        ).forEach { table.addCell(it) }

        return table
    }

    private fun createPurityCell(purity: Double?): Cell {
        if (insufficientQuality()) {
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

    private fun createPloidyCell(ploidy: Double?): Cell {
        return if (ploidy == null) {
            Cells.createContentWarn(Formats.VALUE_UNKNOWN)
        } else {
            Cells.createContent(Formats.singleDigitNumber(ploidy))
        }
    }

    private fun createTMLStatusString(): String? {
        val tumorMutationalLoad = molecular.characteristics.tumorMutationalLoad?.score
        val hasHighTumorMutationalLoad = molecular.characteristics.tumorMutationalLoad?.isHigh
        return if (hasHighTumorMutationalLoad == null || tumorMutationalLoad == null) {
            null
        } else {
            MolecularCharacteristicFormat.formatHighLowCharacteristic(tumorMutationalLoad, hasHighTumorMutationalLoad)
        }
    }

    private fun createTMLStatusCell(): Cell {
        return createCellForCharacteristic(createTMLStatusString(), molecular.characteristics.tumorMutationalLoad?.isHigh)
    }

    private fun createTMBStatusCell(): Cell {
        if (insufficientQuality()) {
            return Cells.createContentWarn(Formats.VALUE_NOT_AVAILABLE)
        }
        val tumorMutationalBurden = molecular.characteristics.tumorMutationalBurden?.score
        val hasHighTumorMutationalBurden = molecular.characteristics.tumorMutationalBurden?.isHigh
        if (hasHighTumorMutationalBurden == null || tumorMutationalBurden == null) {
            return Cells.createContentWarn(Formats.VALUE_UNKNOWN)
        }
        val value = MolecularCharacteristicFormat.formatHighLowCharacteristic(tumorMutationalBurden, hasHighTumorMutationalBurden)
        val cell = if (wgsMolecular?.hasSufficientQualityAndPurity() == true) {
            Cells.createContent(value)
        } else {
            Cells.createContentWarn(value)
        }
        if (hasHighTumorMutationalBurden) {
            cell.addStyle(Styles.tableHighlightStyle())
        }
        return cell
    }

    fun createMSStabilityString(): String {
        return MolecularCharacteristicFormat.formatMicrosatelliteStability(molecular.characteristics)
    }

    private fun createMSStabilityCell(): Cell {
        return createCellForCharacteristic(createMSStabilityString(), molecular.characteristics.microsatelliteStability?.isUnstable)
    }

    fun createHRStatusString(): String {
        return MolecularCharacteristicFormat.formatHomologousRecombination(molecular.characteristics)
    }

    private fun createHRStatusCell(): Cell {
        return createCellForCharacteristic(createHRStatusString(), molecular.characteristics.homologousRecombination?.isDeficient)
    }

    private fun createCellForCharacteristic(summaryString: String?, shouldHighlight: Boolean?): Cell {
        return if (insufficientQuality()) {
            Cells.createContentWarn(Formats.VALUE_NOT_AVAILABLE)
        } else {
            summaryString?.let { value: String ->
                val cell =
                    if (wgsMolecular?.hasSufficientQualityAndPurity() == true)
                        Cells.createContent(value)
                    else
                        Cells.createContentWarn(value)
                if (true == shouldHighlight) {
                    cell.addStyle(Styles.tableHighlightStyle())
                }
                cell
            } ?: Cells.createContentWarn(Formats.VALUE_UNKNOWN)
        }
    }

    private fun createPeachSummaryForGene(pharmaco: Set<PharmacoEntry>, gene: PharmacoGene): String {
        val wgsMolecular = if (molecular is MolecularRecord) molecular else null
        if (wgsMolecular?.isContaminated == true) {
            return Formats.VALUE_NOT_AVAILABLE
        } else {
            val pharmacoEntry = findPharmacoEntry(pharmaco, gene) ?: return Formats.VALUE_UNKNOWN
            return pharmacoEntry.haplotypes.joinToString(", ") { "${it.toHaplotypeString()} (${it.function.display()})" }
        }
    }

    private fun insufficientQuality() = wgsMolecular?.hasSufficientQuality == false

    private fun findPharmacoEntry(pharmaco: Set<PharmacoEntry>, geneToFind: PharmacoGene): PharmacoEntry? {
        return pharmaco.find { it.gene == geneToFind }
    }
}