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

data class ReportConfiguration(
    val patientDetailsType: ReportContentType = ReportContentType.COMPREHENSIVE,
    val clinicalSummaryType: ReportContentType = ReportContentType.COMPREHENSIVE,
    val molecularSummaryType: ReportContentType = ReportContentType.COMPREHENSIVE,
    val standardOfCareSummaryType: ReportContentType = ReportContentType.BRIEF,
    val trialMatchingSummaryType: ReportContentType = ReportContentType.COMPREHENSIVE,
    val molecularChapterType: MolecularChapterType = MolecularChapterType.DETAILED_WITHOUT_PATHOLOGY,
    val efficacyEvidenceChapterType: EfficacyEvidenceChapterType = EfficacyEvidenceChapterType.NONE,
    val clinicalChapterType: ClinicalChapterType = ClinicalChapterType.COMPLETE,
    val filterOnSOCExhaustionAndTumorType: Boolean = false,
    val includeEligibleButNoSlotsTableIfEmpty: Boolean = true,
    val includeTrialMatchingChapter: Boolean = true,
    val includeOnlyExternalTrialsInTrialMatching: Boolean = false,
    val countryOfReference: Country = Country.OTHER,
    val hospitalOfReference: String? = null
) {

    companion object {
        fun create(environmentConfigFile: String?): ReportConfiguration {
            return EnvironmentConfiguration.create(environmentConfigFile).report
        }
    }
}