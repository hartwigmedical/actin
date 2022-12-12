package com.hartwig.actin.molecular.orange.evidence.actionability;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import com.hartwig.actin.doid.DoidModel;
import com.hartwig.actin.doid.TestDoidModelFactory;
import com.hartwig.actin.molecular.orange.evidence.curation.ExternalTrialMapper;
import com.hartwig.actin.molecular.orange.evidence.curation.TestApplicabilityFilteringUtil;
import com.hartwig.actin.molecular.orange.evidence.curation.TestExternalTrialMapperFactory;
import com.hartwig.serve.datamodel.ActionableEvent;
import com.hartwig.serve.datamodel.ActionableEvents;
import com.hartwig.serve.datamodel.ImmutableActionableEvents;
import com.hartwig.serve.datamodel.Knowledgebase;
import com.hartwig.serve.datamodel.hotspot.ActionableHotspot;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class ActionableEventMatcherFactoryTest {

    @Test
    public void canCreateActionableEventMatcherOnEmptyInputs() {
        ExternalTrialMapper externalTrialMapper = TestExternalTrialMapperFactory.createMinimalTestMapper();
        DoidModel doidModel = TestDoidModelFactory.createMinimalTestDoidModel();

        ActionableEventMatcherFactory factory = new ActionableEventMatcherFactory(externalTrialMapper, doidModel, null);

        assertNotNull(factory.create(ImmutableActionableEvents.builder().build()));

        assertNotNull(factory.create(ImmutableActionableEvents.builder()
                .addHotspots(TestServeActionabilityFactory.hotspotBuilder().build())
                .build()));
    }

    @Test
    public void canFilterAndCurateExternalTrials() {
        ExternalTrialMapper externalTrialMapper = TestExternalTrialMapperFactory.create("external", "actin");
        DoidModel doidModel = TestDoidModelFactory.createMinimalTestDoidModel();

        ActionableEvent base = TestServeActionabilityFactory.createActionableEvent(Knowledgebase.ICLUSION, "external");
        ActionableEvents actionable = ImmutableActionableEvents.builder()
                .addHotspots(hotspot("unknown source", "external", Knowledgebase.UNKNOWN))
                .addHotspots(hotspot(TestApplicabilityFilteringUtil.nonApplicableGene(), "external", Knowledgebase.ICLUSION))
                .addHotspots(hotspot("gene 1", "external", Knowledgebase.ICLUSION))
                .addHotspots(hotspot("gene 2", "internal", Knowledgebase.ICLUSION))
                .addHotspots(hotspot("gene 3", "external", Knowledgebase.CKB))
                .addRanges(TestServeActionabilityFactory.rangeBuilder().from(base).build())
                .addGenes(TestServeActionabilityFactory.geneBuilder().from(base).build())
                .addCharacteristics(TestServeActionabilityFactory.characteristicBuilder().from(base).build())
                .addFusions(TestServeActionabilityFactory.fusionBuilder().from(base).build())
                .addHla(TestServeActionabilityFactory.hlaBuilder().from(base).build())
                .build();

        ActionableEvents filteredOnSource =
                ActionableEventMatcherFactory.filterForSources(actionable, ActionableEventMatcherFactory.ACTIONABLE_EVENT_SOURCES);
        assertEquals(4, filteredOnSource.hotspots().size());

        ActionableEvents filteredOnApplicability = ActionableEventMatcherFactory.filterForApplicability(filteredOnSource);
        assertEquals(3, filteredOnApplicability.hotspots().size());

        ActionableEventMatcherFactory factory = new ActionableEventMatcherFactory(externalTrialMapper, doidModel, null);
        ActionableEvents curated = factory.curateExternalTrials(filteredOnApplicability);

        assertEquals("actin", findByGene(curated.hotspots(), "gene 1"));
        assertEquals("internal", findByGene(curated.hotspots(), "gene 2"));
        assertEquals("external", findByGene(curated.hotspots(), "gene 3"));

        assertEquals("actin", curated.ranges().iterator().next().treatment().name());
        assertEquals("actin", curated.genes().iterator().next().treatment().name());
        assertEquals("actin", curated.fusions().iterator().next().treatment().name());
        assertEquals("actin", curated.characteristics().iterator().next().treatment().name());
        assertEquals("actin", curated.hla().iterator().next().treatment().name());
    }

    @NotNull
    private static String findByGene(@NotNull List<ActionableHotspot> hotspots, @NotNull String geneToFind) {
        for (ActionableHotspot hotspot : hotspots) {
            if (hotspot.gene().equals(geneToFind)) {
                return hotspot.treatment().name();
            }
        }

        throw new IllegalStateException("Could not find hotspot with gene: " + geneToFind);
    }

    @NotNull
    private static ActionableHotspot hotspot(@NotNull String gene, @NotNull String treatment, @NotNull Knowledgebase source) {
        return TestServeActionabilityFactory.hotspotBuilder()
                .from(TestServeActionabilityFactory.createActionableEvent(source, treatment))
                .gene(gene)
                .build();
    }
}