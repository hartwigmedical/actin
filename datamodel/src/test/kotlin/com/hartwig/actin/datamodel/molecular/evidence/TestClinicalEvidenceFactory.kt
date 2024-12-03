package com.hartwig.actin.datamodel.molecular.evidence

import java.time.LocalDate

object TestClinicalEvidenceFactory {

    fun createEmpty(): ClinicalEvidence {
        return ClinicalEvidence(treatmentEvidence = emptySet(), eligibleTrials = emptySet())
    }

    fun withEvidence(treatmentEvidence: TreatmentEvidence): ClinicalEvidence {
        return ClinicalEvidence(treatmentEvidence = setOf(treatmentEvidence), eligibleTrials = emptySet())
    }

    fun withEligibleTrial(eligibleTrial: ExternalTrial): ClinicalEvidence {
        return withEligibleTrials(setOf(eligibleTrial))
    }

    fun withEligibleTrials(eligibleTrials: Set<ExternalTrial>): ClinicalEvidence {
        return ClinicalEvidence(treatmentEvidence = emptySet(), eligibleTrials = eligibleTrials)
    }

    fun createExhaustive(): ClinicalEvidence {
        return ClinicalEvidence(
            treatmentEvidence = setOf(
                approved(),
                onLabelExperimental(),
                offLabelExperimental(),
                onLabelPreclinical(),
                offLabelPreclinical(),
                onLabelKnownResistant(),
                offLabelKnownResistant(),
                onLabelSuspectResistant(),
                offLabelSuspectResistant(),
            ),
            eligibleTrials = setOf(createTestExternalTrial()),
        )
    }

    fun approved() =
        evidence(
            treatment = "approved",
            isOnLabel = true,
            evidenceLevel = EvidenceLevel.A,
            evidenceLevelDetails = EvidenceLevelDetails.GUIDELINE,
            evidenceDirection = EvidenceDirection(hasPositiveResponse = true, isCertain = true)
        )

    fun onLabelExperimental() =
        evidence(
            treatment = "on-label experimental",
            isOnLabel = true,
            evidenceLevel = EvidenceLevel.A,
            evidenceLevelDetails = EvidenceLevelDetails.CLINICAL_STUDY,
            evidenceDirection = EvidenceDirection(hasPositiveResponse = true, isCertain = false)
        )

    fun offLabelExperimental() =
        evidence(
            treatment = "off-label experimental",
            isOnLabel = false,
            evidenceLevel = EvidenceLevel.B,
            evidenceLevelDetails = EvidenceLevelDetails.CLINICAL_STUDY,
            evidenceDirection = EvidenceDirection(hasPositiveResponse = true, isCertain = true)
        )

    fun onLabelPreclinical() =
        evidence(
            treatment = "on-label pre-clinical",
            isOnLabel = true,
            evidenceLevel = EvidenceLevel.C,
            evidenceLevelDetails = EvidenceLevelDetails.PRECLINICAL,
            evidenceDirection = EvidenceDirection(hasPositiveResponse = true)
        )

    fun offLabelPreclinical() =
        evidence(
            treatment = "off-label pre-clinical",
            isOnLabel = false,
            evidenceLevel = EvidenceLevel.D,
            evidenceLevelDetails = EvidenceLevelDetails.PRECLINICAL,
            evidenceDirection = EvidenceDirection(hasPositiveResponse = true)
        )

    fun onLabelKnownResistant() =
        evidence(
            treatment = "on-label known resistant",
            isOnLabel = true,
            evidenceLevel = EvidenceLevel.A,
            evidenceLevelDetails = EvidenceLevelDetails.GUIDELINE,
            evidenceDirection = EvidenceDirection(isResistant = true, isCertain = true)
        )

    fun offLabelKnownResistant() =
        evidence(
            treatment = "off-label known resistant",
            isOnLabel = false,
            evidenceLevel = EvidenceLevel.A,
            evidenceLevelDetails = EvidenceLevelDetails.GUIDELINE,
            evidenceDirection = EvidenceDirection(isResistant = true, isCertain = true)
        )

