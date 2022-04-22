package com.hartwig.actin.molecular.datamodel.driver;

import org.jetbrains.annotations.NotNull;

public interface Driver {

    @NotNull
    String event();

    @NotNull
    DriverLikelihood driverLikelihood();
}
