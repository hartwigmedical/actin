package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.doid.TestDoidModelFactory
import com.hartwig.actin.molecular.evidence.TestServeActionabilityFactory
import com.hartwig.actin.molecular.evidence.TestServeActionabilityFactory.createActionableTrial
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
                        TestServeActionabilityFactory.createEfficacyEvidenceWithHotspot()
                    )
                )
            )
        ).isNotNull
    }

    @Test
    fun `Should be able to filter external trials`() {
        val hotspot1: ActionableTrial = createActionableTrial(
            setOf(TestServeActionabilityFactory.createHotspot("unknown gene")),
            Knowledgebase.UNKNOWN,
            "external"
        )
        val hotspot2: ActionableTrial = createActionableTrial(
            setOf(TestServeActionabilityFactory.createHotspot(TestApplicabilityFilteringUtil.nonApplicableGene())),
            Knowledgebase.CKB,
            "external"
        )
        val hotspot3: ActionableTrial = createActionableTrial(
            setOf(TestServeActionabilityFactory.createHotspot("gene 1")),
            Knowledgebase.CKB,
            "external"
        )
        val hotspot4: ActionableTrial = createActionableTrial(
            setOf(TestServeActionabilityFactory.createHotspot("gene 2")),
            Knowledgebase.CKB,
            "internal"
        )
        val hotspot5: ActionableTrial = createActionableTrial(
            setOf(TestServeActionabilityFactory.createHotspot("gene 3")),
            Knowledgebase.CKB,
            "external"
        )
        val codon1: ActionableTrial =
            createActionableTrial(setOf(TestServeActionabilityFactory.createCodon()), Knowledgebase.CKB, "external")
        val exon1: ActionableTrial =
            createActionableTrial(setOf(TestServeActionabilityFactory.createExon()), Knowledgebase.CKB, "external")
        val gene1: ActionableTrial =
            createActionableTrial(setOf(TestServeActionabilityFactory.createGene()), Knowledgebase.CKB, "external")
        val characteristic1: ActionableTrial =
            createActionableTrial(setOf(TestServeActionabilityFactory.createCharacteristic()), Knowledgebase.CKB, "external")
        val fusion1: ActionableTrial =
            createActionableTrial(setOf(TestServeActionabilityFactory.createFusion()), Knowledgebase.CKB, "external")
        val hla1: ActionableTrial =
            createActionableTrial(setOf(TestServeActionabilityFactory.createHla()), Knowledgebase.CKB, "external")
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