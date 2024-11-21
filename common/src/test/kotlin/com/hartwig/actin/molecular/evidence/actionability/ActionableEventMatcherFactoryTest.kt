package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.doid.TestDoidModelFactory
import com.hartwig.actin.molecular.evidence.TestServeActionabilityFactory
import com.hartwig.actin.molecular.evidence.TestServeActionabilityFactory.createActionableTrial
import com.hartwig.actin.molecular.evidence.curation.TestApplicabilityFilteringUtil
import com.hartwig.serve.datamodel.Knowledgebase
import com.hartwig.serve.datamodel.common.ImmutableCancerType
import com.hartwig.serve.datamodel.efficacy.EfficacyEvidence
import com.hartwig.serve.datamodel.molecular.hotspot.ActionableHotspot
import com.hartwig.serve.datamodel.trial.ActionableTrial
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import javax.swing.Action

val DOID_MODEL = TestDoidModelFactory.createMinimalTestDoidModel()
val FACTORY = ActionableEventMatcherFactory(DOID_MODEL, emptySet())

class ActionableEventMatcherFactoryTest {

    @Test
    fun `Should create actionable event matcher on empty inputs`() {
        assertThat(FACTORY.create(ActionableEvents())).isNotNull
        assertThat(
            FACTORY.create(
                ActionableEvents(
                    listOf(TestServeActionabilityFactory.withHotspot())
                )
            )
        ).isNotNull
    }

    @Test
    fun `Should be able to filter external trials`() {
        val hotspot1: ActionableTrial = createActionableTrial(
            Knowledgebase.UNKNOWN,
            "external",
            molecularCriterium = TestServeActionabilityFactory.createHotspot(gene = "unknown gene")
        )
        val hotspot2: ActionableTrial = createActionableTrial(
            Knowledgebase.CKB,
            "external",
            molecularCriterium = TestServeActionabilityFactory.createHotspot(gene = TestApplicabilityFilteringUtil.nonApplicableGene())
        )
        val hotspot3: ActionableTrial = createActionableTrial(
            Knowledgebase.CKB,
            "external",
            molecularCriterium = TestServeActionabilityFactory.createHotspot(gene = "gene 1")
        )
        val hotspot4: ActionableTrial = createActionableTrial(
            Knowledgebase.CKB,
            "internal",
            molecularCriterium = TestServeActionabilityFactory.createHotspot(gene = "gene 2")
        )
        val hotspot5: ActionableTrial = createActionableTrial(
            Knowledgebase.CKB,
            "external",
            molecularCriterium = TestServeActionabilityFactory.createHotspot(gene = "gene 3")
        )
        val codon1: ActionableTrial =
            createActionableTrial(Knowledgebase.CKB, "external", molecularCriterium = TestServeActionabilityFactory.createCodon())
        val exon1: ActionableTrial =
            createActionableTrial(Knowledgebase.CKB, "external", molecularCriterium = TestServeActionabilityFactory.createExon())
        val gene1: ActionableTrial =
            createActionableTrial(Knowledgebase.CKB, "external", molecularCriterium = TestServeActionabilityFactory.createGene())
        val characteristic1: ActionableTrial =
            createActionableTrial(Knowledgebase.CKB, "external", molecularCriterium = TestServeActionabilityFactory.createCharacteristic())
        val fusion1: ActionableTrial =
            createActionableTrial(Knowledgebase.CKB, "external", molecularCriterium = TestServeActionabilityFactory.createFusion())
        val hla1: ActionableTrial =
            createActionableTrial(Knowledgebase.CKB, "external", molecularCriterium = TestServeActionabilityFactory.createHla())
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