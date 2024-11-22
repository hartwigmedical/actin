package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.doid.TestDoidModelFactory
import com.hartwig.actin.molecular.evidence.TestServeActionabilityFactory
import com.hartwig.actin.molecular.evidence.TestServeActionabilityFactory.createActionableTrial
import com.hartwig.actin.molecular.evidence.curation.TestApplicabilityFilteringUtil
import com.hartwig.serve.datamodel.Knowledgebase
import com.hartwig.serve.datamodel.trial.ActionableTrial
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

val DOID_MODEL = TestDoidModelFactory.createMinimalTestDoidModel()
val FACTORY = ActionableEventMatcherFactory(DOID_MODEL, emptySet())

class ActionableEventMatcherFactoryTest {

    @Test
    fun `Should create actionable event matcher on empty inputs`() {
        assertThat(FACTORY.create(ActionableEvents())).isNotNull
        assertThat(
            FACTORY.create(
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
            TestServeActionabilityFactory.createHotspot(gene = "unknown gene"),
            Knowledgebase.UNKNOWN,
            "external"
        )
        val hotspot2: ActionableTrial = createActionableTrial(
            TestServeActionabilityFactory.createHotspot(gene = TestApplicabilityFilteringUtil.nonApplicableGene()),
            Knowledgebase.CKB,
            "external"
        )
        val hotspot3: ActionableTrial = createActionableTrial(
            TestServeActionabilityFactory.createHotspot(gene = "gene 1"),
            Knowledgebase.CKB,
            "external"
        )
        val hotspot4: ActionableTrial = createActionableTrial(
            TestServeActionabilityFactory.createHotspot(gene = "gene 2"),
            Knowledgebase.CKB,
            "internal"
        )
        val hotspot5: ActionableTrial = createActionableTrial(
            TestServeActionabilityFactory.createHotspot(gene = "gene 3"),
            Knowledgebase.CKB,
            "external"
        )
        val codon1: ActionableTrial =
            createActionableTrial(TestServeActionabilityFactory.createCodon(), Knowledgebase.CKB, "external")
        val exon1: ActionableTrial =
            createActionableTrial(TestServeActionabilityFactory.createExon(), Knowledgebase.CKB, "external")
        val gene1: ActionableTrial =
            createActionableTrial(TestServeActionabilityFactory.createGene(), Knowledgebase.CKB, "external")
        val characteristic1: ActionableTrial =
            createActionableTrial(TestServeActionabilityFactory.createCharacteristic(), Knowledgebase.CKB, "external")
        val fusion1: ActionableTrial =
            createActionableTrial(TestServeActionabilityFactory.createFusion(), Knowledgebase.CKB, "external")
        val hla1: ActionableTrial =
            createActionableTrial(TestServeActionabilityFactory.createHla(), Knowledgebase.CKB, "external")
        val actionable = ActionableEvents(
            emptyList(),
            listOf(hotspot1, hotspot2, hotspot3, hotspot4, hotspot5, codon1, exon1, gene1, characteristic1, fusion1, hla1)
        )

        val filteredOnSource = FACTORY.filterForSources(actionable, FACTORY.actionableEventSources)
        assertThat(filteredOnSource.trials.size).isEqualTo(10)

        val filteredOnApplicability = FACTORY.filterForApplicability(filteredOnSource)
        assertThat(filteredOnApplicability.trials).hasSize(9)

        assertThat(findByGene(filteredOnApplicability.trials, "gene 2")).isEqualTo("internal")
        assertThat(findByGene(filteredOnApplicability.trials, "gene 3")).isEqualTo("external")
    }

    private fun findByGene(hotspots: List<ActionableTrial>, geneToFind: String): String {
        return hotspots.firstOrNull { it.anyMolecularCriteria().iterator().next().hotspots().iterator().next().gene() == geneToFind }
            ?.therapyNames()?.iterator()?.next() ?: ""
    }
}