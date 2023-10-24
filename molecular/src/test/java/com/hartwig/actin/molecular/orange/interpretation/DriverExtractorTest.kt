package com.hartwig.actin.molecular.orange.interpretation;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.hartwig.actin.molecular.datamodel.driver.CopyNumber;
import com.hartwig.actin.molecular.datamodel.driver.CopyNumberType;
import com.hartwig.actin.molecular.datamodel.driver.Driver;
import com.hartwig.actin.molecular.datamodel.driver.MolecularDrivers;
import com.hartwig.actin.molecular.datamodel.driver.TestCopyNumberFactory;
import com.hartwig.actin.molecular.datamodel.driver.TestFusionFactory;
import com.hartwig.actin.molecular.datamodel.driver.TestVariantFactory;
import com.hartwig.actin.molecular.datamodel.driver.TestVirusFactory;
import com.hartwig.actin.molecular.filter.TestGeneFilterFactory;
import com.hartwig.actin.molecular.orange.datamodel.TestOrangeFactory;
import com.hartwig.actin.molecular.orange.evidence.TestEvidenceDatabaseFactory;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class DriverExtractorTest {

    @Test
    public void canExtractFromMinimalTestData() {
        DriverExtractor driverExtractor = createTestExtractor();
        MolecularDrivers drivers = driverExtractor.extract(TestOrangeFactory.createMinimalTestOrangeRecord());

        assertEquals(0, drivers.variants().size());
        assertEquals(0, drivers.copyNumbers().size());
        assertEquals(0, drivers.homozygousDisruptions().size());
        assertEquals(0, drivers.disruptions().size());
        assertEquals(0, drivers.fusions().size());
        assertEquals(0, drivers.viruses().size());
    }

    @Test
    public void canExtractFromProperTestData() {
        DriverExtractor driverExtractor = createTestExtractor();
        MolecularDrivers drivers = driverExtractor.extract(TestOrangeFactory.createProperTestOrangeRecord());

        assertEquals(1, drivers.variants().size());
        assertEquals(2, drivers.copyNumbers().size());
        assertEquals(1, drivers.homozygousDisruptions().size());
        assertEquals(1, drivers.disruptions().size());
        assertEquals(1, drivers.fusions().size());
        assertEquals(1, drivers.viruses().size());
    }

    @Test
    public void canDetermineReportableLostGenes() {
        List<CopyNumber> copyNumbers =
                Lists.newArrayList(TestCopyNumberFactory.builder().gene("gene 1").type(CopyNumberType.LOSS).isReportable(true).build(),
                        TestCopyNumberFactory.builder().gene("gene 2").type(CopyNumberType.FULL_GAIN).isReportable(true).build(),
                        TestCopyNumberFactory.builder().gene("gene 3").type(CopyNumberType.LOSS).isReportable(false).build());

        Set<String> lostGenes = DriverExtractor.reportableLostGenes(copyNumbers);
        assertEquals(1, lostGenes.size());
        assertEquals("gene 1", lostGenes.iterator().next());
    }

    @Test
    public void canCountReportableDrivers() {
        List<Driver> drivers = Lists.newArrayList(TestVariantFactory.builder().isReportable(true).build(),
                TestCopyNumberFactory.builder().isReportable(false).build(),
                TestFusionFactory.builder().isReportable(true).build(),
                TestVirusFactory.builder().isReportable(false).build());

        assertEquals(2, DriverExtractor.reportableCount(drivers));
        assertEquals(0, DriverExtractor.reportableCount(Lists.newArrayList()));
    }

    @NotNull
    private static DriverExtractor createTestExtractor() {
        return DriverExtractor.create(TestGeneFilterFactory.createAlwaysValid(), TestEvidenceDatabaseFactory.createEmptyDatabase());
    }
}