package com.hartwig.actin.molecular.datamodel.evidence

import com.hartwig.serve.datamodel.EvidenceLevel

object TestActionableEvidenceFactory {

    fun createEmpty(): ClinicalEvidence {
        return ClinicalEvidence()
    }

    fun createExhaustive(): ClinicalEvidence {
        return ClinicalEvidence(
            treatmentEvidence = setOf(
                treatment("approved", EvidenceLevel.A, ActinEvidenceCategory.APPROVED),
                treatment("on-label", EvidenceLevel.B, ActinEvidenceCategory.ON_LABEL_EXPERIMENTAL),
                treatment("off-label", EvidenceLevel.B, ActinEvidenceCategory.OFF_LABEL_EXPERIMENTAL),
                treatment("pre-clinical", EvidenceLevel.C, ActinEvidenceCategory.PRE_CLINICAL),
                treatment("known resistant", EvidenceLevel.A, ActinEvidenceCategory.KNOWN_RESISTANT),
                treatment("suspect resistant", EvidenceLevel.C, ActinEvidenceCategory.SUSPECT_RESISTANT),
            ),
            externalEligibleTrials = setOf(TestExternalTrialFactory.createTestTrial()),
        )
    }

    fun treatment(treatment: String, evidenceLevel: EvidenceLevel, category: ActinEvidenceCategory) =
        TreatmentEvidence(treatment, evidenceLevel, category, "", applicableCancerType())

    private fun applicableCancerType() = ApplicableCancerType("", setOf(""))

    fun withApprovedTreatment(treatment: String): ClinicalEvidence {
        return ClinicalEvidence(treatmentEvidence = setOf(treatment(treatment, EvidenceLevel.A, ActinEvidenceCategory.APPROVED)))
    }

    fun withExternalEligibleTrial(treatment: ExternalTrial): ClinicalEvidence {
        return ClinicalEvidence(externalEligibleTrials = setOf(treatment))
    }

    fun withOnLabelExperimentalTreatment(treatment: String): ClinicalEvidence {
        return ClinicalEvidence(
            treatmentEvidence = setOf(
                treatment(
                    treatment, EvidenceLevel.A, ActinEvidenceCategory.ON_LABEL_EXPERIMENTAL
                )
            )
        )
    }

    fun withOffLabelExperimentalTreatment(treatment: String): ClinicalEvidence {
        return ClinicalEvidence(
            treatmentEvidence = setOf(
                treatment(
                    treatment, EvidenceLevel.A, ActinEvidenceCategory.OFF_LABEL_EXPERIMENTAL
                )
            )
        )
    }

    fun withPreClinicalTreatment(treatment: String): ClinicalEvidence {
        return ClinicalEvidence(treatmentEvidence = setOf(treatment(treatment, EvidenceLevel.A, ActinEvidenceCategory.PRE_CLINICAL)))
    }

    fun withKnownResistantTreatment(treatment: String): ClinicalEvidence {
        return ClinicalEvidence(treatmentEvidence = setOf(treatment(treatment, EvidenceLevel.A, ActinEvidenceCategory.KNOWN_RESISTANT)))
    }

    fun withSuspectResistantTreatment(treatment: String): ClinicalEvidence {
        return ClinicalEvidence(treatmentEvidence = setOf(treatment(treatment, EvidenceLevel.A, ActinEvidenceCategory.SUSPECT_RESISTANT)))
    }
}
