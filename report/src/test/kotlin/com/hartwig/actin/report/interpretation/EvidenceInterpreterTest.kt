package com.hartwig.actin.report.interpretation

import com.google.common.collect.Lists
import com.hartwig.actin.molecular.interpretation.AggregatedEvidence
import com.hartwig.actin.molecular.interpretation.ImmutableAggregatedEvidence
import org.junit.Assert
import org.junit.Test

class EvidenceInterpreterTest {
    @Test
    fun canInterpretEvidence() {
        val cohortWithInclusion: EvaluatedCohort = EvaluatedCohortTestFactory.builder().addMolecularEvents("inclusion").build()
        val interpreter = EvidenceInterpreter.fromEvaluatedCohorts(Lists.newArrayList(cohortWithInclusion))
        val evidence: AggregatedEvidence = ImmutableAggregatedEvidence.builder()
            .putApprovedTreatmentsPerEvent("approved", "treatment")
            .putExternalEligibleTrialsPerEvent("external", "treatment")
            .putExternalEligibleTrialsPerEvent("approved", "treatment")
            .putExternalEligibleTrialsPerEvent("inclusion", "treatment")
            .putOnLabelExperimentalTreatmentsPerEvent("on-label", "treatment")
            .putOnLabelExperimentalTreatmentsPerEvent("approved", "treatment")
            .putOffLabelExperimentalTreatmentsPerEvent("off-label", "treatment")
            .putOffLabelExperimentalTreatmentsPerEvent("on-label", "treatment")
            .putPreClinicalTreatmentsPerEvent("pre-clinical", "treatment")
            .putKnownResistantTreatmentsPerEvent("known", "treatment")
            .putSuspectResistanceTreatmentsPerEvent("suspect", "treatment")
            .build()
        val approved = interpreter.eventsWithApprovedEvidence(evidence)
        Assert.assertEquals(1, approved.size.toLong())
        Assert.assertTrue(approved.contains("approved"))
        val external = interpreter.additionalEventsWithExternalTrialEvidence(evidence)
        Assert.assertEquals(1, external.size.toLong())
        Assert.assertTrue(external.contains("external"))
        val onLabel = interpreter.additionalEventsWithOnLabelExperimentalEvidence(evidence)
        Assert.assertEquals(1, onLabel.size.toLong())
        Assert.assertTrue(onLabel.contains("on-label"))
        val offLabel = interpreter.additionalEventsWithOffLabelExperimentalEvidence(evidence)
        Assert.assertEquals(1, offLabel.size.toLong())
        Assert.assertTrue(offLabel.contains("off-label"))
    }
}