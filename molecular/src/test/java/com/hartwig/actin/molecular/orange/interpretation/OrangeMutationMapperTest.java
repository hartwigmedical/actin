package com.hartwig.actin.molecular.orange.interpretation;

import static org.junit.Assert.assertEquals;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.molecular.orange.datamodel.EvidenceType;
import com.hartwig.actin.molecular.orange.datamodel.ImmutableTreatmentEvidence;
import com.hartwig.actin.molecular.orange.datamodel.TestTreatmentEvidenceFactory;
import com.hartwig.actin.molecular.orange.datamodel.TreatmentEvidence;
import com.hartwig.actin.serve.datamodel.ImmutableServeRecord;
import com.hartwig.actin.serve.datamodel.ServeRecord;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class OrangeMutationMapperTest {

    @Test
    public void canMapHotspotMutations() {
        OrangeMutationMapper mapper = withMutation("X", "Val600Glu");

        TreatmentEvidence hotspotEvidence1 = ImmutableTreatmentEvidence.builder()
                .from(TestTreatmentEvidenceFactory.create())
                .type(EvidenceType.HOTSPOT_MUTATION)
                .gene("X")
                .event("Val600Glu")
                .build();

        assertEquals("Val600Glu", mapper.map(hotspotEvidence1));

        TreatmentEvidence hotspotEvidence2 = ImmutableTreatmentEvidence.builder()
                .from(TestTreatmentEvidenceFactory.create())
                .type(EvidenceType.HOTSPOT_MUTATION)
                .gene("X")
                .event("V600E")
                .build();

        assertEquals("Val600Glu", mapper.map(hotspotEvidence2));
    }

    @Test
    public void canMapCodonMutations() {
        OrangeMutationMapper mapper = withMutation("X", "R30X");

        TreatmentEvidence codonEvidence = ImmutableTreatmentEvidence.builder()
                .from(TestTreatmentEvidenceFactory.create())
                .type(EvidenceType.CODON_MUTATION)
                .gene("X")
                .rangeRank(30)
                .build();

        assertEquals("R30X", mapper.map(codonEvidence));
    }

    @Test
    public void canMapExonMutations() {
        OrangeMutationMapper mapperExact = withMutation("X", "EXON 20 INSERTION");
        TreatmentEvidence exonEvidence20 = ImmutableTreatmentEvidence.builder()
                .from(TestTreatmentEvidenceFactory.create())
                .type(EvidenceType.EXON_MUTATION)
                .gene("X")
                .rangeRank(20)
                .build();

        assertEquals("EXON 20 INSERTION", mapperExact.map(exonEvidence20));

        OrangeMutationMapper mapperRange = withMutation("X", "exon 2-4");
        TreatmentEvidence exonEvidence2 = ImmutableTreatmentEvidence.builder()
                .from(TestTreatmentEvidenceFactory.create())
                .type(EvidenceType.EXON_MUTATION)
                .gene("X")
                .rangeRank(2)
                .build();

        assertEquals("exon 2-4", mapperRange.map(exonEvidence2));

        TreatmentEvidence exonEvidence3 = ImmutableTreatmentEvidence.builder().from(exonEvidence20).rangeRank(3).build();
        assertEquals("exon 2-4", mapperRange.map(exonEvidence3));

        TreatmentEvidence exonEvidence4 = ImmutableTreatmentEvidence.builder().from(exonEvidence20).rangeRank(4).build();
        assertEquals("exon 2-4", mapperRange.map(exonEvidence4));
    }

    @Test(expected = IllegalArgumentException.class)
    public void crashOnInvalidEvidenceType() {
        OrangeMutationMapper mapper = OrangeMutationMapper.fromServeRecords(Lists.newArrayList());
        TreatmentEvidence invalid =
                ImmutableTreatmentEvidence.builder().from(TestTreatmentEvidenceFactory.create()).type(EvidenceType.ACTIVATION).build();

        mapper.map(invalid);
    }

    @Test(expected = IllegalStateException.class)
    public void crashOnUnmappedHotspot() {
        OrangeMutationMapper mapper = withMutation("X", "V600E");
        TreatmentEvidence unmapped = ImmutableTreatmentEvidence.builder()
                .from(TestTreatmentEvidenceFactory.create())
                .type(EvidenceType.HOTSPOT_MUTATION)
                .gene("unmapped gene")
                .event("V600E")
                .build();

        mapper.map(unmapped);
    }

    @Test(expected = IllegalStateException.class)
    public void crashOnUnmappedCodon() {
        OrangeMutationMapper mapper = withMutation("X", "V1X");
        TreatmentEvidence unmapped = ImmutableTreatmentEvidence.builder()
                .from(TestTreatmentEvidenceFactory.create())
                .type(EvidenceType.CODON_MUTATION)
                .gene("unmapped gene")
                .rangeRank(1)
                .build();

        mapper.map(unmapped);
    }

    @Test(expected = IllegalStateException.class)
    public void crashOnUnmappedExon() {
        OrangeMutationMapper mapper = withMutation("X", "V1X");
        TreatmentEvidence unmapped = ImmutableTreatmentEvidence.builder()
                .from(TestTreatmentEvidenceFactory.create())
                .type(EvidenceType.EXON_MUTATION)
                .gene("unmapped gene")
                .rangeRank(1)
                .build();

        mapper.map(unmapped);
    }

    @NotNull
    private static OrangeMutationMapper withMutation(@NotNull String gene, @NotNull String mutation) {
        List<ServeRecord> records = Lists.newArrayList();

        records.add(ImmutableServeRecord.builder()
                .trial(Strings.EMPTY)
                .rule(EligibilityRule.MUTATION_IN_GENE_X_OF_TYPE_Y)
                .gene(gene)
                .mutation(mutation)
                .isUsedAsInclusion(true)
                .build());

        return OrangeMutationMapper.fromServeRecords(records);
    }
}