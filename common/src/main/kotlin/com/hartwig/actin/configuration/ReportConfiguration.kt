package com.hartwig.actin.configuration

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.datamodel.molecular.evidence.Country

enum class ReportContentType {
    NONE,
    BRIEF,
    COMPREHENSIVE
}

enum class MolecularChapterType {
    STANDARD,
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
    STANDARD_EXTERNAL_TRIALS_ONLY
}

enum class ReportIntendedUse {
    RESEARCH_USE_ONLY,
    NON_MEDICAL
}

enum class ExternalTrialTumorType(val tumorDoids: Set<String>?) {
    LUNG(setOf(DoidConstants.LUNG_CANCER_DOID, DoidConstants.PLEURAL_MESOTHELIOMA_DOID)),
    NONE(null);
}

data class ReportConfiguration(
    val patientDetailsType: ReportContentType = ReportContentType.COMPREHENSIVE,
    val clinicalSummaryType: ReportContentType = ReportContentType.COMPREHENSIVE,
    val molecularSummaryType: ReportContentType = ReportContentType.COMPREHENSIVE,
    val standardOfCareSummaryType: ReportContentType = ReportContentType.NONE,
    val trialMatchingSummaryType: ReportContentType = ReportContentType.COMPREHENSIVE,
    val molecularChapterType: MolecularChapterType = MolecularChapterType.STANDARD,
    val efficacyEvidenceChapterType: EfficacyEvidenceChapterType = EfficacyEvidenceChapterType.NONE,
    val clinicalChapterType: ClinicalChapterType = ClinicalChapterType.COMPLETE,
    val trialMatchingChapterType: TrialMatchingChapterType = TrialMatchingChapterType.STANDARD_ALL_TRIALS,
    val filterOnSOCExhaustionAndTumorType: Boolean = false,
    val countryOfReference: Country = Country.NETHERLANDS,
    val hospitalOfReference: String? = null,
    val dutchExternalTrialsToExclude: ExternalTrialTumorType = ExternalTrialTumorType.NONE,
    val intendedUse: ReportIntendedUse = ReportIntendedUse.RESEARCH_USE_ONLY
) {

    companion object {
        fun create(environmentConfigFile: String?): ReportConfiguration {
            return EnvironmentConfiguration.create(environmentConfigFile).report
        }
    }
}