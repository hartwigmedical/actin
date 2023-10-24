package com.hartwig.actin.molecular.orange.evidence.actionability

import com.hartwig.serve.datamodel.ActionableEvent
import org.immutables.value.Value
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

@Value.Immutable
@Value.Style(passAnnotations = [NotNull::class, Nullable::class])
abstract class ActionabilityMatch {
    abstract fun onLabelEvents(): MutableList<ActionableEvent?>
    abstract fun offLabelEvents(): MutableList<ActionableEvent?>
}
