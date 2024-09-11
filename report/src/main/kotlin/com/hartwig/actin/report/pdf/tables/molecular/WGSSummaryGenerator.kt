package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.molecular.MolecularRecord
import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.report.interpretation.EvaluatedCohort
import com.hartwig.actin.report.interpretation.MolecularDriversSummarizer
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.itextpdf.layout.element.Table

class WGSSummaryGenerator(
    private val isIncludedInTrialMatching: Boolean,
    private val isShort: Boolean,
    private val patientRecord: PatientRecord,
    private val molecular: MolecularTest,
    cohorts: List<EvaluatedCohort>,
    private val keyWidth: Float,
    private val valueWidth: Float
) : TableGenerator {
    private val summarizer: MolecularDriversSummarizer =
        MolecularDriversSummarizer.fromMolecularDriversAndEvaluatedCohorts(molecular.drivers, cohorts)
    private val wgsMolecular = molecular as? MolecularRecord

    override fun title(): String {
        return WGSSummaryGeneratorFunctions.createMolecularSummaryTitle(molecular, isIncludedInTrialMatching)
    }

    override fun contents(): Table {
        return WGSSummaryGeneratorFunctions.createMolecularSummaryTable(
            isShort, patientRecord, molecular, wgsMolecular, keyWidth, valueWidth, summarizer
        )
    }
}