package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.datamodel.molecular.evidence.Country
import com.hartwig.actin.datamodel.molecular.evidence.CountryDetails
import com.hartwig.actin.datamodel.molecular.evidence.EvidenceLevel
import com.hartwig.actin.datamodel.molecular.evidence.EvidenceLevelDetails
import com.hartwig.actin.datamodel.molecular.evidence.TestEvidenceDirectionFactory
import com.hartwig.actin.datamodel.molecular.evidence.TestExternalTrialFactory
import com.hartwig.actin.datamodel.molecular.evidence.TestTreatmentEvidenceFactory
import com.hartwig.actin.molecular.evidence.TestServeEvidenceFactory
import com.hartwig.actin.molecular.evidence.TestServeMolecularFactory
import com.hartwig.actin.molecular.evidence.TestServeTrialFactory
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
                onLabelEvidences = listOf(TestServeEvidenceFactory.create(treatment = onLabel.treatment)),
                offLabelEvidences = emptyList(),
                onLabelTrials = emptyList()
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
                onLabelEvidences = emptyList(),
                offLabelEvidences = listOf(TestServeEvidenceFactory.create(treatment = offLabel.treatment)),
                onLabelTrials = emptyList()
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
                onLabelEvidences = emptyList(),
                offLabelEvidences = emptyList(),
                onLabelTrials = listOf(
                    TestServeTrialFactory.create(
                        source = ActionabilityConstants.EXTERNAL_TRIAL_SOURCE,
                        title = trial.title,
                        molecularCriteria = setOf(molecularCriterium)
                    )
                )
            )

        assertThat(result.treatmentEvidence).isEmpty()
        assertThat(result.eligibleTrials).containsExactly(trial)
    }
}