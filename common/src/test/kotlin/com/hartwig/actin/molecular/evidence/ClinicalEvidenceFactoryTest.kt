package com.hartwig.actin.molecular.evidence

import com.hartwig.actin.datamodel.molecular.evidence.CountryName
import com.hartwig.actin.datamodel.molecular.evidence.EvidenceDirection
import com.hartwig.actin.datamodel.molecular.evidence.EvidenceLevel
import com.hartwig.actin.datamodel.molecular.evidence.TestClinicalEvidenceFactory
import com.hartwig.actin.molecular.evidence.TestServeActionabilityFactory.createActionableTrial
import com.hartwig.actin.molecular.evidence.actionability.ActionabilityMatch
import com.hartwig.serve.datamodel.Knowledgebase
import com.hartwig.serve.datamodel.efficacy.EvidenceLevelDetails
import com.hartwig.serve.datamodel.molecular.ImmutableMolecularCriterium
import com.hartwig.serve.datamodel.molecular.hotspot.ImmutableActionableHotspot
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ClinicalEvidenceFactoryTest {

    @Test
    fun `Should convert SERVE actionable on-label events to clinical evidence`() {
        val onLabel = TestClinicalEvidenceFactory.treatment(
            "on-label",
            EvidenceLevel.D,
            EvidenceLevelDetails.GUIDELINE,
            EvidenceDirection(isCertain = true),
            true,
            isCategoryEvent = false
        )

        val result =
            ClinicalEvidenceFactory.create(
                ActionabilityMatch(
                    onLabelEvidences = listOf(
                        TestServeActionabilityFactory.createEfficacyEvidence(
                            TestServeActionabilityFactory.createHotspot(),
                            treatment = onLabel.treatment
                        )
                    ),
                    offLabelEvidences = emptyList(),
                    onLabelTrials = emptyList(),
                    offLabelTrials = emptyList()
                )
            )

        assertThat(result.treatmentEvidence).containsExactly(onLabel)
        assertThat(result.externalEligibleTrials).isEmpty()
    }

    @Test
    fun `Should convert SERVE actionable off-label events to clinical evidence`() {
        val offLabel = TestClinicalEvidenceFactory.treatment(
            "off-label",
            EvidenceLevel.D,
            EvidenceLevelDetails.GUIDELINE,
            EvidenceDirection(isCertain = true),
            false,
            isCategoryEvent = false
        )

        val result =
            ClinicalEvidenceFactory.create(
                ActionabilityMatch(
                    onLabelEvidences = emptyList(),
                    offLabelEvidences = listOf(
                        TestServeActionabilityFactory.createEfficacyEvidence(
                            TestServeActionabilityFactory.createHotspot(),
                            treatment = offLabel.treatment
                        )
                    ),
                    onLabelTrials = emptyList(),
                    offLabelTrials = emptyList()
                )
            )

        assertThat(result.treatmentEvidence).containsExactly(offLabel)
        assertThat(result.externalEligibleTrials).isEmpty()
    }

    @Test
    fun `Should convert SERVE external trials to clinical evidence`() {
        val trial = TestClinicalEvidenceFactory.createTestExternalTrial()
            .copy(countries = setOf(TestClinicalEvidenceFactory.createCountry(CountryName.OTHER, emptyMap())), isCategoryEvent = false)

        val molecularCriterium = ImmutableMolecularCriterium.builder().addHotspots(
            ImmutableActionableHotspot.builder().from(TestServeActionabilityFactory.createActionableEvent())
                .from(TestServeFactory.createEmptyHotspot()).build()
        ).build()

        val result =
            ClinicalEvidenceFactory.create(
                ActionabilityMatch(
                    onLabelEvidences = emptyList(),
                    offLabelEvidences = emptyList(),
                    onLabelTrials = listOf(createActionableTrial(setOf(molecularCriterium), Knowledgebase.CKB, trial.title)),
                    offLabelTrials = emptyList()
                )
            )

        assertThat(result.treatmentEvidence).isEmpty()
        assertThat(result.externalEligibleTrials).containsExactly(trial)
    }
}