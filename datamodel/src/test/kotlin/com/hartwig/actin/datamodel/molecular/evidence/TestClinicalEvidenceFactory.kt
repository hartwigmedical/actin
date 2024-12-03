package com.hartwig.actin.datamodel.molecular.evidence

object TestClinicalEvidenceFactory {

    fun createEmpty(): ClinicalEvidence {
        return ClinicalEvidence(treatmentEvidence = emptySet(), eligibleTrials = emptySet())
    }

    fun createExhaustive(): ClinicalEvidence {
        return ClinicalEvidence(
            treatmentEvidence = setOf(
                TestTreatmentEvidenceFactory.approved(),
                TestTreatmentEvidenceFactory.onLabelExperimental(),
                TestTreatmentEvidenceFactory.offLabelExperimental(),
                TestTreatmentEvidenceFactory.onLabelPreclinical(),
                TestTreatmentEvidenceFactory.offLabelPreclinical(),
                TestTreatmentEvidenceFactory.onLabelKnownResistant(),
                TestTreatmentEvidenceFactory.offLabelKnownResistant(),
                TestTreatmentEvidenceFactory.onLabelSuspectResistant(),
                TestTreatmentEvidenceFactory.offLabelSuspectResistant(),
            ),
            eligibleTrials = setOf(TestExternalTrialFactory.createTestTrial()),
        )
    }

    fun withEligibleTrial(eligibleTrial: ExternalTrial): ClinicalEvidence {
        return withEligibleTrials(setOf(eligibleTrial))
    }

    fun withEligibleTrials(eligibleTrials: Set<ExternalTrial>): ClinicalEvidence {
        return ClinicalEvidence(treatmentEvidence = emptySet(), eligibleTrials = eligibleTrials)
    }

    fun withApprovedTreatment(treatment: String): ClinicalEvidence {
        return withEvidence(treatmentEvidence = TestTreatmentEvidenceFactory.approved().copy(treatment = treatment))
    }

    fun withOnLabelExperimentalTreatment(treatment: String): ClinicalEvidence {
        return withEvidence(treatmentEvidence = TestTreatmentEvidenceFactory.onLabelExperimental().copy(treatment = treatment))
    }

    fun withOffLabelExperimentalTreatment(treatment: String): ClinicalEvidence {
        return withEvidence(treatmentEvidence = TestTreatmentEvidenceFactory.offLabelExperimental().copy(treatment = treatment))
    }

    fun withOnLabelPreClinicalTreatment(treatment: String): ClinicalEvidence {
        return withEvidence(treatmentEvidence = TestTreatmentEvidenceFactory.onLabelPreclinical().copy(treatment = treatment))
    }

    fun withOnLabelKnownResistantTreatment(treatment: String): ClinicalEvidence {
        return withEvidence(treatmentEvidence = TestTreatmentEvidenceFactory.onLabelKnownResistant().copy(treatment = treatment))
    }

    fun withOnLabelSuspectResistantTreatment(treatment: String): ClinicalEvidence {
        return withEvidence(treatmentEvidence = TestTreatmentEvidenceFactory.onLabelSuspectResistant().copy(treatment = treatment))
    }

    fun withEvidence(treatmentEvidence: TreatmentEvidence): ClinicalEvidence {
        return ClinicalEvidence(treatmentEvidence = setOf(treatmentEvidence), eligibleTrials = emptySet())
    }
}
