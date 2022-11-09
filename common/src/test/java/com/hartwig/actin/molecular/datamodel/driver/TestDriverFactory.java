package com.hartwig.actin.molecular.datamodel.driver;

import com.hartwig.actin.molecular.datamodel.evidence.ActionableEvidence;
import com.hartwig.actin.molecular.datamodel.evidence.ImmutableActionableEvidence;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class TestDriverFactory {

    private TestDriverFactory() {
    }

    @NotNull
    public static Driver createEmptyDriver() {
        return new Driver() {
            @Override
            public boolean isReportable() {
                return false;
            }

            @Nullable
            @Override
            public DriverLikelihood driverLikelihood() {
                return null;
            }

            @NotNull
            @Override
            public ActionableEvidence evidence() {
                return ImmutableActionableEvidence.builder().build();
            }
        };
    }
}
