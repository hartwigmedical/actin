package com.hartwig.actin.datamodel.molecular.evidence

import java.time.LocalDate

object TestTreatmentEvidenceFactory {

    fun approved() =
        create(
            treatment = "approved",
            isOnLabel = true,
            evidenceLevel = EvidenceLevel.A,
            evidenceLevelDetails = EvidenceLevelDetails.GUIDELINE,
            evidenceDirection = TestEvidenceDirectionFactory.certainPositiveResponse()
        )

    fun onLabelExperimental() =
        create(
            treatment = "on-label experimental",
            isOnLabel = true,
            evidenceLevel = EvidenceLevel.A,
            evidenceLevelDetails = EvidenceLevelDetails.CLINICAL_STUDY,
            evidenceDirection = TestEvidenceDirectionFactory.uncertainPositiveResponse()
        )

    fun offLabelExperimental() =
        create(
            treatment = "off-label experimental",
            isOnLabel = false,
            evidenceLevel = EvidenceLevel.B,
            evidenceLevelDetails = EvidenceLevelDetails.CLINICAL_STUDY,
            evidenceDirection = TestEvidenceDirectionFactory.certainPositiveResponse()
        )

    fun onLabelPreclinical() =
        create(
            treatment = "on-label pre-clinical",
            isOnLabel = true,
            evidenceLevel = EvidenceLevel.C,
            evidenceLevelDetails = EvidenceLevelDetails.PRECLINICAL,
            evidenceDirection = TestEvidenceDirectionFactory.uncertainPositiveResponse()
        )

    fun offLabelPreclinical() =
        create(
            treatment = "off-label pre-clinical",
            isOnLabel = false,
            evidenceLevel = EvidenceLevel.D,
            evidenceLevelDetails = EvidenceLevelDetails.PRECLINICAL,
            evidenceDirection = TestEvidenceDirectionFactory.uncertainPositiveResponse()
        )

    fun onLabelKnownResistant() =
        create(
            treatment = "on-label known resistant",
            isOnLabel = true,
            evidenceLevel = EvidenceLevel.A,
            evidenceLevelDetails = EvidenceLevelDetails.GUIDELINE,
            evidenceDirection = TestEvidenceDirectionFactory.certainResistant()
        )

    fun offLabelKnownResistant() =
        create(
            treatment = "off-label known resistant",
            isOnLabel = false,
            evidenceLevel = EvidenceLevel.A,
            evidenceLevelDetails = EvidenceLevelDetails.GUIDELINE,
            evidenceDirection = TestEvidenceDirectionFactory.certainResistant()
        )

    fun onLabelSuspectResistant() =
        create(
            treatment = "on-label suspect resistant",
            isOnLabel = true,
            evidenceLevel = EvidenceLevel.C,
            evidenceLevelDetails = EvidenceLevelDetails.GUIDELINE,
            evidenceDirection = TestEvidenceDirectionFactory.uncertainResistant()
        )

    fun offLabelSuspectResistant() =
        create(
            treatment = "off-label suspect resistant",
            isOnLabel = false,
            evidenceLevel = EvidenceLevel.C,
            evidenceLevelDetails = EvidenceLevelDetails.GUIDELINE,
            evidenceDirection = TestEvidenceDirectionFactory.uncertainResistant()
        )

    fun create(
        treatment: String,
        isOnLabel: Boolean,
        sourceDate: LocalDate = LocalDate.of(2021, 2, 3),
        sourceEvent: String = "",
        evidenceType: EvidenceType = EvidenceType.ACTIVATION,
        matchedCancerType: String = "",
        excludedCancerSubTypes: Set<String> = emptySet(),
        evidenceLevel: EvidenceLevel,
        evidenceLevelDetails: EvidenceLevelDetails,
        evidenceDirection: EvidenceDirection,
        evidenceYear: Int = 2021,
        sourceUrl: String? = null
    ) = TreatmentEvidence(
        treatment = treatment,
        molecularMatch = MolecularMatchDetails(
            sourceDate = sourceDate,
            sourceEvent = sourceEvent,
            sourceEvidenceType = evidenceType,
            sourceUrl = sourceUrl
        ),
        applicableCancerType = CancerType(matchedCancerType, excludedCancerSubTypes = excludedCancerSubTypes),
        isOnLabel = isOnLabel,
        evidenceLevel = evidenceLevel,
        evidenceLevelDetails = evidenceLevelDetails,
        evidenceDirection = evidenceDirection,
        evidenceYear = evidenceYear,
        efficacyDescription = "efficacy description"
    )
}