    fun onLabelSuspectResistant() =
        evidence(
            treatment = "on-label suspect resistant",
            isOnLabel = true,
            evidenceLevel = EvidenceLevel.C,
            evidenceLevelDetails = EvidenceLevelDetails.GUIDELINE,
            evidenceDirection = EvidenceDirection(isResistant = true, isCertain = false)
        )

    fun offLabelSuspectResistant() =
        evidence(
            treatment = "off-label suspect resistant",
            isOnLabel = false,
            evidenceLevel = EvidenceLevel.C,
            evidenceLevelDetails = EvidenceLevelDetails.GUIDELINE,
            evidenceDirection = EvidenceDirection(isResistant = true, isCertain = false)
        )

    fun evidence(
        treatment: String,
        isOnLabel: Boolean,
        isCategoryEvent: Boolean = false,
        evidenceLevel: EvidenceLevel,
        evidenceLevelDetails: EvidenceLevelDetails,
        evidenceDirection: EvidenceDirection
    ) = TreatmentEvidence(
        treatment = treatment,
        molecularMatch = MolecularMatchDetails(sourceEvent = "", isCategoryEvent = isCategoryEvent),
        applicableCancerType = createEmptyApplicableCancerType(),
        isOnLabel = isOnLabel,
        evidenceLevel = evidenceLevel,
        evidenceLevelDetails = evidenceLevelDetails,
        evidenceDirection = evidenceDirection,
        evidenceDate = LocalDate.of(2021, 2, 3),
        evidenceYear = 2021,
        efficacyDescription = "efficacy evidence"
    )

    private fun createEmptyApplicableCancerType() = CancerType(matchedCancerType = "", excludedCancerSubTypes = emptySet())

    fun withApprovedTreatment(treatment: String): ClinicalEvidence {
        return withEvidence(treatmentEvidence = approved().copy(treatment = treatment))
    }

    fun withOnLabelExperimentalTreatment(treatment: String): ClinicalEvidence {
        return withEvidence(treatmentEvidence = onLabelExperimental().copy(treatment = treatment))
    }

    fun withOffLabelExperimentalTreatment(treatment: String): ClinicalEvidence {
        return withEvidence(treatmentEvidence = offLabelExperimental().copy(treatment = treatment))
    }

    fun withOnLabelPreClinicalTreatment(treatment: String): ClinicalEvidence {
        return withEvidence(treatmentEvidence = onLabelPreclinical().copy(treatment = treatment))
    }

    fun withOnLabelKnownResistantTreatment(treatment: String): ClinicalEvidence {
        return withEvidence(treatmentEvidence = onLabelKnownResistant().copy(treatment = treatment))
    }

    fun withOnLabelSuspectResistantTreatment(treatment: String): ClinicalEvidence {
        return withEvidence(treatmentEvidence = onLabelSuspectResistant().copy(treatment = treatment))
    }

    fun createTestExternalTrial(): ExternalTrial {
        return createExternalTrial(
            nctId = "NCT00000001",
            title = "",
            countries = setOf(
                createCountry(Country.NETHERLANDS, mapOf("Leiden" to setOf(Hospital("LUMC", false)))),
                createCountry(Country.BELGIUM, mapOf("Brussels" to emptySet()))
            ),
            url = "https://clinicaltrials.gov/study/NCT00000001"
        )
    }

    fun createExternalTrial(
        nctId: String = "",
        title: String = "",
        countries: Set<CountryDetails> = emptySet(),
        url: String = ""
    ): ExternalTrial {
        return ExternalTrial(
            nctId = nctId,
            title = title,
            molecularMatches = setOf(MolecularMatchDetails(sourceEvent = "", isCategoryEvent = false)),
            applicableCancerTypes = setOf(createEmptyApplicableCancerType()),
            countries = countries,
            url = url
        )
    }

    fun createCountry(country: Country, hospitalsPerCity: Map<String, Set<Hospital>> = emptyMap()): CountryDetails {
        return CountryDetails(country, hospitalsPerCity)
    }
}
