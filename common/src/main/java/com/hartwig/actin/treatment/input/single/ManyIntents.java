package com.hartwig.actin.treatment.input.single;

import java.util.Set;

import com.hartwig.actin.clinical.datamodel.treatment.history.Intent;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class ManyIntents {

    @NotNull
    public abstract Set<Intent> intents();

}