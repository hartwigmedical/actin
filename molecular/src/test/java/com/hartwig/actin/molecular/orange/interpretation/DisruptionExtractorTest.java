package com.hartwig.actin.molecular.orange.interpretation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.Set;
import java.util.List;

import com.google.common.collect.Sets;
import com.hartwig.actin.molecular.datamodel.driver.CodingContext;
import com.hartwig.actin.molecular.datamodel.driver.Disruption;
import com.hartwig.actin.molecular.datamodel.driver.DisruptionType;
import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood;
import com.hartwig.actin.molecular.datamodel.driver.RegionType;
import com.hartwig.actin.molecular.filter.GeneFilter;
import com.hartwig.actin.molecular.filter.TestGeneFilterFactory;
import com.hartwig.actin.molecular.orange.datamodel.TestOrangeFactory;
import com.hartwig.actin.molecular.orange.datamodel.linx.TestLinxFactory;
import com.hartwig.actin.molecular.orange.datamodel.purple.TestPurpleFactory;
import com.hartwig.actin.molecular.orange.evidence.TestEvidenceDatabaseFactory;
import com.hartwig.hmftools.datamodel.gene.TranscriptCodingType;
import com.hartwig.hmftools.datamodel.gene.TranscriptRegionType;
import com.hartwig.hmftools.datamodel.linx.ImmutableLinxRecord;
import com.hartwig.hmftools.datamodel.linx.LinxBreakend;
import com.hartwig.hmftools.datamodel.linx.LinxBreakendType;
import com.hartwig.hmftools.datamodel.linx.LinxRecord;
import com.hartwig.hmftools.datamodel.linx.LinxSvAnnotation;
import com.hartwig.hmftools.datamodel.purple.PurpleDriver;
import com.hartwig.hmftools.datamodel.purple.PurpleDriverType;

