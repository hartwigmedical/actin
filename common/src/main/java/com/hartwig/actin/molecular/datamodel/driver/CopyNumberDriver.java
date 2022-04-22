package com.hartwig.actin.molecular.datamodel.driver;

import org.jetbrains.annotations.NotNull;

public interface CopyNumberDriver extends Driver {

    @NotNull
    String gene();

    boolean isPartial();
}
