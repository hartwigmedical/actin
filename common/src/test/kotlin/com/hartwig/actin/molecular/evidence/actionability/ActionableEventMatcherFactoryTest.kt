package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.doid.TestDoidModelFactory
import com.hartwig.actin.molecular.evidence.TestServeEvidenceFactory
import com.hartwig.actin.molecular.evidence.TestServeMolecularFactory
import com.hartwig.actin.molecular.evidence.TestServeTrialFactory
import com.hartwig.actin.molecular.evidence.curation.TestApplicabilityFilteringUtil
import com.hartwig.serve.datamodel.Knowledgebase
import com.hartwig.serve.datamodel.trial.ActionableTrial
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ActionableEventMatcherFactoryTest {

    val doidModel = TestDoidModelFactory.createMinimalTestDoidModel()
    val factory = ActionableEventMatcherFactory(doidModel, emptySet())

    @Test
    fun `Should create actionable event matcher on empty inputs`() {
        assertThat(factory.create(evidences = emptyList(), trials = emptyList())).isNotNull()
        assertThat(
            factory.create(
                evidences = listOf(TestServeEvidenceFactory.createEvidenceForHotspot()),
                trials = emptyList()
            )
        ).isNotNull()
    }

    @Test
    fun `Should be able to filter external trials`() {
        val hotspot1 = TestServeTrialFactory.create(
            source = Knowledgebase.UNKNOWN,
            title = "external",
            molecularCriteria = setOf(TestServeMolecularFactory.createHotspot("unknown gene"))
        )
        val hotspot2 = TestServeTrialFactory.create(
            source = ActionabilityConstants.EXTERNAL_TRIAL_SOURCE,
            title = "external",
            molecularCriteria = setOf(TestServeMolecularFactory.createHotspot(TestApplicabilityFilteringUtil.nonApplicableGene())),
        )
        val hotspot3 = TestServeTrialFactory.create(
            source = ActionabilityConstants.EXTERNAL_TRIAL_SOURCE,
            title = "external",
            molecularCriteria = setOf(TestServeMolecularFactory.createHotspot("gene 1"))
        )
        val hotspot4 = TestServeTrialFactory.create(
            source = ActionabilityConstants.EXTERNAL_TRIAL_SOURCE,
            title = "internal",
            molecularCriteria = setOf(TestServeMolecularFactory.createHotspot("gene 2"))
        )
        val hotspot5 = TestServeTrialFactory.create(
            source = ActionabilityConstants.EXTERNAL_TRIAL_SOURCE,
            title = "external",
            molecularCriteria = setOf(TestServeMolecularFactory.createHotspot("gene 3"))
        )
        val codon1 = TestServeTrialFactory.create(
            source = ActionabilityConstants.EXTERNAL_TRIAL_SOURCE,
            title = "external",
            molecularCriteria = setOf(TestServeMolecularFactory.createCodon())
        )
        val exon1 = TestServeTrialFactory.create(
            source = ActionabilityConstants.EXTERNAL_TRIAL_SOURCE,
            title = "external",
            molecularCriteria = setOf(TestServeMolecularFactory.createExon())
        )
        val gene1 = TestServeTrialFactory.create(
            source = ActionabilityConstants.EXTERNAL_TRIAL_SOURCE,
            title = "external",
            molecularCriteria = setOf(TestServeMolecularFactory.createGene())
        )
        val characteristic1 = TestServeTrialFactory.create(
            source = ActionabilityConstants.EXTERNAL_TRIAL_SOURCE,
            title = "external",
            molecularCriteria = setOf(TestServeMolecularFactory.createCharacteristic())
        )
        val fusion1 = TestServeTrialFactory.create(
            source = ActionabilityConstants.EXTERNAL_TRIAL_SOURCE,
            title = "external",
            molecularCriteria = setOf(TestServeMolecularFactory.createFusion())
        )
        val hla1 = TestServeTrialFactory.create(
            source = ActionabilityConstants.EXTERNAL_TRIAL_SOURCE,
            title = "external",
            molecularCriteria = setOf(TestServeMolecularFactory.createHLA())
        )

        val actionable = ActionabilityMatch(
            emptyList(),
            listOf(hotspot1, hotspot2, hotspot3, hotspot4, hotspot5, codon1, exon1, gene1, characteristic1, fusion1, hla1)
        )

        /*
        TODO (KD) Fix
        val filteredOnSource = factory.filterForSources(actionable, factory.actionableEventSources)
        assertThat(filteredOnSource.trials.size).isEqualTo(10)

        val filteredOnApplicability = factory.filterForApplicability(filteredOnSource)
        assertThat(filteredOnApplicability.trials).hasSize(9)

        assertThat(findByGene(filteredOnApplicability.trials, "gene 2")).isEqualTo("internal")
        assertThat(findByGene(filteredOnApplicability.trials, "gene 3")).isEqualTo("external")
         */
    }

    private fun findByGene(hotspots: List<ActionableTrial>, geneToFind: String): String {
        return hotspots.firstOrNull { it.anyMolecularCriteria().iterator().next().hotspots().iterator().next().gene() == geneToFind }
            ?.therapyNames()?.iterator()?.next() ?: ""
    }
}