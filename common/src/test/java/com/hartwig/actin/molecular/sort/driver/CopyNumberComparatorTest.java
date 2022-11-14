package com.hartwig.actin.molecular.sort.driver;

import static org.junit.Assert.assertEquals;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.molecular.datamodel.driver.CopyNumberDriver;
import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood;
import com.hartwig.actin.molecular.datamodel.driver.TestAmplificationFactory;
import com.hartwig.actin.molecular.datamodel.driver.TestLossFactory;

import org.junit.Test;

public class CopyNumberComparatorTest {

    @Test
    public void canSortCopyNumbers() {
        CopyNumberDriver driver1 = TestLossFactory.builder().driverLikelihood(DriverLikelihood.HIGH).gene("MYC").build();
        CopyNumberDriver driver2 = TestAmplificationFactory.builder().driverLikelihood(DriverLikelihood.MEDIUM).gene("MYC").build();
        CopyNumberDriver driver3 = TestAmplificationFactory.builder().driverLikelihood(DriverLikelihood.MEDIUM).gene("NTRK").build();

        List<CopyNumberDriver> copyNumberDrivers = Lists.newArrayList(driver2, driver1, driver3);
        copyNumberDrivers.sort(new CopyNumberComparator());

        assertEquals(driver1, copyNumberDrivers.get(0));
        assertEquals(driver2, copyNumberDrivers.get(1));
        assertEquals(driver3, copyNumberDrivers.get(2));
    }
}