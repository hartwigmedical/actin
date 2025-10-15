package com.hartwig.actin.configuration

import com.hartwig.actin.datamodel.molecular.evidence.Country

enum class ReportContentType {
    NONE,
    BRIEF,
    COMPREHENSIVE
}

enum class MolecularChapterType {
    DETAILED_WITHOUT_PATHOLOGY,
    DETAILED_WITH_PATHOLOGY,
    LONGITUDINAL
}

data class ReportConfiguration(
    val patientDetailsType: ReportContentType = ReportContentType.COMPREHENSIVE,
    val clinicalSummaryType: ReportContentType = ReportContentType.COMPREHENSIVE,
    val molecularSummaryType: ReportContentType = ReportContentType.COMPREHENSIVE,
    val approvedTreatmentSummaryType: ReportContentType = ReportContentType.BRIEF,
    val trialMatchingSummaryType: ReportContentType = ReportContentType.COMPREHENSIVE,
    val molecularChapterType: MolecularChapterType = MolecularChapterType.DETAILED_WITHOUT_PATHOLOGY,
    val includeSOCLiteratureEfficacyEvidence: Boolean = false,
    val includeEligibleButNoSlotsTableIfEmpty: Boolean = true,
    val filterOnSOCExhaustionAndTumorType: Boolean = false,
    val includeClinicalDetailsChapter: Boolean = true,
    val includeTrialMatchingChapter: Boolean = true,
    val includeOnlyExternalTrialsInTrialMatching: Boolean = false,
    val includeMolecularEvidenceChapter: Boolean = false,
    val includeTreatmentEvidenceRanking: Boolean = false,
    val countryOfReference: Country = Country.OTHER,
    val hospitalOfReference: String? = null
) {

    companion object {
        fun create(environmentConfigFile: String?): ReportConfiguration {
            return EnvironmentConfiguration.create(environmentConfigFile).report
        }
    }
}