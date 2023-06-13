package com.hartwig.actin.clinical.curation.config;

import java.util.Optional;

import com.hartwig.actin.clinical.datamodel.PriorOtherCondition;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class NonOncologicalHistoryConfig implements CurationConfig {

    @NotNull
    @Override
    public abstract String input();

    @Override
    public abstract boolean ignore();

    public abstract Optional<Double> lvef();

    public abstract Optional<PriorOtherCondition> priorOtherCondition();

}
