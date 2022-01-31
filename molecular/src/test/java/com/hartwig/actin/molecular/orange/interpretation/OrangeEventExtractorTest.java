package com.hartwig.actin.molecular.orange.interpretation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.molecular.datamodel.FusionGene;
import com.hartwig.actin.molecular.datamodel.GeneMutation;
import com.hartwig.actin.molecular.datamodel.InactivatedGene;
import com.hartwig.actin.molecular.orange.datamodel.EvidenceType;
import com.hartwig.actin.molecular.orange.datamodel.ImmutableOrangeRecord;
import com.hartwig.actin.molecular.orange.datamodel.ImmutableTreatmentEvidence;
import com.hartwig.actin.molecular.orange.datamodel.OrangeRecord;
import com.hartwig.actin.molecular.orange.datamodel.TestOrangeDataFactory;
import com.hartwig.actin.molecular.orange.datamodel.TestTreatmentEvidenceFactory;
import com.hartwig.actin.molecular.orange.datamodel.TreatmentEvidence;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class OrangeEventExtractorTest {

    @Test
    public void canExtractHotspotMutations() {
        List<TreatmentEvidence> hotspotEvidence =
                Lists.newArrayList(createTestBuilder().type(EvidenceType.HOTSPOT_MUTATION).gene("gene").event("hotspot").build());
        OrangeEventExtraction hotspotExtraction = OrangeEventExtractor.extract(withEvidences(hotspotEvidence));

        assertEquals(1, hotspotExtraction.mutations().size());
        GeneMutation geneMutation = hotspotExtraction.mutations().iterator().next();
        assertEquals("gene", geneMutation.gene());
        assertEquals("hotspot", geneMutation.mutation());
    }

    @Test
    public void canExtractCodonMutations() {
        OrangeMutationMapper.RangeKey firstCodon = OrangeMutationMapper.CODON_MAPPINGS.keySet().iterator().next();
        List<TreatmentEvidence> codonEvidence = Lists.newArrayList(createTestBuilder().type(EvidenceType.CODON_MUTATION)
                .gene(firstCodon.gene())
                .rangeRank(firstCodon.rank())
                .build());
        OrangeEventExtraction codonExtraction = OrangeEventExtractor.extract(withEvidences(codonEvidence));

        assertEquals(1, codonExtraction.mutations().size());
        GeneMutation geneMutation = codonExtraction.mutations().iterator().next();
        assertEquals(firstCodon.gene(), geneMutation.gene());
        assertEquals(OrangeMutationMapper.CODON_MAPPINGS.get(firstCodon), geneMutation.mutation());
    }

    @Test
    public void canExtractExonMutations() {
        OrangeMutationMapper.RangeKey firstExon = OrangeMutationMapper.EXON_MAPPINGS.keySet().iterator().next();
        List<TreatmentEvidence> exonEvidence = Lists.newArrayList(createTestBuilder().type(EvidenceType.EXON_MUTATION)
                .gene(firstExon.gene())
                .rangeRank(firstExon.rank())
                .build());
        OrangeEventExtraction exonExtraction = OrangeEventExtractor.extract(withEvidences(exonEvidence));

        assertEquals(1, exonExtraction.mutations().size());
        GeneMutation geneMutation = exonExtraction.mutations().iterator().next();
        assertEquals(firstExon.gene(), geneMutation.gene());
        assertEquals(OrangeMutationMapper.EXON_MAPPINGS.get(firstExon), geneMutation.mutation());
    }

    @Test
    public void canExtractActivatedGenes() {
        List<TreatmentEvidence> activationEvidence =
                Lists.newArrayList(createTestBuilder().type(EvidenceType.ACTIVATION).gene("gene").build());
        OrangeEventExtraction activationExtraction = OrangeEventExtractor.extract(withEvidences(activationEvidence));

        assertEquals(1, activationExtraction.activatedGenes().size());
        assertEquals("gene", activationExtraction.activatedGenes().iterator().next());

        List<TreatmentEvidence> fusionEvidence =
                Lists.newArrayList(createTestBuilder().type(EvidenceType.PROMISCUOUS_FUSION).gene("gene").build());
        OrangeEventExtraction fusionExtraction = OrangeEventExtractor.extract(withEvidences(fusionEvidence));

        assertEquals(1, fusionExtraction.activatedGenes().size());
        assertEquals("gene", fusionExtraction.activatedGenes().iterator().next());
    }

    @Test
    public void canExtractInactivatedGenes() {
        ImmutableTreatmentEvidence.Builder builder = createTestBuilder();

        List<TreatmentEvidence> deletedEvidence =
                Lists.newArrayList(builder.type(EvidenceType.INACTIVATION).gene("gene").event("full loss").build());
        OrangeEventExtraction deletedExtraction = OrangeEventExtractor.extract(withEvidences(deletedEvidence));

        assertEquals(1, deletedExtraction.inactivatedGenes().size());
        InactivatedGene inactivatedGene1 = deletedExtraction.inactivatedGenes().iterator().next();
        assertEquals("gene", inactivatedGene1.gene());
        assertTrue(inactivatedGene1.hasBeenDeleted());

        List<TreatmentEvidence> disruptedEvidence =
                Lists.newArrayList(builder.type(EvidenceType.INACTIVATION).gene("gene").event("homozygous disruption").build());
        OrangeEventExtraction disruptedExtraction = OrangeEventExtractor.extract(withEvidences(disruptedEvidence));

        assertEquals(1, disruptedExtraction.inactivatedGenes().size());
        InactivatedGene inactivatedGene2 = disruptedExtraction.inactivatedGenes().iterator().next();
        assertEquals("gene", inactivatedGene2.gene());
        assertFalse(inactivatedGene2.hasBeenDeleted());

        List<TreatmentEvidence> noEvidence =
                Lists.newArrayList(builder.type(EvidenceType.AMPLIFICATION).gene("gene").event("full gain").build());
        OrangeEventExtraction noExtraction = OrangeEventExtractor.extract(withEvidences(noEvidence));

        assertEquals(0, noExtraction.inactivatedGenes().size());
    }

    @Test
    public void canExtractAmplifiedGenes() {
        List<TreatmentEvidence> amplificationEvidence =
                Lists.newArrayList(createTestBuilder().type(EvidenceType.AMPLIFICATION).gene("gene").build());
        OrangeEventExtraction amplificationExtraction = OrangeEventExtractor.extract(withEvidences(amplificationEvidence));

        assertEquals(1, amplificationExtraction.amplifiedGenes().size());
        assertEquals("gene", amplificationExtraction.amplifiedGenes().iterator().next());
    }

    @Test
    public void canExtractFusionGenes() {
        List<TreatmentEvidence> fusionEvidence =
                Lists.newArrayList(createTestBuilder().type(EvidenceType.FUSION_PAIR).event("gene1 - gene2 fusion").build());
        OrangeEventExtraction fusionExtraction = OrangeEventExtractor.extract(withEvidences(fusionEvidence));

        assertEquals(1, fusionExtraction.fusions().size());
        FusionGene fusionGene = fusionExtraction.fusions().iterator().next();
        assertEquals("gene1", fusionGene.fiveGene());
        assertEquals("gene2", fusionGene.threeGene());
    }

    @NotNull
    private static OrangeRecord withEvidences(@NotNull List<TreatmentEvidence> evidences) {
        return ImmutableOrangeRecord.builder().from(TestOrangeDataFactory.createMinimalTestOrangeRecord()).evidences(evidences).build();
    }

    @NotNull
    private static ImmutableTreatmentEvidence.Builder createTestBuilder() {
        return ImmutableTreatmentEvidence.builder()
                .from(TestTreatmentEvidenceFactory.create())
                .addSources(OrangeEventExtractor.ACTIN_SOURCE)
                .reported(true);
    }
}