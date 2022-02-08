package com.hartwig.actin.molecular.orange.interpretation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
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
        TreatmentEvidence hotspotEvidence = createTestBuilder().type(EvidenceType.HOTSPOT_MUTATION).gene("gene").event("hotspot").build();
        OrangeEventExtraction hotspotExtraction = createTestEventExtractor().extract(withEvidence(hotspotEvidence));

        assertEquals(1, hotspotExtraction.mutations().size());
        GeneMutation geneMutation = hotspotExtraction.mutations().iterator().next();
        assertEquals("gene", geneMutation.gene());
        assertEquals("hotspot", geneMutation.mutation());
    }

    @Test
    public void canExtractCodonMutations() {
        TreatmentEvidence codonEvidence = createTestBuilder().type(EvidenceType.CODON_MUTATION).gene("gene").event("codon 40").build();
        OrangeEventExtraction codonExtraction = createTestEventExtractor().extract(withEvidence(codonEvidence));

        assertEquals(1, codonExtraction.mutations().size());
        GeneMutation geneMutation = codonExtraction.mutations().iterator().next();
        assertEquals("gene", geneMutation.gene());
        assertEquals("codon 40", geneMutation.mutation());
    }

    @Test
    public void canExtractExonMutations() {
        TreatmentEvidence exonEvidence = createTestBuilder().type(EvidenceType.EXON_MUTATION).gene("gene").event("exon 2-3").build();
        OrangeEventExtraction exonExtraction = createTestEventExtractor().extract(withEvidence(exonEvidence));

        assertEquals(1, exonExtraction.mutations().size());
        GeneMutation geneMutation = exonExtraction.mutations().iterator().next();
        assertEquals("gene", geneMutation.gene());
        assertEquals("exon 2-3", geneMutation.mutation());
    }

    @Test
    public void canExtractActivatedGenes() {
        TreatmentEvidence activationEvidence = createTestBuilder().type(EvidenceType.ACTIVATION).gene("gene").build();
        OrangeEventExtraction activationExtraction = createTestEventExtractor().extract(withEvidence(activationEvidence));

        assertEquals(1, activationExtraction.activatedGenes().size());
        assertEquals("gene", activationExtraction.activatedGenes().iterator().next());
    }

    @Test
    public void canExtractInactivatedGenes() {
        OrangeEventExtractor extractor = createTestEventExtractor();

        TreatmentEvidence deletedEvidence = createTestBuilder().type(EvidenceType.INACTIVATION).gene("gene").event("full loss").build();
        OrangeEventExtraction deletedExtraction = extractor.extract(withEvidence(deletedEvidence));

        assertEquals(1, deletedExtraction.inactivatedGenes().size());
        InactivatedGene inactivatedGene1 = deletedExtraction.inactivatedGenes().iterator().next();
        assertEquals("gene", inactivatedGene1.gene());
        assertTrue(inactivatedGene1.hasBeenDeleted());

        TreatmentEvidence disruptedEvidence =
                createTestBuilder().type(EvidenceType.INACTIVATION).gene("gene").event("homozygous disruption").build();
        OrangeEventExtraction disruptedExtraction = extractor.extract(withEvidence(disruptedEvidence));

        assertEquals(1, disruptedExtraction.inactivatedGenes().size());
        InactivatedGene inactivatedGene2 = disruptedExtraction.inactivatedGenes().iterator().next();
        assertEquals("gene", inactivatedGene2.gene());
        assertFalse(inactivatedGene2.hasBeenDeleted());

        TreatmentEvidence noEvidence = createTestBuilder().type(EvidenceType.AMPLIFICATION).gene("gene").event("full gain").build();
        OrangeEventExtraction noExtraction = extractor.extract(withEvidence(noEvidence));

        assertEquals(0, noExtraction.inactivatedGenes().size());
    }

    @Test
    public void canExtractAmplifiedGenes() {
        TreatmentEvidence amplificationEvidence = createTestBuilder().type(EvidenceType.AMPLIFICATION).gene("gene").build();
        OrangeEventExtraction amplificationExtraction = createTestEventExtractor().extract(withEvidence(amplificationEvidence));

        assertEquals(1, amplificationExtraction.amplifiedGenes().size());
        assertEquals("gene", amplificationExtraction.amplifiedGenes().iterator().next());
    }

    @Test
    public void canExtractFusionGenes() {
        OrangeEventExtractor extractor = createTestEventExtractor();
        TreatmentEvidence fusionEvidence = createTestBuilder().type(EvidenceType.FUSION_PAIR).event("gene1 - gene2 fusion").build();
        OrangeEventExtraction fusionExtraction = extractor.extract(withEvidence(fusionEvidence));

        assertEquals(1, fusionExtraction.fusions().size());
        FusionGene fusionGene = fusionExtraction.fusions().iterator().next();
        assertEquals("gene1", fusionGene.fiveGene());
        assertEquals("gene2", fusionGene.threeGene());

        TreatmentEvidence promiscuousEvidence =
                createTestBuilder().type(EvidenceType.PROMISCUOUS_FUSION).event("gene1 - gene2 fusion").build();
        OrangeEventExtraction promiscuousExtraction = extractor.extract(withEvidence(promiscuousEvidence));

        assertEquals(1, promiscuousExtraction.fusions().size());
        FusionGene promiscuous = promiscuousExtraction.fusions().iterator().next();
        assertEquals("gene1", promiscuous.fiveGene());
        assertEquals("gene2", promiscuous.threeGene());
    }

    @NotNull
    private static OrangeRecord withEvidence(@NotNull TreatmentEvidence evidence) {
        return ImmutableOrangeRecord.builder()
                .from(TestOrangeDataFactory.createMinimalTestOrangeRecord())
                .evidences(Lists.newArrayList(evidence))
                .build();
    }

    @NotNull
    private static ImmutableTreatmentEvidence.Builder createTestBuilder() {
        return ImmutableTreatmentEvidence.builder()
                .from(TestTreatmentEvidenceFactory.create())
                .addSources(OrangeEventExtractor.ACTIN_SOURCE)
                .reported(true);
    }

    @NotNull
    private static OrangeEventExtractor createTestEventExtractor() {
        return new OrangeEventExtractor(evidence -> Sets.newHashSet(evidence.event()));
    }
}