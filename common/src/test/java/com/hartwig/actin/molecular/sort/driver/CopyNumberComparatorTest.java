package com.hartwig.actin.molecular.sort.driver;

import static org.junit.Assert.assertEquals;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.molecular.datamodel.driver.CopyNumberDriver;
import com.hartwig.actin.molecular.datamodel.driver.TestAmplificationFactory;
import com.hartwig.actin.molecular.datamodel.driver.TestLossFactory;

import org.junit.Test;

public class CopyNumberComparatorTest {

    @Test
    public void canSortCopyNumbers() {
        CopyNumberDriver driver1 = TestAmplificationFactory.builder().gene("MYC").build();
        CopyNumberDriver driver2 = TestLossFactory.builder().gene("APC").build();

        List<CopyNumberDriver> copyNumberDrivers = Lists.newArrayList(driver1, driver2);
        copyNumberDrivers.sort(new CopyNumberComparator());

        assertEquals(driver2, copyNumberDrivers.get(0));
        assertEquals(driver1, copyNumberDrivers.get(1));
    }
}