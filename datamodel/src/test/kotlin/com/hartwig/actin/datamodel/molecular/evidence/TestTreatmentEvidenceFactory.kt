package com.hartwig.actin.datamodel.molecular.evidence

import java.time.LocalDate

const val SOURCE_EVENT_URL: String = "sourceEventUrl"

object TestTreatmentEvidenceFactory {

    fun approved() =
        create(
            treatment = "approved",
            cancerTypeMatchApplicability = CancerTypeMatchApplicability.SPECIFIC_TYPE,
            evidenceLevel = EvidenceLevel.A,
            evidenceLevelDetails = EvidenceLevelDetails.GUIDELINE,
            evidenceDirection = TestEvidenceDirectionFactory.certainPositiveResponse()
        )

    fun onLabelExperimental() =
        create(
            treatment = "on-label experimental",
            cancerTypeMatchApplicability = CancerTypeMatchApplicability.SPECIFIC_TYPE,
            evidenceLevel = EvidenceLevel.A,
            evidenceLevelDetails = EvidenceLevelDetails.CLINICAL_STUDY,
            evidenceDirection = TestEvidenceDirectionFactory.uncertainPositiveResponse()
        )

    fun offLabelExperimental() =
        create(
            treatment = "off-label experimental",
            cancerTypeMatchApplicability = CancerTypeMatchApplicability.OTHER_TYPE,
            evidenceLevel = EvidenceLevel.B,
            evidenceLevelDetails = EvidenceLevelDetails.CLINICAL_STUDY,
            evidenceDirection = TestEvidenceDirectionFactory.certainPositiveResponse()
        )

    fun onLabelPreclinical() =
        create(
            treatment = "on-label pre-clinical",
            cancerTypeMatchApplicability = CancerTypeMatchApplicability.SPECIFIC_TYPE,
            evidenceLevel = EvidenceLevel.C,
            evidenceLevelDetails = EvidenceLevelDetails.PRECLINICAL,
            evidenceDirection = TestEvidenceDirectionFactory.uncertainPositiveResponse()
        )

    fun offLabelPreclinical() =
        create(
            treatment = "off-label pre-clinical",
            cancerTypeMatchApplicability = CancerTypeMatchApplicability.OTHER_TYPE,
            evidenceLevel = EvidenceLevel.D,
            evidenceLevelDetails = EvidenceLevelDetails.PRECLINICAL,
            evidenceDirection = TestEvidenceDirectionFactory.uncertainPositiveResponse()
        )

    fun onLabelKnownResistant() =
        create(
            treatment = "on-label known resistant",
            cancerTypeMatchApplicability = CancerTypeMatchApplicability.SPECIFIC_TYPE,
            evidenceLevel = EvidenceLevel.A,
            evidenceLevelDetails = EvidenceLevelDetails.GUIDELINE,
            evidenceDirection = TestEvidenceDirectionFactory.certainResistant()
        )

    fun offLabelKnownResistant() =
        create(
            treatment = "off-label known resistant",
            cancerTypeMatchApplicability = CancerTypeMatchApplicability.OTHER_TYPE,
            evidenceLevel = EvidenceLevel.A,
            evidenceLevelDetails = EvidenceLevelDetails.GUIDELINE,
            evidenceDirection = TestEvidenceDirectionFactory.certainResistant()
        )

    fun onLabelSuspectResistant() =
        create(
            treatment = "on-label suspect resistant",
            cancerTypeMatchApplicability = CancerTypeMatchApplicability.SPECIFIC_TYPE,
            evidenceLevel = EvidenceLevel.C,
            evidenceLevelDetails = EvidenceLevelDetails.GUIDELINE,
            evidenceDirection = TestEvidenceDirectionFactory.uncertainResistant()
        )

    fun offLabelSuspectResistant() =
        create(
            treatment = "off-label suspect resistant",
            cancerTypeMatchApplicability = CancerTypeMatchApplicability.OTHER_TYPE,
            evidenceLevel = EvidenceLevel.C,
            evidenceLevelDetails = EvidenceLevelDetails.GUIDELINE,
            evidenceDirection = TestEvidenceDirectionFactory.uncertainResistant()
        )

    fun create(
        treatment: String,
        cancerTypeMatchApplicability: CancerTypeMatchApplicability,
        sourceDate: LocalDate = LocalDate.of(2021, 2, 3),
        sourceEvent: String = "",
        evidenceType: EvidenceType = EvidenceType.DELETION,
        matchedCancerType: String = "",
        excludedCancerSubTypes: Set<String> = emptySet(),
        evidenceLevel: EvidenceLevel,
        evidenceLevelDetails: EvidenceLevelDetails,
        evidenceDirection: EvidenceDirection,
        evidenceYear: Int = 2021,
        sourceUrl: String = SOURCE_EVENT_URL
    ) = TreatmentEvidence(
        treatment = treatment,
        molecularMatch = TestMolecularMatchDetailsFactory.create(
            sourceDate = sourceDate,
            sourceEvent = sourceEvent,
            sourceEvidenceType = evidenceType,
            sourceUrl = sourceUrl
        ),
        cancerTypeMatch = CancerTypeMatchDetails(
            CancerType(matchedCancerType, excludedCancerSubTypes = excludedCancerSubTypes),
            cancerTypeMatchApplicability
        ),
        evidenceLevel = evidenceLevel,
        evidenceLevelDetails = evidenceLevelDetails,
        evidenceDirection = evidenceDirection,
        evidenceYear = evidenceYear,
        efficacyDescription = "efficacy description",
        emptySet()
    )
}