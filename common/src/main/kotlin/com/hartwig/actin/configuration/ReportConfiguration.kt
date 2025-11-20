package com.hartwig.actin.configuration

import com.hartwig.actin.datamodel.molecular.evidence.Country

enum class ReportContentType {
    NONE,
    BRIEF,
    COMPREHENSIVE
}

enum class MolecularChapterType {
    STANDARD,
    STANDARD_WITH_PATHOLOGY,
    LONGITUDINAL,
    STANDARD_AND_LONGITUDINAL
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
    DETAILED_ALL_TRIALS
}

data class ReportConfiguration(
    val patientDetailsType: ReportContentType = ReportContentType.COMPREHENSIVE,
    val clinicalSummaryType: ReportContentType = ReportContentType.COMPREHENSIVE,
    val molecularSummaryType: ReportContentType = ReportContentType.COMPREHENSIVE,
    val standardOfCareSummaryType: ReportContentType = ReportContentType.BRIEF,
    val trialMatchingSummaryType: ReportContentType = ReportContentType.COMPREHENSIVE,
    val molecularChapterType: MolecularChapterType = MolecularChapterType.STANDARD,
    val efficacyEvidenceChapterType: EfficacyEvidenceChapterType = EfficacyEvidenceChapterType.NONE,
    val clinicalChapterType: ClinicalChapterType = ClinicalChapterType.COMPLETE,
    val trialMatchingChapterType: TrialMatchingChapterType = TrialMatchingChapterType.STANDARD_ALL_TRIALS,
    val filterOnSOCExhaustionAndTumorType: Boolean = false,
    val countryOfReference: Country = Country.NETHERLANDS,
    val hospitalOfReference: String? = null
) {

    companion object {
        fun create(environmentConfigFile: String?): ReportConfiguration {
            return EnvironmentConfiguration.create(environmentConfigFile).report
        }

        fun extended(): ReportConfiguration {
            return ReportConfiguration(
                patientDetailsType = ReportContentType.COMPREHENSIVE,
                clinicalSummaryType = ReportContentType.COMPREHENSIVE,
                molecularSummaryType = ReportContentType.COMPREHENSIVE,
                standardOfCareSummaryType = ReportContentType.COMPREHENSIVE,
                trialMatchingSummaryType = ReportContentType.COMPREHENSIVE,
                molecularChapterType = MolecularChapterType.STANDARD_AND_LONGITUDINAL,
                efficacyEvidenceChapterType = EfficacyEvidenceChapterType.COMPLETE,
                clinicalChapterType = ClinicalChapterType.COMPLETE,
                trialMatchingChapterType = TrialMatchingChapterType.DETAILED_ALL_TRIALS
            )
        }
    }
}