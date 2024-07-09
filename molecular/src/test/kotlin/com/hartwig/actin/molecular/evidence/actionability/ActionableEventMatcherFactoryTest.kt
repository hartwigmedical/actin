package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.doid.TestDoidModelFactory
import com.hartwig.actin.molecular.evidence.curation.TestApplicabilityFilteringUtil
import com.hartwig.serve.datamodel.ClinicalTrial
import com.hartwig.serve.datamodel.ImmutableActionableEvents
import com.hartwig.serve.datamodel.Knowledgebase
import com.hartwig.serve.datamodel.Treatment
import com.hartwig.serve.datamodel.hotspot.ActionableHotspot
import org.assertj.core.api.Assertions.assertThat
import org.junit.Assert.assertEquals
import org.junit.Test

class ActionableEventMatcherFactoryTest {

    @Test
    fun `Should create actionable event matcher on empty inputs`() {
        val doidModel = TestDoidModelFactory.createMinimalTestDoidModel()
        val factory = ActionableEventMatcherFactory(doidModel, emptySet())
        assertThat(factory.create(ImmutableActionableEvents.builder().build())).isNotNull
        assertThat(
            factory.create(
                ImmutableActionableEvents.builder().addHotspots(TestServeActionabilityFactory.hotspotBuilder().build()).build()
            )
        ).isNotNull
    }

    @Test
    fun `Should filter external trials`() {
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
            ActionableEventMatcherFactory.filterForSources(actionable, ActionableEventMatcherFactory.ACTIONABLE_EVENT_SOURCES)
        assertEquals(4, filteredOnSource.hotspots().size.toLong())

        val filteredOnApplicability = ActionableEventMatcherFactory.filterForApplicability(filteredOnSource)
        assertEquals(3, filteredOnApplicability.hotspots().size.toLong())

        assertEquals("internal", findByGene(filteredOnApplicability.hotspots(), "gene 2"))
        assertEquals("external", findByGene(filteredOnApplicability.hotspots(), "gene 3"))
    }

    private fun findByGene(hotspots: List<ActionableHotspot>, geneToFind: String): String {
        return when (val intervention = hotspots.firstOrNull { it.gene() == geneToFind }?.intervention()) {
            is Treatment -> intervention.name()
            is ClinicalTrial -> intervention.studyAcronym() ?: intervention.studyTitle()
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