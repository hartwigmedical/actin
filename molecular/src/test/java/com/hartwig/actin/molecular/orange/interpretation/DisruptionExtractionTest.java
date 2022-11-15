package com.hartwig.actin.molecular.orange.interpretation;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.Sets;
import com.hartwig.actin.molecular.datamodel.driver.Loss;
import com.hartwig.actin.molecular.datamodel.driver.TestLossFactory;
import com.hartwig.actin.molecular.orange.datamodel.TestOrangeFactory;
import com.hartwig.actin.molecular.orange.datamodel.linx.ImmutableLinxRecord;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxDisruption;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxRecord;
import com.hartwig.actin.molecular.orange.datamodel.linx.TestLinxFactory;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class DisruptionExtractionTest {

    @Test
    public void canFilterDisruptionsWithLosses() {
        String gene = "gene";

        Loss loss = TestLossFactory.builder().gene(gene).build();

        LinxDisruption disruption1 = TestLinxFactory.disruptionBuilder().gene(gene).type("DEL").build();
        assertEquals(0, DisruptionExtraction.extractDisruptions(withDisruption(disruption1), Sets.newHashSet(loss)).size());

        LinxDisruption disruption2 = TestLinxFactory.disruptionBuilder().gene(gene).type("DUP").build();
        assertEquals(1, DisruptionExtraction.extractDisruptions(withDisruption(disruption2), Sets.newHashSet(loss)).size());

        LinxDisruption disruption3 = TestLinxFactory.disruptionBuilder().gene("other").type("DEL").build();
        assertEquals(1, DisruptionExtraction.extractDisruptions(withDisruption(disruption3), Sets.newHashSet(loss)).size());
    }

    @NotNull
    private static LinxRecord withDisruption(@NotNull LinxDisruption disruption) {
        return ImmutableLinxRecord.builder()
                .from(TestOrangeFactory.createMinimalTestOrangeRecord().linx())
                .addDisruptions(disruption)
                .build();
    }
}