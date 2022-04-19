package com.hartwig.actin.molecular.datamodel.driver;

import org.jetbrains.annotations.NotNull;

public interface CopyNumberDriver extends Actionable {

    @NotNull
    String gene();

    boolean isPartial();
}
