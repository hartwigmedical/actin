package com.hartwig.actin.molecular.sort.driver;

import java.util.Comparator;

import com.hartwig.actin.molecular.datamodel.driver.Driver;
import com.hartwig.actin.molecular.sort.evidence.ActionableEvidenceComparator;

import org.jetbrains.annotations.NotNull;

public class DriverComparator implements Comparator<Driver> {

    private static final DriverLikelihoodComparator DRIVER_LIKELIHOOD_COMPARATOR = new DriverLikelihoodComparator();

    private static final ActionableEvidenceComparator ACTIONABLE_EVIDENCE_COMPARATOR = new ActionableEvidenceComparator();

    @Override
    public int compare(@NotNull Driver driver1, @NotNull  Driver driver2) {
        int reportableCompare = Boolean.compare(driver2.isReportable(), driver1.isReportable());
        if (reportableCompare != 0) {
            return reportableCompare;
        }

        int likelihoodCompare = DRIVER_LIKELIHOOD_COMPARATOR.compare(driver1.driverLikelihood(), driver2.driverLikelihood());
        if (likelihoodCompare != 0) {
            return likelihoodCompare;
        }

        int eventCompare = driver1.event().compareTo(driver2.event());
        if (eventCompare != 0) {
            return eventCompare;
        }

        return ACTIONABLE_EVIDENCE_COMPARATOR.compare(driver1.evidence(), driver2.evidence());
    }
}
