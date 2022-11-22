package com.hartwig.actin.molecular.orange.interpretation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.google.common.collect.Sets;
import com.hartwig.actin.molecular.datamodel.driver.Loss;
import com.hartwig.actin.molecular.datamodel.driver.TestLossFactory;
import com.hartwig.actin.molecular.filter.TestGeneFilterFactory;
import com.hartwig.actin.molecular.orange.datamodel.TestOrangeFactory;
import com.hartwig.actin.molecular.orange.datamodel.linx.ImmutableLinxRecord;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxCodingType;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxDisruption;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxRecord;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxRegionType;
import com.hartwig.actin.molecular.orange.datamodel.linx.TestLinxFactory;
import com.hartwig.actin.molecular.orange.evidence.TestEvidenceDatabaseFactory;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class DisruptionExtractorTest {

    @Test
    public void canFilterDisruptionsWithLosses() {
        String gene = "gene";

        Loss loss = TestLossFactory.builder().gene(gene).build();

        DisruptionExtractor disruptionExtractor = createTestExtractor();
        LinxDisruption disruption1 = TestLinxFactory.disruptionBuilder().gene(gene).type("DEL").build();
        assertEquals(0, disruptionExtractor.extractDisruptions(withDisruption(disruption1), Sets.newHashSet(loss)).size());

        LinxDisruption disruption2 = TestLinxFactory.disruptionBuilder().gene(gene).type("DUP").build();
        assertEquals(1, disruptionExtractor.extractDisruptions(withDisruption(disruption2), Sets.newHashSet(loss)).size());

        LinxDisruption disruption3 = TestLinxFactory.disruptionBuilder().gene("other").type("DEL").build();
        assertEquals(1, disruptionExtractor.extractDisruptions(withDisruption(disruption3), Sets.newHashSet(loss)).size());
    }

    @Test
    public void canConvertRegionTypes() {
        for (LinxRegionType regionType : LinxRegionType.values()) {
            if (regionType != LinxRegionType.UNKNOWN) {
                assertNotNull(DisruptionExtractor.determineRegionType(regionType));
            }
        }
    }

    @Test
    public void canConvertCodingTypes() {
        for (LinxCodingType codingType : LinxCodingType.values()) {
            if (codingType != LinxCodingType.UNKNOWN) {
                assertNotNull(DisruptionExtractor.determineCodingContext(codingType));
            }
        }
    }

    @NotNull
    private static LinxRecord withDisruption(@NotNull LinxDisruption disruption) {
        return ImmutableLinxRecord.builder()
                .from(TestOrangeFactory.createMinimalTestOrangeRecord().linx())
                .addDisruptions(disruption)
                .build();
    }

    @NotNull
    private static DisruptionExtractor createTestExtractor() {
        return new DisruptionExtractor(TestGeneFilterFactory.createAlwaysValid(), TestEvidenceDatabaseFactory.createEmptyDatabase());
    }
}