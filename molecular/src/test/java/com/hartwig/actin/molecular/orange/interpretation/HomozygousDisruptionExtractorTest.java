package com.hartwig.actin.molecular.orange.interpretation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood;
import com.hartwig.actin.molecular.datamodel.driver.HomozygousDisruption;
import com.hartwig.actin.molecular.filter.GeneFilter;
import com.hartwig.actin.molecular.filter.TestGeneFilterFactory;
import com.hartwig.actin.molecular.orange.datamodel.TestOrangeFactory;
import com.hartwig.actin.molecular.orange.datamodel.linx.ImmutableLinxRecord;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxHomozygousDisruption;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxRecord;
import com.hartwig.actin.molecular.orange.datamodel.linx.TestLinxFactory;
import com.hartwig.actin.molecular.orange.evidence.TestEvidenceDatabaseFactory;

import org.junit.Test;

public class HomozygousDisruptionExtractorTest {

    @Test
    public void canExtractHomozygousDisruptions() {
        LinxHomozygousDisruption linxHomDisruption = TestLinxFactory.homozygousDisruptionBuilder().gene("gene 1").build();

        LinxRecord linx = ImmutableLinxRecord.builder()
                .from(TestOrangeFactory.createMinimalTestOrangeRecord().linx())
                .addHomozygousDisruptions(linxHomDisruption)
                .build();

        GeneFilter geneFilter = TestGeneFilterFactory.createValidForGenes(linxHomDisruption.gene());
        HomozygousDisruptionExtractor homDisruptionExtractor =
                new HomozygousDisruptionExtractor(geneFilter, TestEvidenceDatabaseFactory.createEmptyDatabase());

        Set<HomozygousDisruption> homDisruptions = homDisruptionExtractor.extractHomozygousDisruptions(linx);
        assertEquals(1, homDisruptions.size());

        HomozygousDisruption homDisruption = homDisruptions.iterator().next();
        assertTrue(homDisruption.isReportable());
        assertEquals(DriverLikelihood.HIGH, homDisruption.driverLikelihood());
        assertEquals("gene 1", homDisruption.gene());
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionWhenFilteringReportedHomozygousDisruption() {
        LinxHomozygousDisruption linxHomDisruption = TestLinxFactory.homozygousDisruptionBuilder().gene("gene 1").build();

        LinxRecord linx = ImmutableLinxRecord.builder()
                .from(TestOrangeFactory.createMinimalTestOrangeRecord().linx())
                .addHomozygousDisruptions(linxHomDisruption)
                .build();

        GeneFilter geneFilter = TestGeneFilterFactory.createValidForGenes("other gene");
        HomozygousDisruptionExtractor homDisruptionExtractor =
                new HomozygousDisruptionExtractor(geneFilter, TestEvidenceDatabaseFactory.createEmptyDatabase());

        homDisruptionExtractor.extractHomozygousDisruptions(linx);
    }
}