package com.hartwig.actin.molecular.evidence

import com.hartwig.actin.molecular.datamodel.evidence.Country
import com.hartwig.actin.molecular.datamodel.evidence.EvidenceDirection
import com.hartwig.actin.molecular.datamodel.evidence.EvidenceLevel
import com.hartwig.actin.molecular.datamodel.evidence.TestClinicalEvidenceFactory
import com.hartwig.actin.molecular.datamodel.evidence.TestExternalTrialFactory
import com.hartwig.actin.molecular.evidence.actionability.ActionabilityMatch
import com.hartwig.serve.datamodel.Knowledgebase
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import com.hartwig.serve.datamodel.EvidenceDirection as ServeDirection

class ClinicalEvidenceFactoryTest {

    @Test
    fun `Should convert SERVE actionable on-label events to clinical evidence`() {
        val onlabel = TestClinicalEvidenceFactory.treatment(
            "on-label",
            EvidenceLevel.D,
            EvidenceDirection(isCertain = true),
            true,
            isCategoryEvent = null
        )
        val result =
            ClinicalEvidenceFactory.create(
                ActionabilityMatch(
                    onLabelEvents = listOf(
                        TestServeActionabilityFactory.createActionableEvent(
                            Knowledgebase.CKB_EVIDENCE,
                            onlabel.treatment
                        )
                    ),
                    offLabelEvents = emptyList()
                )
            )
        assertThat(result.externalEligibleTrials).isEmpty()
        assertThat(result.treatmentEvidence).containsExactly(onlabel)
    }

    @Test
    fun `Should convert SERVE actionable off-label events to clinical evidence`() {
        val onlabel = TestClinicalEvidenceFactory.treatment(
            "off-label",
            EvidenceLevel.D,
            EvidenceDirection(isCertain = true),
            false,
            isCategoryEvent = null
        )
        val result =
            ClinicalEvidenceFactory.create(
                ActionabilityMatch(
                    offLabelEvents = listOf(
                        TestServeActionabilityFactory.createActionableEvent(
                            Knowledgebase.CKB_EVIDENCE,
                            onlabel.treatment
                        )
                    ),
                    onLabelEvents = emptyList()
                )
            )
        assertThat(result.externalEligibleTrials).isEmpty()
        assertThat(result.treatmentEvidence).containsExactly(onlabel)
    }

    @Test
    fun `Should convert SERVE external trials to clinical evidence`() {
        val trial = TestExternalTrialFactory.createTestTrial().copy(countries = setOf(Country.OTHER), isCategoryVariant = null)
        val result =
            ClinicalEvidenceFactory.create(
                ActionabilityMatch(
                    onLabelEvents = listOf(
                        TestServeActionabilityFactory.createActionableEvent(
                            Knowledgebase.CKB_TRIAL,
                            trial.title,
                            ServeDirection.RESPONSIVE
                        )
                    ),
                    offLabelEvents = emptyList()
                )
            )
        assertThat(result.treatmentEvidence).isEmpty()
        assertThat(result.externalEligibleTrials).containsExactly(trial)
    }

}