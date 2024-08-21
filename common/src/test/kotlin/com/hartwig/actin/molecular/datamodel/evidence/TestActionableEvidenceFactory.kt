package com.hartwig.actin.molecular.datamodel.evidence

import com.hartwig.serve.datamodel.EvidenceLevel

object TestActionableEvidenceFactory {

    fun createEmpty(): ActionableEvidence {
        return ActionableEvidence()
    }

    fun createExhaustive(): ActionableEvidence {
        return ActionableEvidence(
            actionableTreatments = setOf(
                treatment("approved", ActinEvidenceCategory.APPROVED),
                ActionableTreatment("on-label", EvidenceLevel.A, ActinEvidenceCategory.ON_LABEL_EXPERIMENTAL),
                ActionableTreatment("off-label", EvidenceLevel.A, ActinEvidenceCategory.OFF_LABEL_EXPERIMENTAL),
                ActionableTreatment("pre-clinical", EvidenceLevel.A, ActinEvidenceCategory.PRE_CLINICAL),
                ActionableTreatment("known resistant", EvidenceLevel.A, ActinEvidenceCategory.KNOWN_RESISTANT),
                ActionableTreatment("suspect resistant", EvidenceLevel.A, ActinEvidenceCategory.SUSPECT_RESISTANT),
            ),
            externalEligibleTrials = setOf(TestExternalTrialFactory.createTestTrial()),
        )
    }

    fun treatment(name: String, category: ActinEvidenceCategory) =
        ActionableTreatment(name, EvidenceLevel.A, category)

    fun withApprovedTreatment(treatment: String): ActionableEvidence {
        return ActionableEvidence(actionableTreatments = setOf(treatment(treatment, ActinEvidenceCategory.APPROVED)))
    }

    fun withExternalEligibleTrial(treatment: ExternalTrial): ActionableEvidence {
        return ActionableEvidence(externalEligibleTrials = setOf(treatment))
    }

    fun withOnLabelExperimentalTreatment(treatment: String): ActionableEvidence {
        return ActionableEvidence(actionableTreatments = setOf(treatment(treatment, ActinEvidenceCategory.ON_LABEL_EXPERIMENTAL)))
    }

    fun withOffLabelExperimentalTreatment(treatment: String): ActionableEvidence {
        return ActionableEvidence(actionableTreatments = setOf(treatment(treatment, ActinEvidenceCategory.OFF_LABEL_EXPERIMENTAL)))
    }

    fun withPreClinicalTreatment(treatment: String): ActionableEvidence {
        return ActionableEvidence(actionableTreatments = setOf(treatment(treatment, ActinEvidenceCategory.PRE_CLINICAL)))
    }

    fun withKnownResistantTreatment(treatment: String): ActionableEvidence {
        return ActionableEvidence(actionableTreatments = setOf(treatment(treatment, ActinEvidenceCategory.KNOWN_RESISTANT)))
    }

    fun withSuspectResistantTreatment(treatment: String): ActionableEvidence {
        return ActionableEvidence(actionableTreatments = setOf(treatment(treatment, ActinEvidenceCategory.SUSPECT_RESISTANT)))
    }
}
