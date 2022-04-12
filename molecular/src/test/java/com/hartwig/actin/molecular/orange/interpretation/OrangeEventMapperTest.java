package com.hartwig.actin.molecular.orange.interpretation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.hartwig.actin.molecular.datamodel.mapping.FusionGene;
import com.hartwig.actin.molecular.datamodel.mapping.GeneMutation;
import com.hartwig.actin.molecular.datamodel.mapping.InactivatedGene;
import com.hartwig.actin.molecular.datamodel.mapping.MappedActinEvents;
import com.hartwig.actin.molecular.orange.datamodel.protect.EvidenceType;
import com.hartwig.actin.molecular.orange.datamodel.protect.ImmutableProtectEvidence;
import com.hartwig.actin.molecular.orange.datamodel.protect.ImmutableProtectRecord;
import com.hartwig.actin.molecular.orange.datamodel.protect.ProtectEvidence;
import com.hartwig.actin.molecular.orange.datamodel.protect.ProtectRecord;
import com.hartwig.actin.molecular.orange.datamodel.protect.TestProtectEvidenceFactory;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class OrangeEventMapperTest {

    @Test
    public void canExtractHotspotMutations() {
        ProtectEvidence hotspotEvidence = createTestBuilder().type(EvidenceType.HOTSPOT_MUTATION).gene("gene").event("hotspot").build();
        MappedActinEvents hotspotExtraction = createTestEventExtractor().map(withEvidence(hotspotEvidence));

        assertEquals(1, hotspotExtraction.mutations().size());
        GeneMutation geneMutation = hotspotExtraction.mutations().iterator().next();
        assertEquals("gene", geneMutation.gene());
        assertEquals("hotspot", geneMutation.mutation());
    }

    @Test
    public void canExtractCodonMutations() {
        ProtectEvidence codonEvidence = createTestBuilder().type(EvidenceType.CODON_MUTATION).gene("gene").event("codon 40").build();
        MappedActinEvents codonExtraction = createTestEventExtractor().map(withEvidence(codonEvidence));

        assertEquals(1, codonExtraction.mutations().size());
        GeneMutation geneMutation = codonExtraction.mutations().iterator().next();
        assertEquals("gene", geneMutation.gene());
        assertEquals("codon 40", geneMutation.mutation());
    }

    @Test
    public void canExtractExonMutations() {
        ProtectEvidence exonEvidence = createTestBuilder().type(EvidenceType.EXON_MUTATION).gene("gene").event("exon 2-3").build();
        MappedActinEvents exonExtraction = createTestEventExtractor().map(withEvidence(exonEvidence));

        assertEquals(1, exonExtraction.mutations().size());
        GeneMutation geneMutation = exonExtraction.mutations().iterator().next();
        assertEquals("gene", geneMutation.gene());
        assertEquals("exon 2-3", geneMutation.mutation());
    }

    @Test
    public void canExtractActivatedGenes() {
        ProtectEvidence activationEvidence = createTestBuilder().type(EvidenceType.ACTIVATION).gene("gene").build();
        MappedActinEvents activationExtraction = createTestEventExtractor().map(withEvidence(activationEvidence));

        assertEquals(1, activationExtraction.activatedGenes().size());
        assertEquals("gene", activationExtraction.activatedGenes().iterator().next());
    }

    @Test
    public void canExtractInactivatedGenes() {
        OrangeEventMapper extractor = createTestEventExtractor();

        ProtectEvidence deletedEvidence = createTestBuilder().type(EvidenceType.INACTIVATION).gene("gene").event("full loss").build();
        MappedActinEvents deletedExtraction = extractor.map(withEvidence(deletedEvidence));

        assertEquals(1, deletedExtraction.inactivatedGenes().size());
        InactivatedGene inactivatedGene1 = deletedExtraction.inactivatedGenes().iterator().next();
        assertEquals("gene", inactivatedGene1.gene());
        assertTrue(inactivatedGene1.hasBeenDeleted());

        ProtectEvidence disruptedEvidence =
                createTestBuilder().type(EvidenceType.INACTIVATION).gene("gene").event("homozygous disruption").build();
        MappedActinEvents disruptedExtraction = extractor.map(withEvidence(disruptedEvidence));

        assertEquals(1, disruptedExtraction.inactivatedGenes().size());
        InactivatedGene inactivatedGene2 = disruptedExtraction.inactivatedGenes().iterator().next();
        assertEquals("gene", inactivatedGene2.gene());
        assertFalse(inactivatedGene2.hasBeenDeleted());

        ProtectEvidence noEvidence = createTestBuilder().type(EvidenceType.AMPLIFICATION).gene("gene").event("full gain").build();
        MappedActinEvents noExtraction = extractor.map(withEvidence(noEvidence));

        assertEquals(0, noExtraction.inactivatedGenes().size());
    }

    @Test
    public void canExtractAmplifiedGenes() {
        ProtectEvidence amplificationEvidence = createTestBuilder().type(EvidenceType.AMPLIFICATION).gene("gene").build();
        MappedActinEvents amplificationExtraction = createTestEventExtractor().map(withEvidence(amplificationEvidence));

        assertEquals(1, amplificationExtraction.amplifiedGenes().size());
        assertEquals("gene", amplificationExtraction.amplifiedGenes().iterator().next());
    }

    @Test
    public void canExtractFusionGenes() {
        OrangeEventMapper extractor = createTestEventExtractor();
        ProtectEvidence fusionEvidence = createTestBuilder().type(EvidenceType.FUSION_PAIR).event("gene1 - gene2 fusion").build();
        MappedActinEvents fusionExtraction = extractor.map(withEvidence(fusionEvidence));

        assertEquals(1, fusionExtraction.fusions().size());
        FusionGene fusionGene = fusionExtraction.fusions().iterator().next();
        assertEquals("gene1", fusionGene.fiveGene());
        assertEquals("gene2", fusionGene.threeGene());

        ProtectEvidence promiscuousEvidence =
                createTestBuilder().type(EvidenceType.PROMISCUOUS_FUSION).event("gene1 - gene2 fusion").build();
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
        return ImmutableProtectEvidence.builder()
                .from(TestProtectEvidenceFactory.create())
                .addSources(OrangeEventMapper.ACTIN_SOURCE)
                .reported(true);
    }

    @NotNull
    private static OrangeEventMapper createTestEventExtractor() {
        return new OrangeEventMapper(evidence -> Sets.newHashSet(evidence.event()));
    }
}