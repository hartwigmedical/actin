package com.hartwig.actin.algo.soc.datamodel;

import java.util.List;

import com.hartwig.actin.algo.datamodel.Evaluation;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class EvaluatedTreatment {

    @NotNull
    public abstract Treatment treatment();

    @NotNull
    public abstract List<Evaluation> evaluations();

    public abstract int score();
}
