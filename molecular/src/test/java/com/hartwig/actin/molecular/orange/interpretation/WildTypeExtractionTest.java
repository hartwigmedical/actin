package com.hartwig.actin.molecular.orange.interpretation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.molecular.orange.datamodel.ImmutableOrangeRecord;
import com.hartwig.actin.molecular.orange.datamodel.OrangeRecord;
import com.hartwig.actin.molecular.orange.datamodel.TestOrangeFactory;
import com.hartwig.actin.molecular.orange.datamodel.purple.ImmutablePurpleRecord;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class WildTypeExtractionTest {

    @Test
    public void canExtractWildTypeGenes() {
        OrangeRecord proper = create(true, true, Sets.newHashSet("KRAS"));
        Set<String> wildTypeGenes = WildTypeExtraction.extract(proper);

        assertNotNull(wildTypeGenes);
        assertEquals(1, wildTypeGenes.size());
        assertEquals("KRAS", wildTypeGenes.iterator().next());

        OrangeRecord noTumorCellsPresent = create(false, true, Sets.newHashSet("KRAS"));
        assertNull(WildTypeExtraction.extract(noTumorCellsPresent));

        OrangeRecord insufficientQuality = create(true, false, Sets.newHashSet("KRAS"));
        assertNull(WildTypeExtraction.extract(insufficientQuality));
    }

    @NotNull
    private static OrangeRecord create(boolean containsTumorCells, boolean hasSufficientQuality, @NotNull Set<String> wildTypeGenes) {
        OrangeRecord base = TestOrangeFactory.createMinimalTestOrangeRecord();
        return ImmutableOrangeRecord.builder()
                .from(base)
                .purple(ImmutablePurpleRecord.builder()
                        .from(base.purple())
                        .containsTumorCells(containsTumorCells)
                        .hasSufficientQuality(hasSufficientQuality)
                        .build())
                .wildTypeGenes(wildTypeGenes)
                .build();
    }
}