package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.doid.TestDoidModelFactory
import com.hartwig.actin.molecular.evidence.curation.TestApplicabilityFilteringUtil
import com.hartwig.serve.datamodel.ActionableEvents
import com.hartwig.serve.datamodel.ImmutableActionableEvents
import com.hartwig.serve.datamodel.Knowledgebase
import com.hartwig.serve.datamodel.hotspot.ActionableHotspot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ActionableEventMatcherFactoryTest {

    @Test
    fun canCreateActionableEventMatcherOnEmptyInputs() {
        val doidModel = TestDoidModelFactory.createMinimalTestDoidModel()
        val factory = ActionableEventMatcherFactory(doidModel, mutableSetOf())
        assertNotNull(factory.create(ImmutableActionableEvents.builder().build()))
        assertNotNull(
            factory.create(
                ImmutableActionableEvents.builder()
                    .addHotspots(TestServeActionabilityFactory.hotspotBuilder().build())
                    .build()
            )
        )
    }

    @Test
    fun canFilterExternalTrials() {
        val base = TestServeActionabilityFactory.createActionableEvent(Knowledgebase.CKB_TRIAL, "external")

        val actionable: ActionableEvents = ImmutableActionableEvents.builder()
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

        val filteredOnSource: ActionableEvents =
            ActionableEventMatcherFactory.filterForSources(actionable, ActionableEventMatcherFactory.ACTIONABLE_EVENT_SOURCES)
        assertEquals(4, filteredOnSource.hotspots().size.toLong())

        val filteredOnApplicability: ActionableEvents = ActionableEventMatcherFactory.filterForApplicability(filteredOnSource)
        assertEquals(3, filteredOnApplicability.hotspots().size.toLong())

        assertEquals("internal", findByGene(filteredOnApplicability.hotspots(), "gene 2"))
        assertEquals("external", findByGene(filteredOnApplicability.hotspots(), "gene 3"))
    }

    companion object {
        private fun findByGene(hotspots: MutableList<ActionableHotspot>, geneToFind: String): String {
            for (hotspot in hotspots) {
                if (hotspot.gene() == geneToFind) {
                    return hotspot.treatment().name()
                }
            }
            throw IllegalStateException("Could not find hotspot with gene: $geneToFind")
        }

        private fun hotspot(gene: String, treatment: String, source: Knowledgebase): ActionableHotspot {
            return TestServeActionabilityFactory.hotspotBuilder()
                .from(TestServeActionabilityFactory.createActionableEvent(source, treatment))
                .gene(gene)
                .build()
        }
    }
}