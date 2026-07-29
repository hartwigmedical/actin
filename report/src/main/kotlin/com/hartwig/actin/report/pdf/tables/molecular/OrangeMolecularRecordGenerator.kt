package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.datamodel.clinical.PathologyReport
import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.report.interpretation.InterpretedCohort
import com.hartwig.actin.report.interpretation.MolecularDriversSummarizer
import com.hartwig.actin.report.pdf.ReportLabels
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.tables.TableGeneratorFunctions
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Formats
import com.hartwig.actin.report.pdf.util.Formats.date
import com.hartwig.actin.report.pdf.util.Tables
import com.hartwig.actin.report.trial.ActionableWithExternalTrial
import com.itextpdf.layout.element.Table

class OrangeMolecularRecordGenerator(
    private val trials: Set<ActionableWithExternalTrial>,
    private val cohorts: List<InterpretedCohort>,
    private val width: Float,
    private val molecular: MolecularTest,
    private val pathologyReport: PathologyReport?,
    private val labels: ReportLabels
) : TableGenerator {

    override fun title(): String {
        val title = "${molecular.experimentType.display()} (${molecular.sampleId}"
        val suffix = pathologyReport?.let { ")" } ?: ", ${date(molecular.date)})"
        return "$title$suffix"
    }

    override fun forceKeepTogether(): Boolean {
        return false
    }

    override fun contents(): Table {
        val table = Tables.createSingleColWithWidth(width)

        if (molecular.hasSufficientQualityButLowPurity()) {
            val purityString = molecular.characteristics.purity?.let { Formats.percentage(it) } ?: labels.miscNotAvailable()
            table.addCell(
                Cells.createContentNoBorder(labels.molecularLowPurity(purityString))
            )
        }

        if (molecular.targetSpecification?.testVersion?.testDateIsBeforeOldestTestVersion == true) {
            table.addCell(
                Cells.createSpanningSubNote(
                    labels.molecularOldTestVersion(
                        molecular.date.toString(),
                        molecular.targetSpecification?.testVersion?.versionDate!!.toString()
                    ),
                    table
                )
            )
        }

        val generators = listOf(MolecularCharacteristicsGenerator(molecular, labels)) + tumorDetailsGenerators(molecular, cohorts, trials)
        TableGeneratorFunctions.addGenerators(generators, table, overrideTitleFormatToSubtitle = true, skipWrappingFooter = true)

        if (!molecular.hasSufficientQuality) {
            table.addCell(
                Cells.createContent(labels.molecularNoWgs())
            )
        }

        return table
    }

    private fun tumorDetailsGenerators(
        molecular: MolecularTest,
        evaluated: List<InterpretedCohort>,
        trials: Set<ActionableWithExternalTrial>
    ): List<TableGenerator> {
        return if (molecular.hasSufficientQuality) {
            listOf(
                PredictedTumorOriginGenerator(molecular, labels),
                MolecularDriversGenerator(
                    molecular.copy(drivers = MolecularDriversSummarizer.filterDriversByDriverLikelihood(molecular.drivers, true)),
                    evaluated,
                    trials,
                    labels.molecularKeyDrivers(),
                    labels
                ),
                MolecularDriversGenerator(
                    molecular.copy(drivers = MolecularDriversSummarizer.filterDriversByDriverLikelihood(molecular.drivers, false)),
                    evaluated,
                    trials,
                    labels.molecularOtherDrivers(),
                    labels
                )
            )
        } else emptyList()
    }
}
