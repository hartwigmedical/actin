package com.hartwig.actin.molecular.orange.evidence.actionability;

import java.util.List;

import com.hartwig.serve.datamodel.ActionableEvent;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class ActionabilityMatch {

    @NotNull
    public abstract List<ActionableEvent> onLabelEvents();

    @NotNull
    public abstract List<ActionableEvent> offLabelEvents();
}
