package com.hartwig.actin.molecular.datamodel.evidence

object TestActionableEvidenceFactory {

    fun createEmpty(): ActionableEvidence {
        return ActionableEvidence()
    }

    fun createExhaustive(): ActionableEvidence {
        return ActionableEvidence(
            approvedTreatments = setOf("approved"),
            externalEligibleTrials = setOf(TestExternalTrialFactory.createTestTrial()),
            onLabelExperimentalTreatments = setOf("on-label experimental"),
            offLabelExperimentalTreatments = setOf("off-label experimental"),
            preClinicalTreatments = setOf("pre-clinical"),
            knownResistantTreatments = setOf("known resistant"),
            suspectResistantTreatments = setOf("suspect resistant")
        )
    }

    fun withApprovedTreatment(treatment: String): ActionableEvidence {
        return ActionableEvidence(approvedTreatments = setOf(treatment))
    }

    fun withExternalEligibleTrial(treatment: ExternalTrial): ActionableEvidence {
        return ActionableEvidence(externalEligibleTrials = setOf(treatment))
    }

    fun withOnLabelExperimentalTreatment(treatment: String): ActionableEvidence {
        return ActionableEvidence(onLabelExperimentalTreatments = setOf(treatment))
    }

    fun withOffLabelExperimentalTreatment(treatment: String): ActionableEvidence {
        return ActionableEvidence(offLabelExperimentalTreatments = setOf(treatment))
    }

    fun withPreClinicalTreatment(treatment: String): ActionableEvidence {
        return ActionableEvidence(preClinicalTreatments = setOf(treatment))
    }

    fun withKnownResistantTreatment(treatment: String): ActionableEvidence {
        return ActionableEvidence(knownResistantTreatments = setOf(treatment))
    }

    fun withSuspectResistantTreatment(treatment: String): ActionableEvidence {
        return ActionableEvidence(suspectResistantTreatments = setOf(treatment))
    }
}
