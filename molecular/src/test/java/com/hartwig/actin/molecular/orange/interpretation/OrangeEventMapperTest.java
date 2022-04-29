package com.hartwig.actin.molecular.orange.interpretation;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.hartwig.actin.molecular.datamodel.mapping.FusionGene;
import com.hartwig.actin.molecular.datamodel.mapping.GeneMutation;
import com.hartwig.actin.molecular.datamodel.mapping.MappedActinEvents;
import com.hartwig.actin.molecular.orange.datamodel.protect.EvidenceType;
import com.hartwig.actin.molecular.orange.datamodel.protect.ImmutableProtectEvidence;
import com.hartwig.actin.molecular.orange.datamodel.protect.ImmutableProtectRecord;
import com.hartwig.actin.molecular.orange.datamodel.protect.ImmutableProtectSource;
import com.hartwig.actin.molecular.orange.datamodel.protect.ProtectEvidence;
import com.hartwig.actin.molecular.orange.datamodel.protect.ProtectRecord;
import com.hartwig.actin.molecular.orange.datamodel.protect.ProtectSource;
import com.hartwig.actin.molecular.orange.datamodel.protect.TestProtectDataFactory;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class OrangeEventMapperTest {

    @Test
    public void canExtractHotspotMutations() {
        ProtectEvidence hotspotEvidence =
                createTestBuilder().gene("gene").event("hotspot").addSources(withType(EvidenceType.HOTSPOT_MUTATION)).build();
        MappedActinEvents hotspotExtraction = createTestEventExtractor().map(withEvidence(hotspotEvidence));

        assertEquals(1, hotspotExtraction.mutations().size());
        GeneMutation geneMutation = hotspotExtraction.mutations().iterator().next();
        assertEquals("gene", geneMutation.gene());
        assertEquals("hotspot", geneMutation.mutation());
    }

    @Test
    public void canExtractCodonMutations() {
        ProtectEvidence codonEvidence =
                createTestBuilder().gene("gene").event("codon 40").addSources(withType(EvidenceType.CODON_MUTATION)).build();
        MappedActinEvents codonExtraction = createTestEventExtractor().map(withEvidence(codonEvidence));

        assertEquals(1, codonExtraction.mutations().size());
        GeneMutation geneMutation = codonExtraction.mutations().iterator().next();
        assertEquals("gene", geneMutation.gene());
        assertEquals("codon 40", geneMutation.mutation());
    }

    @Test
    public void canExtractExonMutations() {
        ProtectEvidence exonEvidence =
                createTestBuilder().gene("gene").event("exon 2-3").addSources(withType(EvidenceType.EXON_MUTATION)).build();
        MappedActinEvents exonExtraction = createTestEventExtractor().map(withEvidence(exonEvidence));

        assertEquals(1, exonExtraction.mutations().size());
        GeneMutation geneMutation = exonExtraction.mutations().iterator().next();
        assertEquals("gene", geneMutation.gene());
        assertEquals("exon 2-3", geneMutation.mutation());
    }

    @Test
    public void canExtractActivatedGenes() {
        ProtectEvidence activationEvidence = createTestBuilder().gene("gene").addSources(withType(EvidenceType.ACTIVATION)).build();
        MappedActinEvents activationExtraction = createTestEventExtractor().map(withEvidence(activationEvidence));

        assertEquals(1, activationExtraction.activatedGenes().size());
        assertEquals("gene", activationExtraction.activatedGenes().iterator().next());
    }

    @Test
    public void canExtractInactivatedGenes() {
        OrangeEventMapper extractor = createTestEventExtractor();

        ProtectEvidence deletedEvidence = createTestBuilder().gene("gene").addSources(withType(EvidenceType.INACTIVATION)).build();
        MappedActinEvents deletedExtraction = extractor.map(withEvidence(deletedEvidence));

        assertEquals(1, deletedExtraction.inactivatedGenes().size());
        assertEquals("gene", deletedExtraction.inactivatedGenes().iterator().next());

        ProtectEvidence noEvidence = createTestBuilder().gene("gene").addSources(withType(EvidenceType.AMPLIFICATION)).build();
        MappedActinEvents noExtraction = extractor.map(withEvidence(noEvidence));

        assertEquals(0, noExtraction.inactivatedGenes().size());
    }

    @Test
    public void canExtractAmplifiedGenes() {
        ProtectEvidence amplificationEvidence = createTestBuilder().gene("gene").addSources(withType(EvidenceType.AMPLIFICATION)).build();
        MappedActinEvents amplificationExtraction = createTestEventExtractor().map(withEvidence(amplificationEvidence));

        assertEquals(1, amplificationExtraction.amplifiedGenes().size());
        assertEquals("gene", amplificationExtraction.amplifiedGenes().iterator().next());
    }

    @Test
    public void canExtractFusionGenes() {
        OrangeEventMapper extractor = createTestEventExtractor();
        ProtectEvidence fusionEvidence =
                createTestBuilder().event("gene1 - gene2 fusion").addSources(withType(EvidenceType.FUSION_PAIR)).build();
        MappedActinEvents fusionExtraction = extractor.map(withEvidence(fusionEvidence));

        assertEquals(1, fusionExtraction.fusions().size());
        FusionGene fusionGene = fusionExtraction.fusions().iterator().next();
        assertEquals("gene1", fusionGene.fiveGene());
        assertEquals("gene2", fusionGene.threeGene());

        ProtectEvidence promiscuousEvidence =
                createTestBuilder().event("gene1 - gene2 fusion").addSources(withType(EvidenceType.PROMISCUOUS_FUSION)).build();
        MappedActinEvents promiscuousExtraction = extractor.map(withEvidence(promiscuousEvidence));

        assertEquals(1, promiscuousExtraction.fusions().size());
        FusionGene promiscuous = promiscuousExtraction.fusions().iterator().next();
        assertEquals("gene1", promiscuous.fiveGene());
        assertEquals("gene2", promiscuous.threeGene());
    }

    @NotNull
    private static ProtectRecord withEvidence(@NotNull ProtectEvidence evidence) {
        return ImmutableProtectRecord.builder().evidences(Lists.newArrayList(evidence)).build();
    }

    @NotNull
    private static ImmutableProtectEvidence.Builder createTestBuilder() {
        return ImmutableProtectEvidence.builder().from(TestProtectDataFactory.create()).reported(true);
    }

    @NotNull
    private static ProtectSource withType(@NotNull EvidenceType type) {
        return ImmutableProtectSource.builder()
                .from(TestProtectDataFactory.createSource())
                .name(OrangeEventMapper.ACTIN_SOURCE)
                .type(type)
                .build();
    }

    @NotNull
    private static OrangeEventMapper createTestEventExtractor() {
        return new OrangeEventMapper(evidence -> Sets.newHashSet(evidence.event()));
    }
}