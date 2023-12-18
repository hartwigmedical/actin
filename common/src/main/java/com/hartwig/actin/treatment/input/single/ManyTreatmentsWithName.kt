package com.hartwig.actin.treatment.input.single

import com.hartwig.actin.treatment.input.datamodel.TreatmentInputWithName
import org.immutables.value.Value
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

@Value.Immutable
@Value.Style(passAnnotations = [NotNull::class, Nullable::class])
abstract class ManyTreatmentsWithName() {
    abstract fun treatmentsWithName(): List<TreatmentInputWithName?>
}
