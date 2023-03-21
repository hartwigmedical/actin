package com.hartwig.actin.clinical.curation.config;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class ECGConfig implements CurationConfig {

    @NotNull
    @Override
    public abstract String input();

    @Override
    public abstract boolean ignore();

    @NotNull
    public abstract String interpretation();

    public abstract boolean isQTCF();

    @Nullable
    public abstract Integer qtcfValue();

    @Nullable
    public abstract String qtcfUnit();

    public abstract boolean isJTC();

    @Nullable
    public abstract Integer jtcValue();

    @Nullable
    public abstract String jtcUnit();
}
