package com.hartwig.actin.molecular.orange.interpretation

import com.hartwig.actin.molecular.datamodel.evidence.ActionableEvidence
import com.hartwig.actin.molecular.datamodel.evidence.TestActionableEvidenceFactory
import com.hartwig.actin.molecular.orange.evidence.actionability.ActionabilityConstants
import com.hartwig.actin.molecular.orange.evidence.actionability.ActionabilityMatch
import com.hartwig.actin.molecular.orange.evidence.actionability.TestServeActionabilityFactory
import com.hartwig.serve.datamodel.ActionableEvent
import com.hartwig.serve.datamodel.EvidenceDirection
import com.hartwig.serve.datamodel.EvidenceLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ActionableEvidenceFactoryTest {

    @Test
    fun canCreateNoEvidence() {
        assertNotNull(ActionableEvidenceFactory.createNoEvidence())
    }

    @Test
    fun handlesNoMatch() {
        assertNull(ActionableEvidenceFactory.create(null))
    }

    @Test
    fun canMapResponsiveEvidence() {
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
        assertNotNull(evidence)
        assertEquals(1, evidence!!.approvedTreatments().size.toLong())
        assertTrue(evidence.approvedTreatments().contains("A on-label responsive"))
        assertTrue(evidence.externalEligibleTrials().isEmpty())
        assertEquals(4, evidence.onLabelExperimentalTreatments().size.toLong())
        assertTrue(evidence.onLabelExperimentalTreatments().contains("A on-label predicted responsive"))
        assertTrue(evidence.onLabelExperimentalTreatments().contains("B on-label responsive"))
        assertTrue(evidence.onLabelExperimentalTreatments().contains("A off-label responsive"))
        assertTrue(evidence.onLabelExperimentalTreatments().contains("A off-label predicted responsive"))
        assertEquals(1, evidence.offLabelExperimentalTreatments().size.toLong())
        assertTrue(evidence.offLabelExperimentalTreatments().contains("B off-label responsive"))
        assertEquals(6, evidence.preClinicalTreatments().size.toLong())
        assertTrue(evidence.preClinicalTreatments().contains("B on-label predicted responsive"))
        assertTrue(evidence.preClinicalTreatments().contains("C on-label responsive"))
        assertTrue(evidence.preClinicalTreatments().contains("C on-label predicted responsive"))
        assertTrue(evidence.preClinicalTreatments().contains("B off-label predicted responsive"))
        assertTrue(evidence.preClinicalTreatments().contains("C off-label responsive"))
        assertTrue(evidence.preClinicalTreatments().contains("C off-label predicted responsive"))
        assertTrue(evidence.knownResistantTreatments().isEmpty())
        assertTrue(evidence.suspectResistantTreatments().isEmpty())
    }

    @Test
    fun canMapResistanceEvidence() {
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
        assertNotNull(evidence)
        assertEquals(1, evidence!!.knownResistantTreatments().size.toLong())
        assertTrue(evidence.knownResistantTreatments().contains("On-label responsive A"))
        assertEquals(2, evidence.suspectResistantTreatments().size.toLong())
        assertTrue(evidence.suspectResistantTreatments().contains("Off-label responsive"))
        assertTrue(evidence.suspectResistantTreatments().contains("On-label responsive C"))
    }

    @Test
    fun canMapTrials() {
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
        assertNotNull(evidence)
        assertTrue(evidence!!.approvedTreatments().isEmpty())
        assertEquals(1, evidence.externalEligibleTrials().size.toLong())
        assertTrue(evidence.externalEligibleTrials().contains("On-label responsive trial"))
        assertTrue(evidence.onLabelExperimentalTreatments().isEmpty())
        assertTrue(evidence.offLabelExperimentalTreatments().isEmpty())
        assertTrue(evidence.preClinicalTreatments().isEmpty())
        assertTrue(evidence.knownResistantTreatments().isEmpty())
        assertTrue(evidence.suspectResistantTreatments().isEmpty())
    }

    @Test
    fun ignoresEvidenceWithNoBenefit() {
        val match = ActionabilityMatch(
            onLabelEvents = listOf(
                evidence("A on-label no-benefit", EvidenceLevel.A, EvidenceDirection.NO_BENEFIT)

            ),
            offLabelEvents = listOf(
                evidence("A off-label no-benefit", EvidenceLevel.A, EvidenceDirection.NO_BENEFIT)

            )
        )

        val evidence = ActionableEvidenceFactory.create(match)
        assertNotNull(evidence)
        assertTrue(evidence!!.approvedTreatments().isEmpty())
        assertTrue(evidence.externalEligibleTrials().isEmpty())
        assertTrue(evidence.onLabelExperimentalTreatments().isEmpty())
        assertTrue(evidence.offLabelExperimentalTreatments().isEmpty())
        assertTrue(evidence.preClinicalTreatments().isEmpty())
        assertTrue(evidence.knownResistantTreatments().isEmpty())
        assertTrue(evidence.suspectResistantTreatments().isEmpty())
    }

    @Test
    fun canFilterLowerLevelEvidence() {
        val evidence: ActionableEvidence = TestActionableEvidenceFactory.builder()
            .addApprovedTreatments("approved")
            .addOnLabelExperimentalTreatments("approved")
            .addOnLabelExperimentalTreatments("on-label experimental")
            .addOffLabelExperimentalTreatments("approved")
            .addOffLabelExperimentalTreatments("off-label experimental")
            .addPreClinicalTreatments("approved")
            .addPreClinicalTreatments("on-label experimental")
            .addPreClinicalTreatments("off-label experimental")
            .addPreClinicalTreatments("pre-clinical")
            .addKnownResistantTreatments("known resistant")
            .addSuspectResistantTreatments("known resistant")
            .addSuspectResistantTreatments("suspect resistant")
            .build()

        val filtered = ActionableEvidenceFactory.filterRedundantLowerEvidence(evidence)
        assertEquals(1, filtered.approvedTreatments().size.toLong())
        assertTrue(filtered.approvedTreatments().contains("approved"))
        assertEquals(1, filtered.onLabelExperimentalTreatments().size.toLong())
        assertTrue(filtered.onLabelExperimentalTreatments().contains("on-label experimental"))
        assertEquals(1, filtered.offLabelExperimentalTreatments().size.toLong())
        assertTrue(filtered.offLabelExperimentalTreatments().contains("off-label experimental"))
        assertEquals(1, filtered.preClinicalTreatments().size.toLong())
        assertTrue(filtered.preClinicalTreatments().contains("pre-clinical"))
        assertEquals(1, filtered.knownResistantTreatments().size.toLong())
        assertTrue(filtered.knownResistantTreatments().contains("known resistant"))
        assertEquals(1, filtered.suspectResistantTreatments().size.toLong())
        assertTrue(filtered.suspectResistantTreatments().contains("suspect resistant"))
    }

    companion object {
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
}