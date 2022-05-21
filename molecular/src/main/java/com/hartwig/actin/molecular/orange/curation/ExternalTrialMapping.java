package com.hartwig.actin.molecular.orange.curation;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class ExternalTrialMapping {

    @NotNull
    public abstract String externalTrial();

    @NotNull
    public abstract String actinTrial();
}
