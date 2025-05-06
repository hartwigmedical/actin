package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.datamodel.clinical.PathologyReport
import com.hartwig.actin.datamodel.molecular.MolecularRecord
import com.hartwig.actin.report.interpretation.InterpretedCohort
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.tables.TableGeneratorFunctions
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Formats
import com.hartwig.actin.report.pdf.util.Formats.date
import com.hartwig.actin.report.pdf.util.Tables
import com.hartwig.actin.report.trial.ExternalTrialSummary
import com.itextpdf.layout.element.Table

class OrangeMolecularRecordGenerator(
    private val trials: Set<ExternalTrialSummary>,
    private val cohorts: List<InterpretedCohort>,
    private val width: Float,
    private val molecular: MolecularRecord,
    private val pathologyReport: PathologyReport?
) : TableGenerator {

    override fun title(): String {
        val title = "${molecular.experimentType.display()} (${molecular.sampleId}"
        val suffix = if (pathologyReport?.tissueId.isNullOrBlank()) ", ${date(molecular.date)})" else ")"
        return "$title$suffix"
    }

    override fun forceKeepTogether(): Boolean {
        return false
    }

    override fun contents(): Table {

        val table = Tables.createSingleColWithWidth(width)

        if (molecular.hasSufficientQualityButLowPurity()) {
            val purityString = molecular.characteristics.purity?.let { Formats.percentage(it) } ?: "NA"
            table.addCell(
                Cells.createContentNoBorder(
                    ("Low tumor purity (${purityString}) indicating that potential (subclonal) " +
                            "DNA aberrations might not have been detected & predicted tumor origin results may be less reliable")
                )
            )
        }

        val generators = listOf(MolecularCharacteristicsGenerator(molecular)) + tumorDetailsGenerators(molecular, cohorts, trials)
        TableGeneratorFunctions.addGenerators(generators, table, overrideTitleFormatToSubtitle = true)

        if (!molecular.hasSufficientQuality) {
            table.addCell(
                Cells.createContent(
                    ("No successful OncoAct WGS and/or tumor NGS panel could be "
                            + "performed on the submitted biopsy (insufficient quality for reporting)")
                )
            )
        }

        return table
    }

    private fun tumorDetailsGenerators(
        molecular: MolecularRecord,
        evaluated: List<InterpretedCohort>,
        trials: Set<ExternalTrialSummary>
    ): List<TableGenerator> {
        return if (molecular.hasSufficientQuality) {
            listOf(
                PredictedTumorOriginGenerator(molecular),
                MolecularDriversGenerator(molecular, evaluated, trials)
            )
        } else emptyList()
    }
}





