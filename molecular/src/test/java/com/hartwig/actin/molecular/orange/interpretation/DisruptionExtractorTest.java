package com.hartwig.actin.molecular.orange.interpretation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.molecular.datamodel.driver.CodingContext;
import com.hartwig.actin.molecular.datamodel.driver.Disruption;
import com.hartwig.actin.molecular.datamodel.driver.DisruptionType;
import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood;
import com.hartwig.actin.molecular.datamodel.driver.RegionType;
import com.hartwig.actin.molecular.filter.GeneFilter;
import com.hartwig.actin.molecular.filter.TestGeneFilterFactory;
import com.hartwig.actin.molecular.orange.datamodel.TestOrangeFactory;
import com.hartwig.actin.molecular.orange.datamodel.linx.ImmutableLinxRecord;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxBreakend;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxBreakendType;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxCodingType;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxRecord;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxRegionType;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxStructuralVariant;
import com.hartwig.actin.molecular.orange.datamodel.linx.TestLinxFactory;
import com.hartwig.actin.molecular.orange.evidence.TestEvidenceDatabaseFactory;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class DisruptionExtractorTest {

    private static final double EPSILON = 1.0E-10;

    @Test
    public void canExtractBreakends() {
        LinxStructuralVariant structuralVariant1 = TestLinxFactory.structuralVariantBuilder().svId(1).clusterId(5).build();
        LinxBreakend breakend1 = TestLinxFactory.breakendBuilder()
                .gene("gene 1")
                .reported(true)
                .type(LinxBreakendType.DUP)
                .junctionCopyNumber(0.2)
                .undisruptedCopyNumber(1.6)
                .regionType(LinxRegionType.EXONIC)
                .codingType(LinxCodingType.CODING)
                .svId(1)
                .build();
        LinxBreakend breakend2 = TestLinxFactory.breakendBuilder().gene("gene 2").reported(true).build();

        LinxRecord linx = ImmutableLinxRecord.builder()
                .from(TestOrangeFactory.createMinimalTestOrangeRecord().linx())
                .addStructuralVariants(structuralVariant1)
                .addBreakends(breakend1, breakend2)
                .build();

        GeneFilter geneFilter = TestGeneFilterFactory.createValidForGenes(breakend1.gene());
        DisruptionExtractor disruptionExtractor = new DisruptionExtractor(geneFilter, TestEvidenceDatabaseFactory.createEmptyDatabase());

        Set<Disruption> disruptions = disruptionExtractor.extractDisruptions(linx, Sets.newHashSet());
        assertEquals(1, disruptions.size());

        Disruption disruption = disruptions.iterator().next();
        assertTrue(disruption.isReportable());
        assertEquals(DriverLikelihood.LOW, disruption.driverLikelihood());
        assertEquals(DisruptionType.DUP, disruption.type());
        assertEquals(0.2, disruption.junctionCopyNumber(), EPSILON);
        assertEquals(1.6, disruption.undisruptedCopyNumber(), EPSILON);
        assertEquals(RegionType.EXONIC, disruption.regionType());
        assertEquals(CodingContext.CODING, disruption.codingContext());
        assertEquals(5, disruption.clusterGroup());
    }

    @Test
    public void canFilterBreakendWithLosses() {
        String gene = "gene";

        DisruptionExtractor disruptionExtractor =
                new DisruptionExtractor(TestGeneFilterFactory.createAlwaysValid(), TestEvidenceDatabaseFactory.createEmptyDatabase());
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
}