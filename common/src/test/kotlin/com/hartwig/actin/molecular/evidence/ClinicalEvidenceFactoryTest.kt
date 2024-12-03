package com.hartwig.actin.molecular.evidence

import com.hartwig.actin.datamodel.molecular.evidence.Country
import com.hartwig.actin.datamodel.molecular.evidence.EvidenceDirection
import com.hartwig.actin.datamodel.molecular.evidence.EvidenceLevel
import com.hartwig.actin.datamodel.molecular.evidence.EvidenceLevelDetails
import com.hartwig.actin.datamodel.molecular.evidence.TestClinicalEvidenceFactory
import com.hartwig.actin.molecular.evidence.actionability.ActionabilityMatch
import com.hartwig.serve.datamodel.Knowledgebase
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ClinicalEvidenceFactoryTest {

    @Test
    fun `Should convert SERVE actionable on-label events to clinical evidence`() {
        val onLabel = TestClinicalEvidenceFactory.evidence(
            treatment = "on-label",
            isOnLabel = true,
            isCategoryEvent = false,
            evidenceLevel = EvidenceLevel.D,
            evidenceLevelDetails = EvidenceLevelDetails.GUIDELINE,
            evidenceDirection = EvidenceDirection(isCertain = true)
        )

        val result =
            ClinicalEvidenceFactory.create(
                ActionabilityMatch(
                    onLabelEvidence = listOf(
                        TestServeEvidenceFactory.create(
                            TestServeMolecularFactory.createHotspot(),
                            treatment = onLabel.treatment
                        )
                    ),
                    offLabelEvidence = emptyList(),
                    onLabelTrials = emptyList(),
                    offLabelTrials = emptyList()
                )
            )

        assertThat(result.treatmentEvidence).containsExactly(onLabel)
        assertThat(result.eligibleTrials).isEmpty()
    }

    @Test
    fun `Should convert SERVE actionable off-label events to clinical evidence`() {
        val offLabel = TestClinicalEvidenceFactory.evidence(
            treatment = "off-label",
            isOnLabel = false,
            isCategoryEvent = false,
            evidenceLevel = EvidenceLevel.D,
            evidenceLevelDetails = EvidenceLevelDetails.GUIDELINE,
            evidenceDirection = EvidenceDirection(isCertain = true)
        )

        val result =
            ClinicalEvidenceFactory.create(
                ActionabilityMatch(
                    onLabelEvidence = emptyList(),
                    offLabelEvidence = listOf(
                        TestServeEvidenceFactory.create(
                            TestServeMolecularFactory.createHotspot(),
                            treatment = offLabel.treatment
                        )
                    ),
                    onLabelTrials = emptyList(),
                    offLabelTrials = emptyList()
                )
            )

        assertThat(result.treatmentEvidence).containsExactly(offLabel)
        assertThat(result.eligibleTrials).isEmpty()
    }

    @Test
    fun `Should convert SERVE external trials to clinical evidence`() {
        // TODO (KD) Handle "isCategoryEvent = false"
        val trial = TestClinicalEvidenceFactory.createTestExternalTrial()
            .copy(countries = setOf(TestClinicalEvidenceFactory.createCountry(Country.OTHER, emptyMap())))

        val molecularCriterium = TestServeMolecularFactory.createHotspot()

        val result =
            ClinicalEvidenceFactory.create(
                ActionabilityMatch(
                    onLabelEvidence = emptyList(),
                    offLabelEvidence = emptyList(),
                    onLabelTrials = listOf(TestServeTrialFactory.create(setOf(molecularCriterium), Knowledgebase.CKB, trial.title)),
                    offLabelTrials = emptyList()
                )
            )

        assertThat(result.treatmentEvidence).isEmpty()
        assertThat(result.eligibleTrials).containsExactly(trial)
    }
}