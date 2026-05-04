package com.hartwig.actin.report

import com.hartwig.actin.PatientPrinter
import com.hartwig.actin.algo.serialization.TreatmentMatchJson
import com.hartwig.actin.algo.util.TreatmentMatchPrinter
import com.hartwig.actin.configuration.ClinicalChapterType
import com.hartwig.actin.configuration.EfficacyEvidenceChapterType
import com.hartwig.actin.configuration.MolecularChapterType
import com.hartwig.actin.configuration.ReportConfiguration
import com.hartwig.actin.configuration.ReportContentType
import com.hartwig.actin.configuration.TrialMatchingChapterType
import com.hartwig.actin.datamodel.molecular.evidence.Country
import com.hartwig.actin.doid.TestDoidModelFactory
import com.hartwig.actin.report.datamodel.Report
import com.hartwig.actin.report.datamodel.TestReportFactory
import com.hartwig.actin.report.pdf.ReportWriterFactory.createProductionReportWriter
import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.File

private val WORK_DIRECTORY = System.getProperty("user.dir")

object TestReportWriterApplication {

    private val logger = KotlinLogging.logger {}
    private val OPTIONAL_TREATMENT_MATCH_JSON = WORK_DIRECTORY + File.separator + "patient.treatment_match.json"

    fun createTestReport(): Report {
        val report = TestReportFactory.createExhaustiveTestReport()
        logger.info { "Printing patient record" }
        PatientPrinter.printRecord(report.patientRecord)

        val updated = if (File(OPTIONAL_TREATMENT_MATCH_JSON).exists()) {
            logger.info { "Loading treatment matches from $OPTIONAL_TREATMENT_MATCH_JSON" }
            val match = TreatmentMatchJson.read(OPTIONAL_TREATMENT_MATCH_JSON)
            report.copy(treatmentMatch = match)
        } else {
            report
        }

        logger.info { "Printing treatment match results" }
        TreatmentMatchPrinter.printMatch(updated.treatmentMatch)
        return updated
    }
}

fun main() {
    val report = TestReportWriterApplication.createTestReport()
    val writer = createProductionReportWriter(WORK_DIRECTORY)
    val reportConfiguration = ReportConfiguration().copy(
        patientDetailsType = ReportContentType.COMPREHENSIVE,
        clinicalSummaryType = ReportContentType.BRIEF,
        molecularSummaryType = ReportContentType.COMPREHENSIVE,
        standardOfCareSummaryType = ReportContentType.NONE,
        trialMatchingSummaryType = ReportContentType.COMPREHENSIVE,
        molecularChapterType = MolecularChapterType.STANDARD,
        efficacyEvidenceChapterType = EfficacyEvidenceChapterType.NONE,
        clinicalChapterType = ClinicalChapterType.COMPLETE,
        trialMatchingChapterType = TrialMatchingChapterType.STANDARD_ALL_TRIALS,
        countryOfReference = Country.NETHERLANDS,
        hospitalOfReference = null
    )

    writer.write(report, configuration = reportConfiguration, TestDoidModelFactory.createMinimalTestDoidModel(), addExtendedSuffix = false)
}