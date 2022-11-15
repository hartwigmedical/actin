package com.hartwig.actin.molecular.orange.datamodel.lilac;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class LilacHlaAllele {

    // TODO Will be renamed to "allele" in upcoming version

    @NotNull
    public abstract String name();

    public abstract double tumorCopyNumber();

    public abstract double somaticMissense();

    public abstract double somaticNonsenseOrFrameshift();

    public abstract double somaticSplice();

    public abstract double somaticInframeIndel();
}
