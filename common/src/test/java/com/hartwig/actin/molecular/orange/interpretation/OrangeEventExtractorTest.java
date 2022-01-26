package com.hartwig.actin.molecular.orange.interpretation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import com.google.common.collect.Lists;
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