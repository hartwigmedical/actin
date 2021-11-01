package com.hartwig.actin.molecular.interpretation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.hartwig.actin.molecular.datamodel.EvidenceDirection;
import com.hartwig.actin.molecular.datamodel.EvidenceLevel;
import com.hartwig.actin.molecular.datamodel.GenomicTreatmentEvidence;
import com.hartwig.actin.molecular.datamodel.ImmutableGenomicTreatmentEvidence;
import com.hartwig.actin.molecular.datamodel.ImmutableMolecularRecord;
import com.hartwig.actin.molecular.datamodel.MolecularRecord;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class MolecularInterpreterTest {

    @Test
    public void canInterpretMolecularRecord() {
        MolecularRecord record = recordWithEvidence(createTestEvidences());

        MolecularInterpretation interpretation = MolecularInterpreter.interpret(record);

        Set<String> responsiveEvidence = interpretation.applicableResponsiveEvents();
        assertEquals(1, responsiveEvidence.size());
        assertTrue(responsiveEvidence.contains("Event 1"));

        Set<String> resistanceEvents = interpretation.applicableResistanceEvents();
        assertEquals(1, resistanceEvents.size());
        assertTrue(resistanceEvents.contains("Event 4 (Treatment 1)"));
    }

    @NotNull
    private static List<GenomicTreatmentEvidence> createTestEvidences() {
        List<GenomicTreatmentEvidence> evidences = Lists.newArrayList();

        // Should be included, all good.
        evidences.add(ImmutableGenomicTreatmentEvidence.builder()
                .treatment("Treatment 1")
                .direction(EvidenceDirection.RESPONSIVE)
                .level(EvidenceLevel.A)
                .onLabel(true)
                .genomicEvent("Event 1")
                .build());

        // Should be filtered out since it is C-level off-label evidence.
        evidences.add(ImmutableGenomicTreatmentEvidence.builder()
                .treatment("Treatment 2")
                .direction(EvidenceDirection.RESPONSIVE)
                .level(EvidenceLevel.C)
                .onLabel(false)
                .genomicEvent("Event 2")
                .build());

        // Should be filtered out as it starts with CDKN2A.
        evidences.add(ImmutableGenomicTreatmentEvidence.builder()
                .treatment("Treatment 4")
                .direction(EvidenceDirection.RESPONSIVE)
                .level(EvidenceLevel.A)
                .onLabel(true)
                .genomicEvent("CDKN2A loss")
                .build());

        // Should be filtered since there is no responsive evidence for treatment 3
        evidences.add(ImmutableGenomicTreatmentEvidence.builder()
                .treatment("Treatment 3")
                .direction(EvidenceDirection.RESISTANT)
                .level(EvidenceLevel.B)
                .onLabel(false)
                .genomicEvent("Event 3")
                .build());

        // Should be included, all good.
        evidences.add(ImmutableGenomicTreatmentEvidence.builder()
                .treatment("Treatment 1")
                .direction(EvidenceDirection.RESISTANT)
                .level(EvidenceLevel.A)
                .onLabel(false)
                .genomicEvent("Event 4")
                .build());

        // Should be filtered since evidence level is not high enough
        evidences.add(ImmutableGenomicTreatmentEvidence.builder()
                .treatment("Treatment 1")
                .direction(EvidenceDirection.RESISTANT)
                .level(EvidenceLevel.B)
                .onLabel(false)
                .genomicEvent("Event 5")
                .build());

        return evidences;
    }

    @NotNull
    private static MolecularRecord recordWithEvidence(@NotNull List<GenomicTreatmentEvidence> evidences) {
        return ImmutableMolecularRecord.builder()
                .sampleId(Strings.EMPTY)
                .hasReliableQuality(true)
                .genomicTreatmentEvidences(evidences)
                .build();
    }
}