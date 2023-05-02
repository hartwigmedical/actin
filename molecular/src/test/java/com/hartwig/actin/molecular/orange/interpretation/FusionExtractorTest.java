package com.hartwig.actin.molecular.orange.interpretation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood;
import com.hartwig.actin.molecular.datamodel.driver.Fusion;
import com.hartwig.actin.molecular.datamodel.driver.FusionDriverType;
import com.hartwig.actin.molecular.filter.GeneFilter;
import com.hartwig.actin.molecular.filter.TestGeneFilterFactory;
import com.hartwig.actin.molecular.orange.datamodel.TestOrangeFactory;
import com.hartwig.actin.molecular.orange.datamodel.linx.ImmutableLinxRecord;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxFusion;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxFusionDriverLikelihood;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxFusionType;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxRecord;
import com.hartwig.actin.molecular.orange.datamodel.linx.TestLinxFactory;
import com.hartwig.actin.molecular.orange.evidence.TestEvidenceDatabaseFactory;

import org.junit.Test;

public class FusionExtractorTest {

    @Test
    public void canExtractFusions() {
        LinxFusion linxFusion = TestLinxFactory.fusionBuilder()
                .reported(true)
                .type(LinxFusionType.PROMISCUOUS_5)
                .geneStart("gene start")
                .geneTranscriptStart("trans start")
                .fusedExonUp(1)
                .geneEnd("gene end")
                .geneTranscriptEnd("trans end")
                .fusedExonDown(4)
                .driverLikelihood(LinxFusionDriverLikelihood.HIGH)
                .build();

        LinxRecord linx = ImmutableLinxRecord.builder()
                .from(TestOrangeFactory.createMinimalTestOrangeRecord().linx())
                .addFusions(linxFusion)
                .build();

        GeneFilter geneFilter = TestGeneFilterFactory.createValidForGenes("gene end");
        FusionExtractor fusionExtractor = new FusionExtractor(geneFilter, TestEvidenceDatabaseFactory.createEmptyDatabase());

        Set<Fusion> fusions = fusionExtractor.extract(linx);
        assertEquals(1, fusions.size());

        Fusion fusion = fusions.iterator().next();
        assertTrue(fusion.isReportable());
        assertEquals(DriverLikelihood.HIGH, fusion.driverLikelihood());
        assertEquals("gene start", fusion.geneStart());
        assertEquals("trans start", fusion.geneTranscriptStart());
        assertEquals(1, fusion.fusedExonUp());
        assertEquals("gene end", fusion.geneEnd());
        assertEquals("trans end", fusion.geneTranscriptEnd());
        assertEquals(4, fusion.fusedExonDown());
        assertEquals(FusionDriverType.PROMISCUOUS_5, fusion.driverType());
    }

    @Test (expected = IllegalStateException.class)
    public void shouldThrowExceptionWhenFilteringReportedFusion() {
        LinxFusion linxFusion = TestLinxFactory.fusionBuilder().reported(true).geneStart("other start").geneEnd("other end").build();

        LinxRecord linx = ImmutableLinxRecord.builder()
                .from(TestOrangeFactory.createMinimalTestOrangeRecord().linx())
                .addFusions(linxFusion)
                .build();

        GeneFilter geneFilter = TestGeneFilterFactory.createValidForGenes("weird gene");
        FusionExtractor fusionExtractor = new FusionExtractor(geneFilter, TestEvidenceDatabaseFactory.createEmptyDatabase());
        fusionExtractor.extract(linx);
    }

    @Test
    public void canDetermineDriverTypeForAllFusions() {
        for (LinxFusionType type : LinxFusionType.values()) {
            LinxFusion fusion = TestLinxFactory.fusionBuilder().type(type).build();
            assertNotNull(FusionExtractor.determineDriverType(fusion));
        }
    }

    @Test
    public void canDetermineDriverLikelihoodForAllFusions() {
        LinxFusion high = TestLinxFactory.fusionBuilder().driverLikelihood(LinxFusionDriverLikelihood.HIGH).build();
        assertEquals(DriverLikelihood.HIGH, FusionExtractor.determineDriverLikelihood(high));

        LinxFusion low = TestLinxFactory.fusionBuilder().driverLikelihood(LinxFusionDriverLikelihood.LOW).build();
        assertEquals(DriverLikelihood.LOW, FusionExtractor.determineDriverLikelihood(low));

        LinxFusion na = TestLinxFactory.fusionBuilder().driverLikelihood(LinxFusionDriverLikelihood.NA).build();
        assertNull(FusionExtractor.determineDriverLikelihood(na));
    }
}