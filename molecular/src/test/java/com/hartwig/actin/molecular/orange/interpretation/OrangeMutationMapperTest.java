package com.hartwig.actin.molecular.orange.interpretation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.hartwig.actin.molecular.orange.datamodel.protect.EvidenceType;
import com.hartwig.actin.molecular.orange.datamodel.protect.ImmutableProtectEvidence;
import com.hartwig.actin.molecular.orange.datamodel.protect.ProtectEvidence;
import com.hartwig.actin.molecular.orange.datamodel.protect.TestProtectEvidenceFactory;
import com.hartwig.actin.serve.datamodel.ImmutableServeRecord;
import com.hartwig.actin.serve.datamodel.ServeRecord;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class OrangeMutationMapperTest {

    @Test
    public void canMapHotspotMutations() {
        OrangeMutationMapper mapper = withMutations("X", "Val600Glu", "X", "V600E");

        ProtectEvidence hotspotEvidence1 = ImmutableProtectEvidence.builder()
                .from(TestProtectEvidenceFactory.create())
                .type(EvidenceType.HOTSPOT_MUTATION)
                .gene("X")
                .event("Val600Glu")
                .build();

        Set<String> mutations1 = mapper.map(hotspotEvidence1);
        assertEquals(2, mutations1.size());
        assertTrue(mutations1.contains("Val600Glu"));
        assertTrue(mutations1.contains("V600E"));

        ProtectEvidence hotspotEvidence2 = ImmutableProtectEvidence.builder()
                .from(TestProtectEvidenceFactory.create())
                .type(EvidenceType.HOTSPOT_MUTATION)
                .gene("X")
                .event("V600E")
                .build();

        Set<String> mutations2 = mapper.map(hotspotEvidence2);
        assertEquals(2, mutations2.size());
        assertTrue(mutations2.contains("Val600Glu"));
        assertTrue(mutations2.contains("V600E"));
    }

    @Test
    public void canMapCodonMutations() {
        OrangeMutationMapper mapper = withMutation("X", "R30X");

        ProtectEvidence codonEvidence = ImmutableProtectEvidence.builder()
                .from(TestProtectEvidenceFactory.create())
                .type(EvidenceType.CODON_MUTATION)
                .gene("X")
                .rangeRank(30)
                .build();

        Set<String> mutations = mapper.map(codonEvidence);
        assertEquals(1, mutations.size());
        assertTrue(mutations.contains("R30X"));
    }

    @Test
    public void canMapExonMutations() {
        OrangeMutationMapper mapperExact = withMutation("X", "EXON 20 INSERTION");
        ProtectEvidence exonEvidence20 = ImmutableProtectEvidence.builder()
                .from(TestProtectEvidenceFactory.create())
                .type(EvidenceType.EXON_MUTATION)
                .gene("X")
                .rangeRank(20)
                .build();

        Set<String> exactMutations = mapperExact.map(exonEvidence20);
        assertEquals(1, exactMutations.size());
        assertTrue(exactMutations.contains("EXON 20 INSERTION"));

        OrangeMutationMapper mapperRange = withMutation("X", "exon 2-4");
        ProtectEvidence base = ImmutableProtectEvidence.builder()
                .from(TestProtectEvidenceFactory.create())
                .type(EvidenceType.EXON_MUTATION)
                .gene("X")
                .build();

        ProtectEvidence exonEvidence2 = ImmutableProtectEvidence.builder().from(base).rangeRank(2).build();
        assertTrue(mapperRange.map(exonEvidence2).contains("exon 2-4"));

        ProtectEvidence exonEvidence3 = ImmutableProtectEvidence.builder().from(base).rangeRank(3).build();
        assertTrue(mapperRange.map(exonEvidence3).contains("exon 2-4"));

        ProtectEvidence exonEvidence4 = ImmutableProtectEvidence.builder().from(base).rangeRank(4).build();
        assertTrue(mapperRange.map(exonEvidence4).contains("exon 2-4"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void crashOnInvalidEvidenceType() {
        OrangeMutationMapper mapper = OrangeMutationMapper.fromServeRecords(Lists.newArrayList());
        ProtectEvidence invalid =
                ImmutableProtectEvidence.builder().from(TestProtectEvidenceFactory.create()).type(EvidenceType.ACTIVATION).build();

        mapper.map(invalid);
    }

    @Test(expected = IllegalStateException.class)
    public void crashOnUnmappedHotspot() {
        OrangeMutationMapper mapper = withMutation("X", "V600E");
        ProtectEvidence unmapped = ImmutableProtectEvidence.builder()
                .from(TestProtectEvidenceFactory.create())
                .type(EvidenceType.HOTSPOT_MUTATION)
                .gene("unmapped gene")
                .event("V600E")
                .build();

        mapper.map(unmapped);
    }

    @Test(expected = IllegalStateException.class)
    public void crashOnUnmappedCodon() {
        OrangeMutationMapper mapper = withMutation("X", "V1X");
        ProtectEvidence unmapped = ImmutableProtectEvidence.builder()
                .from(TestProtectEvidenceFactory.create())
                .type(EvidenceType.CODON_MUTATION)
                .gene("unmapped gene")
                .rangeRank(1)
                .build();

        mapper.map(unmapped);
    }

    @Test(expected = IllegalStateException.class)
    public void crashOnUnmappedExon() {
        OrangeMutationMapper mapper = withMutation("X", "V1X");
        ProtectEvidence unmapped = ImmutableProtectEvidence.builder()
                .from(TestProtectEvidenceFactory.create())
                .type(EvidenceType.EXON_MUTATION)
                .gene("unmapped gene")
                .rangeRank(1)
                .build();

        mapper.map(unmapped);
    }

    @NotNull
    private static OrangeMutationMapper withMutation(@NotNull String gene, @NotNull String mutation) {
        List<ServeRecord> records = Lists.newArrayList();

        ImmutableServeRecord.Builder builder = ImmutableServeRecord.builder()
                .trial(Strings.EMPTY)
                .rule(EligibilityRule.MUTATION_IN_GENE_X_OF_TYPE_Y)
                .isUsedAsInclusion(true);

        records.add(builder.gene(gene).mutation(mutation).build());

        return OrangeMutationMapper.fromServeRecords(records);
    }

    @NotNull
    private static OrangeMutationMapper withMutations(@NotNull String gene1, @NotNull String mutation1, @NotNull String gene2,
            @NotNull String mutation2) {
        List<ServeRecord> records = Lists.newArrayList();

        ImmutableServeRecord.Builder builder = ImmutableServeRecord.builder()
                .trial(Strings.EMPTY)
                .rule(EligibilityRule.MUTATION_IN_GENE_X_OF_TYPE_Y)
                .isUsedAsInclusion(true);

        records.add(builder.gene(gene1).mutation(mutation1).build());
        records.add(builder.gene(gene2).mutation(mutation2).build());

        return OrangeMutationMapper.fromServeRecords(records);
    }
}