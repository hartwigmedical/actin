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
        assertThat(factory.create(ActionableEvents())).isNotNull
        assertThat(
            factory.create(
                ActionableEvents(
                    listOf(
                        TestServeEvidenceFactory.createEvidenceForHotspot()
                    )
                )
            )
        ).isNotNull
    }

    @Test
    fun `Should be able to filter external trials`() {
        val hotspot1: ActionableTrial = TestServeTrialFactory.create(
            setOf(TestServeMolecularFactory.createHotspot("unknown gene")),
            Knowledgebase.UNKNOWN,
            "external"
        )
        val hotspot2: ActionableTrial = TestServeTrialFactory.create(
            setOf(TestServeMolecularFactory.createHotspot(TestApplicabilityFilteringUtil.nonApplicableGene())),
            Knowledgebase.CKB,
            "external"
        )
        val hotspot3: ActionableTrial = TestServeTrialFactory.create(
            setOf(TestServeMolecularFactory.createHotspot("gene 1")),
            Knowledgebase.CKB,
            "external"
        )
        val hotspot4: ActionableTrial = TestServeTrialFactory.create(
            setOf(TestServeMolecularFactory.createHotspot("gene 2")),
            Knowledgebase.CKB,
            "internal"
        )
        val hotspot5: ActionableTrial = TestServeTrialFactory.create(
            setOf(TestServeMolecularFactory.createHotspot("gene 3")),
            Knowledgebase.CKB,
            "external"
        )
        val codon1: ActionableTrial =
            TestServeTrialFactory.create(setOf(TestServeMolecularFactory.createCodon()), Knowledgebase.CKB, "external")
        val exon1: ActionableTrial =
            TestServeTrialFactory.create(setOf(TestServeMolecularFactory.createExon()), Knowledgebase.CKB, "external")
        val gene1: ActionableTrial =
            TestServeTrialFactory.create(setOf(TestServeMolecularFactory.createGene()), Knowledgebase.CKB, "external")
        val characteristic1: ActionableTrial =
            TestServeTrialFactory.create(setOf(TestServeMolecularFactory.createCharacteristic()), Knowledgebase.CKB, "external")
        val fusion1: ActionableTrial =
            TestServeTrialFactory.create(setOf(TestServeMolecularFactory.createFusion()), Knowledgebase.CKB, "external")
        val hla1: ActionableTrial =
            TestServeTrialFactory.create(setOf(TestServeMolecularFactory.createHLA()), Knowledgebase.CKB, "external")
        val actionable = ActionableEvents(
            emptyList(),
            listOf(hotspot1, hotspot2, hotspot3, hotspot4, hotspot5, codon1, exon1, gene1, characteristic1, fusion1, hla1)
        )

        val filteredOnSource = factory.filterForSources(actionable, factory.actionableEventSources)
        assertThat(filteredOnSource.trials.size).isEqualTo(10)

        val filteredOnApplicability = factory.filterForApplicability(filteredOnSource)
        assertThat(filteredOnApplicability.trials).hasSize(9)

        assertThat(findByGene(filteredOnApplicability.trials, "gene 2")).isEqualTo("internal")
        assertThat(findByGene(filteredOnApplicability.trials, "gene 3")).isEqualTo("external")
    }

    private fun findByGene(hotspots: List<ActionableTrial>, geneToFind: String): String {
        return hotspots.firstOrNull { it.anyMolecularCriteria().iterator().next().hotspots().iterator().next().gene() == geneToFind }
            ?.therapyNames()?.iterator()?.next() ?: ""
    }
}