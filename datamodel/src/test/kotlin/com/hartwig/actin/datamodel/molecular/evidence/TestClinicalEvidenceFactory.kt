package com.hartwig.actin.datamodel.molecular.evidence

import com.hartwig.serve.datamodel.efficacy.EvidenceLevelDetails
import com.hartwig.serve.datamodel.trial.Hospital
import com.hartwig.serve.datamodel.trial.ImmutableHospital
import java.time.LocalDate

object TestClinicalEvidenceFactory {

    fun createEmptyClinicalEvidence(): ClinicalEvidence {
        return ClinicalEvidence()
    }

    fun createExhaustiveClinicalEvidence(): ClinicalEvidence {
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

    fun offLabelSuspectResistant() = treatment(
        "off-label suspect resistant",
        EvidenceLevel.C,
        EvidenceLevelDetails.GUIDELINE,
        EvidenceDirection(isResistant = true, isCertain = false),
        false
    )

    fun onLabelSuspectResistant() = treatment(
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

    fun offLabelExperimental() = treatment(
        "off-label experimental",
        EvidenceLevel.B,
        EvidenceLevelDetails.CLINICAL_STUDY,
        EvidenceDirection(hasPositiveResponse = true, isCertain = true),
        false
    )

    fun onLabelExperimental() = treatment(
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

    private fun applicableCancerType() = ApplicableCancerType("", emptySet())

    fun withApprovedTreatment(treatment: String): ClinicalEvidence {
        return ClinicalEvidence(treatmentEvidence = setOf(approved().copy(treatment = treatment)))
    }

    fun withExternalEligibleTrial(trial: ExternalTrial): ClinicalEvidence {
        return ClinicalEvidence(externalEligibleTrials = setOf(trial))
    }

    fun withExternalEligibleTrials(trials: Set<ExternalTrial>): ClinicalEvidence {
        return ClinicalEvidence(externalEligibleTrials = trials)
    }

    fun withOnLabelExperimentalTreatment(treatment: String): ClinicalEvidence {
        return ClinicalEvidence(treatmentEvidence = setOf(onLabelExperimental().copy(treatment = treatment)))
    }

    fun withOffLabelExperimentalTreatment(treatment: String): ClinicalEvidence {
        return ClinicalEvidence(treatmentEvidence = setOf(offLabelExperimental().copy(treatment = treatment)))
    }

    fun withOnLabelPreClinicalTreatment(treatment: String): ClinicalEvidence {
        return ClinicalEvidence(treatmentEvidence = setOf(onLabelPreclinical().copy(treatment = treatment)))
    }

    fun withOnLabelKnownResistantTreatment(treatment: String): ClinicalEvidence {
        return ClinicalEvidence(treatmentEvidence = setOf(onLabelKnownResistant().copy(treatment = treatment)))
    }

    fun withSuspectResistantTreatment(treatment: String): ClinicalEvidence {
        return ClinicalEvidence(treatmentEvidence = setOf(onLabelSuspectResistant().copy(treatment = treatment)))
    }

    fun createTestExternalTrial(): ExternalTrial {
        return createExternalTrial(
            "treatment",
            setOf(
                createCountry(CountryName.NETHERLANDS, mapOf("Leiden" to setOf(Hospital("LUMC", false)))),
                createCountry(CountryName.BELGIUM, mapOf("Brussels" to emptySet()))
            ),
            url = "https://clinicaltrials.gov/study/NCT00000001",
            "NCT00000001"
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

    fun createCountry(
        countryName: CountryName,
        hospitalsPerCity: Map<String, Set<com.hartwig.actin.datamodel.molecular.evidence.Hospital>> = emptyMap()
    ): Country {
        return Country(countryName, hospitalsPerCity)
    }
}
