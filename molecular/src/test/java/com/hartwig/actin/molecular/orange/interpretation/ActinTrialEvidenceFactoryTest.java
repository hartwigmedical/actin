package com.hartwig.actin.molecular.orange.interpretation;

import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.molecular.datamodel.evidence.MolecularEventType;
import com.hartwig.actin.molecular.interpretation.GeneMutation;
import com.hartwig.actin.molecular.orange.datamodel.protect.ProtectEvidence;
import com.hartwig.actin.molecular.orange.datamodel.protect.TestProtectDataFactory;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class ActinTrialEvidenceFactoryTest {

    @Test
    public void canExtractActinTrials() {
        Set<ProtectEvidence> evidences = Sets.newHashSet();
        evidences.add(withEvent("ACTIVATING_MUTATION_IN_GENE_X: ABL1"));
        evidences.add(withEventAndType("ACTIVATION_OR_AMPLIFICATION_OF_GENE_X: EGFR", MolecularEventType.ACTIVATION));
        evidences.add(withEventAndType("ACTIVATION_OR_AMPLIFICATION_OF_GENE_X: MYC", MolecularEventType.AMPLIFICATION));
        evidences.add(withEvent("AMPLIFICATION_OF_GENE_X: CCND1"));
        evidences.add(withEvent("FUSION_IN_GENE_X: ALK"));
        evidences.add(withEvent("WILDTYPE_OF_GENE_X: KRAS"));
        evidences.add(withEvent("INACTIVATION_OF_GENE_X: BAP1"));
        evidences.add(withEvent("MUTATION_IN_GENE_X_OF_TYPE_Y: BRAF V600E"));
        evidences.add(withEvent("MUTATION_IN_GENE_X_OF_TYPE_Y: KRAS exon 2-4"));

//        ActionableActinEvents actionableActinEvents = ActinTrialEvidenceFactory.create(withEvidences(evidences));
//        assertEquals(2, actionableActinEvents.mutations().size());
//        GeneMutation braf = findByGene(actionableActinEvents.mutations(), "BRAF");
//        assertEquals("V600E", braf.mutation());
//
//        GeneMutation kras = findByGene(actionableActinEvents.mutations(), "KRAS");
//        assertEquals("exon 2-4", kras.mutation());
//
//        assertEquals(2, actionableActinEvents.activatedGenes().size());
//        assertTrue(actionableActinEvents.activatedGenes().contains("ABL1"));
//        assertTrue(actionableActinEvents.activatedGenes().contains("EGFR"));
//
//        assertEquals(1, actionableActinEvents.inactivatedGenes().size());
//        assertTrue(actionableActinEvents.inactivatedGenes().contains("BAP1"));
//
//        assertEquals(2, actionableActinEvents.amplifiedGenes().size());
//        assertTrue(actionableActinEvents.amplifiedGenes().contains("CCND1"));
//        assertTrue(actionableActinEvents.amplifiedGenes().contains("MYC"));
//
//        assertEquals(1, actionableActinEvents.wildtypeGenes().size());
//        assertTrue(actionableActinEvents.wildtypeGenes().contains("KRAS"));
//
//        assertEquals(1, actionableActinEvents.fusedGenes().size());
//        assertTrue(actionableActinEvents.fusedGenes().contains("ALK"));
    }

    @NotNull
    private static GeneMutation findByGene(@NotNull Iterable<GeneMutation> geneMutations, @NotNull String geneToFind) {
        for (GeneMutation geneMutation : geneMutations) {
            if (geneMutation.gene().equals(geneToFind)) {
                return geneMutation;
            }
        }

        throw new IllegalStateException("Could not find gene mutation for gene: " + geneToFind);
    }

    @NotNull
    private static ProtectEvidence withEvent(@NotNull String sourceEvent) {
        return TestProtectDataFactory.builder().addSources(TestProtectDataFactory.sourceBuilder().event(sourceEvent).build()).build();
    }

    @NotNull
    private static ProtectEvidence withEventAndType(@NotNull String sourceEvent, @NotNull MolecularEventType sourceType) {
        return TestProtectDataFactory.builder().addSources(TestProtectDataFactory.sourceBuilder().event(sourceEvent).build()).build();
    }

}