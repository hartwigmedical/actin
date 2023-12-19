package com.hartwig.actin.molecular.sort.driver;

import static org.junit.Assert.assertEquals;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.molecular.datamodel.driver.Driver;
import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood;
import com.hartwig.actin.molecular.datamodel.evidence.ActionableEvidence;
import com.hartwig.actin.molecular.datamodel.evidence.TestActionableEvidenceFactory;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

public class DriverComparatorTest {

    @Test
    public void canSortDrivers() {
        Driver driver1 = create(true, "event 1", DriverLikelihood.HIGH, TestActionableEvidenceFactory.createEmpty());
        Driver driver2 = create(true, "event 1", DriverLikelihood.MEDIUM, TestActionableEvidenceFactory.createEmpty());
        Driver driver3 = create(true, "event 2", DriverLikelihood.MEDIUM, TestActionableEvidenceFactory.createExhaustive());
        Driver driver4 = create(true, "event 2", DriverLikelihood.MEDIUM, TestActionableEvidenceFactory.createEmpty());
        Driver driver5 = create(false, "event 1", DriverLikelihood.HIGH, TestActionableEvidenceFactory.createEmpty());

        List<Driver> drivers = Lists.newArrayList(driver4, driver5, driver1, driver2, driver3);
        drivers.sort(new DriverComparator());

        assertEquals(driver1, drivers.get(0));
        assertEquals(driver2, drivers.get(1));
        assertEquals(driver3, drivers.get(2));
        assertEquals(driver4, drivers.get(3));
        assertEquals(driver5, drivers.get(4));
    }

    @NotNull
    private static Driver create(boolean isReportable, @NotNull String event, @Nullable DriverLikelihood driverLikelihood,
            @NotNull ActionableEvidence evidence) {
        return new Driver() {
            @Override
            public boolean isReportable() {
                return isReportable;
            }

            @NotNull
            @Override
            public String event() {
                return event;
            }

            @Nullable
            @Override
            public DriverLikelihood driverLikelihood() {
                return driverLikelihood;
            }

            @NotNull
            @Override
            public ActionableEvidence evidence() {
                return evidence;
            }


            @Override
            public String toString() {
                return isReportable + " " + event + " " + driverLikelihood;
            }
        };
    }
}