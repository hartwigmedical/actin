package com.hartwig.actin.molecular.orange.evidence.known;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.hmftools.datamodel.linx.HomozygousDisruption;
import com.hartwig.actin.molecular.orange.datamodel.linx.TestLinxFactory;
import com.hartwig.hmftools.datamodel.purple.PurpleGainLoss;
import com.hartwig.hmftools.datamodel.purple.CopyNumberInterpretation;
import com.hartwig.actin.molecular.orange.datamodel.purple.TestPurpleFactory;
import com.hartwig.serve.datamodel.gene.GeneEvent;
import com.hartwig.serve.datamodel.gene.KnownCopyNumber;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class CopyNumberLookupTest {

    @Test
    public void canLookupCopyNumbers() {
        KnownCopyNumber amp = TestServeKnownFactory.copyNumberBuilder().gene("gene 1").event(GeneEvent.AMPLIFICATION).build();
        KnownCopyNumber del = TestServeKnownFactory.copyNumberBuilder().gene("gene 2").event(GeneEvent.DELETION).build();
        List<KnownCopyNumber> knownCopyNumbers = Lists.newArrayList(amp, del);

        PurpleGainLoss ampOnGene1 = create("gene 1", CopyNumberInterpretation.FULL_GAIN);
        assertEquals(amp, CopyNumberLookup.findForCopyNumber(knownCopyNumbers, ampOnGene1));

        PurpleGainLoss ampOnGene2 = create("gene 2", CopyNumberInterpretation.FULL_GAIN);
        assertNull(CopyNumberLookup.findForCopyNumber(knownCopyNumbers, ampOnGene2));

        PurpleGainLoss delOnGene1 = create("gene 1", CopyNumberInterpretation.FULL_LOSS);
        assertNull(CopyNumberLookup.findForCopyNumber(knownCopyNumbers, delOnGene1));

        PurpleGainLoss delOnGene2 = create("gene 2", CopyNumberInterpretation.FULL_LOSS);
        assertEquals(del, CopyNumberLookup.findForCopyNumber(knownCopyNumbers, delOnGene2));
    }

    @NotNull
    private static PurpleGainLoss create(@NotNull String gene, @NotNull CopyNumberInterpretation interpretation) {
        return TestPurpleFactory.gainLossBuilder().gene(gene).interpretation(interpretation).build();
    }

    @Test
    public void canLookupHomozygousDisruptions() {
        KnownCopyNumber amp = TestServeKnownFactory.copyNumberBuilder().gene("gene 1").event(GeneEvent.AMPLIFICATION).build();
        KnownCopyNumber del = TestServeKnownFactory.copyNumberBuilder().gene("gene 1").event(GeneEvent.DELETION).build();
        List<KnownCopyNumber> knownCopyNumbers = Lists.newArrayList(amp, del);

        HomozygousDisruption homDisruptionGene1 = TestLinxFactory.homozygousDisruptionBuilder().gene("gene 1").build();
        HomozygousDisruption homDisruptionGene2 = TestLinxFactory.homozygousDisruptionBuilder().gene("gene 2").build();

        assertEquals(del, CopyNumberLookup.findForHomozygousDisruption(knownCopyNumbers, homDisruptionGene1));
        assertNull(CopyNumberLookup.findForHomozygousDisruption(knownCopyNumbers, homDisruptionGene2));
    }
}