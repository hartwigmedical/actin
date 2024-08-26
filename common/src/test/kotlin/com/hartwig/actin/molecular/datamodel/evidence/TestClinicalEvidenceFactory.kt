package com.hartwig.actin.molecular.datamodel.evidence

object TestClinicalEvidenceFactory {

    fun createEmpty(): ClinicalEvidence {
        return ClinicalEvidence()
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
            externalEligibleTrials = setOf(TestExternalTrialFactory.createTestTrial()),
        )
    }

    fun offLabelSuspectResistant() = treatment(
        "off-label suspect resistant",
        EvidenceLevel.C,
        EvidenceDirection(isResistant = true, isCertain = false),
        false
    )

    fun onLabelSuspectResistant() = treatment(
        "on-label suspect resistant",
        EvidenceLevel.C,
        EvidenceDirection(isResistant = true, isCertain = false),
        false
    )

    fun offLabelKnownResistant() =
        treatment("off-label known resistant", EvidenceLevel.A, EvidenceDirection(isResistant = true, isCertain = true), false)

    fun onLabelKnownResistant() =
        treatment("on-label known resistant", EvidenceLevel.A, EvidenceDirection(isResistant = true, isCertain = true), true)

    fun offLabelPreclinical() =
        treatment("off-label pre-clinical", EvidenceLevel.C, EvidenceDirection(hasPositiveResponse = true), false)

    fun onLabelPreclinical() =
        treatment("on-label pre-clinical", EvidenceLevel.C, EvidenceDirection(hasPositiveResponse = true), true)

    fun offLabelExperimental() = treatment(
        "off-label experimental",
        EvidenceLevel.B,
        EvidenceDirection(hasPositiveResponse = true, isCertain = true),
        false
    )

    fun onLabelExperimental() = treatment(
        "on-label experimental",
        EvidenceLevel.A,
        EvidenceDirection(hasPositiveResponse = true, isCertain = false),
        true
    )

    fun approved() =
        treatment("approved", EvidenceLevel.A, EvidenceDirection(hasPositiveResponse = true, isCertain = false), true)

    fun treatment(treatment: String, evidenceLevel: EvidenceLevel, direction: EvidenceDirection, onLabel: Boolean) =
        TreatmentEvidence(treatment, evidenceLevel, onLabel, direction, "", applicableCancerType())

    private fun applicableCancerType() = ApplicableCancerType("", emptySet())

    fun withApprovedTreatment(treatment: String): ClinicalEvidence {
        return ClinicalEvidence(treatmentEvidence = setOf(approved().copy(treatment = treatment)))
    }

    fun withExternalEligibleTrial(trial: ExternalTrial): ClinicalEvidence {
        return ClinicalEvidence(externalEligibleTrials = setOf(trial))
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
}
