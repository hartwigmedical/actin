package com.hartwig.actin.molecular.datamodel.evidence;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class ExternalTrialEvidence implements EvidenceEntry {

    @Override
    @NotNull
    public abstract String event();

    @NotNull
    public abstract String trial();
}
