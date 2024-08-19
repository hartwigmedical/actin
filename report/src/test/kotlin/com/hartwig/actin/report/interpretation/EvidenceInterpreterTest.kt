package com.hartwig.actin.report.interpretation

class EvidenceInterpreterTest {
  /*  @Test
    fun shouldInterpretEvidence() {
        val cohortWithInclusion: EvaluatedCohort = evaluatedCohort(molecularEvents = setOf("inclusion"))
        val interpreter = EvidenceInterpreter.fromEvaluatedCohorts(listOf(cohortWithInclusion))
        val evidence = AggregatedEvidence(
            approvedTreatmentsPerEvent = mapOf("approved" to setOf("treatment")),
            externalEligibleTrialsPerEvent = mapOf(
                "external" to setOf(TestExternalTrialFactory.createTestTrial()),
                "approved" to setOf(TestExternalTrialFactory.createTestTrial()),
                "inclusion" to setOf(TestExternalTrialFactory.createTestTrial())
            ),
            onLabelExperimentalTreatmentsPerEvent = mapOf("on-label" to setOf("treatment"), "approved" to setOf("treatment")),
            offLabelExperimentalTreatmentsPerEvent = mapOf("off-label" to setOf("treatment"), "on-label" to setOf("treatment")),
            preClinicalTreatmentsPerEvent = mapOf("pre-clinical" to setOf("treatment")),
            knownResistantTreatmentsPerEvent = mapOf("known" to setOf("treatment")),
            suspectResistantTreatmentsPerEvent = mapOf("suspect" to setOf("treatment"))
        )

        val approved = interpreter.eventsWithApprovedEvidence(evidence)
        assertThat(approved).containsExactly("approved")

        val external = interpreter.additionalEventsWithExternalTrialEvidence(evidence)
        assertThat(external).containsExactly("external")

        val onLabel = interpreter.additionalEventsWithOnLabelExperimentalEvidence(evidence)
        assertThat(onLabel).containsExactly("on-label")

        val offLabel = interpreter.additionalEventsWithOffLabelExperimentalEvidence(evidence)
        assertThat(offLabel).containsExactly("off-label")
    }*/
}