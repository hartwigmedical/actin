package com.hartwig.actin.molecular.orange.interpretation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.google.common.collect.Sets;
import com.hartwig.actin.molecular.filter.TestGeneFilterFactory;
import com.hartwig.actin.molecular.orange.datamodel.TestOrangeFactory;
import com.hartwig.actin.molecular.orange.datamodel.linx.ImmutableLinxRecord;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxBreakend;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxBreakendType;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxCodingType;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxRecord;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxRegionType;
import com.hartwig.actin.molecular.orange.datamodel.linx.TestLinxFactory;
import com.hartwig.actin.molecular.orange.evidence.TestEvidenceDatabaseFactory;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class DisruptionExtractorTest {

    @Test
    public void canFilterBreakendWithLosses() {
        String gene = "gene";

        DisruptionExtractor disruptionExtractor = createTestExtractor();
        LinxBreakend breakend1 = TestLinxFactory.breakendBuilder().gene(gene).type(LinxBreakendType.DEL).build();
        assertEquals(0, disruptionExtractor.extractDisruptions(withBreakend(breakend1), Sets.newHashSet(gene)).size());

        LinxBreakend breakend2 = TestLinxFactory.breakendBuilder().gene(gene).type(LinxBreakendType.DUP).build();
        assertEquals(1, disruptionExtractor.extractDisruptions(withBreakend(breakend2), Sets.newHashSet(gene)).size());

        LinxBreakend breakend3 = TestLinxFactory.breakendBuilder().gene("other").type(LinxBreakendType.DEL).build();
        assertEquals(1, disruptionExtractor.extractDisruptions(withBreakend(breakend3), Sets.newHashSet(gene)).size());
    }

    @Test
    public void canDetermineAllDisruptionTypes() {
        for (LinxBreakendType breakendType : LinxBreakendType.values()) {
            assertNotNull(DisruptionExtractor.determineDisruptionType(breakendType));
        }
    }

    @Test
    public void canDetermineAllRegionTypes() {
        for (LinxRegionType regionType : LinxRegionType.values()) {
            if (regionType != LinxRegionType.UNKNOWN) {
                assertNotNull(DisruptionExtractor.determineRegionType(regionType));
            }
        }
    }

    @Test
    public void canDetermineAllCodingTypes() {
        for (LinxCodingType codingType : LinxCodingType.values()) {
            if (codingType != LinxCodingType.UNKNOWN) {
                assertNotNull(DisruptionExtractor.determineCodingContext(codingType));
            }
        }
    }

    @NotNull
    private static LinxRecord withBreakend(@NotNull LinxBreakend breakend) {
        return ImmutableLinxRecord.builder()
                .from(TestOrangeFactory.createMinimalTestOrangeRecord().linx())
                .addStructuralVariants(TestLinxFactory.structuralVariantBuilder().svId(breakend.svId()).build())
                .addBreakends(breakend)
                .build();
    }

    @NotNull
    private static DisruptionExtractor createTestExtractor() {
        return new DisruptionExtractor(TestGeneFilterFactory.createAlwaysValid(), TestEvidenceDatabaseFactory.createEmptyDatabase());
    }
}