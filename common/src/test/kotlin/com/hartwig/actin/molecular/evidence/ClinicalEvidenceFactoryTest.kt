package com.hartwig.actin.molecular.evidence

import com.hartwig.actin.datamodel.molecular.evidence.Country
import com.hartwig.actin.datamodel.molecular.evidence.CountryDetails
import com.hartwig.actin.datamodel.molecular.evidence.EvidenceLevel
import com.hartwig.actin.datamodel.molecular.evidence.EvidenceLevelDetails
import com.hartwig.actin.datamodel.molecular.evidence.TestEvidenceDirectionFactory
import com.hartwig.actin.datamodel.molecular.evidence.TestExternalTrialFactory
import com.hartwig.actin.datamodel.molecular.evidence.TestTreatmentEvidenceFactory
import com.hartwig.actin.molecular.evidence.actionability.ActionabilityConstants
import com.hartwig.actin.molecular.evidence.actionability.ActionabilityMatch
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ClinicalEvidenceFactoryTest {

    @Test
    fun `Should convert SERVE actionable on-label events to clinical evidence`() {
        val onLabel = TestTreatmentEvidenceFactory.create(
            treatment = "on-label",
            isOnLabel = true,
            isCategoryEvent = false,
            evidenceLevel = EvidenceLevel.D,
            evidenceLevelDetails = EvidenceLevelDetails.GUIDELINE,
            evidenceDirection = TestEvidenceDirectionFactory.noBenefit()
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
        val offLabel = TestTreatmentEvidenceFactory.create(
            treatment = "off-label",
            isOnLabel = false,
            isCategoryEvent = false,
            evidenceLevel = EvidenceLevel.D,
            evidenceLevelDetails = EvidenceLevelDetails.GUIDELINE,
            evidenceDirection = TestEvidenceDirectionFactory.noBenefit()
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
        val trial = TestExternalTrialFactory.createTestTrial().copy(countries = setOf(CountryDetails(Country.OTHER, emptyMap())))

        val molecularCriterium = TestServeMolecularFactory.createHotspot()

        val result =
            ClinicalEvidenceFactory.create(
                ActionabilityMatch(
                    onLabelEvidence = emptyList(),
                    offLabelEvidence = emptyList(),
                    onLabelTrials = listOf(
                        TestServeTrialFactory.create(
                            setOf(molecularCriterium),
                            ActionabilityConstants.EXTERNAL_TRIAL_SOURCE,
                            trial.title
                        )
                    ),
                    offLabelTrials = emptyList()
                )
            )

        assertThat(result.treatmentEvidence).isEmpty()
        assertThat(result.eligibleTrials).containsExactly(trial)
    }
}