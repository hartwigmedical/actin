package com.hartwig.actin.algo.evaluation;

import com.hartwig.actin.algo.calendar.ReferenceDateProvider;
import com.hartwig.actin.doid.DoidModel;
import com.hartwig.actin.treatment.input.FunctionInputResolver;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class RuleMappingResources {

    @NotNull
    public abstract ReferenceDateProvider referenceDateProvider();

    @NotNull
    public abstract DoidModel doidModel();

    @NotNull
    public abstract FunctionInputResolver functionInputResolver();
}
