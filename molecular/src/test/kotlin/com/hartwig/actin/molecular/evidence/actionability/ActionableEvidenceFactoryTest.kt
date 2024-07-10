package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.molecular.datamodel.evidence.ActionableEvidence
import com.hartwig.actin.molecular.datamodel.evidence.Country
import com.hartwig.actin.molecular.datamodel.evidence.TestExternalTrialFactory
import com.hartwig.serve.datamodel.ActionableEvent
import com.hartwig.serve.datamodel.EvidenceDirection
import com.hartwig.serve.datamodel.EvidenceLevel
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ActionableEvidenceFactoryTest {

    @Test
    fun `Should create no evidence`() {
        assertThat(ActionableEvidenceFactory.createNoEvidence()).isNotNull()
    }

    @Test
    fun `Should return null for no match`() {
        assertThat(ActionableEvidenceFactory.create(null)).isNull()
    }

    @Test
    fun `Should map responsive evidence`() {
        val match = ActionabilityMatch(
            onLabelEvents = listOf(
                evidence("A on-label responsive", EvidenceLevel.A, EvidenceDirection.RESPONSIVE),
                evidence("A on-label predicted responsive", EvidenceLevel.A, EvidenceDirection.PREDICTED_RESPONSIVE),
                evidence("B on-label responsive", EvidenceLevel.B, EvidenceDirection.RESPONSIVE),
                evidence("B on-label predicted responsive", EvidenceLevel.B, EvidenceDirection.PREDICTED_RESPONSIVE),
                evidence("C on-label responsive", EvidenceLevel.C, EvidenceDirection.RESPONSIVE),
                evidence("C on-label predicted responsive", EvidenceLevel.C, EvidenceDirection.PREDICTED_RESPONSIVE)
            ),
            offLabelEvents = listOf(
                evidence("A off-label responsive", EvidenceLevel.A, EvidenceDirection.RESPONSIVE),
                evidence("A off-label predicted responsive", EvidenceLevel.A, EvidenceDirection.PREDICTED_RESPONSIVE),
                evidence("B off-label responsive", EvidenceLevel.B, EvidenceDirection.RESPONSIVE),
                evidence("B off-label predicted responsive", EvidenceLevel.B, EvidenceDirection.PREDICTED_RESPONSIVE),
                evidence("C off-label responsive", EvidenceLevel.C, EvidenceDirection.RESPONSIVE),
                evidence("C off-label predicted responsive", EvidenceLevel.C, EvidenceDirection.PREDICTED_RESPONSIVE),
            )
        )

        val evidence = ActionableEvidenceFactory.create(match)
        assertThat(evidence).isNotNull()
        assertThat(evidence!!.approvedTreatments).hasSize(1)
        assertThat(evidence.approvedTreatments).contains("A on-label responsive")
        assertThat(evidence.externalEligibleTrials).isEmpty()
        assertThat(evidence.onLabelExperimentalTreatments).hasSize(4)
        assertThat(evidence.onLabelExperimentalTreatments).contains("A on-label predicted responsive")
        assertThat(evidence.onLabelExperimentalTreatments).contains("B on-label responsive")
        assertThat(evidence.onLabelExperimentalTreatments).contains("A off-label responsive")
        assertThat(evidence.onLabelExperimentalTreatments).contains("A off-label predicted responsive")
        assertThat(evidence.offLabelExperimentalTreatments).hasSize(1)
        assertThat(evidence.offLabelExperimentalTreatments).contains("B off-label responsive")
        assertThat(evidence.preClinicalTreatments).hasSize(6)
        assertThat(evidence.preClinicalTreatments).contains("B on-label predicted responsive")
        assertThat(evidence.preClinicalTreatments).contains("C on-label responsive")
        assertThat(evidence.preClinicalTreatments).contains("C on-label predicted responsive")
        assertThat(evidence.preClinicalTreatments).contains("B off-label predicted responsive")
        assertThat(evidence.preClinicalTreatments).contains("C off-label responsive")
        assertThat(evidence.preClinicalTreatments).contains("C off-label predicted responsive")
        assertThat(evidence.knownResistantTreatments).isEmpty()
        assertThat(evidence.suspectResistantTreatments).isEmpty()
    }

    @Test
    fun `Should map resistance evidence`() {
        val match = ActionabilityMatch(
            onLabelEvents = listOf(
                evidence("On-label responsive A", EvidenceLevel.A, EvidenceDirection.RESPONSIVE),
                evidence("On-label responsive A", EvidenceLevel.A, EvidenceDirection.RESISTANT),
                evidence("On-label responsive C", EvidenceLevel.A, EvidenceDirection.RESPONSIVE),
                evidence("On-label responsive C", EvidenceLevel.C, EvidenceDirection.RESISTANT)
            ),
            offLabelEvents = listOf(
                evidence("Off-label responsive", EvidenceLevel.B, EvidenceDirection.RESPONSIVE),
                evidence("Off-label responsive", EvidenceLevel.A, EvidenceDirection.PREDICTED_RESISTANT),
                evidence("Other off-label resistant", EvidenceLevel.A, EvidenceDirection.RESISTANT)
            )
        )

        val evidence = ActionableEvidenceFactory.create(match)
        assertThat(evidence).isNotNull()
        assertThat(evidence!!.knownResistantTreatments).hasSize(1)
        assertThat(evidence.knownResistantTreatments).contains("On-label responsive A")
        assertThat(evidence.suspectResistantTreatments).hasSize(2)
        assertThat(evidence.suspectResistantTreatments).contains("Off-label responsive")
        assertThat(evidence.suspectResistantTreatments).contains("On-label responsive C")
    }

    @Test
    fun `Should map trials`() {
        val match = ActionabilityMatch(
            onLabelEvents = listOf(
                trial("On-label responsive trial", EvidenceDirection.RESPONSIVE),
                trial("On-label resistant trial", EvidenceDirection.RESISTANT)
            ),
            offLabelEvents = listOf(
                trial("Off-label responsive trial", EvidenceDirection.RESPONSIVE),
                trial("Off-label resistant trial", EvidenceDirection.RESISTANT)
            )
        )

        val evidence = ActionableEvidenceFactory.create(match)
        assertThat(evidence).isNotNull()
        assertThat(evidence!!.approvedTreatments).isEmpty()
        assertThat(evidence.externalEligibleTrials).containsExactly(
            TestExternalTrialFactory.create(
                "On-label responsive trial", setOf(Country.OTHER), "https://clinicaltrials.gov/study/NCT00000001", "NCT00000001"
            )
        )
        assertThat(evidence.onLabelExperimentalTreatments).isEmpty()
        assertThat(evidence.offLabelExperimentalTreatments).isEmpty()
        assertThat(evidence.preClinicalTreatments).isEmpty()
        assertThat(evidence.knownResistantTreatments).isEmpty()
        assertThat(evidence.suspectResistantTreatments).isEmpty()
    }

    @Test
    fun `Should ignore evidence with no benefit`() {
        val match = ActionabilityMatch(
            onLabelEvents = listOf(
                evidence("A on-label no-benefit", EvidenceLevel.A, EvidenceDirection.NO_BENEFIT)

            ),
            offLabelEvents = listOf(
                evidence("A off-label no-benefit", EvidenceLevel.A, EvidenceDirection.NO_BENEFIT)

            )
        )

        val evidence = ActionableEvidenceFactory.create(match)
        assertThat(evidence).isNotNull()
        assertThat(evidence!!.approvedTreatments).isEmpty()
        assertThat(evidence.externalEligibleTrials).isEmpty()
        assertThat(evidence.onLabelExperimentalTreatments).isEmpty()
        assertThat(evidence.offLabelExperimentalTreatments).isEmpty()
        assertThat(evidence.preClinicalTreatments).isEmpty()
        assertThat(evidence.knownResistantTreatments).isEmpty()
        assertThat(evidence.suspectResistantTreatments).isEmpty()
    }

    @Test
    fun `Should filter lower level evidence`() {
        val evidence = ActionableEvidence(
            approvedTreatments = setOf("approved"),
            onLabelExperimentalTreatments = setOf("approved", "on-label experimental"),
            offLabelExperimentalTreatments = setOf("approved", "off-label experimental"),
            preClinicalTreatments = setOf("approved", "on-label experimental", "off-label experimental", "pre-clinical"),
            knownResistantTreatments = setOf("known resistant"),
            suspectResistantTreatments = setOf("known resistant", "suspect resistant")
        )

        val filtered = ActionableEvidenceFactory.filterRedundantLowerEvidence(evidence)
        assertThat(filtered.approvedTreatments).hasSize(1)
        assertThat(filtered.approvedTreatments).contains("approved")
        assertThat(filtered.onLabelExperimentalTreatments).hasSize(1)
        assertThat(filtered.onLabelExperimentalTreatments).contains("on-label experimental")
        assertThat(filtered.offLabelExperimentalTreatments).hasSize(1)
        assertThat(filtered.offLabelExperimentalTreatments).contains("off-label experimental")
        assertThat(filtered.preClinicalTreatments).hasSize(1)
        assertThat(filtered.preClinicalTreatments).contains("pre-clinical")
        assertThat(filtered.knownResistantTreatments).hasSize(1)
        assertThat(filtered.knownResistantTreatments).contains("known resistant")
        assertThat(filtered.suspectResistantTreatments).hasSize(1)
        assertThat(filtered.suspectResistantTreatments).contains("suspect resistant")
    }

    private fun evidence(treatment: String, level: EvidenceLevel, direction: EvidenceDirection): ActionableEvent {
        return TestServeActionabilityFactory.geneBuilder()
            .treatment(TestServeActionabilityFactory.treatmentBuilder().name(treatment).build())
            .source(ActionabilityConstants.EVIDENCE_SOURCE)
            .level(level)
            .direction(direction)
            .build()
    }

    private fun trial(treatment: String, direction: EvidenceDirection): ActionableEvent {
        return TestServeActionabilityFactory.geneBuilder()
            .treatment(TestServeActionabilityFactory.treatmentBuilder().name(treatment).build())
            .source(ActionabilityConstants.EXTERNAL_TRIAL_SOURCE)
            .direction(direction)
            .build()
    }
}