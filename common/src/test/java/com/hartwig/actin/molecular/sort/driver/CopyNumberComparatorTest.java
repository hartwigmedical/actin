package com.hartwig.actin.molecular.sort.driver;

import static org.junit.Assert.assertEquals;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.molecular.datamodel.driver.CopyNumber;
import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood;
import com.hartwig.actin.molecular.datamodel.driver.TestCopyNumberFactory;

import org.junit.Test;

public class CopyNumberComparatorTest {

    @Test
    public void canSortCopyNumbers() {
        CopyNumber driver1 = TestCopyNumberFactory.builder().driverLikelihood(DriverLikelihood.HIGH).gene("MYC").build();
        CopyNumber driver2 = TestCopyNumberFactory.builder().driverLikelihood(DriverLikelihood.MEDIUM).gene("MYC").build();
        CopyNumber driver3 = TestCopyNumberFactory.builder().driverLikelihood(DriverLikelihood.MEDIUM).gene("NTRK").build();

        List<CopyNumber> copyNumberDrivers = Lists.newArrayList(driver2, driver1, driver3);
        copyNumberDrivers.sort(new CopyNumberComparator());

        assertEquals(driver1, copyNumberDrivers.get(0));
        assertEquals(driver2, copyNumberDrivers.get(1));
        assertEquals(driver3, copyNumberDrivers.get(2));
    }
}