package com.hartwig.actin.molecular.datamodel.driver;

import com.hartwig.actin.molecular.datamodel.evidence.ActionableEvidence;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Driver {

    boolean isReportable();

    @Nullable
    DriverLikelihood driverLikelihood();

    @NotNull
    ActionableEvidence evidence();
}
