package com.hartwig.actin.molecular.orange.interpretation;

import static org.junit.Assert.assertEquals;

import com.hartwig.actin.molecular.orange.datamodel.EvidenceType;
import com.hartwig.actin.molecular.orange.datamodel.ImmutableTreatmentEvidence;
import com.hartwig.actin.molecular.orange.datamodel.TestTreatmentEvidenceFactory;
import com.hartwig.actin.molecular.orange.datamodel.TreatmentEvidence;

import org.junit.Test;

public class OrangeMutationMapperTest {

    @Test
    public void canMapHotspotMutations() {
        String firstEvent = OrangeMutationMapper.HOTSPOT_MAPPINGS.keySet().iterator().next();
        TreatmentEvidence hotspotEvidence = ImmutableTreatmentEvidence.builder()
                .from(TestTreatmentEvidenceFactory.create())
                .type(EvidenceType.HOTSPOT_MUTATION)
                .event(firstEvent)
                .build();

        assertEquals(OrangeMutationMapper.HOTSPOT_MAPPINGS.get(firstEvent), OrangeMutationMapper.map(hotspotEvidence));
    }

    @Test
    public void canMapCodonMutations() {
        OrangeMutationMapper.RangeKey firstKey = OrangeMutationMapper.CODON_MAPPINGS.keySet().iterator().next();
        TreatmentEvidence codonEvidence = ImmutableTreatmentEvidence.builder()
                .from(TestTreatmentEvidenceFactory.create())
                .type(EvidenceType.CODON_MUTATION)
                .gene(firstKey.gene())
                .rangeRank(firstKey.rank())
                .build();

        assertEquals(OrangeMutationMapper.CODON_MAPPINGS.get(firstKey), OrangeMutationMapper.map(codonEvidence));
    }

    @Test
    public void canMapExonMutations() {
        OrangeMutationMapper.RangeKey firstKey = OrangeMutationMapper.EXON_MAPPINGS.keySet().iterator().next();
        TreatmentEvidence exonEvidence = ImmutableTreatmentEvidence.builder()
                .from(TestTreatmentEvidenceFactory.create())
                .type(EvidenceType.EXON_MUTATION)
                .gene(firstKey.gene())
                .rangeRank(firstKey.rank())
                .build();

        assertEquals(OrangeMutationMapper.EXON_MAPPINGS.get(firstKey), OrangeMutationMapper.map(exonEvidence));
    }

    @Test (expected = IllegalArgumentException.class)
    public void crashOnInvalidEvidenceType() {
        TreatmentEvidence invalid = ImmutableTreatmentEvidence.builder()
                .from(TestTreatmentEvidenceFactory.create())
                .type(EvidenceType.ACTIVATION)
                .build();

        OrangeMutationMapper.map(invalid);
    }

    @Test (expected = IllegalStateException.class)
    public void crashOnUnmappedCodon() {
        TreatmentEvidence unmapped = ImmutableTreatmentEvidence.builder()
                .from(TestTreatmentEvidenceFactory.create())
                .type(EvidenceType.CODON_MUTATION)
                .gene("unmapped gene")
                .rangeRank(1)
                .build();

        OrangeMutationMapper.map(unmapped);
    }

    @Test (expected = IllegalStateException.class)
    public void crashOnUnmappedExon() {
        TreatmentEvidence unmapped = ImmutableTreatmentEvidence.builder()
                .from(TestTreatmentEvidenceFactory.create())
                .type(EvidenceType.EXON_MUTATION)
                .gene("unmapped gene")
                .rangeRank(1)
                .build();

        OrangeMutationMapper.map(unmapped);
    }
}