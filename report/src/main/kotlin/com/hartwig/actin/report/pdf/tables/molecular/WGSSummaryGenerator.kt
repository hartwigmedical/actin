package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.clinical.PathologyReport
import com.hartwig.actin.datamodel.molecular.MolecularRecord
import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.report.interpretation.InterpretedCohort
import com.hartwig.actin.report.interpretation.MolecularDriversSummarizer
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.util.Formats.date
import com.itextpdf.layout.element.Table

class WGSSummaryGenerator(
    private val isShort: Boolean,
    private val patientRecord: PatientRecord,
    private val molecular: MolecularTest,
    private val pathologyReport: PathologyReport?,
    cohorts: List<InterpretedCohort>,
    private val keyWidth: Float,
    private val valueWidth: Float,
) : TableGenerator {

    private val summarizer: MolecularDriversSummarizer =
        MolecularDriversSummarizer.fromMolecularDriversAndEvaluatedCohorts(molecular.drivers, cohorts)
    private val wgsMolecular = molecular as? MolecularRecord

    override fun title(): String {
        val title = molecular.testTypeDisplay ?: molecular.experimentType.display()
        val suffix = pathologyReport?.let { "" } ?: " (${date(molecular.date)})"
        return "$title$suffix"
    }

    override fun forceKeepTogether(): Boolean {
        return true
    }

    override fun contents(): Table {
        return WGSSummaryGeneratorFunctions.createMolecularSummaryTable(
            isShort, patientRecord, molecular, wgsMolecular, keyWidth, valueWidth, summarizer
        )
    }
}