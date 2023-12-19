package com.hartwig.actin.molecular.sort.driver;

import static org.junit.Assert.assertEquals;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood;

import org.junit.Test;

public class DriverLikelihoodComparatorTest {

    @Test
    public void canSortDriverLikelihoods() {
        DriverLikelihood high = DriverLikelihood.HIGH;
        DriverLikelihood medium = DriverLikelihood.MEDIUM;
        DriverLikelihood low = DriverLikelihood.LOW;
        DriverLikelihood nothing = null;

        List<DriverLikelihood> driverLikelihoods = Lists.newArrayList(medium, low, high, nothing);
        driverLikelihoods.sort(new DriverLikelihoodComparator());

        assertEquals(high, driverLikelihoods.get(0));
        assertEquals(medium, driverLikelihoods.get(1));
        assertEquals(low, driverLikelihoods.get(2));
        assertEquals(nothing, driverLikelihoods.get(3));
    }
}