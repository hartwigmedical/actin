package com.hartwig.actin.molecular.orange.evidence.known;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxHomozygousDisruption;
import com.hartwig.actin.molecular.orange.datamodel.linx.TestLinxFactory;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleCopyNumber;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleCopyNumberInterpretation;
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

        PurpleCopyNumber ampOnGene1 = create("gene 1", PurpleCopyNumberInterpretation.FULL_GAIN);
        assertEquals(amp, CopyNumberLookup.findForCopyNumber(knownCopyNumbers, ampOnGene1));

        PurpleCopyNumber ampOnGene2 = create("gene 2", PurpleCopyNumberInterpretation.FULL_GAIN);
        assertNull(CopyNumberLookup.findForCopyNumber(knownCopyNumbers, ampOnGene2));

        PurpleCopyNumber delOnGene1 = create("gene 1", PurpleCopyNumberInterpretation.FULL_LOSS);
        assertNull(CopyNumberLookup.findForCopyNumber(knownCopyNumbers, delOnGene1));

        PurpleCopyNumber delOnGene2 = create("gene 2", PurpleCopyNumberInterpretation.FULL_LOSS);
        assertEquals(del, CopyNumberLookup.findForCopyNumber(knownCopyNumbers, delOnGene2));
    }

    @NotNull
    private static PurpleCopyNumber create(@NotNull String gene, @NotNull PurpleCopyNumberInterpretation interpretation) {
        return TestPurpleFactory.copyNumberBuilder().gene(gene).interpretation(interpretation).build();
    }

    @Test
    public void canLookupHomozygousDisruptions() {
        KnownCopyNumber amp = TestServeKnownFactory.copyNumberBuilder().gene("gene 1").event(GeneEvent.AMPLIFICATION).build();
        KnownCopyNumber del = TestServeKnownFactory.copyNumberBuilder().gene("gene 1").event(GeneEvent.DELETION).build();
        List<KnownCopyNumber> knownCopyNumbers = Lists.newArrayList(amp, del);

        LinxHomozygousDisruption homDisruptionGene1 = TestLinxFactory.homozygousDisruptionBuilder().gene("gene 1").build();
        LinxHomozygousDisruption homDisruptionGene2 = TestLinxFactory.homozygousDisruptionBuilder().gene("gene 2").build();

        assertEquals(del, CopyNumberLookup.findForHomozygousDisruption(knownCopyNumbers, homDisruptionGene1));
        assertNull(CopyNumberLookup.findForHomozygousDisruption(knownCopyNumbers, homDisruptionGene2));
    }
}