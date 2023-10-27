package com.hartwig.actin.molecular.orange.interpretation

import com.hartwig.actin.molecular.datamodel.evidence.ActionableEvidence
import com.hartwig.actin.molecular.datamodel.evidence.TestActionableEvidenceFactory
import com.hartwig.actin.molecular.orange.evidence.actionability.ActionabilityConstants
import com.hartwig.actin.molecular.orange.evidence.actionability.ActionabilityMatch
import com.hartwig.actin.molecular.orange.evidence.actionability.TestServeActionabilityFactory
import com.hartwig.serve.datamodel.ActionableEvent
import com.hartwig.serve.datamodel.EvidenceDirection
import com.hartwig.serve.datamodel.EvidenceLevel
import org.junit.Assert
import org.junit.Test
import kotlin.test.assertNotNull

class ActionableEvidenceFactoryTest {
    @Test
    fun canCreateNoEvidence() {
        Assert.assertNotNull(ActionableEvidenceFactory.createNoEvidence())
    }

    @Test
    fun handlesNoMatch() {
        Assert.assertNull(ActionableEvidenceFactory.create(null))
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
        Assert.assertEquals(1, evidence.approvedTreatments().size.toLong())
        Assert.assertTrue(evidence.approvedTreatments().contains("A on-label responsive"))
        Assert.assertTrue(evidence.externalEligibleTrials().isEmpty())
        Assert.assertEquals(4, evidence.onLabelExperimentalTreatments().size.toLong())
        Assert.assertTrue(evidence.onLabelExperimentalTreatments().contains("A on-label predicted responsive"))
        Assert.assertTrue(evidence.onLabelExperimentalTreatments().contains("B on-label responsive"))
        Assert.assertTrue(evidence.onLabelExperimentalTreatments().contains("A off-label responsive"))
        Assert.assertTrue(evidence.onLabelExperimentalTreatments().contains("A off-label predicted responsive"))
        Assert.assertEquals(1, evidence.offLabelExperimentalTreatments().size.toLong())
        Assert.assertTrue(evidence.offLabelExperimentalTreatments().contains("B off-label responsive"))
        Assert.assertEquals(6, evidence.preClinicalTreatments().size.toLong())
        Assert.assertTrue(evidence.preClinicalTreatments().contains("B on-label predicted responsive"))
        Assert.assertTrue(evidence.preClinicalTreatments().contains("C on-label responsive"))
        Assert.assertTrue(evidence.preClinicalTreatments().contains("C on-label predicted responsive"))
        Assert.assertTrue(evidence.preClinicalTreatments().contains("B off-label predicted responsive"))
        Assert.assertTrue(evidence.preClinicalTreatments().contains("C off-label responsive"))
        Assert.assertTrue(evidence.preClinicalTreatments().contains("C off-label predicted responsive"))
        Assert.assertTrue(evidence.knownResistantTreatments().isEmpty())
        Assert.assertTrue(evidence.suspectResistantTreatments().isEmpty())
    }

    @Test
    fun canMapResistanceEvidence() {
        val match: ActionabilityMatch = ActionabilityMatch(
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
        Assert.assertEquals(1, evidence.knownResistantTreatments().size.toLong())
        Assert.assertTrue(evidence.knownResistantTreatments().contains("On-label responsive A"))
        Assert.assertEquals(2, evidence.suspectResistantTreatments().size.toLong())
        Assert.assertTrue(evidence.suspectResistantTreatments().contains("Off-label responsive"))
        Assert.assertTrue(evidence.suspectResistantTreatments().contains("On-label responsive C"))
    }

    @Test
    fun canMapTrials() {
        val match: ActionabilityMatch = ActionabilityMatch(
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
        Assert.assertTrue(evidence.approvedTreatments().isEmpty())
        Assert.assertEquals(1, evidence.externalEligibleTrials().size.toLong())
        Assert.assertTrue(evidence.externalEligibleTrials().contains("On-label responsive trial"))
        Assert.assertTrue(evidence.onLabelExperimentalTreatments().isEmpty())
        Assert.assertTrue(evidence.offLabelExperimentalTreatments().isEmpty())
        Assert.assertTrue(evidence.preClinicalTreatments().isEmpty())
        Assert.assertTrue(evidence.knownResistantTreatments().isEmpty())
        Assert.assertTrue(evidence.suspectResistantTreatments().isEmpty())
    }

    @Test
    fun ignoresEvidenceWithNoBenefit() {
        val match: ActionabilityMatch = ActionabilityMatch(
            onLabelEvents = listOf(
                evidence("A on-label no-benefit", EvidenceLevel.A, EvidenceDirection.NO_BENEFIT)

            ),
            offLabelEvents = listOf(
                evidence("A off-label no-benefit", EvidenceLevel.A, EvidenceDirection.NO_BENEFIT)

            )
        )

        val evidence = ActionableEvidenceFactory.create(match)
        assertNotNull(evidence)
        Assert.assertTrue(evidence.approvedTreatments().isEmpty())
        Assert.assertTrue(evidence.externalEligibleTrials().isEmpty())
        Assert.assertTrue(evidence.onLabelExperimentalTreatments().isEmpty())
        Assert.assertTrue(evidence.offLabelExperimentalTreatments().isEmpty())
        Assert.assertTrue(evidence.preClinicalTreatments().isEmpty())
        Assert.assertTrue(evidence.knownResistantTreatments().isEmpty())
        Assert.assertTrue(evidence.suspectResistantTreatments().isEmpty())
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
        Assert.assertEquals(1, filtered.approvedTreatments().size.toLong())
        Assert.assertTrue(filtered.approvedTreatments().contains("approved"))
        Assert.assertEquals(1, filtered.onLabelExperimentalTreatments().size.toLong())
        Assert.assertTrue(filtered.onLabelExperimentalTreatments().contains("on-label experimental"))
        Assert.assertEquals(1, filtered.offLabelExperimentalTreatments().size.toLong())
        Assert.assertTrue(filtered.offLabelExperimentalTreatments().contains("off-label experimental"))
        Assert.assertEquals(1, filtered.preClinicalTreatments().size.toLong())
        Assert.assertTrue(filtered.preClinicalTreatments().contains("pre-clinical"))
        Assert.assertEquals(1, filtered.knownResistantTreatments().size.toLong())
        Assert.assertTrue(filtered.knownResistantTreatments().contains("known resistant"))
        Assert.assertEquals(1, filtered.suspectResistantTreatments().size.toLong())
        Assert.assertTrue(filtered.suspectResistantTreatments().contains("suspect resistant"))
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