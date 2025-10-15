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

enum class EfficacyEvidenceChapterType {
    NONE,
    STANDARD_OF_CARE_ONLY,
    MOLECULAR_ONLY,
    COMPLETE
}

enum class ClinicalChapterType {
    NONE,
    COMPLETE
}

enum class TrialMatchingChapterType {
    NONE,
    STANDARD_ALL_TRIALS,
    STANDARD_EXTERNAL_TRIALS_ONLY,
    COMPREHENSIVE
}

data class ReportConfiguration(
    val patientDetailsType: ReportContentType = ReportContentType.COMPREHENSIVE,
    val clinicalSummaryType: ReportContentType = ReportContentType.COMPREHENSIVE,
    val molecularSummaryType: ReportContentType = ReportContentType.COMPREHENSIVE,
    val standardOfCareSummaryType: ReportContentType = ReportContentType.BRIEF,
    val trialMatchingSummaryType: ReportContentType = ReportContentType.COMPREHENSIVE,
    val molecularChapterType: MolecularChapterType = MolecularChapterType.DETAILED_WITHOUT_PATHOLOGY,
    val efficacyEvidenceChapterType: EfficacyEvidenceChapterType = EfficacyEvidenceChapterType.NONE,
    val clinicalChapterType: ClinicalChapterType = ClinicalChapterType.COMPLETE,
    val trialMatchingChapterType: TrialMatchingChapterType = TrialMatchingChapterType.STANDARD_ALL_TRIALS,
    val filterOnSOCExhaustionAndTumorType: Boolean = false,
    val includeEligibleButNoSlotsTableIfEmpty: Boolean = true,
    val countryOfReference: Country = Country.OTHER,
    val hospitalOfReference: String? = null
) {

    companion object {
        fun create(environmentConfigFile: String?): ReportConfiguration {
            return EnvironmentConfiguration.create(environmentConfigFile).report
        }
    }
}