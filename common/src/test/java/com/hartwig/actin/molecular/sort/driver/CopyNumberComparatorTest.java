package com.hartwig.actin.molecular.sort.driver;

import static org.junit.Assert.assertEquals;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.molecular.datamodel.driver.CopyNumberDriver;
import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class CopyNumberComparatorTest {

    @Test
    public void canSortCopyNumbers() {
        CopyNumberDriver driver1 = create("MYC");
        CopyNumberDriver driver2 = create("APC");

        List<CopyNumberDriver> copyNumberDrivers = Lists.newArrayList(driver1, driver2);
        copyNumberDrivers.sort(new CopyNumberComparator());

        assertEquals(driver2, copyNumberDrivers.get(0));
        assertEquals(driver1, copyNumberDrivers.get(1));
    }

    @NotNull
    private static CopyNumberDriver create(@NotNull String gene) {
        return new CopyNumberDriver() {
            @NotNull
            @Override
            public String gene() {
                return gene;
            }

            @Override
            public boolean isPartial() {
                return false;
            }

            @NotNull
            @Override
            public String event() {
                return Strings.EMPTY;
            }

            @NotNull
            @Override
            public DriverLikelihood driverLikelihood() {
                return DriverLikelihood.HIGH;
            }
        };
    }
}