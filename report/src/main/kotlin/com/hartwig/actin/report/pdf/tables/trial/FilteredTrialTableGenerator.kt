package com.hartwig.actin.report.pdf.tables.trial

import com.hartwig.actin.datamodel.molecular.evidence.Country
import com.hartwig.actin.report.pdf.tables.trial.TrialGeneratorFunctions.externalTrialLocation
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Tables
import com.hartwig.actin.report.pdf.util.Tables.makeWrapping
import com.hartwig.actin.report.trial.ExternalTrialSummary
import com.itextpdf.layout.element.Table

class FilteredTrialTableGenerator(
    private val trials: Set<ExternalTrialSummary>,
    private val homeCountry: Country? = null,
    private val width: Float
) : TrialTableGenerator {

    override fun getCohortSize(): Int {
        return 0
    }

    override fun title() =
        "Filtered trials potentially eligible based on molecular results which are potentially recruiting (${trials.size})"

    override fun contents(): Table {
        val table = Tables.createFixedWidthCols(width / 9, width / 4, width / 7, width / 2)
        if (trials.isNotEmpty()) {
            listOf("Trial", "Cohort", "Molecular", "Sites").map(Cells::createHeader).forEach(table::addHeaderCell)
            trials.forEach { trial ->
                val trialLabelText = trial.title.takeIf { it.length < 20 } ?: trial.nctId
                val country = if (trial.countries.none { it.country == Country.NETHERLANDS }) null else homeCountry

                listOf(
                    trialLabelText,
                    trial.sourceMolecularEvents.joinToString(", "),
                    trial.actinMolecularEvents.joinToString(", "),
                    externalTrialLocation(trial, country)
                )
                    .map(Cells::createContent).forEach(table::addCell)
            }
            table.addCell(
                Cells.createSpanningSubNote(
                    "Trials were filtered due to eligible local trials for the same molecular target or trial for young adult patients.",
                    table
                )
            )
        }
        return makeWrapping(table)
    }
}