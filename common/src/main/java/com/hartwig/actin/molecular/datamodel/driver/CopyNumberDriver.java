package com.hartwig.actin.molecular.datamodel.driver;

import org.jetbrains.annotations.NotNull;

public interface CopyNumberDriver {

    @NotNull
    String gene();

    boolean isPartial();
}
