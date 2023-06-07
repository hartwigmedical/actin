package com.hartwig.actin.molecular.orange.datamodel.cuppa;

import java.util.Set;

import com.hartwig.actin.molecular.datamodel.characteristics.CuppaPrediction;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class CuppaRecord {

    @NotNull
    public abstract Set<CuppaPrediction> predictions();

}