import org.assertj.core.util.Lists;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class DisruptionExtractorTest {

    private static final double EPSILON = 1.0E-10;

    @Test
    public void canExtractBreakends() {
        LinxSvAnnotation structuralVariant1 = TestLinxFactory.structuralVariantBuilder().svId(1).clusterId(5).build();
        LinxBreakend linxBreakend = TestLinxFactory.breakendBuilder()
                .gene("gene 1")
                .reportedDisruption(true)
                .type(LinxBreakendType.DUP)
                .junctionCopyNumber(0.2)
                .undisruptedCopyNumber(1.6)
                .regionType(TranscriptRegionType.EXONIC)
                .codingType(TranscriptCodingType.CODING)
                .svId(1)
                .build();

        LinxRecord linx = ImmutableLinxRecord.builder()
                .from(TestOrangeFactory.createMinimalTestOrangeRecord().linx())
                .addAllSomaticStructuralVariants(structuralVariant1)
                .addAllSomaticBreakends(linxBreakend)
                .build();

        GeneFilter geneFilter = TestGeneFilterFactory.createValidForGenes(linxBreakend.gene());
        DisruptionExtractor disruptionExtractor = new DisruptionExtractor(geneFilter, TestEvidenceDatabaseFactory.createEmptyDatabase());

        Set<Disruption> disruptions = disruptionExtractor.extractDisruptions(linx, Sets.newHashSet(), List.of());
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

    @Test (expected = IllegalStateException.class)
    public void shouldThrowExceptionWhenFilteringReportedDisruption() {
        LinxBreakend linxBreakend = TestLinxFactory.breakendBuilder().gene("gene 1").reportedDisruption(true).build();
        LinxRecord linx = ImmutableLinxRecord.builder()
                .from(TestOrangeFactory.createMinimalTestOrangeRecord().linx())
                .addAllSomaticBreakends(linxBreakend)
                .build();

        GeneFilter geneFilter = TestGeneFilterFactory.createValidForGenes("weird gene");
        DisruptionExtractor disruptionExtractor = new DisruptionExtractor(geneFilter, TestEvidenceDatabaseFactory.createEmptyDatabase());
        disruptionExtractor.extractDisruptions(linx, Collections.emptySet(), Lists.newArrayList(TestPurpleFactory.driverBuilder().build()));
    }

    @Test
    public void canFilterBreakendWithLosses() {
        String gene = "gene";

        DisruptionExtractor disruptionExtractor =
                new DisruptionExtractor(TestGeneFilterFactory.createAlwaysValid(), TestEvidenceDatabaseFactory.createEmptyDatabase());
        LinxBreakend breakend1 = TestLinxFactory.breakendBuilder().gene(gene).type(LinxBreakendType.DEL).build();
        assertEquals(0, disruptionExtractor.extractDisruptions(withBreakend(breakend1), Sets.newHashSet(gene), List.of()).size());

        LinxBreakend breakend2 = TestLinxFactory.breakendBuilder().gene(gene).type(LinxBreakendType.DUP).build();
        assertEquals(1, disruptionExtractor.extractDisruptions(withBreakend(breakend2), Sets.newHashSet(gene), List.of()).size());

        LinxBreakend breakend3 = TestLinxFactory.breakendBuilder().gene("other").type(LinxBreakendType.DEL).build();
        assertEquals(1, disruptionExtractor.extractDisruptions(withBreakend(breakend3), Sets.newHashSet(gene), List.of()).size());
    }

    @Test
    public void canDetermineAllDisruptionTypes() {
        for (LinxBreakendType breakendType : LinxBreakendType.values()) {
            assertNotNull(DisruptionExtractor.determineDisruptionType(breakendType));
        }
    }

    @Test
    public void canDetermineAllRegionTypes() {
        for (TranscriptRegionType regionType : TranscriptRegionType.values()) {
            if (regionType != TranscriptRegionType.UNKNOWN) {
                assertNotNull(DisruptionExtractor.determineRegionType(regionType));
            }
        }
    }

    @Test
    public void canDetermineAllCodingTypes() {
        for (TranscriptCodingType codingType : TranscriptCodingType.values()) {
            if (codingType != TranscriptCodingType.UNKNOWN) {
                assertNotNull(DisruptionExtractor.determineCodingContext(codingType));
            }
        }
    }

    @Test
    public void canGenerateUndisruptedCopyNumberForHomDupDisruptions() {
        LinxBreakend linxBreakend = TestLinxFactory.breakendBuilder()
                .gene("gene")
                .type(LinxBreakendType.DUP)
                .junctionCopyNumber(1.2)
                .undisruptedCopyNumber(1.4)
                .svId(1)
                .build();

        LinxSvAnnotation structuralVariant1 = TestLinxFactory.structuralVariantBuilder().svId(1).clusterId(2).build();
        LinxRecord linx = ImmutableLinxRecord.builder()
                .from(TestOrangeFactory.createMinimalTestOrangeRecord().linx())
                .addAllSomaticStructuralVariants(structuralVariant1)
                .addAllSomaticBreakends(linxBreakend)
                .build();
        PurpleDriver driver = TestPurpleFactory.driverBuilder().gene("gene").driver(PurpleDriverType.HOM_DUP_DISRUPTION).build();

        GeneFilter geneFilter = TestGeneFilterFactory.createValidForGenes(linxBreakend.gene());
        DisruptionExtractor disruptionExtractor = new DisruptionExtractor(geneFilter, TestEvidenceDatabaseFactory.createEmptyDatabase());

        Set<Disruption> disruptions = disruptionExtractor.extractDisruptions(linx, Sets.newHashSet(), List.of(driver));
        Disruption disruption = disruptions.iterator().next();
        assertEquals(0.2, disruption.undisruptedCopyNumber(), EPSILON);
    }

    @NotNull
    private static LinxRecord withBreakend(@NotNull LinxBreakend breakend) {
        return ImmutableLinxRecord.builder()
                .from(TestOrangeFactory.createMinimalTestOrangeRecord().linx())
                .addAllSomaticStructuralVariants(TestLinxFactory.structuralVariantBuilder().svId(breakend.svId()).build())
                .addAllSomaticBreakends(breakend)
                .build();
    }
}