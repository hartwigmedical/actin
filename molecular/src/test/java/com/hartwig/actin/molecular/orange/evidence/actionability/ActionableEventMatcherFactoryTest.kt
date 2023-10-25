package com.hartwig.actin.molecular.orange.evidence.actionability

import com.hartwig.actin.doid.TestDoidModelFactory
import com.hartwig.actin.molecular.orange.evidence.curation.TestApplicabilityFilteringUtil
import com.hartwig.actin.molecular.orange.evidence.curation.TestExternalTrialMapperFactory
import com.hartwig.serve.datamodel.ActionableEvents
import com.hartwig.serve.datamodel.ImmutableActionableEvents
import com.hartwig.serve.datamodel.Knowledgebase
import com.hartwig.serve.datamodel.hotspot.ActionableHotspot
import org.junit.Assert
import org.junit.Test

class ActionableEventMatcherFactoryTest {
    @Test
    fun canCreateActionableEventMatcherOnEmptyInputs() {
        val externalTrialMapper = TestExternalTrialMapperFactory.createMinimalTestMapper()
        val doidModel = TestDoidModelFactory.createMinimalTestDoidModel()
        val factory = ActionableEventMatcherFactory(externalTrialMapper, doidModel, mutableSetOf())
        Assert.assertNotNull(factory.create(ImmutableActionableEvents.builder().build()))
        Assert.assertNotNull(factory.create(ImmutableActionableEvents.builder()
            .addHotspots(TestServeActionabilityFactory.hotspotBuilder().build())
            .build()))
    }

    @Test
    fun canFilterAndCurateExternalTrials() {
        val externalTrialMapper = TestExternalTrialMapperFactory.create("external", "actin")
        val doidModel = TestDoidModelFactory.createMinimalTestDoidModel()
        val base = TestServeActionabilityFactory.createActionableEvent(Knowledgebase.ICLUSION, "external")
        val actionable: ActionableEvents = ImmutableActionableEvents.builder()
            .addHotspots(hotspot("unknown source", "external", Knowledgebase.UNKNOWN))
            .addHotspots(hotspot(TestApplicabilityFilteringUtil.nonApplicableGene(), "external", Knowledgebase.ICLUSION))
            .addHotspots(hotspot("gene 1", "external", Knowledgebase.ICLUSION))
            .addHotspots(hotspot("gene 2", "internal", Knowledgebase.ICLUSION))
            .addHotspots(hotspot("gene 3", "external", Knowledgebase.CKB))
            .addCodons(TestServeActionabilityFactory.rangeBuilder().from(base).build())
            .addExons(TestServeActionabilityFactory.rangeBuilder().from(base).build())
            .addGenes(TestServeActionabilityFactory.geneBuilder().from(base).build())
            .addCharacteristics(TestServeActionabilityFactory.characteristicBuilder().from(base).build())
            .addFusions(TestServeActionabilityFactory.fusionBuilder().from(base).build())
            .addHla(TestServeActionabilityFactory.hlaBuilder().from(base).build())
            .build()
        val filteredOnSource: ActionableEvents = ActionableEventMatcherFactory.Companion.filterForSources(actionable, ActionableEventMatcherFactory.Companion.ACTIONABLE_EVENT_SOURCES)
        Assert.assertEquals(4, filteredOnSource.hotspots().size.toLong())
        val filteredOnApplicability: ActionableEvents = ActionableEventMatcherFactory.Companion.filterForApplicability(filteredOnSource)
        Assert.assertEquals(3, filteredOnApplicability.hotspots().size.toLong())
        val factory = ActionableEventMatcherFactory(externalTrialMapper, doidModel, mutableSetOf())
        val curated = factory.curateExternalTrials(filteredOnApplicability)
        Assert.assertEquals("actin", findByGene(curated.hotspots(), "gene 1"))
        Assert.assertEquals("internal", findByGene(curated.hotspots(), "gene 2"))
        Assert.assertEquals("external", findByGene(curated.hotspots(), "gene 3"))
        Assert.assertEquals("actin", curated.codons().iterator().next().treatment().name())
        Assert.assertEquals("actin", curated.exons().iterator().next().treatment().name())
        Assert.assertEquals("actin", curated.genes().iterator().next().treatment().name())
        Assert.assertEquals("actin", curated.fusions().iterator().next().treatment().name())
        Assert.assertEquals("actin", curated.characteristics().iterator().next().treatment().name())
        Assert.assertEquals("actin", curated.hla().iterator().next().treatment().name())
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