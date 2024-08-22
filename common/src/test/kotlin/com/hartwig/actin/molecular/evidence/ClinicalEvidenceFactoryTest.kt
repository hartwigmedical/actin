package com.hartwig.actin.molecular.evidence

import com.hartwig.actin.molecular.datamodel.evidence.ActinEvidenceCategory
import com.hartwig.actin.molecular.datamodel.evidence.ClinicalEvidence
import com.hartwig.actin.molecular.datamodel.evidence.Country
import com.hartwig.actin.molecular.datamodel.evidence.TestActionableEvidenceFactory.treatment
import com.hartwig.actin.molecular.datamodel.evidence.TestExternalTrialFactory
import com.hartwig.actin.molecular.evidence.actionability.ActionabilityConstants
import com.hartwig.actin.molecular.evidence.actionability.ActionabilityMatch
import com.hartwig.serve.datamodel.ActionableEvent
import com.hartwig.serve.datamodel.EvidenceDirection
import com.hartwig.serve.datamodel.EvidenceLevel
import com.hartwig.serve.datamodel.gene.ImmutableActionableGene
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ClinicalEvidenceFactoryTest {

    @Test
    fun `Should create no evidence`() {
        assertThat(ActionableEvidenceFactory.createNoEvidence()).isNotNull()
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
        assertThat(evidence.approvedTreatments()).containsExactly("A on-label responsive")
        assertThat(evidence.externalEligibleTrials).isEmpty()
        assertThat(evidence.onLabelExperimentalTreatments()).containsExactlyInAnyOrder(
            "A on-label predicted responsive",
            "B on-label responsive",
            "A off-label responsive",
            "A off-label predicted responsive"
        )
        assertThat(evidence.offLabelExperimentalTreatments()).containsExactly("B off-label responsive")
        assertThat(evidence.preClinicalTreatments()).containsExactlyInAnyOrder(
            "B on-label predicted responsive",
            "C on-label responsive",
            "C on-label predicted responsive",
            "B off-label predicted responsive",
            "C off-label responsive",
            "C off-label predicted responsive"
        )
        assertThat(evidence.knownResistantTreatments()).isEmpty()
        assertThat(evidence.suspectResistantTreatments()).isEmpty()
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
        assertThat(evidence.knownResistantTreatments()).containsExactly("On-label responsive A")
        assertThat(evidence.suspectResistantTreatments()).containsExactlyInAnyOrder("Off-label responsive", "On-label responsive C")
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
        assertThat(evidence.approvedTreatments()).isEmpty()
        assertThat(evidence.externalEligibleTrials).containsExactly(
            TestExternalTrialFactory.create(
                "On-label responsive trial", setOf(Country.OTHER), "https://clinicaltrials.gov/study/NCT00000001", "NCT00000001"
            )
        )
        assertThat(evidence.onLabelExperimentalTreatments()).isEmpty()
        assertThat(evidence.offLabelExperimentalTreatments()).isEmpty()
        assertThat(evidence.preClinicalTreatments()).isEmpty()
        assertThat(evidence.knownResistantTreatments()).isEmpty()
        assertThat(evidence.suspectResistantTreatments()).isEmpty()
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
        assertThat(evidence.approvedTreatments()).isEmpty()
        assertThat(evidence.externalEligibleTrials).isEmpty()
        assertThat(evidence.onLabelExperimentalTreatments()).isEmpty()
        assertThat(evidence.offLabelExperimentalTreatments()).isEmpty()
        assertThat(evidence.preClinicalTreatments()).isEmpty()
        assertThat(evidence.knownResistantTreatments()).isEmpty()
        assertThat(evidence.suspectResistantTreatments()).isEmpty()
    }

    @Test
    fun `Should filter lower level evidence`() {
        val approved = treatment("approved", evidenceLevel = EvidenceLevel.A, category = ActinEvidenceCategory.APPROVED)
        val onLabelExperimental = treatment(
            "on-label experimental",
            evidenceLevel = EvidenceLevel.A,
            category = ActinEvidenceCategory.ON_LABEL_EXPERIMENTAL
        )
        val offLabelExperimental = treatment(
            "off-label experimental",
            evidenceLevel = EvidenceLevel.A,
            category = ActinEvidenceCategory.OFF_LABEL_EXPERIMENTAL
        )
        val preClinical = treatment(
            "pre-clinical",
            evidenceLevel = EvidenceLevel.A,
            category = ActinEvidenceCategory.PRE_CLINICAL
        )
        val knownResistant = treatment(
            "approved",
            evidenceLevel = EvidenceLevel.A,
            category = ActinEvidenceCategory.KNOWN_RESISTANT
        )
        val suspectResistant = treatment(
            "off-label experimental",
            evidenceLevel = EvidenceLevel.A,
            category = ActinEvidenceCategory.SUSPECT_RESISTANT
        )
        val evidence = ClinicalEvidence(
            treatmentEvidence = setOf(
                approved,
                approved.copy(category = ActinEvidenceCategory.ON_LABEL_EXPERIMENTAL),
                onLabelExperimental,
                approved.copy(category = ActinEvidenceCategory.OFF_LABEL_EXPERIMENTAL),
                offLabelExperimental,
                approved.copy(category = ActinEvidenceCategory.PRE_CLINICAL),
                onLabelExperimental.copy(category = ActinEvidenceCategory.PRE_CLINICAL),
                offLabelExperimental.copy(category = ActinEvidenceCategory.PRE_CLINICAL),
                preClinical,
                knownResistant,
                knownResistant.copy(category = ActinEvidenceCategory.SUSPECT_RESISTANT),
                suspectResistant
            )
        )

        assertThat(evidence.approvedTreatments()).containsExactly(approved.treatment)
        assertThat(evidence.onLabelExperimentalTreatments()).containsExactly(onLabelExperimental.treatment)
        assertThat(evidence.offLabelExperimentalTreatments()).containsExactly(offLabelExperimental.treatment)
        assertThat(evidence.preClinicalTreatments()).containsExactly(preClinical.treatment)
        assertThat(evidence.knownResistantTreatments()).containsExactly(knownResistant.treatment)
        assertThat(evidence.suspectResistantTreatments()).containsExactly(suspectResistant.treatment)
    }

    private fun evidence(treatment: String, level: EvidenceLevel, direction: EvidenceDirection): ActionableEvent {
        return TestServeActionabilityFactory.geneBuilder()
            .intervention(TestServeActionabilityFactory.treatmentBuilder().name(treatment).build())
            .source(ActionabilityConstants.EVIDENCE_SOURCE)
            .level(level)
            .direction(direction)
            .build()
    }

    private fun trial(acronym: String, direction: EvidenceDirection): ActionableEvent {
        return ImmutableActionableGene.builder()
            .from(TestServeActionabilityFactory.createActionableEvent(ActionabilityConstants.EXTERNAL_TRIAL_SOURCE, acronym))
            .from(TestServeFactory.createEmptyGeneAnnotation())
            .direction(direction)
            .build()
    }
}