package com.hartwig.actin.configuration

import org.apache.logging.log4j.LogManager

object EnvironmentConfigurationPrinter {

    private val LOGGER = LogManager.getLogger(EnvironmentConfigurationPrinter::class.java)

    fun printAlgoConfig(algo: AlgoConfiguration) {
        LOGGER.info(
            " Algo Config - " +
                    "trialSource: ${algo.trialSource}, " +
                    "warnIfToxicitiesNotFromQuestionnaire: ${algo.warnIfToxicitiesNotFromQuestionnaire}, " +
                    "maxMolecularTestAgeInDays: ${algo.maxMolecularTestAgeInDays}"
        )
    }

    fun printTrialConfig(trial: TrialConfiguration) {
        LOGGER.info(" Trial Config - ignoreAllNewTrialsInTrialStatusDatabase: ${trial.ignoreAllNewTrialsInTrialStatusDatabase}")
    }

    fun printReportConfig(report: ReportConfiguration) {
        LOGGER.info(
            " Report Config - " +
                    "includeOverviewWithClinicalHistorySummary: ${report.includeOverviewWithClinicalHistorySummary}, " +
                    "includeMolecularDetailsChapter: ${report.includeMolecularDetailsChapter}, " +
                    "includeIneligibleTrialsInSummary: ${report.includeIneligibleTrialsInSummary}, " +
                    "includeSOCLiteratureEfficacyEvidence: ${report.includeSOCLiteratureEfficacyEvidence}, " +
                    "includeEligibleSOCTreatmentSummary: ${report.includeEligibleSOCTreatmentSummary}, " +
                    "molecularSummaryType: ${report.molecularSummaryType}, " +
                    "includeOtherOncologicalHistoryInSummary: ${report.includeSOCLiteratureEfficacyEvidence}, " +
                    "includePatientHeader: ${report.includePatientHeader}, " +
                    "includeRelevantNonOncologicalHistoryInSummary: ${report.includeRelevantNonOncologicalHistoryInSummary}, " +
                    "includeApprovedTreatmentsInSummary: ${report.includeApprovedTreatmentsInSummary}, " +
                    "includeTrialMatchingInSummary: ${report.includeTrialMatchingInSummary}, " +
                    "includeEligibleButNoSlotsTableIfEmpty: ${report.includeEligibleButNoSlotsTableIfEmpty}, " +
                    "includeExternalTrialsInSummary: ${report.includeExternalTrialsInSummary}, " +
                    "filterOnSOCExhaustionAndTumorType: ${report.filterOnSOCExhaustionAndTumorType}, " +
                    "includeClinicalDetailsChapter: ${report.includeClinicalDetailsChapter}, " +
                    "includeTrialMatchingChapter: ${report.includeTrialMatchingChapter}, " +
                    "includeOnlyExternalTrialsInTrialMatching: ${report.includeOnlyExternalTrialsInTrialMatching}, " +
                    "includeLongitudinalMolecularChapter: ${report.includeLongitudinalMolecularChapter}, " +
                    "includeMolecularEvidenceChapter: ${report.includeMolecularEvidenceChapter}, " +
                    "includeRawPathologyReport: ${report.includeRawPathologyReport}, " +
                    "countryOfReference: ${report.countryOfReference}, " +
                    "reportDate: ${report.reportDate}"
        )
    }
}