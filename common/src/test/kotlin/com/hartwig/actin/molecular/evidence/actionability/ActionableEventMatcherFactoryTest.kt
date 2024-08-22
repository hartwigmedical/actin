package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.doid.TestDoidModelFactory
import com.hartwig.actin.molecular.evidence.TestServeActionabilityFactory
import com.hartwig.actin.molecular.evidence.curation.TestApplicabilityFilteringUtil
import com.hartwig.serve.datamodel.ClinicalTrial
import com.hartwig.serve.datamodel.ImmutableActionableEvents
import com.hartwig.serve.datamodel.Knowledgebase
import com.hartwig.serve.datamodel.Treatment
import com.hartwig.serve.datamodel.hotspot.ActionableHotspot
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

val DOID_MODEL = TestDoidModelFactory.createMinimalTestDoidModel()
val FACTORY = ActionableEventMatcherFactory(DOID_MODEL, emptySet())

class ActionableEventMatcherFactoryTest {

    @Test
    fun `Should create actionable event matcher on empty inputs`() {
        assertThat(FACTORY.create(ImmutableActionableEvents.builder().build())).isNotNull
        assertThat(
            FACTORY.create(
                ImmutableActionableEvents.builder().addHotspots(TestServeActionabilityFactory.hotspotBuilder().build()).build()
            )
        ).isNotNull
    }

    @Test
    fun `Should be able to filter external trials`() {
        val base = TestServeActionabilityFactory.createActionableEvent(Knowledgebase.CKB_TRIAL, "external")

        val actionable = ImmutableActionableEvents.builder()
            .addHotspots(hotspot("unknown source", "external", Knowledgebase.UNKNOWN))
            .addHotspots(hotspot(TestApplicabilityFilteringUtil.nonApplicableGene(), "external", Knowledgebase.CKB_TRIAL))
            .addHotspots(hotspot("gene 1", "external", Knowledgebase.CKB_TRIAL))
            .addHotspots(hotspot("gene 2", "internal", Knowledgebase.CKB_TRIAL))
            .addHotspots(hotspot("gene 3", "external", Knowledgebase.CKB_EVIDENCE))
            .addCodons(TestServeActionabilityFactory.rangeBuilder().from(base).build())
            .addExons(TestServeActionabilityFactory.rangeBuilder().from(base).build())
            .addGenes(TestServeActionabilityFactory.geneBuilder().from(base).build())
            .addCharacteristics(TestServeActionabilityFactory.characteristicBuilder().from(base).build())
            .addFusions(TestServeActionabilityFactory.fusionBuilder().from(base).build())
            .addHla(TestServeActionabilityFactory.hlaBuilder().from(base).build())
            .build()

        val filteredOnSource =
            FACTORY.filterForSources(actionable, FACTORY.actionableEventSources)
        assertThat(filteredOnSource.hotspots().size).isEqualTo(4)

        val filteredOnApplicability = FACTORY.filterForApplicability(filteredOnSource)
        assertThat(filteredOnApplicability.hotspots()).hasSize(3)

        assertThat(findByGene(filteredOnApplicability.hotspots(), "gene 2")).isEqualTo("internal")
        assertThat(findByGene(filteredOnApplicability.hotspots(), "gene 3")).isEqualTo("external")
    }

    private fun findByGene(hotspots: List<ActionableHotspot>, geneToFind: String): String {
        return when (val intervention = hotspots.firstOrNull { it.gene() == geneToFind }?.intervention()) {
            is Treatment -> intervention.name()
            is ClinicalTrial -> intervention.acronym() ?: intervention.title()
            else -> throw IllegalStateException("Could not find valid hotspot with gene: $geneToFind")
        }
    }

    private fun hotspot(gene: String, treatment: String, source: Knowledgebase): ActionableHotspot {
        return TestServeActionabilityFactory.hotspotBuilder()
            .from(TestServeActionabilityFactory.createActionableEvent(source, treatment))
            .gene(gene)
            .build()
    }
}