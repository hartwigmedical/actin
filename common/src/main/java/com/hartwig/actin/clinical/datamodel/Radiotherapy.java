package com.hartwig.actin.clinical.datamodel;

import java.util.Collections;
import java.util.Set;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class Radiotherapy implements Therapy {

    @NotNull
    public Set<Drug> drugs() {
        return Collections.emptySet();
    }
}
