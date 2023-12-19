package com.hartwig.actin.molecular.datamodel.evidence

object TestActionableEvidenceFactory {
    @JvmStatic
    fun builder(): ImmutableActionableEvidence.Builder {
        return ImmutableActionableEvidence.builder()
    }

    @JvmStatic
    fun createEmpty(): ActionableEvidence {
        return builder().build()
    }

    @JvmStatic
    fun createExhaustive(): ActionableEvidence {
        return builder().addApprovedTreatments("approved")
            .addExternalEligibleTrials("external trial")
            .addOnLabelExperimentalTreatments("on-label experimental")
            .addOffLabelExperimentalTreatments("off-label experimental")
            .addPreClinicalTreatments("pre-clinical")
            .addKnownResistantTreatments("known resistant")
            .addSuspectResistantTreatments("suspect resistant")
            .build()
    }

    @JvmStatic
    fun withApprovedTreatment(treatment: String): ActionableEvidence {
        return builder().addApprovedTreatments(treatment).build()
    }

    @JvmStatic
    fun withExternalEligibleTrial(treatment: String): ActionableEvidence {
        return builder().addExternalEligibleTrials(treatment).build()
    }

    @JvmStatic
    fun withOnLabelExperimentalTreatment(treatment: String): ActionableEvidence {
        return builder().addOnLabelExperimentalTreatments(treatment).build()
    }

    @JvmStatic
    fun withOffLabelExperimentalTreatment(treatment: String): ActionableEvidence {
        return builder().addOffLabelExperimentalTreatments(treatment).build()
    }

    @JvmStatic
    fun withPreClinicalTreatment(treatment: String): ActionableEvidence {
        return builder().addPreClinicalTreatments(treatment).build()
    }

    @JvmStatic
    fun withKnownResistantTreatment(treatment: String): ActionableEvidence {
        return builder().addKnownResistantTreatments(treatment).build()
    }

    @JvmStatic
    fun withSuspectResistantTreatment(treatment: String): ActionableEvidence {
        return builder().addSuspectResistantTreatments(treatment).build()
    }
}
