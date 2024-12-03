package com.hartwig.actin.datamodel.molecular.evidence

import com.hartwig.serve.datamodel.efficacy.EvidenceLevelDetails
import java.time.LocalDate

object TestClinicalEvidenceFactory {

    fun createEmpty(): ClinicalEvidence {
        return ClinicalEvidence(treatmentEvidence = emptySet(), externalEligibleTrials = emptySet())
    }

    fun withEvidence(treatmentEvidence: TreatmentEvidence): ClinicalEvidence {
        return ClinicalEvidence(treatmentEvidence = setOf(treatmentEvidence), externalEligibleTrials = emptySet())
    }

    fun withTrial(externalEligibleTrial: ExternalTrial): ClinicalEvidence {
        return withTrials(setOf(externalEligibleTrial))
    }

    fun withTrials(externalEligibleTrials: Set<ExternalTrial>): ClinicalEvidence {
        return ClinicalEvidence(treatmentEvidence = emptySet(), externalEligibleTrials = externalEligibleTrials)
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
            externalEligibleTrials = setOf(createTestExternalTrial()),
        )
    }

    fun offLabelSuspectResistant() =
        treatment(
            "off-label suspect resistant",
            EvidenceLevel.C,
            EvidenceLevelDetails.GUIDELINE,
            EvidenceDirection(isResistant = true, isCertain = false),
            false
        )

    fun onLabelSuspectResistant() =
        treatment(
            "on-label suspect resistant",
            EvidenceLevel.C,
            EvidenceLevelDetails.GUIDELINE,
            EvidenceDirection(isResistant = true, isCertain = false),
            true
        )

    fun offLabelKnownResistant() =
        treatment(
            "off-label known resistant",
            EvidenceLevel.A,
            EvidenceLevelDetails.GUIDELINE,
            EvidenceDirection(isResistant = true, isCertain = true),
            false
        )

    fun onLabelKnownResistant() =
        treatment(
            "on-label known resistant",
            EvidenceLevel.A,
            EvidenceLevelDetails.GUIDELINE,
            EvidenceDirection(isResistant = true, isCertain = true),
            true
        )

    fun offLabelPreclinical() =
        treatment(
            "off-label pre-clinical",
            EvidenceLevel.D,
            EvidenceLevelDetails.PRECLINICAL,
            EvidenceDirection(hasPositiveResponse = true),
            false
        )

    fun onLabelPreclinical() =
        treatment(
            "on-label pre-clinical",
            EvidenceLevel.C,
            EvidenceLevelDetails.PRECLINICAL,
            EvidenceDirection(hasPositiveResponse = true),
            true
        )

    fun offLabelExperimental() =
        treatment(
            "off-label experimental",
            EvidenceLevel.B,
            EvidenceLevelDetails.CLINICAL_STUDY,
            EvidenceDirection(hasPositiveResponse = true, isCertain = true),
            false
        )

    fun onLabelExperimental() =
        treatment(
            "on-label experimental",
            EvidenceLevel.A,
            EvidenceLevelDetails.CLINICAL_STUDY,
            EvidenceDirection(hasPositiveResponse = true, isCertain = false),
            true
        )

    fun approved() =
        treatment(
            "approved",
            EvidenceLevel.A,
            EvidenceLevelDetails.GUIDELINE,
            EvidenceDirection(hasPositiveResponse = true, isCertain = true),
            true
        )

    fun treatment(
        treatment: String,
        evidenceLevel: EvidenceLevel,
        evidenceLevelDetails: EvidenceLevelDetails,
        direction: EvidenceDirection,
        onLabel: Boolean,
        isCategoryEvent: Boolean = false
    ) = TreatmentEvidence(
        treatment,
        evidenceLevel,
        onLabel,
        direction,
        LocalDate.of(2021, 2, 3),
        "efficacy evidence",
        2021,
        isCategoryEvent,
        "",
        evidenceLevelDetails,
        applicableCancerType()
    )

    private fun applicableCancerType() = ApplicableCancerType(cancerType = "", excludedCancerTypes = emptySet())

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

    fun withSuspectResistantTreatment(treatment: String): ClinicalEvidence {
        return withEvidence(treatmentEvidence = onLabelSuspectResistant().copy(treatment = treatment))
    }

    fun createTestExternalTrial(): ExternalTrial {
        return createExternalTrial(
            title = "",
            countries = setOf(
                createCountry(CountryName.NETHERLANDS, mapOf("Leiden" to setOf(Hospital("LUMC", false)))),
                createCountry(CountryName.BELGIUM, mapOf("Brussels" to emptySet()))
            ),
            url = "https://clinicaltrials.gov/study/NCT00000001",
            nctId = "NCT00000001"
        )
    }

    fun createExternalTrial(title: String = "", countries: Set<Country> = emptySet(), url: String = "", nctId: String = ""): ExternalTrial {
        return ExternalTrial(
            title = title,
            countries = countries,
            url = url,
            nctId = nctId,
            sourceEvent = "",
            evidenceLevelDetails = EvidenceLevelDetails.CLINICAL_STUDY,
            applicableCancerType = ApplicableCancerType(cancerType = "", excludedCancerTypes = emptySet()),
            isCategoryEvent = false
        )
    }

    fun createCountry(countryName: CountryName, hospitalsPerCity: Map<String, Set<Hospital>> = emptyMap()): Country {
        return Country(countryName, hospitalsPerCity)
    }
}
